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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.BUS12
import java.util.logging.Level
import java.util.logging.Level.*

/**
 *
 * SDRAM Controller
 */
class BOOT(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Slave("mmcr", BUS12)
    }

    override val ports = Ports()

    inner class ROM_CTRL_REG(address: Long, name: String) :
            ByteAccessRegister(ports.mmcr, address, DWORD, name) {
        var DGP by bit(12)
        var WIDTH by field(11..10)
        var MODE by bit(9)
        var SUB_DLY by field(5..4)
        var FIRST_DLY by field(2..0)

        override fun stringify() = "${super.stringify()} [DGP=$DGP WIDTH=$WIDTH MODE=$MODE SUB_DLY=$SUB_DLY FIRST_DLY=$FIRST_DLY]"
    }

    val BOOTCSCTL = ROM_CTRL_REG(0x50, "BOOTCSCTL")
    val ROMCS1CTL = ROM_CTRL_REG(0x54, "ROMCS1CTL")
    val ROMCS2CTL = ROM_CTRL_REG(0x58, "ROMCS2CTL")
}