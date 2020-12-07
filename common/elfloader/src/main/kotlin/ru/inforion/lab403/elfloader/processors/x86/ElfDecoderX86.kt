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
package ru.inforion.lab403.elfloader.processors.x86

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader.ElfAccess
import ru.inforion.lab403.elfloader.ElfFile
import ru.inforion.lab403.elfloader.ElfRel
import ru.inforion.lab403.elfloader.enums.ElfEncoding.*
import ru.inforion.lab403.elfloader.enums.ElfObjectSize.*
import ru.inforion.lab403.elfloader.processors.AElfDecoder
import ru.inforion.lab403.elfloader.processors.x86.enum.X86RelocationType
import ru.inforion.lab403.elfloader.processors.x86.enum.X86RelocationType.*


 
class ElfDecoderX86(file: ElfFile) : AElfDecoder(file) {
    companion object {
        private val log = logger()
    }

    override val prefix: String = "386"
    override val pltHeadSize: Int = 16
    override val pltEntrySize: Int = 16

    override fun checkHeader() {
        if (file.objectSize != CLASS_32.id)
            throw EDecodeFault("Only ELF32 supported for x86 architecture (should be x68_64 instead)")
        if (file.encoding != DATA_LSB.id)
            throw EDecodeFault("x86 architecture supports only LSB")
    }
    override fun checkFlags() {
        //TODO("X86-specific flags isn't implemented")
    }
    override fun checkSectionType(type: Int) {
        TODO("There is no any X86-specific sections")
    }
    override fun checkSectionFlags(flags: Int) {
        TODO("There is no any X86-specific section flags")
    }
    override fun checkSectionName(name: String, type: Int, flags: Int): Boolean {
        TODO("There is no any X86-specific section names")
    }
    override fun checkSymbolBinding(bind: Int) {
        TODO("There is no any X86-specific sections")
    }
    override fun checkSymbolType(type: Int) {
        TODO("There is no any X86-specific sections")
    }
    override fun checkSegmentType(type: Int) {
        TODO("There is no any X86-specific segments")
    }
    override fun checkSegmentFlags(flags: Int) {
        TODO("There is no any X86-specific segment flags")
    }
    override fun parseDynamic(hm: HashMap<Int, Long>, tag: Int, ptr: Long) {
        TODO("There is no any X86-specific dynamic tags")
    }
    override fun applyStaticRelocation(rel: ElfRel, vaddr: Long, symbol: Long, got: Long?, data: Long): Long {
        val S = symbol
        val A = if (rel.withAddend) rel.addend.toLong() else data
        val P = vaddr

        return when (rel.type) {
            //None
            R_386_NONE.id -> data

            //S + A
            R_386_32.id -> S + A

            //S + A - P
            R_386_PC32.id -> S - P + A

            /*
            //G + A - P
            R_386_GOT32.id -> {

            }
            */

            //L + A - P
            R_386_PLT32.id -> S - P + A //S -> L

            //Copy
            R_386_COPY.id -> 0

            //S
            R_386_GLOB_DAT.id -> S

            //S
            R_386_JMP_SLOT.id -> S

            /*
            //B + A
            R_386_RELATIVE.id -> {

            }
            */

            //S + A - GOT
            R_386_GOTOFF.id -> {
                if (got == null)
                    throw  Exception("GOT relocation without GOT-section")
                S - got + A
            }

            //GOT + A - P
            R_386_GOTPC.id -> {
                if (got == null)
                    throw  Exception("GOT relocation without GOT-section")
                got - P + A
            }

            else -> TODO("Not implemented type: ${rel.type} (${getRelocationNameById(rel.type)})")
        }
    }
    override fun isLoadableSection(type: Int, access: ElfAccess): Boolean {
        TODO("There is no any X86-specific sections")
    }
    override fun isLoadableSegment(type: Int) : Boolean {
        TODO("There is no any X86-specific segments")
    }
    override fun getProgramHeaderTypeNameById(type: Int): String {
        TODO("There is no any X86-specific segments")
    }
    override fun getRelocationNameById(type: Int): String = X86RelocationType.values().first{ it -> it.id == type}.name
}