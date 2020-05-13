package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.loadstore

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload.POPmr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore.PUSHmr
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.133, A8.8.132
class PushPopMultipleRegistersDecoder(
        cpu: AARMCore,
        val isLoad: Boolean,
        private val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                registers: ARMRegisterList,
                unalignedAllowed: Boolean,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {

    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
        val registers = ARMRegisterList(core, data, data[15..0])
        val unalignedAllowed = false
        if (isLoad && registers.rbits[GPR.SPMain.id] == 1L && core.cpu.ArchVersion() >= 7)
                throw ARMHardwareException.Unpredictable
        return constructor(core, data, cond, registers, unalignedAllowed, 4)
    }
}