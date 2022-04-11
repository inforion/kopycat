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
package ru.inforion.lab403.elfloader2.enums


 
enum class ElfDynamicSectionTag(val id: ULong) {
    DT_NULL(0u),
    DT_NEEDED(1u),
    DT_PLTRELSZ(2u),
    DT_PLTGOT(3u),
    DT_HASH(4u),
    DT_STRTAB(5u),
    DT_SYMTAB(6u),
    DT_RELA(7u),
    DT_RELASZ(8u),
    DT_RELAENT(9u),
    DT_STRSZ(10u),
    DT_SYMENT(11u),
    DT_INIT(12u),
    DT_FINI(13u),
    DT_SONAME(14u),
    DT_RPATH(15u),
    DT_SYMBOLIC(16u),
    DT_REL(17u),
    DT_RELSZ(18u),
    DT_RELENT(19u),
    DT_PLTREL(20u),
    DT_DEBUG(21u),
    DT_TEXTREL(22u),
    DT_JMPREL(23u),
    DT_BIND_NOW(24u),
    DT_INIT_ARRAY(25u),
    DT_FINI_ARRAY(26u),
    DT_INIT_ARRAYSZ(27u),
    DT_FINI_ARRAYSZ(28u),
    DT_RUNPATH(29u),
    DT_FLAGS(30u),
    DT_ENCODING(31u),
    DT_PREINIT_ARRAY(32u),
    DT_PREINIT_ARRAYSZ(33u),
    DT_MAXPOSTAGS(34u),
    DT_GNU_HASH(0x6FFFFEF5u),
    DT_VERSYM(0x6FFFFFF0u),
    DT_VERNEED(0x6FFFFFFEu),
    DT_VERNEEDNUM(0x6FFFFFFFu),
    DT_LOPROC(0x70000000u),
    DT_HIPROC(0x7FFFFFFFu);

}