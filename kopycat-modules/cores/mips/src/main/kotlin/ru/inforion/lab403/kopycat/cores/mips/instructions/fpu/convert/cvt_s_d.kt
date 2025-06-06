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
package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.convert

import ru.inforion.lab403.common.extensions.float
import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.common.extensions.ieee754AsUnsigned
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.mips.instructions.FdFsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * CVT.D.(S,W,L) fd, fs
 *
 * Floating Point Convert to Double Floating Point
 */
class cvt_s_d(
        core: MipsCore,
        data: ULong,
        fd: MipsRegister,
        fs: MipsRegister
) : FdFsInsn(core, data, Type.VOID, fd, fs) {

    override val mnem = "cvt.s.d"

    override fun execute() {
        val double = dfs.ieee754()
        val single = double.float
        vfd = single.ieee754AsUnsigned().ulong_z
//        log.warning { "[%08X] $mnem $op1 = $single".format(cpu.pc) }
    }
}
