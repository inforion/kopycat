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
package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.logging.ALL
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.util.logging.Level

@Suppress("PrivatePropertyName", "PropertyName", "SpellCheckingInspection")
class TSC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient
        private val log = logger(ALL)

        private enum class RegisterType(val offset: ULong) {
            TSC_CR(0x00u),
            TSC_IER(0x04u),
            TSC_ICR(0x08u),
            TSC_ISR(0x0Cu),
            TSC_IOHCR(0x10u),
            TSC_IOASCR(0x18u),
            TSC_IOSCR(0x20u),
            TSC_IOCCR(0x28u),
            TSC_IOGCSR(0x30u),
            TSC_IOG1CR(0x34u),
            TSC_IOG2CR(0x38u),
            TSC_IOG3CR(0x3Cu),
            TSC_IOG4CR(0x40u),
            TSC_IOG5CR(0x44u),
            TSC_IOG6CR(0x48u),
            TSC_IOG7CR(0x4Cu),
            TSC_IOG8CR(0x50u)
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x80)
    }

    override val ports = Ports()

    private open inner class RegisterBase(
        register: RegisterType,
        default: ULong = 0x0000_0000u,
        writable: Boolean = true,
        readable: Boolean = true,
        level: Level = Level.FINE
    ) : Register(ports.mem, register.offset, Datatype.DWORD, register.name, default, writable, readable, level)

    private open inner class TSC_IOGCSR_TYP : RegisterBase(
        RegisterType.TSC_IOGCSR,
        default = 0x00FF_00FFu,
        level = Level.ALL
    ) { // def value signalize that acquisition enable and complete
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val filtered = value.insert(default, 23..16) // this pins are read only
            super.write(ea, ss, size, filtered)
        }
    }

    init {
        RegisterType.values().forEach {
            when (it) {
                RegisterType.TSC_ISR -> RegisterBase(it, default = 0x0000_0001u)
                RegisterType.TSC_IOHCR -> RegisterBase(it, default = 0xFFFF_FFFFu)
                RegisterType.TSC_IOGCSR -> TSC_IOGCSR_TYP()
                RegisterType.TSC_IOG1CR,
                RegisterType.TSC_IOG2CR,
                RegisterType.TSC_IOG3CR,
                RegisterType.TSC_IOG4CR,
                RegisterType.TSC_IOG5CR,
                RegisterType.TSC_IOG6CR,
                RegisterType.TSC_IOG7CR,
                RegisterType.TSC_IOG8CR -> RegisterBase(it, default = 0x0000_000Fu)  // little charge
                else -> RegisterBase(it)
            }
        }
    }
}