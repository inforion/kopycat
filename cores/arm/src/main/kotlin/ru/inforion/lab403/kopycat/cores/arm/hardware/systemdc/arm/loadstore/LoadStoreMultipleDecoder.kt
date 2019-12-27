package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.loadstore

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 31.01.18
 */
class LoadStoreMultipleDecoder(
        cpu: AARMCore,
        val isLoad: Boolean,
        private val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                wback: Boolean,
                rn: ARMRegister,
                registers: ARMRegisterList,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt }?: Condition.AL
        val rn = ARMRegister.gpr(data[19..16].asInt)
        val wback = data[21] == 1L
        val registers = ARMRegisterList(core, data, data[15..0])
        if (rn.reg == 15 || registers.bitCount < 1) throw ARMHardwareException.Unpredictable
        if(isLoad && wback && registers.rbits[rn.reg] == 1L && core.cpu.ArchVersion() >= 7) throw ARMHardwareException.Unpredictable
        return constructor(core, data, cond, wback, rn, registers, 4)
    }
}