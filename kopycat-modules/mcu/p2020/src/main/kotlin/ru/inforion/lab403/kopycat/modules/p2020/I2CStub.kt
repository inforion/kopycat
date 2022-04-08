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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.kopycat.cores.base.common.Module

/**
 * Created by shiftdj on 22.01.2021.
 */

class I2CStub(
        parent: Module,
        name: String,
        address: ULong
): I2CAddressedSlave(parent, name, address) {

    override fun read(index: Int) = throw NotImplementedError("Can't read from I2CStub")
    override fun write(index: Int, value: ULong) {
        log.info { "Write to I2CStub: [${index.hex2}] ${value.hex2}" }
    }
}