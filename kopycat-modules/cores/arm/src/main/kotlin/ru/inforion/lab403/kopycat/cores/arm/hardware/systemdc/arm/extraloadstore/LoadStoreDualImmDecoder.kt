package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.extraloadstore

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class LoadStoreDualImmDecoder(
        cpu: AARMCore,
        private val isStore: Boolean,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                index: Boolean,
                add: Boolean,
                wback: Boolean,
                rn: ARMRegister,
                rt: ARMRegister,
                rt2: ARMRegister,
                imm32: Immediate<AARMCore>) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
        val rn = GPRBank.Operand(data[19..16].asInt)
        val rt = GPRBank.Operand(data[15..12].asInt)
        val rt2 = GPRBank.Operand(data[15..12].asInt + 1)
        val imm32 = ARMImmediate(data[11..8].shl(4) + data[3..0], true)
        val index = data[24] == 1L
        val add = data[23] == 1L
        val wback = data[24] == 0L || data[21] == 1L

        val pc = core.cpu.regs.pc.reg

        if(data[24] == 0L && data[21] == 1L) throw Unpredictable
        if (wback && ((rn.reg == pc && isStore) || rn.reg == rt.reg || rn.reg == rt2.reg)) throw Unpredictable
        if (rt2.reg == pc) throw Unpredictable

        return constructor(core, data, cond, index, add, wback, rn, rt, rt2, imm32)
    }
}