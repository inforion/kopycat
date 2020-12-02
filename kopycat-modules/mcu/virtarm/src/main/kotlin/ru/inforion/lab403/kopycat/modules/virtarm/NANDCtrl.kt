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
package ru.inforion.lab403.kopycat.modules.virtarm

import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.*
import java.util.logging.Level



class NANDCtrl(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient val log = logger(FINER)
    }

    // bit[1]       - CLE, Command Latch Enable
    // bit[0]       - ALE, Address Latch Enable
    inner class Ports : ModulePorts(this) {
        val mem = Slave("inp", BUS02)
        val nand = Master("outp", NAND_BUS_SIZE)
    }

    override val ports = Ports()

    val area = object : Area(ports.mem, 0, 0b11L, "area") {

        override fun read(ea: Long, ss: Int, size: Int): Long = ports.nand.read(NAND_IO, 0, 1)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (ea) {
                0b00L -> ports.nand.write(NAND_IO, 0, 1, value)
                0b01L -> ports.nand.write(NAND_ADDRESS, 0, 1, value)
                0b10L -> ports.nand.write(NAND_CMD, 0, 1, value)
                0b11L -> throw GeneralException("Can't be CLE and ALE at the same time")
            }
        }
    }
}