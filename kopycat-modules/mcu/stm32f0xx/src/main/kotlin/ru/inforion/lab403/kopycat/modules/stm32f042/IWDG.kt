/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.kHz
import ru.inforion.lab403.common.logging.ALL
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import java.util.logging.Level

@Suppress("PrivatePropertyName", "PropertyName", "SpellCheckingInspection")
class IWDG(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient private val log = logger(ALL)
        private enum class RegisterType(val offset: ULong) {
            IWDG_KR     (0x00u),
            IWDG_PR     (0x04u),
            IWDG_RLR    (0x08u),
            IWDG_SR     (0x0Cu),
            IWDG_WINR   (0x10u)
        }
        private enum class State {
            LOCKED,     // lock IWDG_PR, IWDG_PLR and IWDG_WINR
            UNLOCKED    // unlock IWDG_PR, IWDG_PLR and IWDG_WINR
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x400u)
    }

    override val ports = Ports()

    private open inner class RegisterBase(
            register: RegisterType,
            default: ULong = 0x0000_0000u,
            writable: Boolean = true,
            readable: Boolean = true,
            level: Level = Level.FINE
    ) : Register(ports.mem, register.offset, Datatype.DWORD, register.name, default, writable, readable, level)


    private val counter = object : SystemClock.PeriodicalTimer(name) {
        override fun trigger() {
            super.trigger()
            counterTriggerEvent()
        }
    }

    private val watchdogPeriod get() = (core.clock.frequency / (core.clock.frequency / 40.kHz)) / IWDG_PR.divider
    private var state = State.LOCKED
    private var count = 0uL

    private fun refreshWatchdog() {
        if (count > IWDG_WINR.shadow) {
            exception()
        } else {
            reloadWatchdog()
        }
    }

    private fun startWatchdog() {
        log.info { "Watchdog started!" }
        reloadWatchdog()
    }

    private fun reloadWatchdog() {
        IWDG_RLR.updateShadow()
        count = IWDG_RLR.shadow

        IWDG_WINR.updateShadow()
        count = IWDG_WINR.shadow

        counter.connect(core.clock, watchdogPeriod)   // (40kHz)/divider
    }

    private fun counterTriggerEvent() {
        if (count == 0uL) {
            exception()
        } else {
            count--
        }
    }

    private fun exception() {
        throw GeneralException("[IWDG refresh timeout]")
    }


    private val IWDG_KR = object : RegisterBase(Companion.RegisterType.IWDG_KR) {
        override fun read(ea: ULong, ss: Int, size: Int) = 0x0000uL
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            when (value) {
                0x0000uL -> state = State.LOCKED
                0x5555uL -> state = State.UNLOCKED
                0xAAAAuL -> if (counter.enabled) refreshWatchdog() // reset counter and apply IWDG_PLR register
                0xCCCCuL -> startWatchdog()
            }
        }
    }
    private val IWDG_PR     = object : RegisterBase(RegisterType.IWDG_PR) {
        var PLR by field(2..0)

        var divider: Long = 4

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            if (state == State.UNLOCKED) when (PLR) {
                0b000uL -> divider = 4
                0b001uL -> divider = 8
                0b010uL -> divider = 16
                0b011uL -> divider = 32
                0b100uL -> divider = 64
                0b101uL -> divider = 128
                0b110uL -> divider = 256
                0b111uL -> divider = 256
            }
        }
    }
    private val IWDG_RLR    = object : RegisterBase(RegisterType.IWDG_RLR, default = 0x0000_0FFFu) {
        var RL by field(11..0)  // counter reload value
        var shadow = default

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            if (state == State.UNLOCKED) super.write(ea, ss, size, value)
        }

        fun updateShadow() {
            shadow = RL
        }
    }
    private val IWDG_SR     = object : RegisterBase(RegisterType.IWDG_SR) {}
    private val IWDG_WINR   = object : RegisterBase(RegisterType.IWDG_WINR, default = 0x0000_0FFFu) {
        var WIN by field(11..0)
        var shadow = default

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            if (state == State.UNLOCKED) super.write(ea, ss, size, value)
        }

        fun updateShadow() {
            shadow = WIN
        }
    }
}






























