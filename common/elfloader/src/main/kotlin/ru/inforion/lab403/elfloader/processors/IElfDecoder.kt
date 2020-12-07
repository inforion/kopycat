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
package ru.inforion.lab403.elfloader.processors

import ru.inforion.lab403.elfloader.ElfAccess
import ru.inforion.lab403.elfloader.ElfFile
import ru.inforion.lab403.elfloader.ElfRel



interface IElfDecoder {
    val sectionsPrefix: String
    val pltHeadSize: Int
    val pltEntrySize: Int

    fun checkHeader(elf: ElfFile)
    fun setFlags(flags: Int)
    fun checkSectionType(type: Int)
    fun checkSectionFlags(flags: Int)
    fun checkSectionName(name: String, type: Int, flags: Int): Boolean
    fun checkSymbolBinding(bind: Int)
    fun checkSymbolType(type: Int)
    fun checkSegmentType(type: Int)
    fun checkSegmentFlags(flags: Int)
    fun parseDynamic(tag: Int, ptr: Int)
    fun applyStaticRelocation(rel: ElfRel, vaddr: Long, symbol: Long, got: Long?, data: Long, file: ElfFile): Long
    fun isLoadableSection(type: Int, access: ElfAccess): Boolean
    fun isLoadableSegment(type: Int) : Boolean
    fun getProgramHeaderTypeNameById(type: Int): String
    fun getRelocationNameById(type: Int): String
}