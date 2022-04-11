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
package ru.inforion.lab403.elfloader2.processors.mips.enums

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.elfloader2.exceptions.EBadRelocation
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsRelocationType.Companion.Field.*


enum class MipsRelocationType(val id: ULong, val field: Field) {
    R_MIPS_NONE                 (0u, none),
    R_MIPS_16                   (1u, vHalf16),
    R_MIPS_32                   (2u, tWord32),
    R_MIPS_REL32                (3u, tWord32),
    R_MIPS_26                   (4u, tArg26),
    R_MIPS_HI16                 (5u, ttvHi16),
    R_MIPS_LO16                 (6u, ttvLo16),
    R_MIPS_GPREL16              (7u, vRel16),
    R_MIPS_LITERAL              (8u, vLit16),
    R_MIPS_GOT16                (9u, vRel16),
    R_MIPS_PC16                 (10u, vPC16),
    R_MIPS_CALL16               (11u, vRel16),
    R_MIPS_GPREL32              (12u, tWord32),

    R_MIPS_SHIFT5               (16u, none ),
    R_MIPS_SHIFT6               (17u, none ),
    R_MIPS_64                   (18u, none ),
    R_MIPS_GOT_DISP             (19u, none ),
    R_MIPS_GOT_PAGE             (20u, none ),

    R_MIPS_GOT_HI16              (21u, tHi16),
    R_MIPS_GOT_LO16              (22u, tLo16),

    R_MIPS_SUB                  (24u, none ),
    R_MIPS_INSERT_A             (25u, none ),
    R_MIPS_INSERT_B             (26u, none ),
    R_MIPS_DELETE               (27u, none ),
    R_MIPS_HIGHER               (28u, none ),
    R_MIPS_HIGHEST              (29u, none ),

    R_MIPS_CALL_HI16             (30u, tHi16),
    R_MIPS_CALL_LO16             (31u, tLo16),

    R_MIPS_SCN_DISP             (32u, none ),
    R_MIPS_REL16                (33u, none ),
    R_MIPS_ADD_IMMEDIATE        (34u, none ),
    R_MIPS_PJUMP                (35u, none ),
    R_MIPS_RELGOT               (36u, none ),

    R_MIPS_JALR                 (37u, none),

    R_MIPS_TLS_DTPMOD32         (38u, none ),
    R_MIPS_TLS_DTPREL32         (39u, none ),
    R_MIPS_TLS_DTPMOD64         (40u, none ),
    R_MIPS_TLS_DTPREL64         (41u, none ),
    R_MIPS_TLS_GD               (42u, none ),
    R_MIPS_TLS_LDM              (43u, none ),
    R_MIPS_TLS_DTPREL_HI16      (44u, none ),
    R_MIPS_TLS_DTPREL_LO16      (45u, none ),
    R_MIPS_TLS_GOTTPREL         (46u, none ),
    R_MIPS_TLS_TPREL32          (47u, none ),
    R_MIPS_TLS_TPREL64          (48u, none ),
    R_MIPS_TLS_TPREL_HI16       (49u, none ),
    R_MIPS_TLS_TPREL_LO16       (50u, none ),
    R_MIPS_GLOB_DAT             (51u, none ),
    R_MIPS_PC10                 (52u, none ),  /* (obsolete) */
    R_MIPS_PC21_S2              (60u, none ),
    R_MIPS_PC26_S2              (61u, none ),
    R_MIPS_PC18_S3              (62u, none ),
    R_MIPS_PC19_S2              (63u, none ),
    R_MIPS_PCHI16               (64u, none ),
    R_MIPS_PCLO16               (65u, none ),
    R_MIPS16_26                 (100u, none),
    R_MIPS16_GPREL              (101u, none),
    R_MIPS16_GOT16              (102u, none),
    R_MIPS16_CALL16             (103u, none),
    R_MIPS16_HI16               (104u, none),
    R_MIPS16_LO16               (105u, none),
    R_MIPS16_TLS_GD             (106u, none),
    R_MIPS16_TLS_LDM            (107u, none),
    R_MIPS16_TLS_DTPREL_HI16    (108u, none),
    R_MIPS16_TLS_DTPREL_LO16    (109u, none),
    R_MIPS16_TLS_GOTTPREL       (110u, none),
    R_MIPS16_TLS_TPREL_HI16     (111u, none),
    R_MIPS16_TLS_TPREL_LO16     (112u, none),
    R_MIPS16_PC16_S1            (113u, none),
    R_MIPS_COPY                 (126u, none),
    R_MIPS_JUMP_SLOT            (127u, none),
    R_MICROMIPS_26_S1           (133u, none),
    R_MICROMIPS_HI16            (134u, none),
    R_MICROMIPS_LO16            (135u, none),
    R_MICROMIPS_GPREL16         (136u, none),
    R_MICROMIPS_LITERAL         (137u, none),
    R_MICROMIPS_GOT16           (138u, none),
    R_MICROMIPS_PC7_S1          (139u, none),
    R_MICROMIPS_PC10_S1         (140u, none),
    R_MICROMIPS_PC16_S1         (141u, none),
    R_MICROMIPS_CALL16          (142u, none),
    R_MICROMIPS_GOT_DISP        (145u, none),
    R_MICROMIPS_GOT_PAGE        (146u, none),
    R_MICROMIPS_GOT_OFST        (147u, none),
    R_MICROMIPS_GOT_HI16        (148u, none),
    R_MICROMIPS_GOT_LO16        (149u, none),
    R_MICROMIPS_SUB             (150u, none),
    R_MICROMIPS_HIGHER          (151u, none),
    R_MICROMIPS_HIGHEST         (152u, none),
    R_MICROMIPS_CALL_HI16       (153u, none),
    R_MICROMIPS_CALL_LO16       (154u, none),
    R_MICROMIPS_SCN_DISP        (155u, none),
    R_MICROMIPS_JALR            (156u, none),
    R_MICROMIPS_HI0_LO16        (157u, none),
    R_MICROMIPS_PCHI16          (158u, none),
    R_MICROMIPS_PCLO16          (159u, none),
    R_MICROMIPS_TLS_GD          (162u, none),
    R_MICROMIPS_TLS_LDM         (163u, none),
    R_MICROMIPS_TLS_DTPREL_HI16 (164u, none),
    R_MICROMIPS_TLS_DTPREL_LO16 (165u, none),
    R_MICROMIPS_TLS_GOTTPREL    (166u, none),
    R_MICROMIPS_TLS_TPREL_HI16  (169u, none),
    R_MICROMIPS_TLS_TPREL_LO16  (170u, none),
    R_MICROMIPS_GPREL7_S2       (172u, none),
    R_MICROMIPS_PC23_S2         (173u, none),
    R_MICROMIPS_PC21_S2         (174u, none),
    R_MICROMIPS_PC26_S2         (175u, none),
    R_MICROMIPS_PC18_S3         (176u, none),
    R_MICROMIPS_PC19_S2         (177u, none),
    R_MIPS_PC32                 (248u, none),
    R_MIPS_EH                   (249u, none),
    R_MIPS_GNU_REL16_S2         (250u, none),
    R_MIPS_GNU_VTINHERIT        (253u, none),
    R_MIPS_GNU_VTENTRY          (254u, none);

    companion object {
        enum class Field() {
            none,

            tWord32,

            tArg26,
            vHalf16,
            tHi16,
            tLo16,
            ttvHi16,
            ttvLo16,
            vRel16,
            vLit16,
            vPC16
        }

        fun getNameById(id: UInt): String {
            val st = find<MipsSegmentType> { it.id == id }
            return if (st != null) st.name else "Unknown relocation type id ${id.hex8}"
        }

        fun ULong.mipsRelocation(onFail: (ULong) -> MipsRelocationType) = values().find { this == it.id } ?: onFail(this)
        val ULong.mipsRelocation get() = mipsRelocation { throw EBadRelocation("Unknown relocation type $this") }
    }

}