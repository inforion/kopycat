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

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class MRS(cpu: AARMCore,
          opcode: ULong,
          cond: Condition,
          val rd: ARMRegister,
          private val SYSm: ULong):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd) {
    override val mnem = "MRS"

    val result = ARMVariable(Datatype.WORD)
    override fun execute() {
        when (SYSm[7..3]) {
            0b00000uL -> {
                if (SYSm[0] == 1uL)
                    rd.value(core, core.cpu.sregs.ipsr.value[8..0])
                if (SYSm[1] == 1uL)
                    rd.value(core, rd.value(core) clr 24)
                if (SYSm[0] == 0uL){
                    val result = rd.value(core) or (core.cpu.sregs.apsr.value[31..27] shl 27)
                    rd.value(core, result)
                }
            }
            0b00001uL -> {
                if (core.cpu.CurrentModeIsPrivileged()) {
                    when (SYSm[2..0]) {
                        0b000uL -> {
                            rd.value(core, core.cpu.regs.sp.main)
                        }
                        0b001uL -> {
                            rd.value(core, core.cpu.regs.sp.process)
                        }
                        else -> {
                            throw Unpredictable
                        }
                    }
                }
            }
            0b00010uL -> {
                if (core.cpu.CurrentModeIsPrivileged()) {
                    when (SYSm[2..0]) {
                        0b000uL -> {
                            if(core.cpu.CurrentModeIsPrivileged() && core.cpu.spr.primask.pm)
                                rd.value(core, rd.value(core) set 0)
                            else
                                rd.value(core, rd.value(core) clr 0)
                        }
                        0b100uL -> {
                            if(core.cpu.spr.control.npriv)
                                rd.value(core, rd.value(core) set 0)
                            else
                                rd.value(core, rd.value(core) clr 0)
                            if(core.cpu.spr.control.spsel)
                                rd.value(core, rd.value(core) set 1)
                            else
                                rd.value(core, rd.value(core) clr 1)
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