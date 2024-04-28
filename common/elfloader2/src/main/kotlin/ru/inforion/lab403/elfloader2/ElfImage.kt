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

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.putArray
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader2.ElfRegion.Companion.toElfRegion

class ElfImage(
    regions: List<ElfRegion>,
    symbols: List<ElfSymbol>?,
    dynamicSymbols: List<ElfSymbol>?,
    dynSegSymbols: List<ElfSymbol>?,
    relocations: List<ElfRel>?,
    dynamicRelocations: List<ElfRel>
) {
    val regions = regions.toMutableList()
    val symbols = symbols?.map { it.copy() }?.toMutableList()
    val dynamicSymbols = dynamicSymbols?.map { it.copy() }?.toMutableList()
    val dynSegSymbols = dynSegSymbols?.map { it.copy() }?.toMutableList()
    val relocations = relocations?.map { it.copy(symbolTable = this.dynamicSymbols ?: this.symbols!!) }?.toMutableList()
    val dynamicRelocations = dynamicRelocations.map { it.copy(symbolTable = this.dynSegSymbols!!) }.toMutableList()

    companion object {
        val log = logger(INFO)

        fun fromElfLoader(elfFile: ElfFile): ElfImage {
            val regions = if (elfFile.elfHeader.isRel) {
                elfFile.sections.map { it.toElfRegion(elfFile) }
            }
            else {
                // We should perform some manipulations on segments to retrieve maximum information from loadable sections
                // That means that we will cut off every part of every segment that belongs to any of loadable sections
                val segments = elfFile.segments.map { it.toElfRegion(elfFile) }
                val sections = elfFile.sections.map { it.toElfRegion(elfFile) }

                val loadableSections = sections.filter { it.isAllocate }

                val fragmentedSegments = segments.toMutableList()
                loadableSections.forEach { sect ->
                    fragmentedSegments.filter { sect intersects it }.forEach {
                        val (a, b) = it.split(sect)
                        a?.run { fragmentedSegments.add(a) }
                        b?.run { fragmentedSegments.add(b) }
                        fragmentedSegments.remove(it)
                    }
                }
                (loadableSections + fragmentedSegments).toMutableList().apply { sortBy { it.vaddr } }
            }

            return with(elfFile) {
                ElfImage(regions,
                    symbolTable,
                    dynamicSymbolTable,
                    dynSegSymbolTable,
                    relocations,
                    dynamicRelocations
                )
            }
        }

    }

    fun addEmptyVirtualRegion(name: String, vaddr: ULong, size: ULong, align: ULong) = regions.add(ElfRegion.virtual(name, vaddr, size, align))

    val baseAddress: ULong get() = regions.minByOrNull { it.vaddr }!!.vaddr
    val endAddress: ULong get() = regions.maxByOrNull { it.vaddr }!!.end

    val ElfRel.section get() = if (sectionIndex == 0u)
        regions.first { r_offset in it.range }
    else
        regions.first { region -> region.ind.uint == sectionIndex}

    fun log_layout() {
        log.info { "Regions:" }
        regions.forEach {
            log.info { it }
        }
        symbols?.also {
            log.info { "Symbols:" }
        }?.forEach {
            log.info { it }
        }
        dynamicSymbols?.also {
            log.info { "Dynamic symbols:" }
        }?.forEach {
            log.info { it }
        }
        dynSegSymbols?.also {
            log.info { "Dynamic segment symbols:" }
        }?.forEach {
            log.info { it }
        }
        relocations?.also {
            log.info { "Relocations:" }
        }?.forEach {
            log.info { it }
        }
        dynamicRelocations.also {
            if (dynamicRelocations.isNotEmpty())
                log.info { "Dynamic relocations:" }
        }.forEach {
            log.info { it }
        }
    }

    val flatImage: ByteArray get() {
        val baseAddress = baseAddress
        val size = (endAddress - baseAddress).requireInt
        val memory = ByteArray(size) { 0xFF.byte }
        regions.forEach {
            val offset = it.vaddr - baseAddress
            memory.putArray(offset.requireInt, it.data)
        }
        return memory
    }

}