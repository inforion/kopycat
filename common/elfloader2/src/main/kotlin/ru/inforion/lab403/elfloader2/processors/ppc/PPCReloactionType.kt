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
package ru.inforion.lab403.elfloader2.processors.ppc

import ru.inforion.lab403.elfloader2.exceptions.EBadRelocation
import ru.inforion.lab403.elfloader2.processors.ppc.PPCReloactionType.Companion.Field.*

enum class PPCReloactionType(val id: ULong, val field: Field) {
    R_PPC_NONE              (0u,  none),    // none
    R_PPC_ADDR32            (1u,  word32),  // S + A
    R_PPC_ADDR24            (2u,  low24v),  // (S + A) >> 2
    R_PPC_ADDR16            (3u,  half16v), // S + A
    R_PPC_ADDR16_LO         (4u,  half16),  // #lo(S + A)
    R_PPC_ADDR16_HI         (5u,  half16),  // #hi(S + A)
    R_PPC_ADDR16_HA         (6u,  half16),  // #ha(S + A)
    R_PPC_ADDR14            (7u,  low14v),  // (S + A) >> 2
    R_PPC_ADDR14_BRTAKEN    (8u,  low14v),  // (S + A) >> 2
    R_PPC_ADDR14_BRNTAKEN   (9u,  low14v),  // (S + A) >> 2
    R_PPC_REL24             (10u, low24v),  // (S + A - P) >> 2
    R_PPC_REL14             (11u, low14v),  // (S + A - P) >> 2
    R_PPC_REL14_BRTAKEN     (12u, low14v),  // (S + A - P) >> 2
    R_PPC_REL14_BRNTAKEN    (13u, low14v),  // (S + A - P) >> 2
    R_PPC_GOT16             (14u, half16v), // G + A
    R_PPC_GOT16_LO          (15u, half16),  // #lo(G + A)
    R_PPC_GOT16_HI          (16u, half16),  // #hi(G + A)
    R_PPC_GOT16_HA          (17u, half16),  // #ha(G + A)
    R_PPC_PLTREL24          (18u, low24v),  // (L + A - P) >> 2
    R_PPC_COPY              (19u, none),    // none
    R_PPC_GLOB_DAT          (20u, word32),  // S + A
    R_PPC_JMP_SLOT          (21u, none),    // see in documentation
    R_PPC_RELATIVE          (22u, word32),  // B + A
    R_PPC_LOCAL24PC         (23u, low24v),  // see in documentation
    R_PPC_UADDR32           (24u, word32),  // S + A
    R_PPC_UADDR16           (25u, half16v), // S + A
    R_PPC_REL32             (26u, word32),  // S + A - P
    R_PPC_PLT32             (27u, word32),  // L + A
    R_PPC_PLTREL32          (28u, word32),  // L + A - P
    R_PPC_PLT16_LO          (29u, half16),  // #lo(L + A)
    R_PPL_PLT16_HI          (30u, half16),  // #hi(L + A)
    R_PPC_PLT16_HA          (31u, half16),  // #ha(L + A)
    R_PPC_SDAREL16          (32u, half16v), // S + A - _SDA_BASE_
    R_PPC_SECTOFF           (33u, half16v), // R + A
    R_PPC_SECTOFF_LO        (34u, half16),  // #lo(R + A)
    R_PPC_SECTOFF_HI        (35u, half16),  // #hi(R + A)
    R_PPC_SECTOFF_HA        (36u, half16),  // #ha(R + A)
    R_PPC_ADDR30            (37u, word30);  // (S + A - P) >> 2

    companion object {
        enum class Field(val verify: Boolean = false) {
            none,
            word32,
            word30,
            low24,
            low24v(true),
            low14,
            low14v(true),
            half16,
            half16v(true),
        }

        fun ULong.ppcRelocation(onFail: (ULong) -> PPCReloactionType) = values().find { this == it.id } ?: onFail(this)
        val ULong.ppcRelocation get() = ppcRelocation { throw EBadRelocation("Unknown relocation type $this") }
    }
}