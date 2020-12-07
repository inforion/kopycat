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
package ru.inforion.lab403.elfloader


import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.elfloader.enums.*
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderType.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderFlag.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderIndex.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType.*
import ru.inforion.lab403.elfloader.enums.ElfSymbolTableType.*
import ru.inforion.lab403.elfloader.exceptions.EBadAddressValue
import ru.inforion.lab403.elfloader.exceptions.EBadIntersect
import ru.inforion.lab403.elfloader.exceptions.EBadSectionIndex
import ru.inforion.lab403.elfloader.exceptions.EBadSymbolTable
import java.io.File
import java.nio.ByteBuffer


class ElfLoader(val input: ByteBuffer,
                val generateVirtualRegions: Boolean = false,
                val specificBaseAddress: Long = 0x8000000L,
                val forceUseSpecificBaseAddress: Boolean = false) {

    val elfFile = ElfFile(input)

    val dynamicSegment
        get() = elfFile.dynamicSegment!!

    val sectionStringTable = elfFile.sectionStringTable

    
    val baseAddress : Long by lazy {
        filterLoadableRegions(regions).minByOrNull { it.vaddr }!!.vaddr
    }

    val machine = elfFile.machine

    companion object {
        private val log = logger(WARNING)

        fun fromPath(path: String,
                     generateVirtualRegions: Boolean = false,
                     specificBaseAddress: Long = 0x8000000L,
                     forceUseSpecificBaseAddress: Boolean = false): ElfLoader {
            val arr = File(path).readBytes()
            val buf = ByteBuffer.wrap(arr)

            return ElfLoader(buf, generateVirtualRegions, specificBaseAddress, forceUseSpecificBaseAddress)
        }
    }


    class ElfRegion(
            val name: String,
            val ind: Int,
            val type: Int,
            var vaddr: Long,
            val offset: Int,
            val size: Int,
            val data: ByteArray,
            val access: ElfAccess,
            val align: Int
    ) {
        fun toOffset(addr: Long): Int = (addr - vaddr).toInt()

        val end get() = vaddr + size

        val addressRange get() = vaddr until end

        fun isAddressIncluded(addr: Long): Boolean = addr in addressRange

        fun divide(other: ElfRegion): Pair<ElfRegion?, ElfRegion?> {
            val div = other.addressRange
            val my = addressRange

            val first = if (my.first == div.first) null else {
                val name = "${name}_DIV_${div.first.hex8}"
                val end = div.first - 1
                val size = (end - my.first + 1).toInt()
                val data = data.copyOfRange(0, (end - my.first).toInt())
                ElfRegion(name, ind, type, my.first, offset, size, data, access, align)
            }

            val second = if (my.last == div.last) null else {
                val name = "${name}_DIV_${div.last.hex8}"
                val start = div.last + 1
                val size = (my.last - start + 1).toInt()
                val data = data.copyOfRange((start - my.first).toInt(), this.size - 1)
                ElfRegion(name, ind, type, start, offset, size, data, access, align)
            }

            return Pair(first, second)
        }
    }

    data class ElfRelocation(
            val regionIndex: Int,
             val symtabIndex: Int,
             val vaddr: Long,
             val offset: Int,
             val value: Long,
             val type: Int,
             val sym: Int,
             val addend: Int,
             val withAddend: Boolean)

    
    val entryPoint: Long by lazy {
        if (isNeededStaticRebase) {
            if (isRelocatableFile)
                getAddressByOffset(elfFile.entry) ?: throw EBadAddressValue("Entry point is outside of any loadable segment")
            else
                rebaseForced(elfFile.entry)
        } else
            elfFile.entry
    }

    
    val globalOffsetTable: Long? by lazy {
        symbols?.find { it.name == "_GLOBAL_OFFSET_TABLE_"}?.value
    }


    val elfType: String = ElfType.getNameById(elfFile.type)

    val sectionLoading = elfFile.sectionLoading

    val isSharedObject: Boolean = (elfFile.type == ElfType.ET_DYN.id)
    val isRelocatableFile: Boolean = (elfFile.type == ElfType.ET_REL.id)
    val isNeededStaticRebase: Boolean = isRelocatableFile || (forceUseSpecificBaseAddress)


    private fun getAddressByOffset(offset: Long) : Long? {
        val section = elfFile.getSectionByOffset(offset)
        return if (section != null) { //TODO: Is it always works...
            val region = loadableRegions.find { it.ind == section.ind }
            if (region == null)
                null
            else
                region.vaddr + offset
        }
        else
            null
    }

    private fun rebaseForced(vaddr: Long): Long = vaddr + baseAddress - specificBaseAddress

    // TODO: delegates? <- Yeah do a delegate toMutableList() negates effect of lazy { ... }
    private val originalSymbols = elfFile.symbolTable?.toMutableList()
    private val originalDynamicSymbols =  elfFile.dynamicSymbolTable?.toMutableList()
            ?: elfFile.dynamicSegmentSymbolTable?.toMutableList()
    private val originalDynamicSegmentSymbols = elfFile.dynamicSegmentSymbolTable?.toMutableList()

    val hashTable = elfFile.hashTable

    //No use to make nullable because there is no special checks of it
    var commonAddress: Long = 0
    var absAddress: Long = 0
    var externAddress: Long = 0
    private var virtualRegionSize: Int = 0

    private fun filterCommon(syms: MutableList<ElfSymbol>): List<ElfSymbol> = syms.filter {
        it.infoType == STT_OBJECT.id && it.shndx == SHN_COMMON.id
    }

    private fun filterAbs(syms: MutableList<ElfSymbol>): List<ElfSymbol> = syms.filter {
        /*it.infoType == STT_OBJECT.id &&*/ it.shndx == SHN_ABS.id
    }
    /*syms.filter {
        it.infoType == STT_NOTYPE.id && it.shndx == SHN_ABS.id
    }*/

    private fun filterUndefined(syms: MutableList<ElfSymbol>): List<ElfSymbol> = syms.filter {
        //(it.infoType == STT_FUNC.id || it.infoType == STT_OBJECT.id || (isRelocatableFile && it.infoType == STT_NOTYPE.id)) && (it.shndx == SHN_UNDEF.id) && (it.ind != 0)
        it.shndx == SHN_UNDEF.id && it.ind != 0 && (it.infoType != STT_NOTYPE.id || elfFile.machine != ElfMachine.EM_ARM.id)
//    }.sortedBy { it.infoBind }
    }.sortedBy { it.ind }
    private var comSymbolsLastOffset = 0
    private var absSymbolsLastOffset = 0
    private var undSymbolsLastOffset = 0

    private fun applyVirtualRegions(syms: MutableList<ElfSymbol>, isDynamic: Boolean): MutableList<ElfSymbol>? {
        loadableRegions //lazy call
        if (isDynamic)
            symbols //lazy call

        val commonSymbols = filterCommon(syms)
        for ((i, it) in commonSymbols.withIndex()) {
            it.value = commonAddress + comSymbolsLastOffset + i * 4
        }
        comSymbolsLastOffset += commonSymbols.size * 4

        val absSymbols = filterAbs(syms)
        for ((i, it) in absSymbols.withIndex()) {
            it.value = absAddress + absSymbolsLastOffset + i * 4
        }
        absSymbolsLastOffset += absSymbols.size * 4

        val externSymbols = filterUndefined(syms)
        for ((i, it) in externSymbols.withIndex()) {
            it.value = externAddress + undSymbolsLastOffset + i * 4
//            log.info("${it.nameString}: ${it.value.hex8}")
        }
        undSymbolsLastOffset += externSymbols.size * 4

        return syms
    }

    
    val symbols: MutableList<ElfSymbol>? by lazy {
//        val result = originalSymbols?.toMutableList() //Make copy
        val result = originalSymbols
        //val result = symtable?.toMutableList()
        if ((result != null) && (isNeededStaticRebase)) {
            for (it in result) {
                it.value = if (isRelocatableFile) {
                    val shndx = it.shndx.toInt()
                    val specShndx = when (it.shndx) {
                        SHN_COMMON.id,
                        SHN_ABS.id,
                        SHN_UNDEF.id -> true
                        else -> false
                    }
                    if (specShndx)
                        it.value
                    else {

                        val region = regions.find {
                            it.ind == shndx
                        } ?: throw EBadSectionIndex("Symbol is outside of any region: ${it.name} (${it.ind})")

                        it.value + region.vaddr
                    }
                }
                else
                    rebaseForced(it.value)
            }
        }
        when {
            result == null -> result
//            generateVirtualRegions -> applyVirtualRegions(result, false)
            else -> result
        }
    }

    
    val dynamicSymbols: MutableList<ElfSymbol>? by lazy {
        val result = originalDynamicSymbols?.toMutableList() //Make copy
        if ((result != null) && (forceUseSpecificBaseAddress)) {
            for (it in result)
                it.value = rebaseForced(it.value)
        }
        when {
            result == null -> result
//            generateVirtualRegions -> applyVirtualRegions(result, true)
            else -> result
        }
    }

    // TODO: REWRITE
    
    val dynsegSymbols: MutableList<ElfSymbol>? by lazy {
        val result = originalDynamicSegmentSymbols?.toMutableList() //Make copy
        if ((result != null) && (forceUseSpecificBaseAddress)) {
            for (it in result)
                it.value = rebaseForced(it.value)
        }
        when {
            result == null -> result
            generateVirtualRegions -> applyVirtualRegions(result, true)
            else -> result
        }
    }

    val undefinedSymbols: MutableList<ElfSymbol>?
        get() {
            val syms: MutableList<ElfSymbol> = dynamicSymbols ?: return null
            return filterUndefined(syms).toMutableList()
        }

    private fun calculateNextAddress(start: Long, size: Int, align: Int): Long {
        var offset = start
        if (align > 1)
            if (offset % align != 0L)
                offset = (offset / align + 1) * align
        return offset + size
    }

    private fun getData(input: ByteBuffer, offset: Int, memSize: Int, fileSize: Int): ByteArray {
        val data = ByteArray(memSize)
        input.position(offset)
        input.get(data, 0, fileSize)
        return data
    }

    
    private val regions: MutableList<ElfRegion> by lazy {
        val result = //mutableListOf<ElfRegion>()
        if (sectionLoading)
            elfFile.sectionHeaderTable.map {
                var vAddr = elfFile.decoder.fixPaddr(it.addr)
                val data = if ((it.type != SHT_NOBITS.id) && (it.flags and SHF_ALLOC.id != 0)) getData(input, it.offset, it.size, it.size)
                else ByteArray(it.size)

                val access = ElfAccess.fromSectionHeaderFlags(it.flags)
                val lastAddr = vAddr + if (it.size > 0) it.size - 1 else 0
                log.config { "Allocate memory region ${vAddr.hex8}..${lastAddr.hex8} -> ${it.name}" }
                ElfRegion(it.name, it.ind, it.type, vAddr, it.offset, it.size, data, access, it.addralign)
            }.toMutableList()
        else
            elfFile.programHeaderTable.map {
                val nametype = ElfProgramHeaderType.getNameById(it.type)
                        ?: elfFile.decoder.getProgramHeaderTypeNameById(it.type)
                val name = "SEGMENT_${nametype}_${it.offset.hex8}"
                var vAddr = elfFile.decoder.fixPaddr(it.vaddr)

                val data = if ((it.type != PT_NULL.id) && (it.memsz != 0))
                    getData(input, it.offset, it.memsz, it.filesz)
                else ByteArray(it.memsz)

                val access = ElfAccess.fromProgramHeaderFlags(it.flags)
                val lastAddr = vAddr + if (it.memsz > 0) it.memsz - 1 else 0
                log.config { "Allocate memory region ${vAddr.hex8}..${lastAddr.hex8} -> $name" }
                ElfRegion(name, it.ind, it.type, vAddr, it.offset, it.memsz, data, access, it.align)
            }.toMutableList()

            // REFACTORING !!! Add virtualRegion

        if (generateVirtualRegions) {
            val lastRegion = filterLoadableRegions(result).maxByOrNull { it.vaddr }!!
            var lastAddress = lastRegion.vaddr + lastRegion.size

            val comSymbols = mutableListOf<ElfSymbol>()
            val absSymbols = mutableListOf<ElfSymbol>()
            val undSymbols = mutableListOf<ElfSymbol>()

            virtualRegionSize = 0

//            if (originalSymbols != null) {
//                virtualRegionSize += originalSymbols.size * 4
//                comSymbols.addAll(filterCommon(originalSymbols))
//                absSymbols.addAll(filterAbs(originalSymbols))
//                undSymbols.addAll(filterUndefined(originalSymbols))
//            }
            if (originalDynamicSymbols != null) {
                virtualRegionSize += originalDynamicSymbols.size * 4
                comSymbols.addAll(filterCommon(originalDynamicSymbols))
                absSymbols.addAll(filterAbs(originalDynamicSymbols))
                undSymbols.addAll(filterUndefined(originalDynamicSymbols))
            }
//            if (originalDynamicSegmentSymbols != null) {
//                virtualRegionSize += originalDynamicSegmentSymbols.size * 4
//                comSymbols.addAll(filterCommon(originalDynamicSegmentSymbols))
//                absSymbols.addAll(filterAbs(originalDynamicSegmentSymbols))
//                undSymbols.addAll(filterUndefined(originalDynamicSegmentSymbols))
//            }

            if (elfFile.machine == ElfMachine.EM_ARM.id) {
                // TODO: determine, WTF is it
                result.add(ElfRegion(".prgend", -1, PT_LOAD.id, lastAddress, 0, 4, ByteArray(4), ElfAccess.virtual(), 1))
                lastAddress += 4
            }

            // TODO: move it outside ElfLoader
            if (isSharedObject) {
                originalSymbols?.let {
                    val absSize = filterAbs(it).size * 4  // TODO: NPE here for mips-vanilla lighttpd!!
                    result.add(ElfRegion("abs", -1, PT_LOAD.id, lastAddress, 0, absSize, ByteArray(absSize), ElfAccess.virtual(), 1))
                    lastAddress += absSize
                }
                createExternRegion(lastAddress, result, undSymbols)
            } else if (isRelocatableFile) {
                lastAddress = createCommonRegion(lastAddress, result, comSymbols)
                lastAddress = createAbsRegion(lastAddress, result, absSymbols)
                createExternRegion(lastAddress, result, undSymbols)
            } else {
                if (elfFile.machine == ElfMachine.EM_MIPS.id) {
                    // TODO: determine, WTF is it
                    result.add(ElfRegion(".prgend", -1, PT_LOAD.id, lastAddress, 0, 4, ByteArray(4), ElfAccess.virtual(), 1))
                    lastAddress += 4
                }

                lastAddress = createExternRegion(lastAddress, result, undSymbols)
                createAbsRegion(lastAddress, result, absSymbols)
            }
        }
        result
    }


    private fun createCommonRegion(lastAddress: Long, regs: MutableList<ElfRegion>, syms: MutableList<ElfSymbol>): Long {
        if (syms.isEmpty())
            return lastAddress

        commonAddress = lastAddress

        val comSize = syms.size * 4
        val comData = ByteArray(comSize)
        val comBuffer = ByteBuffer.wrap(comData)
        comBuffer.order(input.order())
        for (it in syms)
            comBuffer.putInt(it.size)
        regs.add(ElfRegion("common", -1, PT_LOAD.id, lastAddress, 0, comSize, comData, ElfAccess.virtual(), 1))
        log.config { "Allocate memory region ${lastAddress.hex8}..${(lastAddress + comSize).hex8} -> common" }
        return lastAddress + virtualRegionSize
    }

    private fun createAbsRegion(lastAddress: Long, regs: MutableList<ElfRegion>, syms: MutableList<ElfSymbol>): Long {
        if (syms.isEmpty())
            return lastAddress

        absAddress = lastAddress

        val absSize = syms.size * 4
        val absData = ByteArray(absSize)
        val absBuffer = ByteBuffer.wrap(absData)
        absBuffer.order(input.order())
        for (it in syms)
            absBuffer.putInt(it.value.toInt())
        regs.add(ElfRegion("abs", -1, PT_LOAD.id, lastAddress, 0, absSize, absData, ElfAccess.virtual(), 1))
        log.config { "Allocate memory region ${lastAddress.hex8}..${(lastAddress + absSize).hex8} -> abs" }
        return lastAddress + virtualRegionSize
    }

    private fun createExternRegion(lastAddress: Long, regs: MutableList<ElfRegion>, syms: MutableList<ElfSymbol>): Long {
        if (syms.isEmpty())
            return lastAddress

        externAddress = lastAddress

        val undSize = syms.size * 4
        regs.add(ElfRegion("extern", -1, PT_LOAD.id, lastAddress, 0, undSize, ByteArray(undSize), ElfAccess.virtual(), 1))
        log.config { "Allocate memory region ${lastAddress.hex8}..${(lastAddress + undSize).hex8} -> extern" }
        return lastAddress + virtualRegionSize
    }

    private fun filterLoadableRegions(regs: MutableList<ElfRegion>): MutableList<ElfRegion> =
        if (sectionLoading)
            regs.filter {
                if (it.type in SHT_LOPROC.id..SHT_HIPROC.id)
                    elfFile.decoder.isLoadableSection(it.type, it.access)
                else
                    ((it.type == SHT_PROGBITS.id)
                            || (it.type == SHT_NOBITS.id)
                            || (it.type == SHT_INIT_ARRAY.id)
                            || (it.type == SHT_FINI_ARRAY.id)
                            ) && (it.access.isLoad)
            }.toMutableList()
        else
            regs.filter {
                if (it.type in PT_LOPROC.id..PT_HIPROC.id)
                    elfFile.decoder.isLoadableSegment(it.type)
                else
                    (it.type == PT_LOAD.id)
            }.toMutableList()

    
    val loadableRegions: MutableList<ElfRegion> by lazy {
//        val rebased = filterLoadableRegions(regions).toMutableList() // Make a copy
//        log.warning { ">> before filterLoadableRegions <<" }
        val rebased = filterLoadableRegions(regions) // Do not make a copy

//        log.warning { ">> before isNeededStaticRebase <<" }

        if (isNeededStaticRebase) {
            if (elfFile.type == ElfType.ET_REL.id) {
                var rebaseAddr = specificBaseAddress
                rebased.forEachIndexed { i, it ->
                    val size = when (it.name) {
                        "common", "abs", "extern" -> virtualRegionSize
                        else -> it.size
                    }

                    rebaseAddr = calculateNextAddress(rebaseAddr, size, it.align)
                    val vaddr = rebaseAddr - size
                    rebased[i].vaddr = vaddr

                    when (it.name) {
                        "common" -> commonAddress = vaddr
                        "abs" -> absAddress = vaddr
                        "extern" -> externAddress = vaddr
                    }
                }
            } else {
                rebased.forEachIndexed { i, it ->
                    val vaddr = rebaseForced(it.vaddr)
                    rebased[i].vaddr = vaddr

                    when (it.name) {
                        "common" -> commonAddress = vaddr
                        "abs" -> absAddress = vaddr
                        "extern" -> externAddress = vaddr
                    }
                }
            }
        }

//            log.warning { ">> before `area in iterable` <<" }

        rebased.forEach { area ->
            val first = area.addressRange
            val intersect = rebased.find {
                if (area != it) {
                    val second = it.addressRange
                    if (first.first in second || first.last in second) {
                        if (first.first !in second && second.last !in first || second.first !in first && first.last !in second)
                            throw EBadIntersect("Area ${area.name} [${first.hex8}] has bad intersection with ${it.name} [${second.hex8}]")
                        else
                            first.first in second && first.last in second
                    } else false
                } else false
            }

            if (intersect != null) {
                val (first, second) = intersect.divide(area)
                log.warning { "Area ${area.name} [${area.addressRange.hex8}] has been divided by ${intersect.name} [${intersect.addressRange.hex8}]" }

                rebased.remove(intersect)

                if (first != null) {
                    log.warning { "First is ${first.name} [${first.addressRange.hex8}]" }
                    rebased.add(first)
                }

                if (second != null) {
                    log.warning { "Second is ${second.name} [${second.addressRange.hex8}]" }
                    rebased.add(second)
                }
            }
        }

//        log.warning { ">> before `rebased.sortBy { it.vaddr }` <<" }
        rebased.apply { sortBy { it.vaddr } }
    }


    private fun applyStaticRelocation(syms: MutableList<ElfSymbol>, rel: ElfRel, vaddr: Long, offset: Int?): Long {
        val data: Long = if (offset != null) {
            input.position(offset)
            input.int.toULong()
        }
        else
            0L

        val symbol = syms[rel.sym].value
        val got = globalOffsetTable

        //if (rel.type == ArmRelocationType.R_ARM_GLOB_DAT.id)
        //    log.info("BP")

        val relData =  elfFile.decoder.applyStaticRelocation(rel, vaddr, symbol, got, data) mask 32 //TODO: for Elf64 should remake
//        log.warning{"Relocation data of type ${elfFile.decoder!!.getRelocationNameById(rel.type)} for symbol \"${syms[rel.sym].nameString}\": ${relData.hex8}"}
        return relData
    }

    
    val staticRelocations: MutableList<ElfRelocation> by lazy {
        //TODO: To map?
        val result = mutableListOf<ElfRelocation>()

        if (isRelocatableFile) {
//            log.warning { "==== before rel in elfFile.staticRelocations!! ====" }

            for (rel in elfFile.staticRelocations!!) {
                val region = loadableRegions.find {
                    it.ind == rel.sectionIndex //It seems to be never wrong if file is relocatable
                }

                if (region == null) {
                    val unloadedRegion = regions.find {
                        it.ind == rel.sectionIndex //It seems to be never wrong if file is relocatable
                    } ?: throw EBadSectionIndex("Reloc for offset ${rel.vaddr.hex8} isn't found (section index ${rel.sectionIndex})")

                    log.info {"Reloc for offset ${rel.vaddr.hex8} is in unloaded region \"${unloadedRegion.name}\"(section index ${rel.sectionIndex})" }
                    continue
                }

                val vaddr = region.vaddr + rel.vaddr
                val fileOffset = (rel.vaddr + region.offset).toInt()

                val symtable = when (regions[rel.symtabIndex].type) {
                    SHT_SYMTAB.id -> symbols
                    SHT_DYNSYM.id -> dynamicSymbols
                    else -> throw EBadSymbolTable("Not valid symbol table for reloc entry ${rel.vaddr.hex8}")
                } ?: throw EBadSymbolTable("Can't relocate without symbol table")

                val value = applyStaticRelocation(symtable, rel, vaddr, fileOffset)
                result.add(ElfRelocation(
                        rel.sectionIndex,
                        rel.symtabIndex,
                        vaddr,
                        rel.vaddr.toInt(),
                        value,
                        rel.type,
                        rel.sym,
                        rel.addend,
                        rel.withAddend))
            }

        }
        else {
            for (rel in elfFile.staticRelocations!!) {
                val vaddr = if (forceUseSpecificBaseAddress) rebaseForced(rel.vaddr) else rel.vaddr

                val region = loadableRegions.find { it.isAddressIncluded(vaddr) }

                if (region == null) {
                    val unloadedRegion = regions.find {
                        it.isAddressIncluded(vaddr)
                    } ?: throw EBadAddressValue("Reloc for address ${vaddr.hex8} isn't found (section index ${rel.sectionIndex})")

                    log.info { "Reloc for address ${vaddr.hex8} is in unloaded region \"${unloadedRegion.name}\"(section index ${rel.sectionIndex})" }
                    continue
                }

                val offset = (vaddr - region.vaddr).toInt()
                val fileOffset = offset + region.offset

                val symtable = when (regions[rel.symtabIndex].type) {
                    SHT_SYMTAB.id -> symbols
                    SHT_DYNSYM.id -> dynamicSymbols
                    else -> throw EBadSymbolTable("Not valid symbol table for reloc entry ${rel.vaddr.hex8}")
                } ?: throw EBadSymbolTable("Can't relocate without symbol table")

                val value = applyStaticRelocation(symtable, rel, vaddr, fileOffset)
                result.add(ElfRelocation(
                        rel.sectionIndex,
                        rel.symtabIndex,
                        vaddr,
                        offset,
                        value,
                        rel.type,
                        rel.sym,
                        rel.addend,
                        rel.withAddend))
            }
        }
        result
    }


    //TODO: are we need it?
    
    val dynamicRelocations: MutableList<ElfRelocation> by lazy {
        val result = mutableListOf<ElfRelocation>()
        for (rel in elfFile.dynamicRelocations) {
            val vaddr = if (forceUseSpecificBaseAddress)
                rebaseForced(rel.vaddr)
            else
                rel.vaddr

            val region = loadableRegions.find { it.isAddressIncluded(vaddr) }
            if (region == null) {
                val unloadedRegion = regions.find {
                    it.isAddressIncluded(vaddr)
                } ?: throw EBadAddressValue("Reloc for address ${vaddr.hex8} isn't found (section index ${rel.sectionIndex})")

                log.info { "Reloc for address ${vaddr.hex8} is in unloaded region \"${unloadedRegion.name}\"" }
                continue
            }

            val fileOffset: Int?
            val offset: Int
            if ((sectionLoading) && (region.type == SHT_NOBITS.id)) {
                fileOffset = null
                offset = 0
            }
            else {
                offset = (vaddr - region.vaddr).toInt()
                fileOffset = offset + region.offset
            }

            if (dynamicSymbols == null)
                throw EBadSymbolTable("Can't relocate without dynamic segment symbol table")

            val value = applyStaticRelocation(dynamicSymbols!!, rel, vaddr, fileOffset)
            result.add(ElfRelocation(
                    region.ind,
                    0,
                    vaddr,
                    offset,
                    value,
                    rel.type,
                    rel.sym,
                    rel.addend,
                    rel.withAddend))
        }

        result
    }

    val jumpSlots: List<ElfRelocation>
        get() = staticRelocations.filter { elfFile.decoder.isJumpSlot(it.type) }

    val globalData: List<ElfRelocation>
        get() = staticRelocations.filter { elfFile.decoder.isGlobDat(it.type) }
    /*
    TODO PLAN:
        + Make rebase of all relocations
        + Make vitrual sections
        + Replace symbols ABS, UND, COM with virtual offsets
        + Make it elf work
        ~ Refactoring
        * ?????
        * PROFFIT!!!
    */

    //TODO: lazy?
    
    val relocatedRegions: MutableList<ElfRegion> by lazy {
//        val result = loadableRegions.toMutableList() // Make a copy
        val result = loadableRegions // Make a copy

//        log.warning { "==== before staticRelocations ====" }

        for (rel in staticRelocations) {
//            log.warning { "==== before `result.first ` ====" }
            val region = result.first { it.isAddressIncluded(rel.vaddr) }
//            log.warning { "==== before `ByteBuffer.wrap` ====" }
//           ByteBuffer.wrap(region.data)
//                   .order(input.order())
//                   .putInt(rel.offset, rel.value.toInt())
            region.data.putInt32(rel.offset, rel.value.toInt())
        }

//        log.warning { "==== before dynamicRelocations ====" }

        if (elfFile.dynamicSymbolTable == null)
            for (rel in dynamicRelocations) {
                val region = result.first { it.isAddressIncluded(rel.vaddr) }
                ByteBuffer.wrap(region.data)
                        .order(input.order())
                        .putInt(rel.offset, rel.value.toInt())
            }
        result
    }


}