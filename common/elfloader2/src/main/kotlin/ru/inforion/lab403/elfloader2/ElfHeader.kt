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
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader2.enums.*
import ru.inforion.lab403.elfloader2.enums.ELFCLASS.*
import ru.inforion.lab403.elfloader2.enums.ELFDATA.*
import ru.inforion.lab403.elfloader2.enums.ElfMachine.*
import ru.inforion.lab403.elfloader2.enums.ElfType.*
import ru.inforion.lab403.elfloader2.enums.ElfVersion.*
import ru.inforion.lab403.elfloader2.exceptions.EBadElfHeader
import ru.inforion.lab403.elfloader2.exceptions.EBadMagic
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ElfHeader(buffer: ByteBuffer) {

    companion object {
        private val log = logger(INFO)
    }

    // By default, we can change it in init
    var input: IElfDataTypes = ElfDataTypes32(buffer)

    /** Disclaimer:
     * Some of the fields in ELF header can have one of known values -
     * in such cases we use enums as type of field
     * Other cases like with [e_ident_osabi] can have some unknown values,
     * but it isn't a big deal, so we ignore them, but also we save both
     * value and enumerator (nullable) for future use (ex. for ELF modification)
     */

    //32 or 64 bit ELF
    val e_ident_class: ELFCLASS

    //LSB or MSB endian
    val e_ident_data: ELFDATA

    //Only 1 is valid
    val e_ident_version: ElfVersion

    //Operating system/ABI identification
    val e_ident_osabi_value: UByte
    val e_ident_osabi: ELFOSABI?

    //ABI version
    val e_ident_abiversion: UByte

    //Relocatable, executable, shared object, core or processor-specific
    val e_type_value: UShort
    val e_type: ElfType
    val isRel get() = e_type == ET_REL
    val isExec get() = e_type == ET_EXEC
    val isDyn get() = e_type == ET_DYN

    //Machine identifier
    var e_machine: ElfMachine
    val isX86 get() = e_machine == EM_386
    val isARM get() = e_machine == EM_ARM
    val isMIPS get() = e_machine == EM_MIPS
    val isPPC get() = e_machine == EM_PPC

    //ELF file version
    val e_version: ElfVersion

    //ELF entry address
    val e_entry: ULong

    //Program header table's offset
    var e_phoff: ULong = 0uL

    //Sector header table's offset
    var e_shoff: ULong = 0uL

    //Processor-specific flags
    val e_flags: UInt

    //ELF header's size
    val e_ehsize: UShort

    //Size of entry in program header table
    var e_phentsize: UShort = 0u

    //Number of entries in program header table
    var e_phnum: UShort = 0u

    //Size of entry in section header table
    var e_shentsize: UShort = 0u

    //Number of entries in section header table
    var e_shnum: UShort = 0u

    //Section header table string index (section name string table)
    var e_shstrndx: UShort = SectionHeaderNumber.SHN_UNDEF.low

    init {
        // Rewind to start of file
        input.position = 0

        val magic = input.uint
        if (magic != 0x7F454C46u)
            throw EBadMagic("Bad ELF magic number") // TODO: separate exception? why?

        e_ident_class = ELFCLASS.cast(input.byte) { throw EBadElfHeader("Unknown ELF class: $it") }

        // Further parsing requires choosing 32/64-bit data types
        input = when (e_ident_class) {
            ELFCLASSNONE -> throw EBadElfHeader("Invalid ELF class")
            ELFCLASS32 -> {
                log.fine { "Class: ELF32" }
                ElfDataTypes32(buffer)
            }
            ELFCLASS64 -> {
                log.fine { "Class: ELF64" }
                ElfDataTypes64(buffer)
            }
        }
        e_ident_data = ELFDATA.cast(input.byte) { throw EBadElfHeader("Unknown ELF data encoding: $it") }
        e_ident_version = ElfVersion.cast(input.byte) { throw EBadElfHeader("Unknown ELF header version (in ident): $it") }
        e_ident_osabi_value = input.byte
        e_ident_osabi = ELFOSABI.cast(e_ident_osabi_value) { null }
        e_ident_abiversion = input.byte

        //Skip padding bits
        val padding = ByteArray(7)
        input.get(padding)

        when (e_ident_data) {
            ELFDATANONE -> throw EBadElfHeader("Invalid ELF data encoding")
            ELFDATA2LSB -> {
                log.fine { "Data: 2's complement, little endian" }
                buffer.order(ByteOrder.LITTLE_ENDIAN)
            }
            ELFDATA2MSB -> {
                log.fine { "Data: 2's complement, big endian" }
                buffer.order(ByteOrder.BIG_ENDIAN) //Needless
            }
        }

        when (e_ident_version) {
            EV_NONE -> throw EBadElfHeader("Invalid ELF header version")
            EV_CURRENT -> { log.fine { "Version: 1 (current)" } }
        }

        when (e_ident_osabi) {
            null -> "Unknown (0x${e_ident_osabi_value.hex2})"
            else -> e_ident_osabi.shortName
        }.also {
            log.fine { "OS/ABI: $it" }
        }
        log.fine { "ABI Version: $e_ident_abiversion" }

        e_type_value = input.half
        e_type = ElfType.cast(e_type_value) { throw EBadElfHeader("Unknown ELF type: $it") }
        e_machine = ElfMachine.cast(input.half) {throw EBadElfHeader("Unknown ELF machine: $it") }
        e_version = ElfVersion.cast(input.word) { throw EBadElfHeader("Unknown ELF header version: $it") }
        e_entry = input.addr
        e_phoff = input.off
        e_shoff = input.off
        e_flags = input.word
        e_ehsize = input.half
        e_phentsize = input.half
        e_phnum = input.half
        e_shentsize = input.half
        e_shnum = input.half
        e_shstrndx = input.half

        when (e_type) {
            ET_NONE -> throw EBadElfHeader("There isn't file type")
            ET_REL -> "REL (Relocatable file)"
            ET_EXEC -> "EXEC (Executable file)"
            ET_DYN -> "DYN (Shared object file)"
            ET_CORE -> TODO("Core files isn't implemented")
            ET_PROC -> throw EBadElfHeader("Unknown processor-specific type: $e_type")
        }.also {
            log.fine { "Type: $it" }
        }

        log.fine { "Machine: ${e_machine.shortName}" }

        when (e_version) {
            EV_NONE -> throw EBadElfHeader("Invalid ELF version")
            EV_CURRENT -> log.fine { "Version: 0x1" }
        }

        log.fine { "Entry point address: 0x${e_entry.hex}" }
        log.fine { "Start of program headers: 0x${e_phoff.hex}" }
        log.fine { "Start of section headers: 0x${e_shoff.hex}" }
        log.fine { "Flags:  0b${e_flags.binary}" }
        log.fine { "Size of this header: $e_ehsize (bytes)" }
        log.fine { "Size of program headers: $e_phentsize (bytes)" }
        log.fine { "Number of program headers: $e_phnum" }
        log.fine { "Size of section headers: $e_shentsize (bytes)" }
        log.fine { "Number of section headers: $e_shnum" }
        log.fine { "Section header string table index: $e_shstrndx" }

        if (e_ehsize != e_ident_class.headerSize)
            throw EBadElfHeader("Invalid ELF header size: $e_ehsize (0x${e_ehsize.hex4}) bytes")
    }

}