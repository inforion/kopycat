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

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.types.Resource
import java.io.InputStream

/**
 * Created by shiftdj on 22.01.2021.
 */

// Microchip 24C02
// chipSelect - 3 bits (0 - 7)
// 256 x 8-bit = 2K bit
class M24C02(
        parent: Module,
        name: String,
        stream: InputStream? = null,
        val chipSelect: Int = 0
): I2CAddressedSlave(parent, name, 0b1010_000uL + chipSelect.uint) {

    companion object {
        const val size = 256
    }

    constructor(parent: Module, name: String, data: Resource, chipSelect: Int = 0) :
            this(parent, name, data.openStream(), chipSelect)

    private val memory = ByteArray(size).apply { stream?.read(this) }

    override fun read(index: Int) = memory[index].ulong_z
    override fun write(index: Int, value: ULong) = run { memory[index] = value.byte }

//    private var index = 0

//    enum class State {
//        Selected,
//        Addressed,
//    }
//    var state = State.Selected
//
//    val mem = object : Register(ports.mem, 0, Datatype.BYTE, "Control") {
//
//        override fun read(dtyp: Datatype, ea: Long, ss: Int): Long {
//            require(dtyp == Datatype.BYTE) { "Only byte-access allowed" }
//            require(ss == 0) { "Unknown SS = $ss" }
//            return memory[index++].asULong
//        }
//
//        override fun write(dtyp: Datatype, ea: Long, value: Long, ss: Int) {
//            require(dtyp == Datatype.BYTE) { "Only byte-access allowed" }
//            when (ss) {
//                0 -> when (state) {
//                    State.Selected -> {
//                        index = value.int like dtyp
//                        state = State.Addressed
//                    }
//                    State.Addressed -> memory[index++] = value.byte like dtyp
//                }
//                1 -> state = State.Selected
//                else -> throw GeneralException("Unknown SS = $ss")
//            }
//        }
//    }
}