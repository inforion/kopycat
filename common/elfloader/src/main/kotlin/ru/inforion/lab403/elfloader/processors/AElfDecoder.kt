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
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderType.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType.*
import java.io.Serializable


abstract class AElfDecoder (val file: ElfFile): Serializable {
    abstract val prefix: String
    abstract val pltHeadSize: Int
    abstract val pltEntrySize: Int

    abstract fun checkHeader()
    abstract fun checkFlags()
    abstract fun checkSectionType(type: Int)
    abstract fun checkSectionFlags(flags: Int)
    abstract fun checkSectionName(name: String, type: Int, flags: Int): Boolean
    abstract fun checkSymbolBinding(bind: Int)
    abstract fun checkSymbolType(type: Int)
    abstract fun checkSegmentType(type: Int)
    abstract fun checkSegmentFlags(flags: Int)
    abstract fun parseDynamic(hm: HashMap<Int, Long>, tag: Int, ptr: Long)
    abstract fun applyStaticRelocation(rel: ElfRel, vaddr: Long, symbol: Long, got: Long?, data: Long): Long
    open fun isLoadableSection(type: Int, access: ElfAccess): Boolean {
        return ((type == SHT_PROGBITS.id) || (type == SHT_NOBITS.id))
                && (access.isLoad)
    }

    open fun isLoadableSegment(type: Int) : Boolean {
        return type == PT_LOAD.id
    }

    abstract fun getProgramHeaderTypeNameById(type: Int): String
    abstract fun getRelocationNameById(type: Int): String

    open fun fixPaddr(addr: Long): Long = addr

    fun isJumpSlot(type: Int) = getRelocationNameById(type).endsWith("_JUMP_SLOT")
    fun isGlobDat(type: Int) = getRelocationNameById(type).endsWith("_GLOB_DAT")
}
