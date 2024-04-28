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
@file:Suppress("MemberVisibilityCanBePrivate", "unused", "MemberVisibilityCanBePrivate")

package ru.inforion.lab403.elfloader

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.elfloader.ElfRel.Companion.elfRel
import ru.inforion.lab403.elfloader.headers.ElfSectionHeader.Companion.elfSectionHeader
import ru.inforion.lab403.elfloader.enums.ElfDynamicSectionTag.*
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderType.*
import ru.inforion.lab403.elfloader.enums.ElfMachine.*
import ru.inforion.lab403.elfloader.enums.ElfObjectSize.*
import ru.inforion.lab403.elfloader.enums.ElfEncoding.*
import ru.inforion.lab403.elfloader.enums.ElfOSABI
import ru.inforion.lab403.elfloader.enums.ElfOSABI.*
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderFlag.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderFlag.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderIndex.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType.*
import ru.inforion.lab403.elfloader.enums.ElfType.*
import ru.inforion.lab403.elfloader.enums.ElfVersion.*
import ru.inforion.lab403.elfloader.exceptions.*
import ru.inforion.lab403.elfloader.headers.ElfProgramHeader
import ru.inforion.lab403.elfloader.headers.ElfSectionHeader
import ru.inforion.lab403.elfloader.processors.AElfDecoder
import ru.inforion.lab403.elfloader.processors.arm.ElfDecoderArm
import ru.inforion.lab403.elfloader.processors.mips.ElfDecoderMips
import ru.inforion.lab403.elfloader.processors.ppc.ElfDecoderPPC
import ru.inforion.lab403.elfloader.processors.x86.ElfDecoderX86
import ru.inforion.lab403.elfloader.tables.ElfGnuHashTable
import ru.inforion.lab403.elfloader.tables.ElfHashTable
import ru.inforion.lab403.elfloader.tables.IHashTable
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ElfFile(val input: ByteBuffer) {

    companion object {
        private val log = logger(INFO)
    }

    //32 or 64 bit ELF
    val objectSize: Byte

    //LSB or MSB endian
    val encoding: Byte

    //Only 1 is valid
    val headerVersion: Byte

    //Operating system/ABI identification
    val eh_osabi: Byte

    //ABI version
    val eh_abiversion: Byte

    //Relocatable, executable, shared object, core or processor-specific
    val type: Short

    //Machine identifier
    var machine: Short = ET_NONE.id

    //ELF file version
    val version: Int

    //ELF entry address
    val entry: UInt

    //Program header table's offset
    var phoff: Int = 0

    //Sector header table's offset
    var shoff: Int = 0

    //Processor-specific flags
    val flags: UInt

    //ELF header's size
    val ehsize: Short

    //Size of entry in program header table
    var phentsize: Short = 0

    //Number of entries in program header table
    var phnum: Short = 0

    //Size of entry in section header table
    var shentsize: Short = 0

    //Number of entries in section header table
    var shnum: Short = 0

    //Section header table string index (section name string table)
    var shstrndx: Short = SHN_UNDEF.id

    //TODO: optimize it?
    fun getOffsetByAddress(vAddr: ULong): Int {
//        val section = sectionHeaderTable.filter { it.flags and SHF_ALLOC.id != 0 }.find { vAddr in it.addr until (it.addr + it.size) }
        val segment = programHeaderTable.find { vAddr in it.vaddr until (it.vaddr + maxOf(it.memsz, it.filesz)) }
                ?: throw EBadAddressValue("Virtual address in unallocated zone")
        return (vAddr - segment.vaddr.int + segment.offset).int
//        return (section.offset + (vAddr - section.addr.asInt)).toInt()
    }

    fun sectionHeaderAddr(name: String) = sectionHeaderTable.find { it.name == name }?.addr

    //TODO: optimize it?
    fun getSectionByOffset(offset: Int): ElfSectionHeader? = sectionHeaderTable.find {
        (offset in it.offset..it.offset + it.size) && (it.type != SHT_GROUP.id)
    }


    val dynamicSegment: Map<Int, ULong>? by lazy {
        val ph = programHeaderTable.find { it.type == PT_DYNAMIC.id }
        if (ph != null) {
            val hm = dictionary<Int, ULong>()
            input.position(ph.offset)
            read@ for (i in 0 until ph.filesz) {
                val tag = input.int
                val ptr = input.int.ulong_z
                when (tag) {
                    DT_NULL.id -> break@read
                    DT_NEEDED.id -> dynsegNeeded.add(ptr)
                    in DT_LOPROC.id..DT_HIPROC.id -> decoder.parseDynamic(hm, tag, ptr)
                    else -> hm[tag] = ptr
                }
            }
            hm
        } else null// ElfDynamicSegment(ph.offset, ph.filesz)
    }

    val dynsegNeeded = mutableListOf<ULong>()

    val dynsegPltGotPtr get() = dynamicSegment!![DT_PLTGOT.id]
    val dynsegPltRelTablePtr get() = dynamicSegment!![DT_JMPREL.id]
    val dynsegPltRelSize get() = dynamicSegment!![DT_PLTRELSZ.id]
    val dynsegPltRelType get() = dynamicSegment!![DT_PLTREL.id]
    val dynsegHashTablePtr get() = dynamicSegment!![DT_HASH.id]
    val dynsegGnuHashTablePtr get() = dynamicSegment!![DT_GNU_HASH.id]
    val dynsegStringTablePtr get() = dynamicSegment!![DT_STRTAB.id]
    val dynsegStringTableSize get() = dynamicSegment!![DT_STRSZ.id]
    val dynsegSymbolTablePtr get() = dynamicSegment!![DT_SYMTAB.id]
    val dynsegSymbolTableEntry get() = dynamicSegment!![DT_SYMENT.id]
    val dynsegRelaTablePtr get() = dynamicSegment!![DT_RELA.id]
    val dynsegRelaTableSize get() = dynamicSegment!![DT_RELASZ.id]
    val dynsegRelaTableEntry get() = dynamicSegment!![DT_RELAENT.id]
    val dynsegRelTablePtr get() = dynamicSegment!![DT_REL.id]
    val dynsegRelTableSize get() = dynamicSegment!![DT_RELSZ.id]
    val dynsegRelTableEntry get() = dynamicSegment!![DT_RELENT.id]
    val dynsegInitArrayPtr get() = dynamicSegment!![DT_INIT_ARRAY.id]
    val dynsegInitArraySize get() = dynamicSegment!![DT_INIT_ARRAYSZ.id]
    val dynsegFiniArrayPtr get() = dynamicSegment!![DT_FINI_ARRAY.id]
    val dynsegFiniArraySize get() = dynamicSegment!![DT_FINI_ARRAYSZ.id]
    val dynsegInitPtr get() = dynamicSegment!![DT_INIT.id] //TODO: Use it
    val dynsegFiniPtr get() = dynamicSegment!![DT_FINI.id]


    //If you have a question, why both Array and MutabelList are used here:
    //Array is used when the area representing it meets once in file
    //MutableList is used when an object represents the union of different collections
    //Nullable is needed when the object may not appear in the file


    //Processor-specific decoder
    val decoder: AElfDecoder by lazy {
        when (machine) {
            EM_NONE.id -> throw EBadElfHeader("No machine identifier")
            EM_386.id -> {
                log.fine { "X86 machine" }
                ElfDecoderX86(this)
            }
            EM_ARM.id -> {
                log.fine { "ARM machine" }
                ElfDecoderArm(this)
            }
            EM_MIPS.id -> {
                log.fine { "MIPS machine" }
                ElfDecoderMips(this)
            }
            EM_PPC.id -> {
                log.fine { "PowerPC machine" }
                ElfDecoderPPC(this)
            }

            else -> throw EDecodeFault("Machine $machine isn't implemented")     // TODO("Machine $machine isn't implemented")
        }
    }

    //We should check type
    val sectionHeaderTable by lazy {
        Array(shnum.int_z) { input.elfSectionHeader(this, it, shoff, shentsize) }
    }
//    Sequence<ElfSectionHeader> get() = Sequence {
//    val sectionHeaderTable by lazy {
//        println("**** lazy sectionHeaderTable ****")
//        Sequence { ElfIterator(shnum.asUInt) { ElfSectionHeader(input, shoff, shentsize, it, stringTableOffset)}}
//    }

    val programHeaderTable by lazy { Array(phnum.int_z) { ElfProgramHeader.fromPosition(input, phoff, phentsize, it) } }

    val sectionLoading by lazy {
        sectionHeaderTable.find { it.flags and SHF_ALLOC.id != 0u } != null
    }

    //String table link
    val stringTableIndex: Int? by lazy {
        when (shstrndx) {
            SHN_ABS.id, SHN_COMMON.id, SHN_UNDEF.id -> null
            in 0 until shnum -> shstrndx.int_z
            else -> null
        }
    }

    private fun readStringTable(offset: Int, size: Int): Map<Int, String> {
        val bytes = ByteArray(size)
        input.position(offset)
        input.get(bytes, 0, size)

        var prev = 0
        return bytes.mapIndexed { i, byte ->
            if (byte.int_z == 0) i else null
        }.filterNotNull().map {
            val range = prev until it
            prev = it + 1
            val str = if (range.isEmpty()) String() else String(bytes[range])
            range.first to str
        }.toMap()
    }


    //TODO: Nullable?
    val sectionStringTable: Map<Int, String> by lazy {
        val ind = stringTableIndex
        if (ind != null) {
            val strTab = sectionHeaderTable[ind]
            readStringTable(strTab.offset, strTab.size)
        } else mapOf()
    }

    private fun symbolTableBySectionType(type: Int): Array<ElfSymbol>? {
        val sh = sectionHeaderTable.firstOrNull { it.type == type }
        return if (sh != null) {
//            Array(sh.size / sh.entsize) { ElfSymbol(input, sh.offset + it * sh.entsize, sectionHeaderTable[sh.link].offset, it) }
            //val strTblOffset = sectionHeaderTable[sh.link].offset
            log.severe { "There is >=-: we need to check it out" } // TODO: if (type == SHT_DYNSYM.id) dynsymStringTable!! else symStringTable!!
            Array(sh.size / sh.entsize) {
                ElfSymbol(this, input, sh.offset + it * sh.entsize, if (type == SHT_DYNSYM.id) dynsymStringTable!! else symStringTable!! /*strTblOffset,*/, it)
            }
        } else null
    }

    //TODO: By symtab type?
    fun middleString(strtab: Map<Int, String>, offset: Int): String? {
        val entry = strtab.entries.find {
            offset in it.key until (it.key + it.value.length)
        }
        return entry?.value?.substring(offset - entry.key)
    }

    val symbolTable: Array<ElfSymbol>? by lazy {
//        println("zzz before symbolTableBySectionType(SHT_SYMTAB.id) zzz")
        symbolTableBySectionType(SHT_SYMTAB.id)
    }

    val dynamicSymbolTable: Array<ElfSymbol>? by lazy { symbolTableBySectionType(SHT_DYNSYM.id) }

    val symStringTable: Map<Int, String>? by lazy {
        val symtab = sectionHeaderTable.find { it.type == SHT_SYMTAB.id }
        if (symtab != null) {
            if (symtab.link == shstrndx.int_z)
                sectionStringTable
            else {
                val sect = sectionHeaderTable[symtab.link]
                readStringTable(sect.offset, sect.size)
            }
        } else null
    }

    val dynsymStringTable: Map<Int, String>? by lazy {
        if (dynamicSegment != null) {
            val offset = dynsegStringTablePtr
            val size = dynsegStringTableSize
            if ((offset != null) && (size != null))
                readStringTable(getOffsetByAddress(offset), size.int)
            else null
        } else null
    }

    private val dynsymCount get() = when (val ht = hashTable) {
        null -> throw IllegalStateException("Hash table isn't found")
        is ElfHashTable -> ht.nchain
        is ElfGnuHashTable -> dynamicSymbolTable!!.size - ht.symbias
        else -> throw IllegalStateException("Unknown hash table type")
    }

    val dynamicSegmentSymbolTable: Array<ElfSymbol>? by lazy {
        if (dynamicSegment != null) {
            val ptr = dynsegSymbolTablePtr
            if (ptr != null) {
                val offset = getOffsetByAddress(ptr)
                //val symtabOffset = getOffsetByAddress(dynsegStringTablePtr!!)
                val entsize = dynsegSymbolTableEntry!!.int
                Array(dynsymCount) {
                    ElfSymbol(this, input, offset + it * entsize, dynsymStringTable!!,/*symtabOffset,*/ it)
                }
            } else null
        } else null
    }


    val hashTable: IHashTable? by lazy {
        val hash = sectionHeaderTable.find { it.type == SHT_HASH.id }
        val gnuHash = sectionHeaderTable.find { it.type == SHT_GNU_HASH.id && it.name == ".gnu.hash "}
        when {
            dynamicSegment != null -> {
                dynsegHashTablePtr?.let {
                    ElfHashTable(input, getOffsetByAddress(it))
                } ?: dynsegGnuHashTablePtr?.let {
                    ElfGnuHashTable(input, getOffsetByAddress(it))
                }
            }
            hash != null -> ElfHashTable(input, hash.offset)
            gnuHash != null -> ElfGnuHashTable(input, gnuHash.offset)
            else -> null
        }
    }

    val staticRelocations: List<ElfRel>? by lazy {
        sectionHeaderTable.filter {
            !it.name.startsWith(".debug") && (it.type == SHT_RELA.id || it.type == SHT_REL.id)
        }.map { rel ->
            val n = rel.size / rel.entsize
            List(n) { input.elfRel(it, rel.offset, rel.entsize, rel.type == SHT_RELA.id, rel.link, rel.info) }
        }.flatten()
    }

    val dynamicRelocations by lazy {
        val dynRelocs = mutableListOf<ElfRel>()
        if (dynamicSegment != null) {
            dynsegPltRelTablePtr?.let { ptr ->
                val isRela = dynsegPltRelType!!.int == DT_RELA.id
                val entsize = (if (isRela) dynsegRelaTableEntry else dynsegRelTableEntry)
                        ?: throw EDynamicTagUndefined("Plt relocation entry size is undefined")
                val size = dynsegPltRelSize ?: throw EDynamicTagUndefined("Plt relocation table size is undefined")
                val n = (size / entsize).int
                dynRelocs += List(n) { input.elfRel(it, getOffsetByAddress(ptr), entsize.int, isRela) }
            }

            dynsegRelaTablePtr?.let { ptr ->
                val entsize = dynsegRelaTableEntry ?: throw EDynamicTagUndefined("Relocation entry size is undefined")
                val size = dynsegRelaTableSize ?: throw EDynamicTagUndefined("Relocation table size is undefined")
                val n = (size / entsize).int
                dynRelocs += List(n) { input.elfRel(it, getOffsetByAddress(ptr), entsize.int, true) }
            }

            dynsegRelTablePtr?.let { ptr ->
                val entsize = dynsegRelTableEntry ?: throw EDynamicTagUndefined("Relocation entry size is undefined")
                val size = dynsegRelTableSize ?: throw EDynamicTagUndefined("Relocation table size is undefined")
                val n = (size / entsize).int
                dynRelocs += List(n) { input.elfRel(it, getOffsetByAddress(ptr), entsize.int, false) }
            }
        }
        dynRelocs
    }

    val initArray: Array<ULong>? by lazy {
        val initArraySection = sectionHeaderTable.find { it.name == ".init_array" }
        when {
            initArraySection != null -> {
                val size = initArraySection.size
                input.position(initArraySection.offset)
                Array(size / 4) { input.int.ulong_z }
            }
            dynamicSegment != null -> {
                val ptr = dynsegInitArrayPtr
                if (ptr != null) {
                    val size = dynsegInitArraySize ?: throw EDynamicTagUndefined("Init array size is undefined")
                    input.position(getOffsetByAddress(ptr))
                    Array(size.int / 4) { input.int.ulong_z }
                } else null
            }
            else -> null
        }
    }

    val finiArray: Array<ULong>? by lazy {
        val finiArraySection = sectionHeaderTable.find { it.name == ".fini_array" }
        when {
            finiArraySection != null -> {
                val size = finiArraySection.size
                input.position(finiArraySection.offset)
                Array(size / 4) { input.int.ulong_z }
            }
            dynamicSegment != null -> {
                val ptr = dynsegFiniArrayPtr
                if (ptr != null) {
                    val size = dynsegFiniArraySize ?: throw EDynamicTagUndefined("Fini array size is undefined")
                    input.position(getOffsetByAddress(ptr))
                    Array(size.int / 4) { input.int.ulong_z }
                } else null
            }
            else -> null
        }
    }

    //TODO: remove it
    val globalOffsetTableOffset: Int? by lazy { sectionHeaderTable.find { it.name == ".got" }?.offset }
    var globalOffsetTable: Int? = null

    //TODO: remove it?
    val relIndexes = dictionary<String, IntRange>()

    init {
        val magic = input.int

        if (magic != 0x7F454C46)
            throw EBadMagic("Bad ELF magic number")

        //Skip padding bits
        val padding = ByteArray(7)

        //TODO: change to specification names
        objectSize = input.byte
        encoding = input.byte
        headerVersion = input.byte
        //End of TODO
        eh_osabi = input.byte
        eh_abiversion = input.byte
        input.get(padding)

        //TODO: change to specification names
        when (objectSize) {
            CLASS_NONE.id -> throw EBadElfHeader("Invalid ELF class")
            CLASS_32.id -> log.fine { "Class ELF32" }
            CLASS_64.id -> TODO("ELF64 isn't implemented")
            else -> throw EBadElfHeader("Unknown ELF class: $objectSize")
        }

        //TODO: change to specification names
        when (encoding) {
            DATA_NONE.id -> throw EBadElfHeader("Invalid ELF data encoding")
            DATA_LSB.id -> {
                input.order(ByteOrder.LITTLE_ENDIAN)
                log.fine { "Encoding LSB" }
            }
            DATA_MSB.id -> {
                input.order(ByteOrder.BIG_ENDIAN) //Needless
                log.fine { "Encoding MSB" }
            }
            else -> throw EBadElfHeader("Unknown ELF data encoding: $encoding")
        }

        //TODO: change to specification names
        when (headerVersion.int_z) {
            EV_NONE.id -> throw EBadElfHeader("Invalid ELF header version")
            EV_CURRENT.id -> {
                log.fine { "Valid ELF header version" }
            }
            else -> throw EBadElfHeader("Unknown ELF header version: $headerVersion")
        }

        when (eh_osabi) {
            ELFOSABI_NONE.id -> Unit
            in ELFOSABI_HPUX.id..ELFOSABI_OPENVOS.id -> log.warning { "OS/ABI is ${ElfOSABI.getNameById(eh_osabi)}, version $eh_abiversion" }
            in ELFOSABI_LOPROC.id..ELFOSABI_HIPROC.id -> Unit
            else -> log.warning { "Unknown OS/ABI: 0x${eh_osabi.hex2}" }
        }

        type = input.short
        machine = input.short
        version = input.int
        entry = input.uint
        phoff = input.int
        shoff = input.int
        flags = input.uint
        ehsize = input.short
        phentsize = input.short
        phnum = input.short
        shentsize = input.short
        shnum = input.short
        shstrndx = input.short

        //Overflow assertions...
        //JVM without native unsigned types - one love

//        assertMajorBit(entry)     // 0x800107BC - MIPS, for example!!!
        assertMajorBit(phoff)
        assertMajorBit(shoff)
        assertMajorBit(ehsize)
        assertMajorBit(phentsize)
        assertMajorBit(phnum)
        assertMajorBit(shentsize)
        assertMajorBit(shnum)
        assertMajorBit(shstrndx)

        when (type) {
            ET_NONE.id -> throw EBadElfHeader("There isn't file type")
            ET_REL.id -> {
                log.fine { "Relocatable type" } //TODO("Relocatable files isn't implemented")
            }
            ET_EXEC.id -> {
                log.fine { "Executable type" }
            }
            ET_DYN.id -> {
                log.fine { "Shared object" }
            }
            ET_CORE.id -> TODO("Core files isn't implemented")
            in ET_LOPROC.id..ET_HIPROC.id -> throw EBadElfHeader("Unknown processor-specific type: $type")
            else -> throw EBadElfHeader("Unknown ELF type: $type")
        }

        when (version) {
            EV_NONE.id -> throw EBadElfHeader("Invalid ELF version")
            EV_CURRENT.id -> log.fine { "Valid ELF version" }
            else -> throw EBadElfHeader("Unknown ELF version: $headerVersion")
        }

        log.info { "entry=0x${entry.hex8} phoff=0x${phoff.hex8} shoff=0x${shoff.hex8} flags=${flags.binary} ehsize=0x${ehsize.hex4} phentsize=0x${phentsize.hex4} phnum=$phnum shentsize=0x${shentsize.hex4} shnum=$shnum shstrndx=$shstrndx" }
        decoder.checkFlags()

        if (ehsize.int_z != 0x34)
            throw EBadElfHeader("Invalid ELF header size: $ehsize (0x${ehsize.hex4}) bytes")

        // TODO: pass as argument
        // TODO: set as false due to not regular ELF
        val throwOnInvalidSection = false

        sectionHeaderTable.forEach { sh ->
            with(sh) {
                val trueFlags = flags and 0b111u
                val invalid: Boolean = when (name) {
                    ".bss" -> type != SHT_NOBITS.id || trueFlags != SHF_ALLOC.id or SHF_WRITE.id
                    ".comment" -> type != SHT_PROGBITS.id || trueFlags != 0u
                    ".data" -> type != SHT_PROGBITS.id || trueFlags != SHF_ALLOC.id or SHF_WRITE.id
                    ".data1" -> type != SHT_PROGBITS.id || trueFlags != SHF_ALLOC.id or SHF_WRITE.id
                    ".debug" -> type != SHT_PROGBITS.id || trueFlags != 0u
                    ".dynamic" -> type != SHT_DYNAMIC.id || trueFlags and SHF_ALLOC.id == 0u || trueFlags and SHF_EXECINSTR.id != 0u //SHF_WRITE is proc-spec
                    ".dynstr" -> type != SHT_STRTAB.id || flags != SHF_ALLOC.id
                    ".dynsym" -> type != SHT_DYNSYM.id || flags != SHF_ALLOC.id
                    ".fini" -> type != SHT_PROGBITS.id || flags != SHF_ALLOC.id or SHF_EXECINSTR.id
                    ".hash" -> type != SHT_HASH.id || flags != SHF_ALLOC.id
                    ".init" -> type != SHT_PROGBITS.id || flags != SHF_ALLOC.id or SHF_EXECINSTR.id
                    ".interp" -> type != SHT_PROGBITS.id || flags != SHF_ALLOC.id && flags != 0u
                    ".line" -> type != SHT_PROGBITS.id || flags != 0u
                    ".note" -> type != SHT_NOTE.id || flags != 0u
                    ".rodata" -> type != SHT_PROGBITS.id || flags and SHF_ALLOC.id == 0u
                    ".rodata1" -> type != SHT_PROGBITS.id || flags != SHF_ALLOC.id
                    ".shstrtab" -> type != SHT_STRTAB.id || flags != 0u
                    ".strtab" -> type != SHT_STRTAB.id || flags != SHF_ALLOC.id && flags != 0u
                    ".symtab" -> type != SHT_SYMTAB.id || flags != SHF_ALLOC.id && flags != 0u
                    ".text" -> type != SHT_PROGBITS.id || flags != SHF_ALLOC.id or SHF_EXECINSTR.id
                    ".gnu.hash " -> type != SHT_GNU_HASH.id || flags != SHF_ALLOC.id
                    ".gnu.version" -> type != SHT_GNU_VERSYM.id || flags != SHF_ALLOC.id
                    ".gnu.version_d" -> type != SHT_GNU_VERDEF.id || flags != SHF_ALLOC.id
                    ".gnu.version_r" -> type != SHT_GNU_VERNEED.id || flags != SHF_ALLOC.id
                    ".got" -> false
                    ".plt" -> false
                    ".got.plt" -> false
                    else -> when {
                        name.startsWith(".rela") -> (type != SHT_RELA.id)
                        name.startsWith(".rel") -> (type != SHT_REL.id)
                        name.startsWith(".$decoder.prefix") -> decoder.checkSectionName(name, type, flags)
                        name.isEmpty() -> false
                        else -> {
                            log.config { "Non standard section name: $name" }
                            false
                        }
                    }
                }
                if (invalid) {
                    val msg = "Not valid $name section (${type.hex8}, ${flags.hex8})"
                    if (throwOnInvalidSection)
                        throw EBadSection(msg)
                    log.severe { msg }
                }
            }
            if (sh.flags and SHF_MASKPROC.id != 0u) {
                log.warning { "Section \"${sh.name}\"(${sh.ind}) has processor-specific flags" }
                decoder.checkSectionFlags(sh.flags)
            }
        }

        if (!sectionLoading) {
            if (flags and PF_MASKOS.id != 0u)
                log.severe { "There are OS-specific flags" }

            if (flags and PF_MASKPROC.id != 0u)
                decoder.checkSegmentFlags(flags)
        }
    }

}