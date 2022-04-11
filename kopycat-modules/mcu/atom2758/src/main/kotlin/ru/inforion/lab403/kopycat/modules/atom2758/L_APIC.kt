/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import java.util.logging.Level.CONFIG

class L_APIC(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x10_0000)
        val lio = Slave("lio", 0x100)  // local i/o
    }

    override val ports = Ports()

    inner class LINTRegister(index: Int) : Register(ports.mem, 0x350uL + index.uint * 0x10u, DWORD, "APIC_LINT$index", default = 0x0001_0000uL) {
        var mask by bit(16)
        var trigger by bit(15)
        var remoteIRR by bit(14)
        var polarity by bit(13)
        var deliveryStat by bit(12)
        var deliveryMode by field(10..8)
        var vector by field(7..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value clr 12 clr 14)
            log.warning { "${this.name} override: mask=$mask, trigger=$trigger, remote_IRR_flag=$remoteIRR, polarity=$polarity, delivery_status=$deliveryStat, delivery_mode=$deliveryMode, vector=${vector.hex2}" }
        }
    }


    //  FEE0_0000 - FEEF_FFFF
    // vol 3
    val ID = Register(ports.mem, 0x20u, DWORD, "APIC_ID", 0x8000000u, level = CONFIG) // ID: 8
    val LVR = Register(ports.mem, 0x30u, DWORD, "APIC_LVR", 32u, level = CONFIG)      // Version: 32
//    private val APIC_ID = Register(ports.mem, 0x020u, DWORD, "APIC_ID", 0x4655434Bu, level = CONFIG)
//    private val APIC_VER = Register(ports.mem, 0x030u, DWORD, "APIC_VER", 0x494E544Cu, level = CONFIG)

    private val TPR = object : Register(ports.mem, 0x80u, DWORD, "Task-Priority Register") {
        var cls by field(7..4)
        var subCls by field(3..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.warning { "${this.name}: Task-priority class: ${cls.hex2}, task-priority sub-class: ${subCls.hex2}" }
        }
    }
    private val PPR = Register(ports.mem, 0x0A0u, DWORD, "PPR", level = CONFIG)
    private val EOI = Register(ports.mem, 0x0B0u, DWORD, "EOI", level = CONFIG)

    private val LDR = object : Register(ports.mem, 0xD0u, DWORD, "Logical Destination Register") {
        var logicalAPICID by field(31..24)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.warning { "${this.name}: logical APIC ID is ${logicalAPICID.hex2}" }
        }
    }

    private val DFR = object : Register(ports.mem, 0xE0u, DWORD, " Destination Format Register", default = 0xFFFF_FFFFu) {
        var model by field(31..28)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            val modelName = when (model) {
                0b1111uL -> "flat"
                0b0000uL -> "cluster"
                else -> "UNKNOWN"
            }
            log.warning { "${this.name}: selected $modelName model (${model.binary(4)})" }
        }
    }

    val SPIV = object : Register(ports.mem, 0xF0u, DWORD, "APIC_SPIV") {
        var EOIBroadcastSuppression by bit(12)
        var focusProcessorChecking by bit(9)
        var enabled by bit(8)
        var spuriousVector by field(7..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.warning { "${this.name}: EOI-Broadcast Suppression = $EOIBroadcastSuppression" }
            log.warning { "${this.name}: Focus Processor Checking = $focusProcessorChecking" }
            log.warning { "${this.name}: Enabled = $enabled" }
            log.warning { "${this.name}: Spurious Vector = ${spuriousVector.hex2}" }
        }
    }

    private val ISR0 = Register(ports.mem, 0x100u, DWORD, "ISR0", level = CONFIG, writable = false)
    private val ISR1 = Register(ports.mem, 0x110u, DWORD, "ISR1", level = CONFIG, writable = false)
    private val ISR2 = Register(ports.mem, 0x120u, DWORD, "ISR2", level = CONFIG, writable = false)
    private val ISR3 = Register(ports.mem, 0x130u, DWORD, "ISR3", level = CONFIG, writable = false)
    private val ISR4 = Register(ports.mem, 0x140u, DWORD, "ISR4", level = CONFIG, writable = false)
    private val ISR5 = Register(ports.mem, 0x150u, DWORD, "ISR5", level = CONFIG, writable = false)
    private val ISR6 = Register(ports.mem, 0x160u, DWORD, "ISR6", level = CONFIG, writable = false)
    private val ISR7 = Register(ports.mem, 0x170u, DWORD, "ISR7", level = CONFIG, writable = false)

    private val TMR0 = Register(ports.mem, 0x180u, DWORD, "TMR0", level = CONFIG)
    private val TMR1 = Register(ports.mem, 0x190u, DWORD, "TMR1", level = CONFIG)
    private val TMR2 = Register(ports.mem, 0x1A0u, DWORD, "TMR2", level = CONFIG)
    private val TMR3 = Register(ports.mem, 0x1B0u, DWORD, "TMR3", level = CONFIG)
    private val TMR4 = Register(ports.mem, 0x1C0u, DWORD, "TMR4", level = CONFIG)
    private val TMR5 = Register(ports.mem, 0x1D0u, DWORD, "TMR5", level = CONFIG)
    private val TMR6 = Register(ports.mem, 0x1E0u, DWORD, "TMR6", level = CONFIG)
    private val TMR7 = Register(ports.mem, 0x1F0u, DWORD, "TMR7", level = CONFIG)

    private val IRR0 = Register(ports.mem, 0x200u, DWORD, "IRR0", level = CONFIG, writable = false)
    private val IRR1 = Register(ports.mem, 0x210u, DWORD, "IRR1", level = CONFIG, writable = false)
    private val IRR2 = Register(ports.mem, 0x220u, DWORD, "IRR2", level = CONFIG, writable = false)
    private val IRR3 = Register(ports.mem, 0x230u, DWORD, "IRR3", level = CONFIG, writable = false)
    private val IRR4 = Register(ports.mem, 0x240u, DWORD, "IRR4", level = CONFIG, writable = false)
    private val IRR5 = Register(ports.mem, 0x250u, DWORD, "IRR5", level = CONFIG, writable = false)
    private val IRR6 = Register(ports.mem, 0x260u, DWORD, "IRR6", level = CONFIG, writable = false)
    private val IRR7 = Register(ports.mem, 0x270u, DWORD, "IRR7", level = CONFIG, writable = false)

    val ESR = object : Register(ports.mem, 0x280u, DWORD, "APIC_ESR") {
        var timerMode by field(18..17)
        var mask by bit(16)
        var trigger by bit(15)
        var remoteIRR by bit(14)
        var polarity by bit(13)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value clr 14)
            log.warning { "$name override: timer_mode=$timerMode, mask=$mask, trigger=$trigger, remote_IRR_flag=$remoteIRR, polarity=$polarity" }
        }
    }

    private val ICR0 = Register(ports.mem, 0x300u, DWORD, "ICR0", level = CONFIG)
    private val ICR1 = Register(ports.mem, 0x304u, DWORD, "ICR1", level = CONFIG)
    private val ICR2 = Register(ports.mem, 0x308u, DWORD, "ICR2", level = CONFIG)
    private val ICR3 = Register(ports.mem, 0x30Cu, DWORD, "ICR3", level = CONFIG)
    private val ICR4 = Register(ports.mem, 0x310u, DWORD, "ICR4", level = CONFIG)

    val LVTT = object : Register(ports.mem, 0x320u, DWORD, "APIC_LVTT", default = 0x0001_0000u) {
        var mode by bit(17) // Periodical = 1, one-shot = 0
        var mask by bit(16) // Forbidden = 1, allowed = 0
        var deliveryStat by bit(12) // Delivery status (RO)
        var vector by field(7..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value clr 12)
            timerMode = TimerMode.values().find { it.value == mode } ?: throw GeneralException("Unknown mode: $mode")
            masked = mask.truth
            log.warning { "${this.name} override: mode=$mode, mask=$mask, delivery_status=$deliveryStat, vector=${vector.hex2}" }
        }
    }

    private val LVT_SEN = Register(ports.mem, 0x330u, DWORD, "LVT_SEN", level = CONFIG)
//    private val LVT_PER = Register(ports.mem, 0x340u, DWORD, "LVT_PER", level = CONFIG)
//    private val LVT_LINT0 = Register(ports.mem, 0x350u, DWORD, "LVT_LINT0", level = CONFIG)
//    private val LVT_LINT1 = Register(ports.mem, 0x360u, DWORD, "LVT_LINT1", level = CONFIG)
//    private val LVT_ERR = Register(ports.mem, 0x370u, DWORD, "LVT_ERR", level = CONFIG)

    val LVT_PC = object : Register(ports.mem, 0x340u, DWORD, "APIC_LVTPC", default = 0x0001_0000uL) {
        var mask by bit(16)
        var deliveryStat by bit(12)
        var deliveryMode by field(10..8)
        var vector by field(7..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value clr 12)
            log.warning { "${this.name} override: mask=$mask, delivery_status=$deliveryStat, delivery_mode=$deliveryMode, vector=${vector.hex2}" }
        }
    }
    val LVT_LINT0 = LINTRegister(0) // 0x350u
    val LVT_LINT1 = LINTRegister(1) // 0x360u

    val LVT_E = object : Register(ports.mem, 0x370u, DWORD, "APIC_LVTERR", default = 0x0001_0000uL) {
        var mask by bit(16)
        var deliveryStat by bit(12)
        var vector by field(7..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value clr 12)
            log.warning { "${this.name} override: mask=$mask, delivery_status=$deliveryStat, vector=${vector.hex2}" }
        }
    }

    enum class TimerMode(val value: Int) {
        OneShot(0b00),
        Periodic(0b01),
        TSCDeadline(0b10)
    }

    private var timerMode = TimerMode.OneShot
    private var initial: ULong = 0u
    private var count: ULong = 0u
    private var divider: ULong = 2u
    private var masked = true

    override fun reset() {
        super.reset()
        timer.reset()
        timerMode = TimerMode.OneShot
        initial = 0u
        count = 0u
        divider = 2u
        masked = true
    }

    // Initial Count Register
    private val LVT_INIT = object : Register(ports.mem, 0x380u, DWORD, "LVT_INIT", level = CONFIG) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            initial = data
            count = data
            timer.enabled = true
        }
    }

    // Current Count Register
    private val CCR = object : Register(ports.mem, 0x390u, DWORD, "CCR", level = CONFIG) {
        override fun read(ea: ULong, ss: Int, size: Int) = count
    }

    // Divide Configuration Register
    private val DCR = object : Register(ports.mem, 0x3E0u, DWORD, "DCR", level = CONFIG) {
        var mode by field(3..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            divider = when (mode) {
                0b0000uL -> 2uL
                0b0001uL -> 4uL
                0b0010uL -> 8uL
                0b0011uL -> 16uL
                0b1000uL -> 32uL
                0b1001uL -> 64uL
                0b1010uL -> 128uL
                0b1011uL -> 1uL
                else -> throw GeneralException("Unknown mode: ${mode.binary(4)}")
            }
            log.warning { "${this.name}: divider is $divider" }
        }
    }

    private val IOAPIC_IDX = Register(ports.lio, 0x00u, DWORD, "IOAPIC_IDX", level = CONFIG)
    private val IOAPIC_WDW = Register(ports.lio, 0x10u, DWORD, "IOAPIC_WDW", level = CONFIG)
    private val IOAPIC_EOI = Register(ports.lio, 0x40u, DWORD, "IOAPIC_EOI", level = CONFIG)

    private val timer = object : SystemClock.PeriodicalTimer("PIT Counter") {
        override fun trigger() {
            if (triggered >= divider) {
                --count
                triggered = 0u
            }
            if (count == 0uL) {
                if (!masked)
                    TODO("Interrupts aren't implemented yet")

                when (timerMode) {
                    TimerMode.OneShot -> enabled = false
                    TimerMode.Periodic -> count = initial
                    else -> TODO("Not implemented mode: $timerMode")
                }
            }
        }
    }

//    inline fun freqToPeriod(freq: Long): Pair<Long, Time> {
//        val units = Time.values().find { freq <= it.divider } :? Time.values().last()
//        return (units.divider / freq) to units
//    }

    override fun initialize(): Boolean {
        if (!super.initialize()) return false
//        val (period, units) = freqToPeriod(core.frequency) // Well, because of KC's SystemTimer, passing (1, null) will cause the same result
        core.clock.connect(timer, 1, false)
        return true
    }


}