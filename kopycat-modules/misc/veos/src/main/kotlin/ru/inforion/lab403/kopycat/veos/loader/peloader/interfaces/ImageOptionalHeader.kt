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
package ru.inforion.lab403.kopycat.veos.loader.peloader.interfaces

import ru.inforion.lab403.kopycat.veos.loader.peloader.headers.ImageDataDirectory

interface ImageOptionalHeader {
    val magic: Int
    val majorLinkerVersion: Short
    val minorLinkerVersion: Short
    val sizeOfCode: ULong
    val sizeOfInitializedData: ULong
    val sizeOfUninitializedData: ULong
    val addressOfEntryPoint: ULong
    val baseOfCode: ULong
    val baseOfData: ULong // 32 only
    val imageBase: ULong // ULONGLONG
    val sectionAlignment: ULong
    val fileAlignment: ULong
    val majorOperatingSystemVersion: Int
    val minorOperatingSystemVersion: Int
    val majorImageVersion: Int
    val minorImageVersion: Int
    val majorSubsystemVersion: Int
    val minorSubsystemVersion: Int
    val win32VersionValue: ULong
    val sizeOfImage: ULong
    val sizeOfHeaders: ULong
    val checkSum: ULong
    val subsystem: Int
    val dllCharacteristics: Int
    val SizeOfStackReserve: ULong // ULONGLONG
    val SizeOfStackCommit: ULong // ULONGLONG
    val SizeOfHeapReserve: ULong // ULONGLONG
    val SizeOfHeapCommit: ULong // ULONGLONG
    val LoaderFlags: ULong
    val NumberOfRvaAndSizes: ULong
    val DataDirectory: Array<ImageDataDirectory>

    val export get()        = DataDirectory[0]
    val import get()        = DataDirectory[1]
    val resource get()      = DataDirectory[2]
    val exception get()     = DataDirectory[3]
    val security get()      = DataDirectory[4]
    val baseReloc get()     = DataDirectory[5]
    val debug get()         = DataDirectory[6]
    val architecture get()  = DataDirectory[7]
    val globalptr get()     = DataDirectory[8]
    val tls get()           = DataDirectory[9]
    val loadConfig get()    = DataDirectory[10]
    val boundImport get()   = DataDirectory[11]
    val iat get()           = DataDirectory[12]
    val delayImport get()   = DataDirectory[13]
    val comDescriptor get() = DataDirectory[14]
}