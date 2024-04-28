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
import ru.inforion.lab403.kopycat.cores.base.common.Module

/**
 * Created by shiftdj on 03.02.2021.
 */
 
class IOCTRLBIST(
        parent: Module,
        name: String,
        address: ULong): I2CAddressedSlave(parent, name, address) {

    companion object {
        const val size = 256
    }

    private val memory = ByteArray(size)

    private fun validateSequence(): Boolean {
        if (!memory.slice(0x28..0x2C).toByteArray().contentEquals(byteArrayOf(0x00, 0x0C, 0x00, 0x00, 0x00)))
            return false
        if (stage < 2 && memory[0x2D] != stage.byte)
            return false
        stage++
        return true
    }

    private var stage: Int = 0

    override fun read(index: Int): ULong {
        check(stage == 3) { "Unknown case of read on stage $stage" }
        return 0b10u // Second bit is ready bit
    }
    override fun write(index: Int, value: ULong) {
        require (index in 0x28..0x2D) { "Unknown sequence received" }

        memory[index] = value.byte
        if ((stage < 2 && index == 0x2D) || (stage == 2 && index == 0x29)) {
            check (validateSequence()) { "Unknown sequence received" }
        }
    }
}