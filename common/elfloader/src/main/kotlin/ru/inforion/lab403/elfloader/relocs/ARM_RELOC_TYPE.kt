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
package ru.inforion.lab403.elfloader.relocs

enum class ARM_RELOC_TYPE(val id: Int) {
    R_ARM_NONE         (0 ),     //  Static Miscellaneous
    R_ARM_PC24         (1 ),     //  Deprecated ARM ((S + A) | T) – P
    R_ARM_ABS32        (2 ),     //  Static Data (S + A) | T
    R_ARM_REL32        (3 ),     //  Static Data ((S + A) | T) – P
    R_ARM_LDR_PC_G0    (4 ),     //  Static ARM S + A – P
    R_ARM_ABS16        (5 ),     //  Static Data S + A
    R_ARM_ABS12        (6 ),     //  Static ARM S + A
    R_ARM_THM_ABS5     (7 ),     //  Static Thumb16 S + A
    R_ARM_ABS8         (8 ),     //  Static Data S + A
    R_ARM_SBREL32      (9 ),     //  Static Data ((S + A) | T) – B(S)
    R_ARM_THM_CALL     (10),     //  Static Thumb32 ((S + A) | T) – P
    R_ARM_THM_PC8      (11),     //  Static Thumb16 S + A – Pa
    R_ARM_BREL_ADJ     (12),     //  Dynamic Data ΔB(S) + A
    R_ARM_TLS_DESC     (13),     //  Dynamic Data
    R_ARM_THM_SWI8     (14),     //  Obsolete
    R_ARM_XPC25        (15),     //  Obsolete
    R_ARM_THM_XPC22    (16),     //  Obsolete
    R_ARM_TLS_DTPMOD32 (17),     //  Dynamic Data Module[S]
    R_ARM_TLS_DTPOFF32 (18),     //  Dynamic Data S + A – TLS
    R_ARM_TLS_TPOFF32  (19),     //  Dynamic Data S + A – tp
    R_ARM_COPY         (20),     //  Dynamic Miscellaneous
    R_ARM_GLOB_DAT     (21),     //  Dynamic Data (S + A) | T
    R_ARM_JUMP_SLOT    (22),     //  Dynamic Data (S + A) | T
    R_ARM_RELATIVE     (23),     //  Dynamic Data B(S) + A
    R_ARM_GOTOFF32     (24),     //  Static Data ((S + A) | T) – GOT_ORG
    R_ARM_BASE_PREL    (25),     //  Static Data B(S) + A – P
    R_ARM_GOT_BREL     (26),     //  Static Data GOT(S) + A – GOT_ORG
    R_ARM_PLT32        (27),     //  Deprecated ARM ((S + A) | T) – P
    R_ARM_CALL         (28),     //  Static ARM ((S + A) | T) – P
    R_ARM_JUMP24       (29),     //  Static ARM ((S + A) | T) – P
    R_ARM_THM_JUMP24   (30),     //  Static Thumb32 ((S + A) | T) – P
    R_ARM_BASE_ABS     (31),     //  Static Data B(S) + A
}