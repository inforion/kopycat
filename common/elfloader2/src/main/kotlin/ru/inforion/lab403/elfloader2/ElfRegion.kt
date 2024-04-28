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
package ru.inforion.lab403.elfloader2

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader2.ElfAccess.Companion.toElfAccessFrProgram
import ru.inforion.lab403.elfloader2.ElfAccess.Companion.toElfAccessFrSection
import ru.inforion.lab403.elfloader2.enums.ElfProgramHeaderType
import ru.inforion.lab403.elfloader2.enums.ElfProgramHeaderType.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderType
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderType.*
import ru.inforion.lab403.elfloader2.headers.ElfProgramHeader
import ru.inforion.lab403.elfloader2.headers.ElfSectionHeader
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ElfRegion(
    val name: String,
    val ind: Int,
    private val type: UInt,
    var vaddr: ULong,
    val offset: ULong,
    val size: ULong,
    val data: ByteArray,
    val access: ElfAccess,
    val align: ULong,
    val isSection: Boolean,
    val isAllocate: Boolean,
    val order: ByteOrder
) {

    companion object {
        private val log = logger(WARNING)

        fun ElfSectionHeader.toElfRegion(elfFile: ElfFile): ElfRegion {
            val data = if (sh_type != SHT_NOBITS && isAlloc)
                elfFile.input.get(sh_offset.requireInt, sh_size.requireInt, sh_size.requireInt)
            else
                ByteArray(sh_size.requireInt)

            val access = sh_flags.toElfAccessFrSection

            val isAllocate = if (sh_type == SHT_PROC)
                elfFile.decoder.isLoadableSection(sh_type_value, access)
            else
                isAlloc && sh_type != SHT_NOTE // IDA don't loads .note

            val order = elfFile.input.order

            return ElfRegion(name, index, sh_type_value, sh_addr, sh_offset, sh_size, data, access, sh_addralign, true, isAllocate, order).also {
                log.config { "Allocate memory region ${it.range.hex8} -> $name" }
            }
        }

        fun ElfProgramHeader.toElfRegion(elfFile: ElfFile): ElfRegion {
            val typeName = if (p_type == PT_PROC)
                elfFile.decoder.getProgramHeaderTypeNameById(p_type_value)
            else
                p_type.shortName

            val name = typeName

            val data = if ((p_type != PT_NULL) && (p_memsz != 0uL))
                elfFile.input.get(p_offset.requireInt, p_memsz.requireInt, p_filesz.requireInt)
            else
                ByteArray(p_memsz.requireInt)

            val access = p_flags.toElfAccessFrProgram

            val isAllocate = if (p_type == PT_PROC)
                elfFile.decoder.isLoadableSegment(p_type_value)
            else
                p_type == PT_LOAD

            val order = elfFile.input.order

            return ElfRegion(name, ind, p_type_value, p_vaddr, p_offset, p_memsz, data, access, p_align, false, isAllocate, order).also {
                log.config { "Allocate memory region ${it.range.hex8} -> $name" }
            }
        }

        fun virtual(name: String, vaddr: ULong, size: ULong, align: ULong, order: ByteOrder = ByteOrder.BIG_ENDIAN) =
            ElfRegion(name, -1, PT_LOAD.low, vaddr, 0u, size, ByteArray(size.requireInt) { 0xFF.byte }, ElfAccess.virtual, align, false, true, order)

    }

    val buffer = ByteBuffer.wrap(data).apply { order(order) }

//    fun copy() = ElfRegion(name, ind, type, vaddr, offset, size, data, access, align, isSection, isAllocate, order)

    val sectionType by lazy { ElfSectionHeaderType.castOrThrow(type) }
    val segmentType by lazy { ElfProgramHeaderType.castOrThrow(type) }

    val end get() = vaddr + size
    val range get() = vaddr until end
    val rfirst get() = range.first
    val rlast get() = range.last

    val offsetEnd get() = offset + size
    val offsetRange get() = offset until offsetEnd

    // TODO: local offset or file?
//    fun toOffset(addr: ULong): ULong {
//        require(addr in range) { "Wrong address" }
//        return addr - vaddr
//    }

//    fun toAddress(off: ULong): ULong {
//        require(off in offsetRange) { "Wrong offset" }
//        return off - offset + vaddr
//    }

    fun toBufferOffset(addr: ULong): ULong {
        require(addr in range) { "Wrong address" }
        return addr - vaddr
    }

    operator fun contains(addr: ULong) = addr in range

    infix fun intersects(other: ElfRegion) = rfirst in other.range || rlast in other.range
    infix fun notIntersects(other: ElfRegion) = !intersects(other)
    infix fun divides(other: ElfRegion) = rfirst in other.range && rlast in other.range
    infix fun fits(other: ElfRegion) = (rfirst in other.range || other.rlast in range) && (other.rfirst in range || rlast in other.range)
    infix fun notFits(other: ElfRegion) = !fits(other)

    infix fun split(other: ElfRegion): Pair<ElfRegion?, ElfRegion?> {
        val div = other.range
        val my = range

        require(my.first <= div.first && my.last >= div.last) { "Bad overlapping" }

        val first = if (my.first == div.first) null else {
            val name = "${name}_0"
            val range = my.first..div.first // last index is exclusive
            val data = data.copyOfRange((range shiftDown my.first).requireIntRange)
            ElfRegion(name, ind, type, my.first, offset, range.length, data, access, align, isSection, isAllocate, order)
        }

        val second = if (my.last == div.last) null else {
            val name = "${name}_1"
            val start = div.last + 1u
            val range = start..my.last // last index is exclusive
            val data = data.copyOfRange((range shiftDown my.first).requireIntRange)
            ElfRegion(name, ind, type, start, offset, range.length, data, access, align, isSection, isAllocate, order)
        }

        return first to second
    }

    override fun toString(): String {
        val (regType, itType) = if (isSection) "Section" to sectionType.shortName else "Segment" to segmentType.shortName
        val allocStr = if (isAllocate) "A" else "N"
        val logInd = if (ind < 10) "$ind " else ind.toString()
        return "$regType [$logInd] ${name.field(30)} ${itType.field(10)} V:${vaddr.hex16} O:${offset.hex16} S:${size.hex16} $access $allocStr /$align"
    }
}