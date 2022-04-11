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
package ru.inforion.lab403.elfloader2.processors.aarch64

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.elfloader2.ElfAccess
import ru.inforion.lab403.elfloader2.ElfFile
import ru.inforion.lab403.elfloader2.ElfRel
import ru.inforion.lab403.elfloader2.enums.ELFCLASS
import ru.inforion.lab403.elfloader2.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader2.processors.AElfDecoder

class ElfDecoderPPC64 (file: ElfFile) : AElfDecoder(file) {
    companion object {
        private val log = logger()
    }
    override val prefix: String = "ppc64"

    override fun checkHeader() {
        if (file.elfHeader.e_ident_class != ELFCLASS.ELFCLASS64)
            throw EDecodeFault("Only ELF64 supported for PPC64 architecture (should be PPC instead)")
    }

    override fun checkFlags() = log.severe { "PPC64-specific flags aren't implemented" }

    override fun checkSectionType(type: UInt): Unit = TODO("There is no any PPC64-specific sections")
    override fun checkSectionFlags(flags: ULong): Unit = TODO("There is no any PPC64-specific section flags")
    override fun checkSectionName(name: String, type: UInt, flags: ULong) =
        TODO("There is no any PPC64-specific section names")

    override fun checkSymbolBinding(bind: UInt) = TODO("There is no any PPC64-specific sections")
    override fun checkSymbolType(type: Int) = TODO("There is no any PPC64-specific sections")
    override fun checkSegmentType(type: UInt) = TODO("There is no any PPC64-specific segments")
    override fun checkSegmentFlags(flags: UInt) = TODO("There is no any PPC64-specific segment flags")
    override fun parseDynamic(hm: MutableMap<ULong, ULong>, tag: ULong, ptr: ULong) = log.severe { "PPC64-specific dynamic tags aren't implemented" }

    override fun applyStaticRelocationRemoveMe(
        rel: ElfRel,
        vaddr: ULong,
        symbol: ULong,
        got: Optional<ULong>,
        data: ULong
    ): ULong {
        // TODO: There is a virtual section for PPC64 with associated relocation type R_PPC64_TOC
        TODO("")
    }

    override fun isLoadableSection(type: UInt, access: ElfAccess) = TODO("There is no any PPC64-specific sections")
    override fun isLoadableSegment(type: UInt) = TODO("There is no any PPC64-specific segments")
    override fun getProgramHeaderTypeNameById(type: UInt) = TODO("There is no any PPC64-specific segments")
    override fun getRelocationNameById(type: UInt) = "" // TODO("There is no any PPC64-specific relocs")
}