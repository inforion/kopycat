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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.long
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 *
 * SLTI rt, rs, immediate
 */
class slti(
        core: MipsCore,
        data: ULong,
        rt: MipsRegister,
        rs: MipsRegister,
        imm: MipsImmediate) : RtRsImmInsn(core, data, Type.VOID, rt, rs, imm)  {

//    override val isSigned = true
    override val mnem = "slti"

    override fun execute() {
        // ancient bug awaken here
        // 50026BC4 slti   $v0, $s2, 50 ; WTF??? 500267B4 addiu  $s2, $zero, -1
        vrt = if (core.is32bit) (vrs.int < imm.ssext.int).ulong else (vrs.long < imm.ssext).ulong
    }
}