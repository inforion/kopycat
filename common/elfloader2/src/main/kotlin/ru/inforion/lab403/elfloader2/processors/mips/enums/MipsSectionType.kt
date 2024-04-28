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
package ru.inforion.lab403.elfloader2.processors.mips.enums

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderType.*

enum class MipsSectionType(val id: UInt) {
    SHT_MIPS_LIBLIST        (SHT_PROC.low + 0x00u),   // DSO library information used to link
    SHT_MIPS_MSYM           (SHT_PROC.low + 0x01u),   // MIPS symbol table extension
    SHT_MIPS_CONFLICT       (SHT_PROC.low + 0x02u),   // Symbol conflicting with DSO defined symbols
    SHT_MIPS_GPTAB          (SHT_PROC.low + 0x03u),   // Global pointer table
    SHT_MIPS_UCODE          (SHT_PROC.low + 0x04u),   // Reserved
    SHT_MIPS_DEBUG          (SHT_PROC.low + 0x05u),   // Reserved (obsolete debug information)
    SHT_MIPS_REGINFO        (SHT_PROC.low + 0x06u),   // Register usage information
    SHT_MIPS_PACKAGE        (SHT_PROC.low + 0x07u),   // OSF reserved
    SHT_MIPS_PACKSYM        (SHT_PROC.low + 0x08u),   // OSF reserved
    SHT_MIPS_RELD           (SHT_PROC.low + 0x09u),   // Dynamic relocations (obsolete)
    //                      (SHT_PROC + 0x0a),         //u Unused
    SHT_MIPS_IFACE          (SHT_PROC.low + 0x0bu),   // Subprogram interface information
    SHT_MIPS_CONTENT        (SHT_PROC.low + 0x0cu),   // Section content information
    SHT_MIPS_OPTIONS        (SHT_PROC.low + 0x0du),   // General options
    //                      (SHT_PROC + 0x0e),         //u Unused
    //                      (SHT_PROC + 0x0f),             //u Unused
    SHT_MIPS_SHDR           (SHT_PROC.low + 0x10u),   // xxx
    SHT_MIPS_FDESC          (SHT_PROC.low + 0x11u),   // xxx
    SHT_MIPS_EXTSYM         (SHT_PROC.low + 0x12u),   // xxx
    SHT_MIPS_DENSE          (SHT_PROC.low + 0x13u),   // xxx
    SHT_MIPS_PDESC          (SHT_PROC.low + 0x14u),   // xxx
    SHT_MIPS_LOCSYM         (SHT_PROC.low + 0x15u),   // xxx
    SHT_MIPS_AUXSYM         (SHT_PROC.low + 0x16u),   // xxx
    SHT_MIPS_OPTSYM         (SHT_PROC.low + 0x17u),   // xxx
    SHT_MIPS_LOCSTR         (SHT_PROC.low + 0x18u),   // xxx
    SHT_MIPS_LINE           (SHT_PROC.low + 0x19u),   // xxx
    SHT_MIPS_RFDESC         (SHT_PROC.low + 0x1au),   // xxx
    SHT_MIPS_DELTASYM       (SHT_PROC.low + 0x1bu),   // Delta C++ symbol table (obsolete)
    SHT_MIPS_DELTAINST      (SHT_PROC.low + 0x1cu),   // Delta C++ instance table (obsolete)
    SHT_MIPS_DELTACLASS     (SHT_PROC.low + 0x1du),   // Delta C++ class table (obsolete)
    SHT_MIPS_DWARF          (SHT_PROC.low + 0x1eu),   // Dwarf debug information
    SHT_MIPS_DELTADECL      (SHT_PROC.low + 0x1fu),   // Delta C++ declarations (obsolete)
    SHT_MIPS_SYMBOL_LIB     (SHT_PROC.low + 0x20u),   // Symbol to library mapping
    SHT_MIPS_EVENTS         (SHT_PROC.low + 0x21u),   // Section event mapping
    SHT_MIPS_TRANSLATE      (SHT_PROC.low + 0x22u),   // Old pixie translation table (obsolete)
    SHT_MIPS_PIXIE          (SHT_PROC.low + 0x23u),   // Pixie specific sections (SGI)
    SHT_MIPS_XLATE          (SHT_PROC.low + 0x24u),   // Address translation table
    SHT_MIPS_XLATE_DEBUG    (SHT_PROC.low + 0x25u),   // SGI internal address translation table
    SHT_MIPS_WHIRL          (SHT_PROC.low + 0x26u),   // Intermediate code (MipsPro compiler)
    SHT_MIPS_EH_REGION      (SHT_PROC.low + 0x27u),   // C++ exception handling region information
    SHT_MIPS_XLATE_OLD      (SHT_PROC.low + 0x28u),   // obsolete
    SHT_MIPS_PDR_EXCEPTION  (SHT_PROC.low + 0x29u),   // Runtime procedure descriptor table exception information (ucode)
    SHT_MIPS_ABIFLAGS       (SHT_PROC.low + 0x2au);   // Runtime procedure descriptor table exception information (ucode)

    companion object {
        fun getNameById(id: UInt): String {
            val st = find<MipsSectionType> { it.id == id }
            return if (st != null) st.name else "Unknown MIPS section type id ${id.hex8}"
        }
    }
}