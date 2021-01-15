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
package ru.inforion.lab403.elfloader.processors.arm.enums


 
enum class ArmRelocationType(val id : Int) {
    R_ARM_NONE                  (0),
    R_ARM_PC24                  (1),
    R_ARM_ABS32                 (2),
    R_ARM_REL32                 (3),
    R_ARM_LDR_PC_G0             (4),
    R_ARM_ABS16                 (5),
    R_ARM_ABS12                 (6),
    R_ARM_THM_ABS5              (7),
    R_ARM_ABS8                  (8),
    R_ARM_SBREL32               (9),
    R_ARM_THM_CALL              (10),
    R_ARM_THM_PC8               (11),
    R_ARM_BREL_ADJ              (12),
    R_ARM_TLS_DESC              (13),
    R_ARM_THM_SWI8              (14),
    R_ARM_XPC25                 (15),
    R_ARM_THM_XPC22             (16),
    R_ARM_TLS_DTPMOD32          (17),
    R_ARM_TLS_DTPOFF32          (18),
    R_ARM_TLS_TPOFF32           (19),
    R_ARM_COPY                  (20),
    R_ARM_GLOB_DAT              (21),
    R_ARM_JUMP_SLOT             (22),
    R_ARM_RELATIVE              (23),
    R_ARM_GOTOFF32              (24),
    R_ARM_BASE_PREL             (25),
    R_ARM_GOT_BREL              (26),
    R_ARM_PLT32                 (27),
    R_ARM_CALL                  (28),
    R_ARM_JUMP24                (29),
    R_ARM_THM_JUMP24            (30),
    R_ARM_BASE_ABS              (31),
    R_ARM_ALU_PCREL_7_0         (32),
    R_ARM_ALU_PCREL_15_8        (33),
    R_ARM_ALU_PCREL_23_15       (34),
    R_ARM_LDR_SBREL_11_0_NC     (35),
    R_ARM_ALU_SBREL_19_12_NC    (36),
    R_ARM_ALU_SBREL_27_20_CK    (37),
    R_ARM_TARGET1               (38),
    R_ARM_SBREL31               (39),
    R_ARM_V4BX                  (40),
    R_ARM_TARGET2               (41),
    R_ARM_PREL31                (42)

}