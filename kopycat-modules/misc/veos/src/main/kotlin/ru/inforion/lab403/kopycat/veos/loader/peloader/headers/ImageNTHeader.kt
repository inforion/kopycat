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
package ru.inforion.lab403.kopycat.veos.loader.peloader.headers

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.veos.loader.peloader.enums.ImageFileMachine
import ru.inforion.lab403.kopycat.veos.loader.peloader.headers.x32.ImageOptionalHeader32
import ru.inforion.lab403.kopycat.veos.loader.peloader.interfaces.ImageOptionalHeader
import java.nio.ByteBuffer

class ImageNTHeader(input: ByteBuffer) {
    val signature = input.int.asULong           /* "PE"\0\0 */	/* 0x00 */
    val fileHeader = ImageFileHeader(input)     /* 0x04 */
    val optionalHeader: ImageOptionalHeader   /* 0x18 */

    init {
        require(signature == 0x4550L) { "PE signature check failed" }

        optionalHeader = when (fileHeader.machine) {
            ImageFileMachine.I386 -> ImageOptionalHeader32(input)
            else -> TODO("Not implemented")
        }
    }
}

