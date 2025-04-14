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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field

/**
 *
 * SDRAM Controller
 */
class SDRAM(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Port("mmcr")
    }

    override val ports = Ports()

    val DBCTL = object : Register(ports.mmcr, 0x40u, BYTE, "DBCTL") {
        var WB_ENB by bit(0)
        var WB_FLUSH by bit(1)
        var WB_WM by field(3..2)
        var RAB_ENB by bit(4)
    }

    val ECCCTL = object : Register(ports.mmcr, 0x20u, BYTE, "ECCCTL") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            if (value != 0uL)
                throw GeneralException("not implemented for this case!")
            super.write(ea, ss, size, value)
        }
    }

    val DRCCTL = Register(ports.mmcr, 0x10u, BYTE, "DRCCTL")
    val DRCTMCTL = Register(ports.mmcr, 0x12u, BYTE, "DRCTMCTL")
    val DRCCFG = Register(ports.mmcr, 0x14u, BYTE, "DRCCFG")

    val DRCBENDADR0 = Register(ports.mmcr, 0x18u, BYTE, "DRCBENDADR0")
    val DRCBENDADR1 = Register(ports.mmcr, 0x19u, BYTE, "DRCBENDADR1")
    val DRCBENDADR2 = Register(ports.mmcr, 0x1Au, BYTE, "DRCBENDADR2")
    val DRCBENDADR3 = Register(ports.mmcr, 0x1Bu, BYTE, "DRCBENDADR3")
}