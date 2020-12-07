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
package ru.inforion.lab403.elfloader.processors.mips.enums

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType.SHT_LOPROC

enum class MipsSectionType(val id: Int) {
    SHT_MIPS_LIBLIST        (SHT_LOPROC.id + 0x00),   // DSO library information used to link
    SHT_MIPS_MSYM           (SHT_LOPROC.id + 0x01),   // MIPS symbol table extension
    SHT_MIPS_CONFLICT       (SHT_LOPROC.id + 0x02),   // Symbol conflicting with DSO defined symbols
    SHT_MIPS_GPTAB          (SHT_LOPROC.id + 0x03),   // Global pointer table
    SHT_MIPS_UCODE          (SHT_LOPROC.id + 0x04),   // Reserved
    SHT_MIPS_DEBUG          (SHT_LOPROC.id + 0x05),   // Reserved (obsolete debug information)
    SHT_MIPS_REGINFO        (SHT_LOPROC.id + 0x06),   // Register usage information
    SHT_MIPS_PACKAGE        (SHT_LOPROC.id + 0x07),   // OSF reserved
    SHT_MIPS_PACKSYM        (SHT_LOPROC.id + 0x08),   // OSF reserved
    SHT_MIPS_RELD           (SHT_LOPROC.id + 0x09),   // Dynamic relocations (obsolete)
    //                      (SHT_LOPROC + 0x0a),         // Unused
    SHT_MIPS_IFACE          (SHT_LOPROC.id + 0x0b),   // Subprogram interface information
    SHT_MIPS_CONTENT        (SHT_LOPROC.id + 0x0c),   // Section content information
    SHT_MIPS_OPTIONS        (SHT_LOPROC.id + 0x0d),   // General options
    //                      (SHT_LOPROC + 0x0e),         // Unused
    //                      (SHT_LOPROC + 0x0f),             // Unused
    SHT_MIPS_SHDR           (SHT_LOPROC.id + 0x10),   // xxx
    SHT_MIPS_FDESC          (SHT_LOPROC.id + 0x11),   // xxx
    SHT_MIPS_EXTSYM         (SHT_LOPROC.id + 0x12),   // xxx
    SHT_MIPS_DENSE          (SHT_LOPROC.id + 0x13),   // xxx
    SHT_MIPS_PDESC          (SHT_LOPROC.id + 0x14),   // xxx
    SHT_MIPS_LOCSYM         (SHT_LOPROC.id + 0x15),   // xxx
    SHT_MIPS_AUXSYM         (SHT_LOPROC.id + 0x16),   // xxx
    SHT_MIPS_OPTSYM         (SHT_LOPROC.id + 0x17),   // xxx
    SHT_MIPS_LOCSTR         (SHT_LOPROC.id + 0x18),   // xxx
    SHT_MIPS_LINE           (SHT_LOPROC.id + 0x19),   // xxx
    SHT_MIPS_RFDESC         (SHT_LOPROC.id + 0x1a),   // xxx
    SHT_MIPS_DELTASYM       (SHT_LOPROC.id + 0x1b),   // Delta C++ symbol table (obsolete)
    SHT_MIPS_DELTAINST      (SHT_LOPROC.id + 0x1c),   // Delta C++ instance table (obsolete)
    SHT_MIPS_DELTACLASS     (SHT_LOPROC.id + 0x1d),   // Delta C++ class table (obsolete)
    SHT_MIPS_DWARF          (SHT_LOPROC.id + 0x1e),   // Dwarf debug information
    SHT_MIPS_DELTADECL      (SHT_LOPROC.id + 0x1f),   // Delta C++ declarations (obsolete)
    SHT_MIPS_SYMBOL_LIB     (SHT_LOPROC.id + 0x20),   // Symbol to library mapping
    SHT_MIPS_EVENTS         (SHT_LOPROC.id + 0x21),   // Section event mapping
    SHT_MIPS_TRANSLATE      (SHT_LOPROC.id + 0x22),   // Old pixie translation table (obsolete)
    SHT_MIPS_PIXIE          (SHT_LOPROC.id + 0x23),   // Pixie specific sections (SGI)
    SHT_MIPS_XLATE          (SHT_LOPROC.id + 0x24),   // Address translation table
    SHT_MIPS_XLATE_DEBUG    (SHT_LOPROC.id + 0x25),   // SGI internal address translation table
    SHT_MIPS_WHIRL          (SHT_LOPROC.id + 0x26),   // Intermediate code (MipsPro compiler)
    SHT_MIPS_EH_REGION      (SHT_LOPROC.id + 0x27),   // C++ exception handling region information
    SHT_MIPS_XLATE_OLD      (SHT_LOPROC.id + 0x28),   // obsolete
    SHT_MIPS_PDR_EXCEPTION  (SHT_LOPROC.id + 0x29),   // Runtime procedure descriptor table exception information (ucode)
    SHT_MIPS_ABIFLAGS       (SHT_LOPROC.id + 0x2a);   // Runtime procedure descriptor table exception information (ucode)

    companion object {
        fun getNameById(id: Int): String {
            val st = find<MipsSectionType> { it.id == id }
            return if (st != null) st.name else "Unknown MIPS section type id ${id.hex8}"
        }
    }
}