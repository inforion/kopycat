package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR as eGPR

class ThumbPopDecoder(
        cpu: AARMCore,
        private val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                rn: ARMRegister,
                unalignedAllowed: Boolean,
                registers: ARMRegisterList,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val P = data[8].asInt
        val registerList = data[7..0].insert(P, 15)
        val unalignedAllowed = false
        val registers = ARMRegisterList(core, data, registerList)

        if(registers.bitCount < 1) throw Unpredictable
        // TODO: UGLY
        if(registers.contains(GPRBank.Operand(eGPR.PC.id)) && core.cpu.InITBlock() && !core.cpu.LastInITBlock()) throw Unpredictable

        // TODO: UGLY
        return constructor(core, data, Condition.AL, GPRBank.Operand(eGPR.SPMain.id), unalignedAllowed, registers, 2)
    }
}