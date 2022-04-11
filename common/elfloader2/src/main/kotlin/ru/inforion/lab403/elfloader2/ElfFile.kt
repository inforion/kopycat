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
@file:Suppress("MemberVisibilityCanBePrivate", "unused", "MemberVisibilityCanBePrivate")

package ru.inforion.lab403.elfloader2

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.elfloader2.ElfRel.Companion.elfRel
import ru.inforion.lab403.elfloader2.ElfStringTable.Companion.elfStringTable
import ru.inforion.lab403.elfloader2.ElfSymbol.Companion.elfSymbol
import ru.inforion.lab403.elfloader2.headers.ElfSectionHeader.Companion.elfSectionHeader
import ru.inforion.lab403.elfloader2.enums.ElfDynamicSectionTag.*
import ru.inforion.lab403.elfloader2.enums.ElfProgramHeaderType.*
import ru.inforion.lab403.elfloader2.enums.ElfMachine.*
import ru.inforion.lab403.elfloader2.enums.ElfProgramHeaderFlag.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderFlag.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderType
import ru.inforion.lab403.elfloader2.enums.SectionHeaderNumber.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderType.*
import ru.inforion.lab403.elfloader2.exceptions.*
import ru.inforion.lab403.elfloader2.headers.ElfProgramHeader.Companion.elfProgramHeader
import ru.inforion.lab403.elfloader2.headers.ElfSectionHeader
import ru.inforion.lab403.elfloader2.processors.AElfDecoder
import ru.inforion.lab403.elfloader2.processors.aarch64.ElfDecoderAARCH64
import ru.inforion.lab403.elfloader2.processors.aarch64.ElfDecoderPPC64
import ru.inforion.lab403.elfloader2.processors.arm.ElfDecoderArm
import ru.inforion.lab403.elfloader2.processors.mips.ElfDecoderMips
import ru.inforion.lab403.elfloader2.processors.ppc.ElfDecoderPPC
import ru.inforion.lab403.elfloader2.processors.x86.ElfDecoderX86
import ru.inforion.lab403.elfloader2.processors.x86_64.ElfDecoderX86_64
import ru.inforion.lab403.elfloader2.tables.ElfGnuHashTable.Companion.elfGnuHashTable
import ru.inforion.lab403.elfloader2.tables.ElfHashTable.Companion.elfHashTable
import ru.inforion.lab403.elfloader2.tables.IHashTable
import java.nio.ByteBuffer
import kotlin.IllegalStateException


class ElfFile(val inputBuffer: ByteBuffer, val throwOnInvalidSection: Boolean = false) {

    companion object {
        private val log = logger(INFO)
    }

    val elfHeader = ElfHeader(inputBuffer)

    val input = elfHeader.input

    //Processor-specific decoder
    val decoder: AElfDecoder by lazy {
        when (elfHeader.e_machine) {
            EM_NONE -> throw EBadElfHeader("No machine identifier")
            EM_386 -> ElfDecoderX86(this)
            EM_ARM -> ElfDecoderArm(this)
            EM_MIPS -> ElfDecoderMips(this)
            EM_PPC -> ElfDecoderPPC(this)
            EM_PPC64 -> ElfDecoderPPC64(this)
            EM_X86_64 -> ElfDecoderX86_64(this)
            EM_AARCH64 -> ElfDecoderAARCH64(this)
            else -> throw EDecodeFault("Machine ${elfHeader.e_machine} isn't implemented")
        }
    }

    /** ==================== Elf routines ==================== **/

    val ULong.elfOffset: ULong get() {
        val segment = segments.find {
            this in it.fileRange // TODO: was maxRange, but it makes no sense
        } ?: throw EBadAddressValue("Virtual address in unallocated zone")
        return this - segment.p_vaddr + segment.p_offset
    }

    fun offsetToSection(offset: ULong): ElfSectionHeader? = sections.find {
        (offset in it.sh_offset..it.sh_offset + it.sh_size) && (it.sh_type != SHT_GROUP)
    }

    //    fun sectionHeaderAddr(name: String) = sections.find { it.name == name }?.sh_addr


    /** ==================== Sections and Program headers ==================== **/

    //We should check type
    val sections by lazy {
        Array(elfHeader.e_shnum.int_z) {
            input.elfSectionHeader(this, it, elfHeader.e_shoff, elfHeader.e_shentsize)
        }.toList()
    }

    val segments by lazy {
        Array(elfHeader.e_phnum.int_z) {
            input.elfProgramHeader(it, elfHeader.e_phoff, elfHeader.e_phentsize)
        }.toList()
    }

    /** ==================== String tables ==================== **/

    // String table link
    val stringTableIndex: Int? by lazy {
        when (elfHeader.e_shstrndx) {
            SHN_ABS.low, SHN_COMMON.low, SHN_UNDEF.low -> null
            in 0u.ushort until elfHeader.e_shnum -> elfHeader.e_shstrndx.int_z
            else -> null
        }
    }

    //TODO: Nullable?
    val sectionStringTable: ElfStringTable by lazy {
        val ind = stringTableIndex ?: return@lazy mutableMapOf<UInt, String>().elfStringTable
        val strTab = sections[ind]
        input.elfStringTable(strTab.sh_offset, strTab.sh_size)
    }

    val symStringTable: ElfStringTable? by lazy {
        val symtab = sections.find { it.sh_type == SHT_SYMTAB } ?: return@lazy null

        if (symtab.sh_link == elfHeader.e_shstrndx.uint_z)
            sectionStringTable
        else {
            val sect = sections[symtab.sh_link]
            input.elfStringTable(sect.sh_offset, sect.sh_size)
        }
    }

    val dynsymStringTable: ElfStringTable? by lazy {
        dynamicSegment ?: return@lazy null
        val address = dynSegStringTablePtr ?: return@lazy null
        val size = dynSegStringTableSize ?: return@lazy null
        input.elfStringTable(address.elfOffset, size)
    }

    /** ==================== Dynamic segment ==================== **/

    val dynamicSegment: Map<ULong, ULong>? by lazy {
        val dynamicPH = segments.find { it.p_type == PT_DYNAMIC } ?: return@lazy null

        val result = dictionary<ULong, ULong>()

        input.position = dynamicPH.p_offset.requireInt

        read@ while (input.position.ulong_z - dynamicPH.p_offset < dynamicPH.p_filesz) {
            val d_tag = input.wordpref
            val d_val = input.wordpref
            when (d_tag) {
                DT_NULL.id -> break@read
                DT_NEEDED.id -> dynSegNeeded.add(d_val)
                in DT_LOPROC.id..DT_HIPROC.id -> decoder.parseDynamic(result, d_tag, d_val)
                else -> result[d_tag] = d_val
            }
        }
        result
    }

    val dynamicSegmentOrThrow get() = dynamicSegment ?: throw IllegalStateException("Dynamic segment isn't present")

    val dynSegNeeded = mutableListOf<ULong>()
    val dynSegPltGotPtr get() = dynamicSegmentOrThrow[DT_PLTGOT.id]
    val dynSegPltRelTablePtr get() = dynamicSegmentOrThrow[DT_JMPREL.id]
    val dynSegPltRelSize get() = dynamicSegmentOrThrow[DT_PLTRELSZ.id]
    val dynSegPltRelType get() = dynamicSegmentOrThrow[DT_PLTREL.id]
    val dynSegHashTablePtr get() = dynamicSegmentOrThrow[DT_HASH.id]
    val dynSegGnuHashTablePtr get() = dynamicSegmentOrThrow[DT_GNU_HASH.id]
    val dynSegStringTablePtr get() = dynamicSegmentOrThrow[DT_STRTAB.id]
    val dynSegStringTableSize get() = dynamicSegmentOrThrow[DT_STRSZ.id]
    val dynSegSymbolTablePtr get() = dynamicSegmentOrThrow[DT_SYMTAB.id]
    val dynSegSymbolTableEntry get() = dynamicSegmentOrThrow[DT_SYMENT.id]
    val dynSegRelaTablePtr get() = dynamicSegmentOrThrow[DT_RELA.id]
    val dynSegRelaTableSize get() = dynamicSegmentOrThrow[DT_RELASZ.id]
    val dynSegRelaTableEntry get() = dynamicSegmentOrThrow[DT_RELAENT.id]
    val dynSegRelTablePtr get() = dynamicSegmentOrThrow[DT_REL.id]
    val dynSegRelTableSize get() = dynamicSegmentOrThrow[DT_RELSZ.id]
    val dynSegRelTableEntry get() = dynamicSegmentOrThrow[DT_RELENT.id]
    val dynSegInitArrayPtr get() = dynamicSegmentOrThrow[DT_INIT_ARRAY.id]
    val dynSegInitArraySize get() = dynamicSegmentOrThrow[DT_INIT_ARRAYSZ.id]
    val dynSegFiniArrayPtr get() = dynamicSegmentOrThrow[DT_FINI_ARRAY.id]
    val dynSegFiniArraySize get() = dynamicSegmentOrThrow[DT_FINI_ARRAYSZ.id]
    val dynSegInitPtr get() = dynamicSegmentOrThrow[DT_INIT.id] //TODO: Use it
    val dynSegFiniPtr get() = dynamicSegmentOrThrow[DT_FINI.id]

    /** ==================== Symbol tables ==================== **/

    private fun symbolTableBySectionType(type: ElfSectionHeaderType): List<ElfSymbol>? {
        val section = sections.firstOrNull { it.sh_type == type } ?: return null
        val count = section.sh_size / section.sh_entsize
        val stringTable = if (type == SHT_DYNSYM) dynsymStringTable else symStringTable

        return Array(count.requireInt) {
            input.elfSymbol(this, stringTable!!, it, section.sh_offset, section.sh_entsize)
        }.toList()
    }

    val symbolTable: List<ElfSymbol>? by lazy { symbolTableBySectionType(SHT_SYMTAB) }
    val dynamicSymbolTable: List<ElfSymbol>? by lazy { symbolTableBySectionType(SHT_DYNSYM) }
    val dynSegSymbolTable: List<ElfSymbol>? by lazy {
        dynamicSegment ?: return@lazy null
        val address = dynSegSymbolTablePtr ?: return@lazy null
        val entrySize = dynSegSymbolTableEntry!! // Must be present?
        Array(dynSegSymbolCount.requireInt) {
            input.elfSymbol(this, dynsymStringTable!!, it, address.elfOffset, entrySize)
        }.toList()
    }

    /** ==================== Hash table ==================== **/

    val hashTable: IHashTable? by lazy {
        val hash = sections.find { it.sh_type == SHT_HASH }
        val gnuHash = sections.find { it.sh_type == SHT_GNU_HASH && it.name == ".gnu.hash "}
        when {
            dynamicSegment != null -> {
                dynSegHashTablePtr?.let {
                    input.elfHashTable(it.elfOffset)
                } ?: dynSegGnuHashTablePtr?.let {
                    input.elfGnuHashTable(it.elfOffset)
                }
            }
            hash != null -> input.elfHashTable(hash.sh_offset)
            gnuHash != null -> input.elfGnuHashTable(gnuHash.sh_offset).also {
                require(elfHeader.e_machine != EM_MIPS) { "Not implemented, see readelf.c" }
            }
            else -> null
        }
    }

    private val dynSegSymbolCount get() = when (val dst = dynamicSymbolTable) {
        null -> when (val ht = hashTable) {
            null -> throw IllegalStateException("Hash table isn't found")
            else -> ht.symbolCount
        }
        else -> dst.size.uint
    }

    /** ==================== Relocations ==================== **/

    val relocations: List<ElfRel>? by lazy {
        sections.filter {
            // I removed !it.name.startsWith(".debug")
            // We have to trace it somewhere else
            it.sh_type == SHT_RELA || it.sh_type == SHT_REL
        }.map { rel ->
            val table = dynamicSymbolTable ?: symbolTable!!.also { check(elfHeader.isRel) { "Static-dynamic relocs in non-relocatable file" } }
            val count = rel.sh_size / rel.sh_entsize
            List(count.requireInt) {
                input.elfRel(this, table, it, rel.sh_offset, rel.sh_entsize, rel.sh_type == SHT_RELA, rel.sh_link, rel.sh_info)
            }
        }.flatten() // TODO: remove flatten, so we can differ sections?
    }

    val dynamicRelocations by lazy {
        val dynRelocs = mutableListOf<ElfRel>()
        if (dynamicSegment != null) {
            // PLT relocations
            dynSegPltRelTablePtr?.let { ptr ->
                val isRela = dynSegPltRelType!! == DT_RELA.id
                val entrySize = (if (isRela) dynSegRelaTableEntry else dynSegRelTableEntry)
                    ?: throw EDynamicTagUndefined("Plt relocation entry size is undefined")
                val size = dynSegPltRelSize ?: throw EDynamicTagUndefined("Plt relocation table size is undefined")
                val n = (size / entrySize).int
                dynRelocs += List(n) { input.elfRel(this, dynSegSymbolTable!!, it, ptr.elfOffset, entrySize, isRela) }
            }

            // RELA relocations
            dynSegRelaTablePtr?.let { ptr ->
                val entsize = dynSegRelaTableEntry ?: throw EDynamicTagUndefined("Relocation entry size is undefined")
                val size = dynSegRelaTableSize ?: throw EDynamicTagUndefined("Relocation table size is undefined")
                val n = (size / entsize).int
                dynRelocs += List(n) { input.elfRel(this, dynSegSymbolTable!!, it, ptr.elfOffset, entsize, true) }
            }

            // REL relocations
            dynSegRelTablePtr?.let { ptr ->
                val entsize = dynSegRelTableEntry ?: throw EDynamicTagUndefined("Relocation entry size is undefined")
                val size = dynSegRelTableSize ?: throw EDynamicTagUndefined("Relocation table size is undefined")
                val n = (size / entsize).int
                dynRelocs += List(n) { input.elfRel(this, dynSegSymbolTable!!, it, ptr.elfOffset, entsize, false) }
            }
        }
        dynRelocs
    }

    /** ==================== Init and Fini ==================== **/

    val initArray: Array<ULong>? by lazy {
        val initArraySection = sections.find { it.name == ".init_array" }
        when {
            initArraySection != null -> {
                input.position = initArraySection.sh_offset.requireInt
                val count = initArraySection.sh_size / input.ptrSize
                Array(count.requireInt) { input.addr }
            }
//            dynamicSegment != null -> {
//                val ptr = dynSegInitArrayPtr
//                if (ptr != null) {
//                    val size = dynSegInitArraySize ?: throw EDynamicTagUndefined("Init array size is undefined")
//                    input.position(ptr.elfOffset)
//                    Array(size.int / 4) { input.int.ulong_z }
//                } else null
//            }
            else -> null
        }
    }

    val finiArray: Array<ULong>? by lazy {
        val finiArraySection = sections.find { it.name == ".fini_array" }
        when {
            finiArraySection != null -> {
                input.position = finiArraySection.sh_offset.requireInt
                val count = finiArraySection.sh_size / input.ptrSize
                Array(count.requireInt) { input.addr }
            }
//            dynamicSegment != null -> {
//                val ptr = dynSegFiniArrayPtr
//                if (ptr != null) {
//                    val size = dynSegFiniArraySize ?: throw EDynamicTagUndefined("Fini array size is undefined")
//                    input.position(ptr.elfOffset)
//                    Array(size.int / 4) { input.int.ulong_z }
//                } else null
//            }
            else -> null
        }
    }

    val hasAllocSections by lazy { sections.any { it.isAlloc } }

    init {
        decoder.checkFlags()

        sections.forEach { sh ->
            with(sh) {
                val attrs = sh_flags and 0b111u
                val invalid: Boolean = when (name) {
                    ".bss" -> sh_type != SHT_NOBITS || attrs != SHF_ALLOC.mask or SHF_WRITE.mask
                    ".comment" -> sh_type != SHT_PROGBITS || attrs != 0uL
                    ".data" -> sh_type != SHT_PROGBITS || attrs != SHF_ALLOC.mask or SHF_WRITE.mask
                    ".data1" -> sh_type != SHT_PROGBITS || attrs != SHF_ALLOC.mask or SHF_WRITE.mask
                    ".debug" -> sh_type != SHT_PROGBITS || attrs != 0uL
                    ".dynamic" -> sh_type != SHT_DYNAMIC || attrs and SHF_ALLOC.mask == 0uL || attrs and SHF_EXECINSTR.mask != 0uL //SHF_WRITE is proc-spec
                    ".dynstr" -> sh_type != SHT_STRTAB || sh_flags != SHF_ALLOC.mask
                    ".dynsym" -> sh_type != SHT_DYNSYM || sh_flags != SHF_ALLOC.mask
                    ".fini" -> sh_type != SHT_PROGBITS || sh_flags != SHF_ALLOC.mask or SHF_EXECINSTR.mask
                    ".hash" -> sh_type != SHT_HASH || sh_flags != SHF_ALLOC.mask
                    ".init" -> sh_type != SHT_PROGBITS || sh_flags != SHF_ALLOC.mask or SHF_EXECINSTR.mask
                    ".interp" -> sh_type != SHT_PROGBITS || sh_flags != SHF_ALLOC.mask && sh_flags != 0uL
                    ".line" -> sh_type != SHT_PROGBITS || sh_flags != 0uL
                    ".note" -> sh_type != SHT_NOTE || sh_flags != 0uL
                    ".rodata" -> sh_type != SHT_PROGBITS || sh_flags and SHF_ALLOC.mask == 0uL
                    ".rodata1" -> sh_type != SHT_PROGBITS || sh_flags != SHF_ALLOC.mask
                    ".shstrtab" -> sh_type != SHT_STRTAB || sh_flags != 0uL
                    ".strtab" -> sh_type != SHT_STRTAB || sh_flags != SHF_ALLOC.mask && sh_flags != 0uL
                    ".symtab" -> sh_type != SHT_SYMTAB || sh_flags != SHF_ALLOC.mask && sh_flags != 0uL
                    ".text" -> sh_type != SHT_PROGBITS || sh_flags != SHF_ALLOC.mask or SHF_EXECINSTR.mask
                    ".gnu.hash " -> sh_type != SHT_GNU_HASH || sh_flags != SHF_ALLOC.mask
                    ".gnu.version" -> sh_type != SHT_GNU_VERSYM || sh_flags != SHF_ALLOC.mask
                    ".gnu.version_d" -> sh_type != SHT_GNU_VERDEF || sh_flags != SHF_ALLOC.mask
                    ".gnu.version_r" -> sh_type != SHT_GNU_VERNEED || sh_flags != SHF_ALLOC.mask
                    ".got" -> false
                    ".plt" -> false
                    ".got.plt" -> false
                    else -> when {
                        name.startsWith(".rela") -> (sh_type != SHT_RELA)
                        name.startsWith(".rel") -> (sh_type != SHT_REL)
                        name.startsWith(".${decoder.prefix}.prefix") -> decoder.checkSectionName(name, sh_type_value, sh_flags)
                        name.isEmpty() -> false
                        else -> {
                            log.config { "Non standard section name: $name" }
                            false
                        }
                    }
                }
                if (invalid) {
                    val msg = "Not valid $name section (${sh_type_value.hex8}, ${sh_flags.hex8})"
                    if (throwOnInvalidSection)
                        throw EBadSection(msg)
                    log.severe { msg }
                }
            }
            if (sh.sh_flags and SHF_MASKPROC.mask != 0uL) {
                log.warning { "Section \"${sh.name}\"(${sh.index}) has processor-specific flags" }
                decoder.checkSectionFlags(sh.sh_flags)
            }
        }

        if (!hasAllocSections) {
            if (elfHeader.e_flags and PF_MASKOS.mask != 0u)
                log.severe { "There are OS-specific flags" }

            if (elfHeader.e_flags and PF_MASKPROC.mask != 0u)
                decoder.checkSegmentFlags(elfHeader.e_flags)
        }
    }

}