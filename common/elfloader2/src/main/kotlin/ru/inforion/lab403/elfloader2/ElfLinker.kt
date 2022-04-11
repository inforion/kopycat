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
package ru.inforion.lab403.elfloader2

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.opt
import ru.inforion.lab403.elfloader2.ElfLinker.VirtualRegion.*
import ru.inforion.lab403.elfloader2.enums.ELFCLASS
import ru.inforion.lab403.elfloader2.enums.ElfMachine.*
import java.nio.ByteBuffer

class ElfLinker(val elfFile: ElfFile) {

    companion object {
        private val log = logger(WARNING)

        fun rebaseAddress(address: ULong, baseAddress: ULong, rebaseAddress: ULong): ULong {
            require(address >= baseAddress) { "Not-based address" }
            return address - baseAddress + rebaseAddress
        }

        // aligns forward
        fun alignedAddress(start: ULong, align: ULong): ULong {
            val addr = (start / align) * align
            val rem = (start % align).truth
            return addr + if (rem) align else 0u
        }

        fun alignedNextAddress(start: ULong, size: ULong, align: ULong): ULong {
            var offset = start
            if (align > 1u)
                if (offset % align.uint != 0uL)
                    offset = (offset / align.uint + 1u) * align.uint
            return offset + size
        }

//        fun List<ElfSymbol>.addOffset()
    }

    private val order = elfFile.input.order

    private val is64bit = elfFile.elfHeader.e_ident_class == ELFCLASS.ELFCLASS64
    private val isExec = elfFile.elfHeader.isExec
    private val isShared = elfFile.elfHeader.isDyn
    private val isRelocatable = elfFile.elfHeader.isRel

    private val isPPC64 = elfFile.elfHeader.e_machine == EM_PPC64
    private val isArm = elfFile.elfHeader.e_machine == EM_ARM
    private val isMips = elfFile.elfHeader.e_machine == EM_MIPS

    private val ptrSize = elfFile.input.ptrSize
    private inline val ULong.aligned get() = alignedAddress(this, ptrSize.ulong_z)

    private fun List<ElfSymbol>.filterCommon() = filter { elfFile.decoder.isSymbolCommon(it) }
    private fun List<ElfSymbol>.filterAbs() = filter { elfFile.decoder.isSymbolAbs(it) }
    private fun List<ElfSymbol>.filterUndefined() = filter { elfFile.decoder.isSymbolUndefined(it) }

    private fun List<ElfSymbol>.filterNoFile() = filter { !it.isFile }
    private fun List<ElfSymbol>.findGOT() = find { it.name == "_GLOBAL_OFFSET_TABLE_" }

    enum class VirtualRegion(val regName: String) {
        ABS("abs"),
        COMMON("common"),
        EXTERN("extern"),
        GOT(".got");
    }
    private fun ByteBuffer.putPtr(value: ULong) = if (is64bit) putLong(value.long) else putInt(value.int)

    private fun List<ElfSymbol>.reorder(vararg preds: (ElfSymbol) -> Boolean): List<ElfSymbol> {
        val result = mutableListOf<ElfSymbol>()
        val buffer = toMutableList()
        preds.forEach { pred ->
            val filtered = buffer.filter(pred)
            result.addAll(filtered)
            buffer.removeIf { it in filtered }
        }
        return result + buffer
    }

    private fun ElfImage.linkVirtualRegion(address: ULong, type: VirtualRegion, symbols: List<ElfSymbol>): ULong {
        if (symbols.isEmpty())
            return address

        val size = symbols.size.ulong_z * ptrSize
        val region = ElfRegion.virtual(type.regName, address, size, ptrSize.ulong_z, order)

        val filteredSymbols = when (type) {
            ABS -> symbols.filterNoFile()
            else -> symbols
        }
        // Place weak symbols at the end

        val sortedSymbols = when(elfFile.elfHeader.e_machine) {
            EM_386 -> {
                filteredSymbols.reorder(
                    { !it.isWeak || it.isFunc }
                )
            }
            EM_X86_64 -> {
                filteredSymbols.reorder (
                    { !it.isWeak },
                    { it.isFunc}
                )
            }
            EM_ARM -> {
                filteredSymbols.reorder (
                    { !it.isWeak }
                )
            }
            EM_MIPS -> filteredSymbols
            else -> {
                log.severe { "Not tested extern ordering" }
                filteredSymbols
            }
        }

//        filteredSymbols.forEach {
//            region.buffer.putPtr(it.st_value)
//        }
        sortedSymbols.forEachIndexed { index, elfSymbol ->
            region.buffer.putPtr(elfSymbol.st_value)
            elfSymbol.st_value = address + index.uint * ptrSize
        }
//        when (type) {
//            // Relocate symbols
//            COMMON, EXTERN, GOT ->
//            else -> Unit
//        }

        log.config { "Allocate memory region ${region.range.hex8} -> ${type.regName}" }
        regions.add(region)
        return address + size
    }

    private fun ElfImage.linkVirtualRegions(address: ULong, vararg data: Pair<VirtualRegion, List<ElfSymbol>>): ULong {
        var lastAddress = address
        data.forEach { (type, symbols) ->
            lastAddress = linkVirtualRegion(lastAddress.aligned, type, symbols)
        }
        return lastAddress
    }

    private fun ElfImage.addPrgend(address: ULong): ULong {
        // I have no idea, wtf is this section
        // But added it for compatibility with IDA
        addEmptyVirtualRegion(".prgend", address, ptrSize.ulong_z, ptrSize.ulong_z)
        return address + ptrSize.ulong_z
    }

    val gotStringTable = ElfStringTable(mutableMapOf())

    private fun ElfImage.makeGOTSymbol(name: String, value: ULong = 0u, sectionIndex: UShort = 0u): ElfSymbol {
        val st_name = gotStringTable.allocate(name)
        return ElfSymbol(elfFile, symbols!!.minOf { it.ind } - 1, gotStringTable, st_name, value, ptrSize.ulong_z, 0u, 0u, sectionIndex).also {
            symbols.add(it)
        }
    }

    private fun generateGlobalOffsetTable(image: ElfImage, address: ULong) = with(image) {
        val gotSymbol = symbols!!.findGOT() ?: makeGOTSymbol("_GLOBAL_OFFSET_TABLE_")
        val globalOffsetTable = mutableListOf(gotSymbol)
        val symbolMapping = mutableMapOf(gotSymbol to gotSymbol)

        relocations!!.filter { elfFile.decoder.isGOTRelated(it.type) }.forEach {
            if (it.symbol !in symbolMapping) {
                val symbol = with(it.symbol) {
                    val value = if (isSection) 0u else st_value
                    makeGOTSymbol(".got.${complexName}", value, st_shndx_value)
                }
                globalOffsetTable.add(symbol)
                symbolMapping[it.symbol] = symbol
            }
            val symbol = symbolMapping[it.symbol]!!
            relocations[relocations.indexOf(it)] = it.copy(r_info = it.info(symbol.ind.ulong_z, it.type))
        }
        linkVirtualRegion(address.aligned, GOT, globalOffsetTable)
    }

    fun relocate(image: ElfImage): Unit = with(image) {
        if (isRelocatable) {
            require(dynamicRelocations.isEmpty()) { "Dynamic relocations in relocatable ELF-file" }
            val symbols = symbols ?: return@with
            val relocations = relocations ?: return@with

            val comSymbols = symbols.filterCommon()
            val absSymbols = symbols.filterAbs()
            val undSymbols = symbols.filterUndefined().toMutableList()

            // GOT never applies as part of external segment
            val gotSymbol = undSymbols.findGOT()?.also { undSymbols.remove(it) }

            val linkRegions = when {
                // In MIPS we have different order
                isMips -> arrayOf(
                    COMMON to comSymbols,
                    EXTERN to undSymbols,
                    ABS to absSymbols.filterNoFile()
                )
                else -> arrayOf(
                    COMMON to comSymbols,
                    ABS to absSymbols,
                    EXTERN to undSymbols
                )
            }

            val lastAddress = linkVirtualRegions(
                endAddress,
                *linkRegions
            )

//            val generateGOT = when {
//                isMips -> relocations.any { elfFile.decoder.isGOTRelated(it.type) }
//                gotSymbol != null -> {
//                    val gotSection = regions.find { it.name == ".got" }
//                    require(gotSection == null) { ".got section should be not presented in relocatable files" }
//
//                    val gotReloc = relocations.find { it.symbol == gotSymbol }
//
//                    gotReloc != null
//                }
//                else -> false
//            }
            // Let's try now simply check GOT-related relocations
            val generateGOT = relocations.any { elfFile.decoder.isGOTRelated(it.type) }
            if (generateGOT) {
                val gotSection = regions.find { it.name == ".got" }
                require(gotSection == null) { ".got section should be not presented in relocatable files" }
                generateGlobalOffsetTable(this, lastAddress)
            }
            relocations.forEach { rel ->
                elfFile.decoder.applyRelocation(relocations, rel.section, rel, symbols.findGOT()?.st_value.opt, baseAddress)
            }
        }
        else {
            val dynamicSymbols = dynamicSymbols ?: dynSegSymbols ?: return@with
            val comSymbols = dynamicSymbols.filterCommon().toMutableList()
            require(comSymbols.isEmpty()) { "Common symbols should not appear in executable and dynamic ELF-files" }

            // ABS symbols - in static symbol table
            val absSymbols = symbols?.filterAbs()?.filterNoFile()?.toMutableList() ?: emptyList()

            val undSymbols = dynamicSymbols.filterUndefined().toMutableList()
            var lastAddress = endAddress

            if (symbols?.find { it.name == "_end" } != null)
                lastAddress = addPrgend(lastAddress)

            linkVirtualRegions(
                lastAddress,
                EXTERN to undSymbols,
                ABS to absSymbols
            )
            val relocations = relocations ?: dynamicRelocations

            relocations.forEach { rel ->
                elfFile.decoder.applyRelocation(relocations, rel.section, rel, symbols?.findGOT()?.st_value.opt, baseAddress)
            }

//            // TODO: move it outside ElfLoader
//            when {
//                isShared -> {
////            originalSymbols?.let {
////                val absSize = filterAbs(it).size * 4  // TODO: NPE here for mips-vanilla lighttpd!!
////                regions.add(virtualRegion("abs", lastAddress, absSize.ulong_z))
////                lastAddress += absSize
////            }
////            regions.addVirtualRegions(lastAddress, ABS to filterAbs(originalSymbols))
//                    if (symbols != null)
//                        TODO("Here is a strange case. Check it out (virtualRegionSize, originalSymbols, absSymbols)")
////                lastAddress = regions.addVirtualRegion(lastAddress, ABS, filterAbs(originalSymbols))
//                    linkVirtualRegion(lastAddress, EXTERN, undSymbols)
//                    TODO("Calibrate")
//                }
//
//                else -> {
//                    if (isMips)
//                        lastAddress = addPrgend(lastAddress)
//                    linkVirtualRegions(
//                        lastAddress,
//                        EXTERN to undSymbols,
//                        ABS to absSymbols
//                    )
//                    TODO("Calibrate")
//                }
//            }
        }
    }

    fun removeNonLoadable(image: ElfImage): Unit = with(image) {
        regions.filter { !it.isAllocate }.forEach { region ->
            relocations?.removeIf { it.section == region }
            dynamicRelocations.removeIf { it.section == region }
            regions.remove(region)
        }
    }

    fun rebase(image: ElfImage, newBaseAddress: ULong): Unit = with(image) {
        when {
            isRelocatable -> {
//                if (isPPC64)
//                    TODO("There is a bug with .tocstart section")

                var rebaseAddress = newBaseAddress
                regions.forEach {
                    it.vaddr = alignedAddress(rebaseAddress, it.align)
                    val size = if (it.size == 0uL) 1uL else it.size
                    rebaseAddress = it.vaddr + size

                    symbols?.filter { sym -> sym.st_shndx_value == it.ind.ushort }?.forEach { sym ->
                        sym.st_value += it.vaddr
                    }
                    relocations?.filter { rel -> rel.sectionIndex.requireInt == it.ind }?.forEach { rel ->
                        rel.r_offset += it.vaddr
                    }
                }

                check(dynamicSymbols == null) { "Unknown case for relocatable file" }
                check(dynamicRelocations.isEmpty()) { "Unknown case for relocatable file" }
            }
            else -> {
                regions.forEach {
                    it.vaddr = rebaseAddress(it.vaddr, baseAddress, newBaseAddress)
                }
                symbols?.forEach {
                    it.st_value = rebaseAddress(it.st_value, baseAddress, newBaseAddress)
                }
                relocations?.forEach {
                    it.r_offset = rebaseAddress(it.r_offset, baseAddress, newBaseAddress)
                }
                dynamicSymbols?.forEach {
                    it.st_value = rebaseAddress(it.st_value, baseAddress, newBaseAddress)
                }
                dynamicRelocations.forEach {
                    it.r_offset = rebaseAddress(it.r_offset, baseAddress, newBaseAddress)
                }
            }

        }



    }
}