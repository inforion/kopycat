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
package ru.inforion.lab403.elfloader2.processors.arm.enums

import ru.inforion.lab403.elfloader2.exceptions.EBadRelocation
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmRelocationType.Companion.Field.*


enum class ArmRelocationType(val id : ULong, val field: Field) {
    R_ARM_NONE                  (0u, none),
    R_ARM_PC24                  (1u, arm_b_bl),
    R_ARM_ABS32                 (2u, word32),
    R_ARM_REL32                 (3u, word32),
    R_ARM_LDR_PC_G0             (4u, arm_ldr_r_pc),
    R_ARM_ABS16                 (5u, half16),
    R_ARM_ABS12                 (6u, arm_ldr_str),
    R_ARM_THM_ABS5              (7u, thumb_ldr_str),
    R_ARM_ABS8                  (8u, byte8),
    R_ARM_SBREL32               (9u, word32),
    R_ARM_THM_CALL              (10u, thumb_bl_pair),
    R_ARM_THM_PC8               (11u, thumb_ldr_r_pc),
    R_ARM_AMP_VCALL9            (12u, amp_vcall), //  Obsolete—SA-1500 only.
    R_ARM_SWI24                 (13u, arm_swi),
    R_ARM_THM_SWI8              (14u, thumb_swi),
    R_ARM_XPC25                 (15u, arm_blx),
    R_ARM_THM_XPC22             (16u, thumb_blx_pair),

    R_ARM_TLS_DTPMOD32          (17u, none),
    R_ARM_TLS_DTPOFF32          (18u, none),
    R_ARM_TLS_TPOFF32           (19u, none),

    R_ARM_COPY                  (20u, word32),
    R_ARM_GLOB_DAT              (21u, word32),
    R_ARM_JUMP_SLOT             (22u, word32),
    R_ARM_RELATIVE              (23u, word32),
    R_ARM_GOTOFF                (24u, word32),
    R_ARM_BASE_PREL             (25u, word32),
    R_ARM_GOT_BREL             (26u, word32),
    R_ARM_PLT32                 (27u, arm_bl),

    R_ARM_CALL                  (28u, arm_bl_blx),
    R_ARM_JUMP24                (29u, arm_b_bl),
    R_ARM_THM_JUMP24            (30u, none),
    R_ARM_BASE_ABS              (31u, none),

    R_ARM_ALU_PCREL_7_0         (32u, arm_add_sub),
    R_ARM_ALU_PCREL_15_8        (33u, arm_add_sub),
    R_ARM_ALU_PCREL_23_15       (34u, arm_add_sub),
    R_ARM_LDR_SBREL_11_0_NC     (35u, arm_ldr_str),
    R_ARM_ALU_SBREL_19_12_NC    (36u, arm_add_sub),
    R_ARM_ALU_SBREL_27_20_CK    (37u, arm_add_sub),

    R_ARM_TARGET1               (38u, none),
    R_ARM_SBREL31               (39u, none),
    R_ARM_V4BX                  (40u, none),
    R_ARM_TARGET2               (41u, none),
    R_ARM_PREL31                (42u, none);

    companion object {
        enum class Field {
            none,
            word32,
            half16,
            byte8,
            arm_add_sub,
            arm_b_bl,
            arm_bl_blx,
            arm_bl,
            arm_blx,
            arm_ldr_str,
            arm_ldr_r_pc,
            arm_swi,
            thumb_bl_pair,
            thumb_blx_pair,
            thumb_ldr_str,
            thumb_ldr_r_pc,
            thumb_swi,
            amp_vcall
        }
        fun ULong.armRelocation(onFail: (ULong) -> ArmRelocationType) = values().find { this == it.id } ?: onFail(this)
        val ULong.armRelocation get() = armRelocation { throw EBadRelocation("Unknown relocation type $this") }
    }

}