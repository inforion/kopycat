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

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * LUI rt, immediate
 *
 * To load a constant into the upper half of a word
 *
 * The 16-bit immediate is shifted left 16 bits and concatenated with 16 bits of low-order zeros.
 * The 32-bit result is placed into GPR rt.
 */
class lui(
        core: MipsCore,
        data: Long,
        rt: MipsRegister,
        imm: MipsImmediate) : RtImmInsn(core, data, Type.VOID, rt, imm) {

    override val mnem = "lui"

    override fun execute() {
        vrt = imm.zext shl 16
    }
}