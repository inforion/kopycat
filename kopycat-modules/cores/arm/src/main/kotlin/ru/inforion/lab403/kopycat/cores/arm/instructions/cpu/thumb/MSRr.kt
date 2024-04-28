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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.Mode
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class MSRr(cpu: AARMCore,
          opcode: ULong,
          cond: Condition,
          val rn: ARMRegister,
          private val SYSm: ULong):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn) {
    override val mnem = "MSR$mcnd"

    val result = ARMVariable(Datatype.WORD)
    override fun execute() {
        when(SYSm[7..3]) {
            0b00000uL -> {
                if(SYSm[2] == 0uL)
                    core.cpu.sregs.apsr.value = rn.value(core)[31..27] shl 27
            }
            0b00001uL -> {
                if(core.cpu.CurrentModeIsPrivileged()) {
                    when(SYSm[2..0]) {
                        0b000uL -> {
                            core.cpu.regs.sp.main = rn.value(core)[31..2] shl 2
                        }
                        0b001uL -> {
                            core.cpu.regs.sp.process = rn.value(core)[31..2] shl 2
                        }
                        else -> {
                            throw Unpredictable
                        }
                    }
                }
            }
            0b00010uL -> {
                if(core.cpu.CurrentModeIsPrivileged()) {
                    when(SYSm[2..0]) {
                        0b000uL -> {
                            core.cpu.spr.primask.pm = rn.value(core)[0] == 1uL
                        }
                        0b100uL -> {
                            core.cpu.spr.control.npriv = rn.value(core)[0] == 1uL
                            if(core.cpu.CurrentMode() == Mode.Thread)
                                core.cpu.spr.control.spsel = rn.value(core)[1] == 1uL
                        }
                        else -> {
                            throw Unpredictable
                        }
                    }
                }
            }
            else -> {
                throw Unpredictable
            }
        }
    }
}