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

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level


class RCC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient val log = logger(Level.WARNING)

        private enum class RegisterType(val offset: Long) {
            RCC_CR          (0x00),
            RCC_CFGR        (0x04),
            RCC_CIR         (0x08),
            RCC_APB2RSTR    (0x0C),
            RCC_APB1RSTR    (0x10),
            RCC_AHBENR      (0x14),
            RCC_APB2ENR     (0x18),
            RCC_APB1ENR     (0x1C),
            RCC_BDCR        (0x20),
            RCC_CSR         (0x24),
            RCC_AHBRSTR     (0x28),
            RCC_CFGR2       (0x2C),
            RCC_CFGR3       (0x30),
            RCC_CR2         (0x34)
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x80)
        val irq = Master("irq", PIN)
    }

    override val ports = Ports()

    private open inner class RegisterBase(
            register: RegisterType,
            default: Long = 0x0000_0000,
            writable: Boolean = true,
            readable: Boolean = true,
            level: Level = Level.FINE
    ) : Register(ports.mem, register.offset, Datatype.DWORD, register.name, default, writable, readable, level)

    private val RCC_CR          = object : RegisterBase(RegisterType.RCC_CR,    0x0000_0083) {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (value) {
                data.and(0xFEFFFFFF) -> super.write(ea, ss, size, value.clr(25))
                data.or(0x1000000) -> super.write(ea, ss, size, value.set(25))
                else -> super.write(ea, ss, size, value)
            }
        }
    } // 0x2000_0000 set hardware PLL lock
    private val RCC_CFGR        = object : RegisterBase(RegisterType.RCC_CFGR) {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value.insert(value[1..0], 3..2))
        }
    }
    private val RCC_APB2RSTR    = object : RegisterBase(RegisterType.RCC_APB2RSTR) {}
    private val RCC_APB1RSTR    = object : RegisterBase(RegisterType.RCC_APB1RSTR) {}
    private val RCC_CIR         = object : RegisterBase(RegisterType.RCC_CIR) {}
    private val RCC_AHBENR      = object : RegisterBase(RegisterType.RCC_AHBENR,0x0000_0014) {}
    private val RCC_APB2ENR     = object : RegisterBase(RegisterType.RCC_APB2ENR) {}
    private val RCC_APB1ENR     = object : RegisterBase(RegisterType.RCC_APB1ENR) {}
    private val RCC_BDCR        = object : RegisterBase(RegisterType.RCC_BDCR) {}
    private val RCC_CSR         = object : RegisterBase(RegisterType.RCC_CSR,   0x0000_0002) {} // set hardware osc ready
    private val RCC_AHBRSTR     = object : RegisterBase(RegisterType.RCC_AHBRSTR) {}
    private val RCC_CFGR2       = object : RegisterBase(RegisterType.RCC_CFGR2) {}
    private val RCC_CFGR3       = object : RegisterBase(RegisterType.RCC_CFGR3) {}
    private val RCC_CR2         = object : RegisterBase(RegisterType.RCC_CR2,   0x0000_0080) {}
}