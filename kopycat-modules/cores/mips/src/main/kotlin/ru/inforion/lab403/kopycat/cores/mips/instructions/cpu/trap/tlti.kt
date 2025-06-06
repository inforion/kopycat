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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.long
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * TLTI rs, immediate
 *
 * To compare a GPR to a constant and do a conditional trap
 *
 * Compare the contents of GPR rs and the 16-bit signed immediate as signed integers; if GPR rs is less than
 * immediate, then take a Trap exception.
 */
class tlti(core: MipsCore,
           data: ULong,
           rs: MipsRegister,
           imm: MipsImmediate) : RsImmInsn(core, data, Type.VOID, rs, imm) {

//    override val isSigned: Boolean = true
//    override val construct = ::tlti
    override val mnem = "tlti"

    override fun execute() {
        // Compare as signed integers
        if (core.is32bit) {
            if (vrs.int < imm.ssext.int) throw MipsHardwareException.TR(core.pc)
        } else {
            if (vrs.long < imm.ssext) throw MipsHardwareException.TR(core.pc)
        }
    }

}