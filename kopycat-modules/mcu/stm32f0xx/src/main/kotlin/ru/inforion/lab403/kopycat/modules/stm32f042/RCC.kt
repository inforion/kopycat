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
@file:Suppress("PrivatePropertyName", "unused")

package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.LogLevel
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype


class RCC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient
        val log = logger(WARNING)

        private enum class RegisterType(val offset: ULong) {
            RCC_CR(0x00u),
            RCC_CFGR(0x04u),
            RCC_CIR(0x08u),
            RCC_APB2RSTR(0x0Cu),
            RCC_APB1RSTR(0x10u),
            RCC_AHBENR(0x14u),
            RCC_APB2ENR(0x18u),
            RCC_APB1ENR(0x1Cu),
            RCC_BDCR(0x20u),
            RCC_CSR(0x24u),
            RCC_AHBRSTR(0x28u),
            RCC_CFGR2(0x2Cu),
            RCC_CFGR3(0x30u),
            RCC_CR2(0x34u)
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Port("mem")
        val irq = Port("irq")
    }

    override val ports = Ports()

    private open inner class RegisterBase(
        register: RegisterType,
        default: ULong = 0x0000_0000u,
        writable: Boolean = true,
        readable: Boolean = true,
        level: LogLevel = FINE
    ) : Register(ports.mem, register.offset, Datatype.DWORD, register.name, default, writable, readable, level)

    private val RCC_CR = object : RegisterBase(RegisterType.RCC_CR, 0x0000_0083u) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            when (value) {
                data and 0xFEFFFFFFuL -> super.write(ea, ss, size, value clr 25)
                data or 0x1000000uL -> super.write(ea, ss, size, value set 25)
                else -> super.write(ea, ss, size, value)
            }
        }
    } // 0x2000_0000 set hardware PLL lock
    private val RCC_CFGR = object : RegisterBase(RegisterType.RCC_CFGR) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value.insert(value[1..0], 3..2))
        }
    }
    private val RCC_APB2RSTR = RegisterBase(RegisterType.RCC_APB2RSTR)
    private val RCC_APB1RSTR = RegisterBase(RegisterType.RCC_APB1RSTR)
    private val RCC_CIR = RegisterBase(RegisterType.RCC_CIR)
    private val RCC_AHBENR = RegisterBase(RegisterType.RCC_AHBENR, 0x0000_0014u)
    private val RCC_APB2ENR = RegisterBase(RegisterType.RCC_APB2ENR)
    private val RCC_APB1ENR = RegisterBase(RegisterType.RCC_APB1ENR)
    private val RCC_BDCR = RegisterBase(RegisterType.RCC_BDCR)
    private val RCC_CSR = RegisterBase(RegisterType.RCC_CSR, 0x0000_0002u) // set hardware osc ready
    private val RCC_AHBRSTR = RegisterBase(RegisterType.RCC_AHBRSTR)
    private val RCC_CFGR2 = RegisterBase(RegisterType.RCC_CFGR2)
    private val RCC_CFGR3 = RegisterBase(RegisterType.RCC_CFGR3)
    private val RCC_CR2 = RegisterBase(RegisterType.RCC_CR2, 0x0000_0080u)
}