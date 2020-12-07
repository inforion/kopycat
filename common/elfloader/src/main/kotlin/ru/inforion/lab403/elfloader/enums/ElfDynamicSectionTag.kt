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


 
enum class ElfDynamicSectionTag(val id: Int) {
    DT_NULL(0),
    DT_NEEDED(1),
    DT_PLTRELSZ(2),
    DT_PLTGOT(3),
    DT_HASH(4),
    DT_STRTAB(5),
    DT_SYMTAB(6),
    DT_RELA(7),
    DT_RELASZ(8),
    DT_RELAENT(9),
    DT_STRSZ(10),
    DT_SYMENT(11),
    DT_INIT(12),
    DT_FINI(13),
    DT_SONAME(14),
    DT_RPATH(15),
    DT_SYMBOLIC(16),
    DT_REL(17),
    DT_RELSZ(18),
    DT_RELENT(19),
    DT_PLTREL(20),
    DT_DEBUG(21),
    DT_TEXTREL(22),
    DT_JMPREL(23),
    DT_BIND_NOW(24),
    DT_INIT_ARRAY(25),
    DT_FINI_ARRAY(26),
    DT_INIT_ARRAYSZ(27),
    DT_FINI_ARRAYSZ(28),
    DT_RUNPATH(29),
    DT_FLAGS(30),
    DT_ENCODING(31),
    DT_PREINIT_ARRAY(32),
    DT_PREINIT_ARRAYSZ(33),
    DT_MAXPOSTAGS(34),
    DT_GNU_HASH(0x6FFFFEF5),
    DT_VERSYM(0x6FFFFFF0),
    DT_VERNEED(0x6FFFFFFE),
    DT_VERNEEDNUM(0x6FFFFFFF),
    DT_LOPROC(0x70000000),
    DT_HIPROC(0x7FFFFFFF);

}