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
package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * ADD, ADDU, AND, CLO, CLZ, DIV(rd=0), DIVU(rd=0), MADD(rd=0), MADDU(rd=0), MSUB, MSUBU,
 * MUL(rd=0), MULT(rd=0), MULTU(rd=0), NOR, OR, SLLV, SLT, SLTU, SUB, SUBU, XOR, MOVN, MOVZ
 */
class RdRsRt(
        core: MipsCore,
        val construct: (MipsCore, Long, MipsRegister, MipsRegister, MipsRegister) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        return construct(core, data,
                gpr(data[15..11].toInt()),
                gpr(data[25..21].toInt()),
                gpr(data[20..16].toInt()))
    }
}