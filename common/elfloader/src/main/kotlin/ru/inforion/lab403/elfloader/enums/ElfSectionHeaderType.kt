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
package ru.inforion.lab403.elfloader.enums

import ru.inforion.lab403.common.extensions.*


enum class ElfSectionHeaderType(val id: Int) {
    SHT_NULL            (0),            // Inactive section
    SHT_PROGBITS        (1),            // Program determined-only section
    SHT_SYMTAB          (2),            // Symbol table                              //1 Only
    SHT_STRTAB          (3),            // String table
    SHT_RELA            (4),            // Relocation section
    SHT_HASH            (5),            // Symbol hash table (for dynamic linking)   //1 Only
    SHT_DYNAMIC         (6),            // Dynamic linking information               //1 Only
    SHT_NOTE            (7),            // Note section
    SHT_NOBITS          (8),            // Section holds no space in file
    SHT_REL             (9),            // Relocation entries
    SHT_SHLIB           (10),           // Unspecified
    SHT_DYNSYM          (11),           // Dynamic linking symbol table              //1 Only

    SHT_INIT_ARRAY      (14),           // Section data contains an array of constructors
    SHT_FINI_ARRAY      (15),           // Section data contains an array of destructors
    SHT_PREINIT_ARRAY   (16),           // Section data contains an array of pre-constructors
    SHT_GROUP           (17),           // Section group
    SHT_SYMTAB_SHNDX    (18),           // Extended symbol table section index
    SHT_NUM             (19),           // Number of reserved SHT_* values

    SHT_GNU_ATTRIBUTES  (0x6FFFFFF5),   // Object attributes
    SHT_GNU_HASH        (0x6FFFFFF5),   // GNU-style hash section
    SHT_GNU_LIBLIST     (0x6FFFFFF5),   // Pre-link library list

    SHT_GNU_VERDEF      (0x6FFFFFFD),   // Symbol definitions
    SHT_GNU_VERNEED     (0x6FFFFFFE),   // Symbol requirements
    SHT_GNU_VERSYM      (0x6FFFFFFF),   // Symbol version table

    SHT_LOPROC          (0x70000000),   // Processor-specific
    SHT_HIPROC          (0x7FFFFFFF),
    SHT_LOUSER          (0x80000000.toInt()),    // Application programs //???
    SHT_HIUSER          (0xFFFFFFFF.toInt());

    companion object {
        fun nameById(id: Int) : String {
            val stdTypes = find<ElfSectionHeaderType> { it.id == id }
            return stdTypes?.name ?: if (id in SHT_LOPROC.id..SHT_HIPROC.id) "PROC_SPEC[${id.hex8}]" else "NON_STANDARD[${id.hex8}]"
        }
    }

}