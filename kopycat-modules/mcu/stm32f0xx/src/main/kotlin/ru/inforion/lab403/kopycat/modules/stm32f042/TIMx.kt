/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
@file:Suppress("unused")

package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level
import java.util.logging.Level.FINE
import java.util.logging.Level.INFO

@Suppress("EnumEntryName", "PrivatePropertyName", "PropertyName")
class TIMx(parent: Module, name: String, index: Int) : Module(parent, name) {
    companion object {
        @Transient private val log = logger(INFO)
        private enum class RegisterType(val offset: Long) {
            TIMx_CR1    (0x00),
            TIMx_CR2    (0x04),
            TIMx_SMCR   (0x08),
            TIMx_DIER   (0x0C),
            TIMx_SR     (0x10),
            TIMx_EGR    (0x14),
            TIMx_CCMR1  (0x18),
            TIMx_CCMR2  (0x1C),
            TIMx_CCER   (0x20),
            TIMx_CNT    (0x24),
            TIMx_PSC    (0x28),
            TIMx_ARR    (0x2C),
            TIMx_RCR    (0x30),
            TIMx_CCR1   (0x34),
            TIMx_CCR2   (0x38),
            TIMx_CCR3   (0x3C),
            TIMx_CCR4   (0x40),
            TIMx_BDTR   (0x44),
            TIMx_DCR    (0x48),
            TIMx_DMAR   (0x4C)
        }
    }
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x80)
        val irq_ut = Master("irq_ut", PIN)  // Break, Update, Trigger and Commutation - only TIM1
        val irq_cc = Master("irq_cc", PIN)  // Capture Compare
        val drq = Master("drq", PIN)
    }
    override val ports = Ports()

    private fun irqUpdateTriggerRequest(enabled: Int) = if (enabled == 1) ports.irq_ut.request(0) else Unit
    private fun irqCaptureCompareRequest(enabled: Int) = if (enabled == 1) ports.irq_cc.request(0) else Unit

    private fun drqRequest(enabled: Int) {
//        if (enabled == 1) ports.drq.request(0) else Unit
        if (enabled == 1)
            throw NotImplementedError("Perhaps DMA-request in TIM1 has different buses like IRQ -> should be checked!")
    }

    private open inner class RegisterBase(
            register: RegisterType,
            default: Long = 0x0000,
            useDWORD: Boolean = false,
            writable: Boolean = true,
            readable: Boolean = true,
            level: Level = FINE
    ) : Register(ports.mem, register.offset, if (useDWORD) DWORD else WORD, register.name, default, writable, readable, level)

    private open inner class RegisterBaseWithShadow(
            register: RegisterType,
            default: Long = 0x0000,
            useDWORD: Boolean = false,
            writable: Boolean = true,
            readable: Boolean = true,
            level: Level = FINE,
            shadowRange: IntRange = if (useDWORD) 31..0 else 15..0
    ) : RegisterBase(register, default, useDWORD, writable, readable, level) {
        val first = shadowRange.first
        val last = shadowRange.last

        var shadow = data[first..last].asInt
        open fun updateShadow() {
            shadow = data[first..last].asInt
        }
    }

    private val counter = object : SystemClock.PeriodicalTimer(name) {
        override fun trigger() {
            super.trigger()
            updateCounter()
            compareInterrupt()
        }
    }

    private fun disableTimer() {
        TIMx_CR1.CEN = 0
        counter.enabled = false
    }

    private fun startTimer() {
        if (TIMx_ARR.shadow == 0) return

        counter.connect(core.clock, TIMx_PSC.shadow.toLong() + 1)
        TIMx_CR1.CEN = 1
        counter.enabled = true
    }

    private fun updateCounter() {
//        log.warning { "$this -> trigger $TIMx_CNT.CNT / ${TIMx_ARR.shadow}" }
        if (TIMx_CR1.DIR == 1) {
            if (TIMx_CNT.CNT == 0) {
                TIMx_CNT.CNT = TIMx_ARR.ARR
                updateEvent()
            } else {
                TIMx_CNT.CNT--
            }
        } else {
            if (TIMx_CNT.CNT == TIMx_ARR.shadow) {
                TIMx_CNT.CNT = 0
                updateEvent()
            } else {
                TIMx_CNT.CNT++
            }
        }
    }

    private fun updateEvent(interrupt: Boolean = true) {  // if timer overload
        if (TIMx_CR1.OPM == 1)
            disableTimer()

        TIMx_PSC.updateShadow() // prescaler updates every time

        shadowedRegistersUpdate()
        
        if (interrupt)
            updateInterrupt()
    }

    private fun shadowedRegistersUpdate() {
        if (TIMx_CR1.ARPE == 1 && TIMx_CR1.UDIS == 0) TIMx_ARR.updateShadow()
        if (TIMx_CCMR1.OC1PE == 1) TIMx_CCR1.updateShadow()
        if (TIMx_CCMR1.OC2PE == 1) TIMx_CCR2.updateShadow()
        if (TIMx_CCMR2.OC3PE == 1) TIMx_CCR3.updateShadow()
        if (TIMx_CCMR2.OC4PE == 1) TIMx_CCR4.updateShadow()
    }

    private fun updateInterrupt() {
        TIMx_SR.UIF = 1
        irqUpdateTriggerRequest(TIMx_DIER.UIE)
        drqRequest(TIMx_DIER.UDE)
    }

    private fun triggerInterrupt() {
        TIMx_SR.TIF = 1
        irqUpdateTriggerRequest(TIMx_DIER.TIE)
        drqRequest(TIMx_DIER.TDE)
    }

    private fun interruptCCRx(enabled: Int, captured: Boolean, ie: Int, de: Int, flag: () -> Unit) {
        if (enabled == 1 && captured) {
            flag()
            irqCaptureCompareRequest(ie)
            drqRequest(de)
        }
    }

    private fun interruptCCR1(enabled: Int, captured: Boolean) =
            interruptCCRx(enabled, captured, TIMx_DIER.CC1IE, TIMx_DIER.CC1DE) { TIMx_SR.CC1IF = 1 }
    private fun interruptCCR2(enabled: Int, captured: Boolean) =
            interruptCCRx(enabled, captured, TIMx_DIER.CC2IE, TIMx_DIER.CC2DE) { TIMx_SR.CC2IF = 1 }
    private fun interruptCCR3(enabled: Int, captured: Boolean) =
            interruptCCRx(enabled, captured, TIMx_DIER.CC3IE, TIMx_DIER.CC3DE) { TIMx_SR.CC3IF = 1 }
    private fun interruptCCR4(enabled: Int, captured: Boolean) =
            interruptCCRx(enabled, captured, TIMx_DIER.CC4IE, TIMx_DIER.CC4DE) { TIMx_SR.CC4IF = 1 }

    private fun compareInterrupt(value: Int = TIMx_CNT.CNT) {
        interruptCCR1(TIMx_CCER.CC1E, value == TIMx_CCR1.CCR)
        interruptCCR2(TIMx_CCER.CC2E, value == TIMx_CCR2.CCR)
        interruptCCR3(TIMx_CCER.CC3E, value == TIMx_CCR3.CCR)
        interruptCCR4(TIMx_CCER.CC4E, value == TIMx_CCR4.CCR)
    }

    private val is32BitTimer = index == 2

    private val TIMx_CR1    = object : RegisterBase(RegisterType.TIMx_CR1) {
        var CKD by field(9..8)
        var ARPE by bit(7)  // 0 not buffered, 1 buffered and use shadowed register
        var CMS by field(6..5)
        var DIR by bit(4)   // 0 upcounter, 1 downcounter
        var OPM by bit(3)   // is one-pulse mode
        var URS by bit(2)   // Если 0, то TIMx_SR.UIF устанавливается при переполнении счётчика таймера и при установки TIMx_EGR.UG, если 1, то только при переполнении
        var UDIS by bit(1)  // Если 1, то не будет происходить обновления теневого регистра TIMx_ARR, однако, таймер продолжит считать
        var CEN by bit(0)   // Counter enable

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            if (CEN == 1) {
                startTimer()
            } else {
                disableTimer()
            }
        }
    }
    private val TIMx_CR2    = object : RegisterBase(RegisterType.TIMx_CR2) {
        var OIS4 by bit(15)
        var OIS3N by bit(13)
        var OIS3 by bit(12)
        var OIS2N by bit(11)
        var OIS2 by bit(10)
        var OIS1N by bit(9)
        var OIS1 by bit(8)
        var TI1S by bit(7)
        var MMS by field(6..4)
        var CCDS by bit(3)
        var CCUS by bit(2)
        var CCPS by bit(0)
    }
    private val TIMx_SMCR   = object : RegisterBase(RegisterType.TIMx_SMCR) {
        var ETP by bit(15)
        var ECE by bit(14)
        var ETPS by field(13..12)
        var ETF by field(11..8)
        var MSM by bit(7)
        var TS by field(6..4)
        var SMS by field(2..0)
    }
    private val TIMx_DIER   = object : RegisterBase(RegisterType.TIMx_DIER) {
        var TDE by bit(14)      // Trigger DMA request enable
        var COMDE by bit(13)    // COM DMA request enable
        var CC4DE by bit(12)    // Capture/Compare 4 DMA request enable
        var CC3DE by bit(11)    // Capture/Compare 3 DMA request enable
        var CC2DE by bit(10)    // Capture/Compare 2 DMA request enable
        var CC1DE by bit(9)     // Capture/Compare 1 DMA request enable
        var UDE by bit(8)       // Update DMA request enable
        var BIE by bit(7)       // Break interrupt enable
        var TIE by bit(6)       // Trigger interrupt enable
        var COMIE by bit(5)     // COM interrupt enable
        var CC4IE by bit(4)     // Capture/Compare 4 interrupt enable
        var CC3IE by bit(3)     // Capture/Compare 3 interrupt enable
        var CC2IE by bit(2)     // Capture/Compare 2 interrupt enable
        var CC1IE by bit(1)     // Capture/Compare 1 interrupt enable
        var UIE by bit(0)       // Update interrupt enable
    }
    private val TIMx_SR     = object : RegisterBase(RegisterType.TIMx_SR) {
        var CC4OF by bit(12)
        var CC3OF by bit(11)
        var CC2OF by bit(10)
        var CC1OF by bit(9)
        var BIF by bit(7)   // Break interrupt flag
        var TIF by bit(6)   // Trigger interrupt flag
        var COMIF by bit(5) // COM interrupt flag
        var CC4IF by bit(4) // Capture/compare 4 interrupt flag
        var CC3IF by bit(3) // Capture/compare 3 interrupt flag
        var CC2IF by bit(2) // Capture/compare 2 interrupt flag
        var CC1IF by bit(1) // Capture/compare 1 interrupt flag
        var UIF by bit(0)   // update interrupt flag
    }
    private val TIMx_EGR    = object : RegisterBase(RegisterType.TIMx_EGR, readable = false) {
        var BG by bit(7)
        var TG by bit(6)    // Trigger generation
        var COMG by bit(5)
        var CC4G by bit(4)  // Capture/compare 4 generation
        var CC3G by bit(3)  // Capture/compare 3 generation
        var CC2G by bit(2)  // Capture/compare 2 generation
        var CC1G by bit(1)  // Capture/compare 1 generation
        var UG by bit(0)    // update generation

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            if (UG == 1) updateEvent(TIMx_CR1.URS == 0)
            // TODO: Check if it ignore enable-flag
            if (CC1G == 1) interruptCCR1(1, true)
            if (CC2G == 1) interruptCCR2(1, true)
            if (CC3G == 1) interruptCCR3(1, true)
            if (CC4G == 1) interruptCCR4(1, true)
            if (TG == 1) triggerInterrupt()
        }
    }
    private val TIMx_CCMR1  = object : RegisterBase(RegisterType.TIMx_CCMR1) {
        var OC2CE by bit(15)
        var OC2M by field(14..12)   // Output compare 2 mode
        var OC2PE by bit(11)        // Output Compare 2 preload enable
        var OC2FE by bit(10)
        var CC2S by field(9..8)
        var OC1CE by bit(7)
        var OC1M by field(6..4)     // Output compare 1 mode
        var OC1PE by bit(3)         // Output Compare 1 preload enable
        var OC1FE by bit(2)
        var CC1S by field(1..0)

        var IC2F by field(15..12)
        var IC2PSC by field(11..10)
        var IC1F by field(7..4)
        var IC1PSC by field(3..2)
    }
    private val TIMx_CCMR2  = object : RegisterBase(RegisterType.TIMx_CCMR2) {
        var OC4CE by bit(15)
        var OC4M by field(14..12)   // Output compare 4 mode
        var OC4PE by bit(11)        // Output Compare 4 preload enable
        var OC4FE by bit(10)
        var CC4S by field(9..8)
        var OC3CE by bit(7)
        var OC3M by field(6..4)     // Output compare 3 mode
        var OC3PE by bit(3)         // Output Compare 3 preload enable
        var OC3FE by bit(2)
        var CC3S by field(1..0)

        var IC4F by field(15..12)
        var IC4PSC by field(11..10)
        var IC3F by field(7..4)
        var IC3PSC by field(3..2)
    }
    private val TIMx_CCER   = object : RegisterBase(RegisterType.TIMx_CCER) {
        var CC4P by bit(13)
        var CC4E by bit(12) // Capture/Compare 4 output enable.
        var CC3NP by bit(11)
        var CC3NE by bit(10)
        var CC3P by bit(9)
        var CC3E by bit(8)  // Capture/Compare 3 output enable.
        var CC2NP by bit(7)
        var CC2NE by bit(6)
        var CC2P by bit(5)
        var CC2E by bit(4)  // Capture/Compare 2 output enable.
        var CC1NP by bit(3)
        var CC1NE by bit(2)
        var CC1P by bit(1)
        var CC1E by bit(0)  // Capture/Compare 1 output enable
    }
    private val TIMx_CNT    = object : RegisterBase(RegisterType.TIMx_CNT, useDWORD = is32BitTimer) {
        var CNT by field(if (is32BitTimer) 31..0 else 15..0)

        override fun read(ea: Long, ss: Int, size: Int) = CNT.toLong()
    }
    private val TIMx_PSC    = object : RegisterBaseWithShadow(RegisterType.TIMx_PSC) {
        var PSC by field(15..0)

        override fun updateShadow() { // always buffered
            super.updateShadow()
            counter.connect(core.clock, shadow.toLong() + 1)
        }
    }
    private val TIMx_ARR    = object : RegisterBaseWithShadow(RegisterType.TIMx_ARR, useDWORD = is32BitTimer) {
        var ARR by field(if (is32BitTimer) 31..0 else 15..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            if (TIMx_CR1.ARPE == 0 && TIMx_CR1.UDIS == 0) updateShadow()
        }

        override fun updateShadow() {
            super.updateShadow()
            if (shadow == 0) {
                disableTimer()
            }
        }
    }
    private val TIMx_RCR    = object : RegisterBase(RegisterType.TIMx_RCR) {
        var REP by field(7..0)
    }

    private open inner class CaptureCompare(register: RegisterType) : RegisterBaseWithShadow(register, useDWORD = is32BitTimer) {
        var CCR by field(if (is32BitTimer) 31..0 else 15..0)
    }
    private val TIMx_CCR1   = object : CaptureCompare(RegisterType.TIMx_CCR1) {}
    private val TIMx_CCR2   = object : CaptureCompare(RegisterType.TIMx_CCR2) {}
    private val TIMx_CCR3   = object : CaptureCompare(RegisterType.TIMx_CCR3) {}
    private val TIMx_CCR4   = object : CaptureCompare(RegisterType.TIMx_CCR4) {}

    private val TIMx_BDTR   = object : RegisterBase(RegisterType.TIMx_BDTR) {
        var MOE by bit(15)
        var AOE by bit(14)
        var BKP by bit(13)
        var BKE by bit(12)
        var OSSR by bit(11)
        var OSSL by bit(10)
        var LOCK by field(9..8)
        var DTG by field(7..0)
    }
    private val TIMx_DCR    = object : RegisterBase(RegisterType.TIMx_DCR) {
        var DBL by field(12..8)
        var DBA by field(4..0)
    }
    private val TIMx_DMAR   = object : RegisterBase(RegisterType.TIMx_DMAR) {
        var DMAB by field(31..0)
    }
}