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

import ru.inforion.lab403.common.extensions.int_z
import java.nio.ByteBuffer


class ImageDosHeader(input: ByteBuffer) {

    val magic = input.short.int_z                /* 00: MZ Header signature */
    val cblp = input.short.int_z                 /* 02: Bytes on last page of file */
    val cp = input.short.int_z                   /* 04: Pages in file */
    val crlc = input.short.int_z                 /* 06: Relocations */
    val cparhdr = input.short.int_z              /* 08: Size of header in paragraphs */
    val minalloc = input.short.int_z             /* 0a: Minimum extra paragraphs needed */
    val maxalloc = input.short.int_z             /* 0c: Maximum extra paragraphs needed */
    val ss = input.short.int_z                   /* 0e: Initial (relative) SS value */
    val sp = input.short.int_z                   /* 10: Initial SP value */
    val csum = input.short.int_z                 /* 12: Checksum */
    val ip = input.short.int_z                   /* 14: Initial IP value */
    val cs = input.short.int_z                   /* 16: Initial (relative) CS value */
    val lfarlc = input.short.int_z               /* 18: File address of relocation table */
    val ovno = input.short.int_z                 /* 1a: Overlay number */
    val res = Array(4) { input.short.int_z }     /* 1c: Reserved words */
    val oemid = input.short.int_z                /* 24: OEM identifier (for e_oeminfo) */
    val oeminfo = input.short.int_z              /* 26: OEM information; e_oemid specific */
    val res2 = Array(10) { input.short.int_z }   /* 28: Reserved words */
    val lfanew = input.short.int_z               /* 3c: Offset to extended header */

    init {
        require(magic == 0x5A4D) { "MZ signature check failed" }
    }
}
