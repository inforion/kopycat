package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.loadstore

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload.LDREXD
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore.STREXD
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.77
class LoadExclusiveDoublewordDecoder(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {

    // A1
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL

        val rn = GPRBank.Operand(data[19..16].asInt)
        val rt = GPRBank.Operand(data[15..12].asInt)
        val rt2 = GPRBank.Operand(rt.reg + 1)

        val pc = core.cpu.regs.pc.reg

        if (rt.reg[0] == 1 || rt.reg == 0b1110 || rn.reg == pc) throw ARMHardwareException.Unpredictable

        return LDREXD(core, data, cond, rn, rt, rt2)
    }
}