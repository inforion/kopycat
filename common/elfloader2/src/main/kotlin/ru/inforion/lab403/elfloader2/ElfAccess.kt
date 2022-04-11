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
package ru.inforion.lab403.elfloader2

import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.elfloader2.enums.ElfProgramHeaderFlag.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderFlag.*


class ElfAccess(
    val isRead: Boolean,
    val isWrite: Boolean,
    val isExec: Boolean,
    val isLoad: Boolean
) {

    companion object {
        val ULong.toElfAccessFrSection: ElfAccess get() {
            val write = (this and SHF_WRITE.mask).truth
            val exec = (this and SHF_EXECINSTR.mask).truth
            val load = (this and SHF_ALLOC.mask).truth
            return ElfAccess(true, write, exec, load)
        }

        val UInt.toElfAccessFrProgram: ElfAccess get() {
            val read = (this and PF_R.mask).truth
            val write = (this and PF_W.mask).truth
            val exec = (this and PF_X.mask).truth
            return ElfAccess(read, write, exec, true)
        }
        val virtual = ElfAccess(true, false, true, true)
    }

    override fun toString(): String {
        val R = if (isRead) "R" else "-"
        val W = if (isWrite) "W" else "-"
        val X = if (isExec) "X" else "-"
        val L = if (isLoad) "L" else "-"
        return "$R$W$X$L"
    }
}