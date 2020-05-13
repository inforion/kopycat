package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.immediate

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class TEQi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val setFlags: Boolean,
           val rd: ARMRegister,
           val rn: ARMRegister,
           val imm32: Immediate<AARMCore>,
           val carry: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn, imm32) {

    override val mnem = "TEQ$mcnd"

    private var result = ARMVariable(Datatype.DWORD)

    override fun execute() {
        result.xor(core, rn, imm32)
        FlagProcessor.processLogicFlag(core, result, carry)
    }
}