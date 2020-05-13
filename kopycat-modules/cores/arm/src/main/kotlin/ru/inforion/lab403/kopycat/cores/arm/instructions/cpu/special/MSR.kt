package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.111
class MSR(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val imm32: Immediate<AARMCore>,
          mask: Int,
          write_spr: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, imm32) {
    override val mnem = "MSR$mcnd"

    val writeNZCVQ = mask[3].toBool()
    val writeG = mask[2].toBool()

    override fun execute() {
        if(writeNZCVQ){
            core.cpu.flags.n = imm32.value[31] == 1L
            core.cpu.flags.z = imm32.value[30] == 1L
            core.cpu.flags.c = imm32.value[29] == 1L
            core.cpu.flags.v = imm32.value[28] == 1L
            core.cpu.status.q = imm32.value[27] == 1L
        }
        if(writeG)
            core.cpu.status.ge = imm32.value[19..16]
    }
}