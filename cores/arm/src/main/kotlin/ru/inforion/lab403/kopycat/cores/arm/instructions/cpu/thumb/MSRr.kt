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

/**
 * Created by r.valitov on 25.01.18
 */

class MSRr(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rn: ARMRegister,
          private val SYSm: Long):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn) {
    override val mnem = "MSR$mcnd"

    val result = ARMVariable(Datatype.WORD)
    override fun execute() {
        when(SYSm[7..3]) {
            0b00000L -> {
                if(SYSm[2] == 0L)
                    core.cpu.sregs.apsr = rn.value(core)[31..27] shl 27
            }
            0b00001L -> {
                if(core.cpu.CurrentModeIsPrivileged()) {
                    when(SYSm[2..0]) {
                        0b000L -> {
                            core.cpu.regs.spMain = rn.value(core)[31..2] shl 2
                        }
                        0b001L -> {
                            core.cpu.regs.spProcess = rn.value(core)[31..2] shl 2
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
                            core.cpu.spr.pm = rn.value(core)[0] == 1L
                        }
                        0b100L -> {
                            core.cpu.spr.npriv = rn.value(core)[0] == 1L
                            if(core.cpu.CurrentMode() == Mode.Thread)
                                core.cpu.spr.spsel = rn.value(core)[1] == 1L
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