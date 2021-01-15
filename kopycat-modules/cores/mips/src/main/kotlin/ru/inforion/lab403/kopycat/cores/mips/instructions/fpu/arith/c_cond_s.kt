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
package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.arith

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.kopycat.cores.mips.enums.COND
import ru.inforion.lab403.kopycat.cores.mips.instructions.CcFsFtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class c_cond_s(
        core: MipsCore,
        data: Long,
        fs: MipsRegister,
        ft: MipsRegister,
        cc: MipsImmediate) : CcFsFtInsn(core, data, Type.VOID, fs, ft, cc) {

    override val mnem get() = "c.$cond.s".toLowerCase()

    override fun execute() {
//        log.warning { "[%08X] $mnem $op1, $op3 [$fs $cond $vft]".format(cpu.pc) }
        when (cond) {
            COND.F -> { vcc = false }
            COND.UN -> { vcc = false }
            COND.EQ -> { vcc = vfs.ieee754() == vft.ieee754() }
            COND.UEQ -> { vcc = vfs.ieee754() == vft.ieee754() }
            COND.OLT -> { vcc = vfs.ieee754() < vft.ieee754() }
            COND.ULT -> { vcc = vfs.ieee754() < vft.ieee754() }
            COND.OLE -> { vcc = vfs.ieee754() <= vft.ieee754() }
            // FIXME: WTF???
            COND.ULE -> { vfs.ieee754() <= vft.ieee754() }
            COND.SF -> { vcc = false }
            COND.NGLE -> { vcc = false }
            COND.SEQ -> { vcc = vfs.ieee754() == vft.ieee754() }
            COND.NGL -> { vcc = vfs.ieee754() == vft.ieee754() }
            COND.LT -> { vcc = vfs.ieee754() < vft.ieee754() }
            COND.NGE -> { vcc = vfs.ieee754() < vft.ieee754() }
            COND.LE -> { vcc = vfs.ieee754() <= vft.ieee754() }
            COND.NGT -> { vcc = vfs.ieee754() <= vft.ieee754() }
        }
    }
}
