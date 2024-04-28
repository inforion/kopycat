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


import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.logging.WARNING
import java.io.File
import java.nio.ByteBuffer


class ElfLoader(
    input: ByteBuffer,
    var rebaseAddress: ULong = 0u,
    var forceRebase: Boolean = false
) {

    val elfFile = ElfFile(input)

    val isSharedObject = elfFile.elfHeader.isDyn
    val isRelocatableFile = elfFile.elfHeader.isRel
    val doRebase = isRelocatableFile || forceRebase

    val linker = ElfLinker(elfFile)

    val image get() = ElfImage.fromElfLoader(elfFile)

    val basedImage get() = image.apply {
        linker.removeNonLoadable(this)
        if (doRebase) {
            val address = when {
                forceRebase -> rebaseAddress
                elfFile.elfHeader.isX86 -> 0x08000000uL // IDA selects this address as base
                else -> 0x0uL
            }
            linker.rebase(this, address)
        }
    }

    val relocatedImage get() = basedImage.apply {
//        if (linkVirtualRegions)
        linker.relocate(this)
//        linker.applyRelocations(this)
    }
    // TODO: ---------------------------------------- NEW BORDER -------------------------------------------------


    companion object {
        private val log = logger(WARNING)

        fun fromPath(
            path: String,
            rebaseAddress: ULong = 0uL,
            forceRebase: Boolean = false
        ): ElfLoader {
            val arr = File(path).readBytes()
            val buf = ByteBuffer.wrap(arr)

            return ElfLoader(buf, rebaseAddress, forceRebase)
        }

//        fun applyRelocations(regions: ElfRegionImage, relocations: List<ElfRelocation>) {
//            for (rel in relocations) {
//                TODO("ELF64")
//                val region = regions.byAddress(rel.vaddr)
//                region.buffer.putInt(rel.offset, rel.value.int)
//            }
//        }
    }

//    data class ElfRelocation(
//        val regionIndex: Int,
//        val symtabIndex: Int,
//        val vaddr: ULong,
//        val offset: Int,
//        val value: ULong,
//        val type: Int,
//        val sym: Int,
//        val addend: Int,
//        val withAddend: Boolean)
//
//
//
//    // Stage 1: get regions from sections or segments
//    val regions: ElfRegionImage by lazy { ElfRegionImage.fromElfLoader(this, generateVirtualRegions) }
//
//    // Stage 2: filter loadable, rebase and fix intersections
//    val loadedRegions: ElfRegionImage by lazy {
//        regions
//            .filterLoadable()
//            .doRebaseOrCopy(doStaticRebase, specificBaseAddress, isRelocatableFile)
//            .fixIntersections()
//    }
//
//    // Stage 3: apply static and dynamic relocations
//    val relocatedRegions: ElfRegionImage by lazy {
//        val result = loadedRegions.copy()
//
//        applyRelocations(result, staticRelocations)
//        // Why? Lazy-deoptimization?
//        if (elfFile.dynamicSymbolTable == null)
//            applyRelocations(result, dynamicRelocations)
//        result
//    }
//
////    val baseAddress : ULong by lazy {
////        regions.filterLoadable().minByOrNull { it.vaddr }!!.vaddr
////    }
//

//
//
//    private fun rebaseAddress(address: ULong) = rebaseAddress(address, baseAddress, specificBaseAddress)
//
//    val entryPoint by lazy {
//        if (doStaticRebase)
//            if (isRelocatableFile)
//                loadedRegions.offsetToAddress(elfFile, elfFile.elfHeader.e_entry)
//            else
//                rebaseAddress(elfFile.elfHeader.e_entry)
//        else
//            elfFile.elfHeader.e_entry
//    }
//    // TODO: ---------------------------------------- BORDER -------------------------------------------------
//
//
//
//
//    // TODO: rename this
//    val originalSymbols = elfFile.symbolTable?.toList()
//    val originalDynamicSymbols = elfFile.dynamicSymbolTable?.toList() ?: elfFile.dynSegSymbolTable?.toList()
//    val originalDynamicSegmentSymbols = elfFile.dynSegSymbolTable?.toList()
//
////    val hashTable = elfFile.hashTable
//
//
////    val symbols: List<ElfSymbol>? by lazy {
////        val result = originalSymbols ?: return@lazy null
////
////        if (doStaticRebase) {
////            for (it in result) {
////                it.value = if (isRelocatableFile) {
////
////                } else
////                    rebaseAddress(it.value.uint)
////            }
////        }
////        result
////    }
////
//
//
//
////    fun <T> List<T>.optionalMap(condition: Boolean, block: T.() -> T) = if (condition) map(block) else this
//
//
//
//
//
////    val dynamicSegment get() = elfFile.dynamicSegment
//
////    val sectionStringTable = elfFile.sectionStringTable
//
//
//
//
////    val machine = elfFile.e_machine
//
//
//
//
//
//    val globalOffsetTable by lazy { symbols?.find { it.name == "_GLOBAL_OFFSET_TABLE_"}?.value.opt }
//
//
//
//
//
//
//
//
//
//    /*syms.filter {
//        it.infoType == STT_NOTYPE.id && it.shndx == SHN_ABS.id
//    }*/
//
//
//    private var comSymbolsLastOffset = 0
//    private var absSymbolsLastOffset = 0
//    private var undSymbolsLastOffset = 0
//
//    private fun applyVirtualRegions(syms: MutableList<ElfSymbol>, isDynamic: Boolean): MutableList<ElfSymbol>? {
//        loadedRegions //lazy call
//        if (isDynamic)
//            symbols //lazy call
//
//        val commonSymbols = filterCommon(syms)
//        for ((i, it) in commonSymbols.withIndex()) {
//            it.value = commonAddress + comSymbolsLastOffset + i * 4
//        }
//        comSymbolsLastOffset += commonSymbols.size * 4
//
//        val absSymbols = filterAbs(syms)
//        for ((i, it) in absSymbols.withIndex()) {
//            it.value = absAddress + absSymbolsLastOffset + i * 4
//        }
//        absSymbolsLastOffset += absSymbols.size * 4
//
//        val externSymbols = filterUndefined(syms)
//        for ((i, it) in externSymbols.withIndex()) {
//            it.value = externAddress + undSymbolsLastOffset + i * 4
////            log.info("${it.nameString}: ${it.value.hex8}")
//        }
//        undSymbolsLastOffset += externSymbols.size * 4
//
//        return syms
//    }
//
//
//
////    val dynamicSymbols: MutableList<ElfSymbol>? by lazy {
////        val result = originalDynamicSymbols?.toMutableList() //Make copy
////        if (result != null && forceUseSpecificBaseAddress) {
////            result.forEach { it.value = rebaseAddress(it.value.uint) }
////        }
////        when {
////            result == null -> result
//////            generateVirtualRegions -> applyVirtualRegions(result, true)
////            else -> result
////        }
////    }
//
//    // TODO: REWRITE
//
////    val dynsegSymbols: MutableList<ElfSymbol>? by lazy {
////        val result = originalDynamicSegmentSymbols?.toMutableList() //Make copy
////        if (result != null && forceUseSpecificBaseAddress) {
////            result.forEach { it.value = rebaseAddress(it.value.uint) }
////        }
////        when {
////            result == null -> result
////            generateVirtualRegions -> applyVirtualRegions(result, true)
////            else -> result
////        }
////    }
//
//    val undefinedSymbols: MutableList<ElfSymbol>?
//        get() {
//            val syms: MutableList<ElfSymbol> = dynamicSymbols ?: return null
//            return filterUndefined(syms).toMutableList()
//        }
//
//
//
//
//    private fun applyStaticRelocation(syms: MutableList<ElfSymbol>, rel: ElfRel, vaddr: ULong, offset: Int?): ULong {
//        val data = if (offset != null) {
//            input.position(offset)
//            input.int.ulong_z
//        } else 0uL
//
//        val symbol = syms[rel.sym].value
//        val got = globalOffsetTable
//
//        //if (rel.type == ArmRelocationType.R_ARM_GLOB_DAT.id)
//        //    log.info("BP")
//
//        val relData =  elfFile.decoder.applyStaticRelocation(rel, vaddr, symbol, got, data) mask 32 //TODO: for Elf64 should remake
////        log.warning{"Relocation data of type ${elfFile.decoder!!.getRelocationNameById(rel.type)} for symbol \"${syms[rel.sym].nameString}\": ${relData.hex8}"}
//        return relData
//    }
//
//
//    val staticRelocations: MutableList<ElfRelocation> by lazy {
//        //TODO: To map?
//        val result = mutableListOf<ElfRelocation>()
//
//        if (isRelocatableFile) {
////            log.warning { "==== before rel in elfFile.staticRelocations!! ====" }
//
//            for (rel in elfFile.staticRelocations!!) {
//                val region = loadedRegions.find {
//                    it.ind == rel.sectionIndex //It seems to be never wrong if file is relocatable
//                }
//
//                if (region == null) {
//                    val unloadedRegion = regions.find {
//                        it.ind == rel.sectionIndex //It seems to be never wrong if file is relocatable
//                    } ?: throw EBadSectionIndex("Reloc for offset ${rel.r_offset.hex8} isn't found (section index ${rel.sectionIndex})")
//
//                    log.info {"Reloc for offset ${rel.r_offset.hex8} is in unloaded region \"${unloadedRegion.name}\"(section index ${rel.sectionIndex})" }
//                    continue
//                }
//
//                val vaddr = region.vaddr + rel.r_offset
//                val fileOffset = (rel.r_offset + region.offset).int
//
//                val symtable = when (regions[rel.symTabIndex].sectionType) {
//                    SHT_SYMTAB -> symbols
//                    SHT_DYNSYM -> dynamicSymbols
//                    else -> throw EBadSymbolTable("Not valid symbol table for reloc entry ${rel.r_offset.hex8}")
//                } ?: throw EBadSymbolTable("Can't relocate without symbol table")
//
//                val value = applyStaticRelocation(symtable, rel, vaddr, fileOffset)
//                result.add(ElfRelocation(
//                        rel.sectionIndex,
//                        rel.symTabIndex,
//                        vaddr,
//                        rel.r_offset.int,
//                        value,
//                        rel.type,
//                        rel.sym,
//                        rel.addend,
//                        rel.withAddend))
//            }
//
//        }
//        else {
//            for (rel in elfFile.staticRelocations!!) {
//                val vaddr = if (forceUseSpecificBaseAddress) rebaseAddress(rel.r_offset.uint) else rel.r_offset
//
//                val region = loadedRegions.find { vaddr in it }
//
//                if (region == null) {
//                    val unloadedRegion = regions.find {
//                        vaddr int it
//                    } ?: throw EBadAddressValue("Reloc for address ${vaddr.hex8} isn't found (section index ${rel.sectionIndex})")
//
//                    log.info { "Reloc for address ${vaddr.hex8} is in unloaded region \"${unloadedRegion.name}\"(section index ${rel.sectionIndex})" }
//                    continue
//                }
//
//                val offset = (vaddr - region.vaddr).int
//                val fileOffset = offset + region.offset
//
//                val symtable = when (regions[rel.symTabIndex].type) {
//                    SHT_SYMTAB.low -> symbols
//                    SHT_DYNSYM.low -> dynamicSymbols
//                    else -> throw EBadSymbolTable("Not valid symbol table for reloc entry ${rel.r_offset.hex8}")
//                } ?: throw EBadSymbolTable("Can't relocate without symbol table")
//
//                val value = applyStaticRelocation(symtable, rel, vaddr, fileOffset)
//                result.add(ElfRelocation(
//                        rel.sectionIndex,
//                        rel.symTabIndex,
//                        vaddr,
//                        offset,
//                        value,
//                        rel.type,
//                        rel.sym,
//                        rel.addend,
//                        rel.withAddend))
//            }
//        }
//        result
//    }
//
//
//    //TODO: are we need it?
//
//    val dynamicRelocations: MutableList<ElfRelocation> by lazy {
//        val result = mutableListOf<ElfRelocation>()
//        for (rel in elfFile.dynamicRelocations) {
//            val vaddr = if (forceUseSpecificBaseAddress) rebaseAddress(rel.r_offset.uint) else rel.r_offset
//
//            val region = loadedRegions.find { vaddr in it }
//            if (region == null) {
//                val unloadedRegion = regions.find {
//                    vaddr in it
//                } ?: throw EBadAddressValue("Reloc for address ${vaddr.hex8} isn't found (section index ${rel.sectionIndex})")
//
//                log.info { "Reloc for address ${vaddr.hex8} is in unloaded region \"${unloadedRegion.name}\"" }
//                continue
//            }
//
//            val fileOffset: Int?
//            val offset: Int
//            if ((hasAllocSections) && (region.sectionType == SHT_NOBITS)) {
//                fileOffset = null
//                offset = 0
//            } else {
//                offset = (vaddr - region.vaddr).int
//                fileOffset = offset + region.offset
//            }
//
//            if (dynamicSymbols == null)
//                throw EBadSymbolTable("Can't relocate without dynamic segment symbol table")
//
//            val value = applyStaticRelocation(dynamicSymbols!!, rel, vaddr, fileOffset)
//            result.add(ElfRelocation(
//                    region.ind,
//                    0,
//                    vaddr,
//                    offset,
//                    value,
//                    rel.type,
//                    rel.sym,
//                    rel.addend,
//                    rel.withAddend))
//        }
//
//        result
//    }
//
//    val jumpSlots: List<ElfRelocation>
//        get() = staticRelocations.filter { elfFile.decoder.isJumpSlot(it.type) }
//
//    val globalData: List<ElfRelocation>
//        get() = staticRelocations.filter { elfFile.decoder.isGlobDat(it.type) }


}