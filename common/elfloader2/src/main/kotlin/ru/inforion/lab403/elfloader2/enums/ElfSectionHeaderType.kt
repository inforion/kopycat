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
package ru.inforion.lab403.elfloader2.enums


enum class ElfSectionHeaderType(val low: UInt, val high: UInt = low) {
    SHT_NULL            (0u),            // Inactive section
    SHT_PROGBITS        (1u),            // Program determined-only section
    SHT_SYMTAB          (2u),            // Symbol table                              //1 Only
    SHT_STRTAB          (3u),            // String table
    SHT_RELA            (4u),            // Relocation section
    SHT_HASH            (5u),            // Symbol hash table (for dynamic linking)   //1 Only
    SHT_DYNAMIC         (6u),            // Dynamic linking information               //1 Only
    SHT_NOTE            (7u),            // Note section
    SHT_NOBITS          (8u),            // Section holds no space in file
    SHT_REL             (9u),            // Relocation entries
    SHT_SHLIB           (10u),           // Unspecified
    SHT_DYNSYM          (11u),           // Dynamic linking symbol table              //1 Only

    SHT_INIT_ARRAY      (14u),           // Section data contains an array of constructors
    SHT_FINI_ARRAY      (15u),           // Section data contains an array of destructors
    SHT_PREINIT_ARRAY   (16u),           // Section data contains an array of pre-constructors
    SHT_GROUP           (17u),           // Section group
    SHT_SYMTAB_SHNDX    (18u),           // Extended symbol table section index
    SHT_NUM             (19u),           // Number of reserved SHT_* values

    SHT_GNU_ATTRIBUTES  (0x6FFFFFF5u),   // Object attributes
    SHT_GNU_HASH        (0x6FFFFFF6u),   // GNU-style hash section
    SHT_GNU_LIBLIST     (0x6FFFFFF7u),   // Pre-link library list

    SHT_GNU_VERDEF      (0x6FFFFFFDu),   // Symbol definitions
    SHT_GNU_VERNEED     (0x6FFFFFFEu),   // Symbol requirements
    SHT_GNU_VERSYM      (0x6FFFFFFFu),   // Symbol version table

    SHT_PROC          (0x70000000u, 0x7FFFFFFFu),   // Processor-specific
    SHT_USER          (0x80000000u, 0xFFFFFFFFu);    // Application programs //???

    val range = low..high
    val shortName get() = name.removePrefix("SHT_")

    companion object {
        fun cast(id: UInt, onFail: (UInt) -> ElfSectionHeaderType) = values().find { id in it.range } ?: onFail(id)
        fun castOrThrow(id: UInt) = cast(id) { throw NotImplementedError("Unknown section header type: $it") }
    }

}