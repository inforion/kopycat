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
package ru.inforion.lab403.elfloader2.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.elfloader2.*
import java.io.Serializable


abstract class AElfDecoder (val file: ElfFile): Serializable {
    companion object {
        private val log = logger()
    }

    abstract val prefix: String
//    abstract val pltHeadSize: Int
//    abstract val pltEntrySize: Int

    abstract fun checkHeader() // TODO: remove
    abstract fun checkFlags()
    abstract fun checkSectionType(type: UInt) // TODO: remove
    abstract fun checkSectionFlags(flags: ULong)
    abstract fun checkSectionName(name: String, type: UInt, flags: ULong): Boolean
    abstract fun checkSymbolBinding(bind: UInt)
    abstract fun checkSymbolType(type: Int)// TODO: remove
    abstract fun checkSegmentType(type: UInt)// TODO: remove
    abstract fun checkSegmentFlags(flags: UInt)
    abstract fun parseDynamic(hm: MutableMap<ULong, ULong>, tag: ULong, ptr: ULong)
    abstract fun applyStaticRelocationRemoveMe(rel: ElfRel, vaddr: ULong, symbol: ULong, got: Optional<ULong>, data: ULong): ULong // TODO: remove
    open fun isLoadableSection(type: UInt, access: ElfAccess) = false
    open fun isLoadableSegment(type: UInt) = false

    open fun isSymbolCommon(sym: ElfSymbol) = sym.isObject && sym.isCommon
    open fun isSymbolAbs(sym: ElfSymbol) = /*it.infoType == STT_OBJECT.id &&*/ sym.isAbs
    open fun isSymbolUndefined(sym: ElfSymbol) = sym.isUndef && sym.ind != 0 //&& !sym.isNoType

    open fun relocationSize(type: ULong): Int = 0 // TODO: abstract
    open fun prepareToRelocation(region: ElfRegion, rel: ElfRel, got: Optional<ULong>) = Unit
    open fun relocationValue(
        relocations: List<ElfRel>,
        region: ElfRegion,
        rel: ElfRel,
        got: Optional<ULong>,
        baseAddress: ULong
    ): ULong? = 0uL

    open fun readData(region: ElfRegion, rel: ElfRel): ULong {
        val relSize = relocationSize(rel.type)
        val offset = region.toBufferOffset(rel.r_offset).requireInt
        val buffer = region.buffer
        return with (buffer) {
            position(offset)
            when (relSize) {
                1 -> byte.ulong_z
                2 -> short.ulong_z
                4 -> int.ulong_z
                8 -> ulong
                else -> throw NotImplementedError("Unknown size $relSize")
            }
        }
    }

    open fun writeData(region: ElfRegion, rel: ElfRel, value: ULong) {
        val relSize = relocationSize(rel.type)
        val offset = region.toBufferOffset(rel.r_offset).requireInt
        val buffer = region.buffer
        with (buffer) {
            position(offset)
            when (relSize) {
                1 -> put(value.byte)
                2 -> putShort(value.short)
                4 -> putInt(value.int)
                8 -> putLong(value.long)
                else -> throw NotImplementedError("Unknown size $relSize")
            }
        }
    }


    fun applyRelocation(relocations: List<ElfRel>, region: ElfRegion, rel: ElfRel, got: Optional<ULong>, baseAddress: ULong) {
        prepareToRelocation(region, rel, got)

        if (!rel.withAddend)
            rel.r_addend = readData(region, rel)

        val value = relocationValue(relocations, region, rel, got, 0u) ?: return

        log.warning { "$rel: ${value.hex16}" }

        writeData(region, rel, value)
    }

    abstract fun getProgramHeaderTypeNameById(type: UInt): String
    abstract fun getRelocationNameById(type: UInt): String

    fun isJumpSlot(type: UInt) = getRelocationNameById(type).endsWith("_JUMP_SLOT")
    fun isGlobDat(type: UInt) = getRelocationNameById(type).endsWith("_GLOB_DAT")

    open fun isGOTRelated(type: ULong) = false
}
