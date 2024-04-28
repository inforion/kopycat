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
package ru.inforion.lab403.elfloader2.processors.x86_64

import ru.inforion.lab403.elfloader2.exceptions.EBadRelocation

enum class x86_64RelocationType(val id : ULong, val size: Int) {
    R_X86_64_NONE(0u, 0),
    R_X86_64_64(1u, 8),
    R_X86_64_PC32(2u, 4),
    R_X86_64_GOT32(3u, 4),
    R_X86_64_PLT32(4u, 4),
    R_X86_64_COPY(5u, 0),
    R_X86_64_GLOB_DAT(6u, 8),
    R_X86_64_JUMP_SLOT(7u, 8),
    R_X86_64_RELATIVE(8u, 8),
    R_X86_64_GOTPCREL(9u, 4),
    R_X86_64_32(10u, 4),
    R_X86_64_32S(11u, 4),
    R_X86_64_16(12u, 2),
    R_X86_64_PC16(13u, 2),
    R_X86_64_8(14u, 1),
    R_X86_64_PC8(15u, 1),
    R_X86_64_DTPMOD64(16u, 8),
    R_X86_64_DTPOFF64(17u, 8),
    R_X86_64_TPOFF64(18u, 8),
    R_X86_64_TLSGD(19u, 4),
    R_X86_64_TLSLD(20u, 4),
    R_X86_64_DTPOFF32(21u, 4),
    R_X86_64_GOTTPOFF(22u, 4),
    R_X86_64_TPOFF32(23u, 4),
    R_X86_64_PC64(24u, 8),
    R_X86_64_GOTOFF64(25u, 8),
    R_X86_64_GOTPC32(26u, 4),
    R_X86_64_SIZE32(32u, 4),
    R_X86_64_SIZE64(33u, 8),
    R_X86_64_GOTPC32_TLSDESC(34u, 4),
    R_X86_64_TLSDESC_CALL(35u, 0),
    R_X86_64_TLSDESC(36u, 16),
    R_X86_64_IRELATIVE(37u, 8),
    R_X86_64_GOTPCRELX(41u, 4),
    R_X86_64_REX_GOTPCRELX(42u, 4);

    companion object {
        fun ULong.x86_64relocation(onFail: (ULong) -> x86_64RelocationType) = values().find { this == it.id } ?: onFail(this)
        val ULong.x86_64relocation get() = x86_64relocation { throw EBadRelocation("Unknown relocation type $this") }
    }
}