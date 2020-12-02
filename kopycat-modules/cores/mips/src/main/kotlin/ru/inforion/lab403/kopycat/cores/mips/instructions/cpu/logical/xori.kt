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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.logical

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * XORI rt, rs, immediate
 *
 * To do a boolbitman logical XOR with a constant
 *
 * The 16-bit immediate is zero-extended to the left and combined with the contents of GPR rs in a boolbitman
 * logical XOR operation. The result is placed into GPR rt.
 */
class xori(
        core: MipsCore,
        data: Long,
        rt: MipsRegister,
        rs: MipsRegister,
        imm: MipsImmediate) : RtRsImmInsn(core, data, Type.VOID, rt, rs, imm)  {

//    override val isSigned = true
    override val mnem = "xori"

    override fun execute() {
        vrt = vrs xor imm.zext
    }
}