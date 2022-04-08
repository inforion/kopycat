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
package ru.inforion.lab403.kopycat.veos.loader.peloader.headers

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.common.extensions.ushr
import ru.inforion.lab403.kopycat.veos.loader.peloader.PEFile
import java.nio.ByteBuffer


class ImageThunkData(private val peFile: PEFile, private val input: ByteBuffer, val importAddress: ULong) {
    val forwarderString = input.int.ulong_z
    val function = forwarderString
    val addressOfData = forwarderString
    val ordinal = (forwarderString ushr 30 == 1uL)

    fun toImageImportByName(): ImageImportByName {
        require (!ordinal) {"Can't translate for ordinal system"}
        input.position(peFile.rva2foa(addressOfData).int)
        return ImageImportByName(input)
    }
}