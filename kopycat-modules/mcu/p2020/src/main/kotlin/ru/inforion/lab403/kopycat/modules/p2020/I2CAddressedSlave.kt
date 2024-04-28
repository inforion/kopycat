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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.BUS07


/**
 * Created by shiftdj on 22.01.2021.
 */


abstract class I2CAddressedSlave(parent: Module, name: String, val address: ULong): Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS07)
    }

    override val ports = Ports()

    enum class State { Selected, Addressed }

    private var state = State.Selected
    private var index = 0

    abstract fun read(index: Int): ULong
    abstract fun write(index: Int, value: ULong)


    private val mem = object : Register(ports.mem, address, Datatype.BYTE, "Control") {

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            require(size == 1) { "Only byte-access allowed" }
            require(ss == 0) { "Unknown SS = $ss" }
            return when (state) {
                State.Selected -> {
                    state = State.Addressed
                    0x00uL
                }
                State.Addressed -> read(index++)
            }
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            require(size == 1) { "Only byte-access allowed" }
            when (ss) {
                0 -> when (state) {
                    State.Selected -> {
                        index = value.int like Datatype.BYTE
                        state = State.Addressed
                    }
                    State.Addressed -> write(index++, value like Datatype.BYTE)
                }
                1 -> state = State.Selected
                else -> throw GeneralException("Unknown SS = $ss")
            }
        }
    }
}