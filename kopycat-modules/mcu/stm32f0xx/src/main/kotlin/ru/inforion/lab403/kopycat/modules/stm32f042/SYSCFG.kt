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

import ru.inforion.lab403.common.logging.ALL
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.util.logging.Level

@Suppress("PrivatePropertyName", "PropertyName", "SpellCheckingInspection")
class SYSCFG(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient
        private val log = logger(ALL)

        private enum class RegisterType(val offset: ULong) {
            SYSCFG_CFGR1(0x00u),
            SYSCFG_EXTICR1(0x08u),
            SYSCFG_EXTICR2(0x0Cu),
            SYSCFG_EXTICR3(0x10u),
            SYSCFG_EXTICR4(0x14u),
            SYSCFG_CFGR2(0x18u),
            SYSCFG_ITLINE0(0x80u),
            SYSCFG_ITLINE1(0x84u),
            SYSCFG_ITLINE2(0x88u),
            SYSCFG_ITLINE3(0x8Cu),
            SYSCFG_ITLINE4(0x90u),
            SYSCFG_ITLINE5(0x94u),
            SYSCFG_ITLINE6(0x98u),
            SYSCFG_ITLINE7(0x9Cu),
            SYSCFG_ITLINE8(0xA0u),
            SYSCFG_ITLINE9(0xA4u),
            SYSCFG_ITLINE10(0xA8u),
            SYSCFG_ITLINE11(0xACu),
            SYSCFG_ITLINE12(0xB0u),
            SYSCFG_ITLINE13(0xB4u),
            SYSCFG_ITLINE14(0xB8u),
            SYSCFG_ITLINE15(0xBCu),
            SYSCFG_ITLINE16(0xC0u),
            SYSCFG_ITLINE17(0xC4u),
            SYSCFG_ITLINE18(0xC8u),
            SYSCFG_ITLINE19(0xCCu),
            SYSCFG_ITLINE20(0xD0u),
            SYSCFG_ITLINE21(0xD4u),
            SYSCFG_ITLINE22(0xD8u),
            SYSCFG_ITLINE23(0xDCu),
            SYSCFG_ITLINE24(0xE0u),
            SYSCFG_ITLINE25(0xE4u),
            SYSCFG_ITLINE26(0xE8u),
            SYSCFG_ITLINE27(0xECu),
            SYSCFG_ITLINE28(0xF0u),
            SYSCFG_ITLINE29(0xF4u),
            SYSCFG_ITLINE30(0xF8u)
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x200)
    }

    override val ports = Ports()

    private open inner class RegisterBase(
        register: RegisterType,
        default: ULong = 0x0000_0000u,
        writable: Boolean = true,
        readable: Boolean = true,
        level: Level = Level.FINE
    ) : Register(ports.mem, register.offset, Datatype.DWORD, register.name, default, writable, readable, level)

    init {
        RegisterType.values().forEach { RegisterBase(it) }
    }
}