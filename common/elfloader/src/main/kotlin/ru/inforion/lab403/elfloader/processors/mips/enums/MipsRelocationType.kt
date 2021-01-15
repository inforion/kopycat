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


 
enum class MipsRelocationType(val id : Int) {
    R_MIPS_NONE                 (0 ),
    R_MIPS_16                   (1 ),
    R_MIPS_32                   (2 ),
    R_MIPS_ADD                  (2 ),
    R_MIPS_REL32               (3 ),
    R_MIPS_REL                  (3 ),
    R_MIPS_26                   (4 ),
    R_MIPS_HI16                 (5 ),
    R_MIPS_LO16                 (6 ),
    R_MIPS_GPREL16              (7 ),
    R_MIPS_GPREL                (7 ),
    R_MIPS_LITERAL              (8 ),
    R_MIPS_GOT16                (9 ),
    R_MIPS_GOT                  (9 ),
    R_MIPS_PC16                 (10 ),
    R_MIPS_CALL16               (11 ),
    R_MIPS_CALL                 (11 ),
    R_MIPS_GPREL32              (12 ),
    R_MIPS_SHIFT5               (16 ),
    R_MIPS_SHIFT6               (17 ),
    R_MIPS_64                   (18 ),
    R_MIPS_GOT_DISP             (19 ),
    R_MIPS_GOT_PAGE             (20 ),
    R_MIPS_GOT_OFST             (21 ),
    R_MIPS_GOT_HI16             (22 ),
    R_MIPS_GOT_LO16             (23 ),
    R_MIPS_SUB                  (24 ),
    R_MIPS_INSERT_A             (25 ),
    R_MIPS_INSERT_B             (26 ),
    R_MIPS_DELETE               (27 ),
    R_MIPS_HIGHER               (28 ),
    R_MIPS_HIGHEST              (29 ),
    R_MIPS_CALL_HI16            (30 ),
    R_MIPS_CALL_LO16            (31 ),
    R_MIPS_SCN_DISP             (32 ),
    R_MIPS_REL16                (33 ),
    R_MIPS_ADD_IMMEDIATE        (34 ),
    R_MIPS_PJUMP                (35 ),
    R_MIPS_RELGOT               (36 ),
    R_MIPS_JALR                 (37 ),
    R_MIPS_TLS_DTPMOD32         (38 ),
    R_MIPS_TLS_DTPREL32         (39 ),
    R_MIPS_TLS_DTPMOD64         (40 ),
    R_MIPS_TLS_DTPREL64         (41 ),
    R_MIPS_TLS_GD               (42 ),
    R_MIPS_TLS_LDM              (43 ),
    R_MIPS_TLS_DTPREL_HI16      (44 ),
    R_MIPS_TLS_DTPREL_LO16      (45 ),
    R_MIPS_TLS_GOTTPREL         (46 ),
    R_MIPS_TLS_TPREL32          (47 ),
    R_MIPS_TLS_TPREL64          (48 ),
    R_MIPS_TLS_TPREL_HI16       (49 ),
    R_MIPS_TLS_TPREL_LO16       (50 ),
    R_MIPS_GLOB_DAT             (51 ),
    R_MIPS_PC10                 (52 ),  /* (obsolete) */
    R_MIPS_PC21_S2              (60 ),
    R_MIPS_PC26_S2              (61 ),
    R_MIPS_PC18_S3              (62 ),
    R_MIPS_PC19_S2              (63 ),
    R_MIPS_PCHI16               (64 ),
    R_MIPS_PCLO16               (65 ),
    R_MIPS16_26                 (100),
    R_MIPS16_GPREL              (101),
    R_MIPS16_GOT16              (102),
    R_MIPS16_CALL16             (103),
    R_MIPS16_HI16               (104),
    R_MIPS16_LO16               (105),
    R_MIPS16_TLS_GD             (106),
    R_MIPS16_TLS_LDM            (107),
    R_MIPS16_TLS_DTPREL_HI16    (108),
    R_MIPS16_TLS_DTPREL_LO16    (109),
    R_MIPS16_TLS_GOTTPREL       (110),
    R_MIPS16_TLS_TPREL_HI16     (111),
    R_MIPS16_TLS_TPREL_LO16     (112),
    R_MIPS16_PC16_S1            (113),
    R_MIPS_COPY                 (126),
    R_MIPS_JUMP_SLOT            (127),
    R_MICROMIPS_26_S1           (133),
    R_MICROMIPS_HI16            (134),
    R_MICROMIPS_LO16            (135),
    R_MICROMIPS_GPREL16         (136),
    R_MICROMIPS_LITERAL         (137),
    R_MICROMIPS_GOT16           (138),
    R_MICROMIPS_PC7_S1          (139),
    R_MICROMIPS_PC10_S1         (140),
    R_MICROMIPS_PC16_S1         (141),
    R_MICROMIPS_CALL16          (142),
    R_MICROMIPS_GOT_DISP        (145),
    R_MICROMIPS_GOT_PAGE        (146),
    R_MICROMIPS_GOT_OFST        (147),
    R_MICROMIPS_GOT_HI16        (148),
    R_MICROMIPS_GOT_LO16        (149),
    R_MICROMIPS_SUB             (150),
    R_MICROMIPS_HIGHER          (151),
    R_MICROMIPS_HIGHEST         (152),
    R_MICROMIPS_CALL_HI16       (153),
    R_MICROMIPS_CALL_LO16       (154),
    R_MICROMIPS_SCN_DISP        (155),
    R_MICROMIPS_JALR            (156),
    R_MICROMIPS_HI0_LO16        (157),
    R_MICROMIPS_PCHI16          (158),
    R_MICROMIPS_PCLO16          (159),
    R_MICROMIPS_TLS_GD          (162),
    R_MICROMIPS_TLS_LDM         (163),
    R_MICROMIPS_TLS_DTPREL_HI16 (164),
    R_MICROMIPS_TLS_DTPREL_LO16 (165),
    R_MICROMIPS_TLS_GOTTPREL    (166),
    R_MICROMIPS_TLS_TPREL_HI16  (169),
    R_MICROMIPS_TLS_TPREL_LO16  (170),
    R_MICROMIPS_GPREL7_S2       (172),
    R_MICROMIPS_PC23_S2         (173),
    R_MICROMIPS_PC21_S2         (174),
    R_MICROMIPS_PC26_S2         (175),
    R_MICROMIPS_PC18_S3         (176),
    R_MICROMIPS_PC19_S2         (177),
    R_MIPS_PC32                 (248),
    R_MIPS_EH                   (249),
    R_MIPS_GNU_REL16_S2         (250),
    R_MIPS_GNU_VTINHERIT        (253),
    R_MIPS_GNU_VTENTRY          (254);

    companion object {
        fun getNameById(id: Int): String {
            val st = find<MipsSegmentType> { it.id == id }
            return if (st != null) st.name else "Unknown relocation type id ${id.hex8}"
        }
    }

}