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
package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.kHz
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
        @Transient private val log = logger(Level.ALL)
        private enum class RegisterType(val offset: Long) {
            IWDG_KR     (0x00),
            IWDG_PR     (0x04),
            IWDG_RLR    (0x08),
            IWDG_SR     (0x0C),
            IWDG_WINR   (0x10)
        }
        private enum class State {
            LOCKED,     // lock IWDG_PR, IWDG_PLR and IWDG_WINR
            UNLOCKED    // unlock IWDG_PR, IWDG_PLR and IWDG_WINR
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x400)
    }

    override val ports = Ports()

    private open inner class RegisterBase(
            register: RegisterType,
            default: Long = 0x0000_0000,
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
    private var count = 0

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
        if (count == 0) {
            exception()
        } else {
            count--
        }
    }

    private fun exception() {
        throw GeneralException("[IWDG refresh timeout]")
    }


    private val IWDG_KR = object : RegisterBase(Companion.RegisterType.IWDG_KR) {
        override fun read(ea: Long, ss: Int, size: Int): Long = 0x0000
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (value) {
                0x0000L -> state = State.LOCKED
                0x5555L -> state = State.UNLOCKED
                0xAAAAL -> if (counter.enabled) refreshWatchdog() // reset counter and apply IWDG_PLR register
                0xCCCCL -> startWatchdog()
            }
        }
    }
    private val IWDG_PR     = object : RegisterBase(RegisterType.IWDG_PR) {
        var PLR by field(2..0)

        var divider: Long = 4

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            if (state == State.UNLOCKED) {
                when (PLR) {
                    0b000 -> divider = 4
                    0b001 -> divider = 8
                    0b010 -> divider = 16
                    0b011 -> divider = 32
                    0b100 -> divider = 64
                    0b101 -> divider = 128
                    0b110 -> divider = 256
                    0b111 -> divider = 256
                }
            }
        }
    }
    private val IWDG_RLR    = object : RegisterBase(RegisterType.IWDG_RLR, default = 0x0000_0FFF) {
        var RL by field(11..0)  // counter reload value
        var shadow = default.asInt

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            if (state == State.UNLOCKED) {
                super.write(ea, ss, size, value)
            }
        }

        fun updateShadow() {
            shadow = RL
        }
    }
    private val IWDG_SR     = object : RegisterBase(RegisterType.IWDG_SR) {}
    private val IWDG_WINR   = object : RegisterBase(RegisterType.IWDG_WINR, default = 0x0000_0FFF) {
        var WIN by field(11..0)
        var shadow = default.asInt

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            if (state == State.UNLOCKED) {
                super.write(ea, ss, size, value)
            }
        }

        fun updateShadow() {
            shadow = WIN
        }
    }
}






























