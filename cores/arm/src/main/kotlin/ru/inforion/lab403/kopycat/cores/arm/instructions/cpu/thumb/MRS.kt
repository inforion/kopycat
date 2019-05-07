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

/**
 * Created by r.valitov on 25.01.18
 */

class MRS(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rd: ARMRegister,
          private val SYSm: Long):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd) {
    override val mnem = "MRS"

    val result = ARMVariable(Datatype.WORD)
    override fun execute() {
        when(SYSm[7..3]) {
            0b00000L -> {
                if(SYSm[0] == 1L)
                    rd.value(core, core.cpu.sregs.ipsr[8..0])
                if(SYSm[1] == 1L)
                    rd.value(core, rd.value(core) clr 24)
                if(SYSm[0] == 0L){
                    val result = rd.value(core) or (core.cpu.sregs.apsr[31..27] shl 27)
                    rd.value(core, result)
                }
            }
            0b00001L -> {
                if(core.cpu.CurrentModeIsPrivileged()) {
                    when(SYSm[2..0]) {
                        0b000L -> {
                            rd.value(core, core.cpu.regs.spMain)
                        }
                        0b001L -> {
                            rd.value(core, core.cpu.regs.spProcess)
                        }
                        else -> {
                            throw Unpredictable
                        }
                    }
                }
            }
            0b00010L -> {
                if(core.cpu.CurrentModeIsPrivileged()) {
                    when(SYSm[2..0]) {
                        0b000L -> {
                            if(core.cpu.CurrentModeIsPrivileged() && core.cpu.spr.pm)
                                rd.value(core, rd.value(core) set 0)
                            else
                                rd.value(core, rd.value(core) clr 0)
                        }
                        0b100L -> {
                            if(core.cpu.spr.npriv)
                                rd.value(core, rd.value(core) set 0)
                            else
                                rd.value(core, rd.value(core) clr 0)
                            if(core.cpu.spr.spsel)
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