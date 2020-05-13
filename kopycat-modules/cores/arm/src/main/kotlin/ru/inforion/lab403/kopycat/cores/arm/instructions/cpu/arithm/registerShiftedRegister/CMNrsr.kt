package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.registerShiftedRegister

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.AddWithCarry
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class CMNrsr(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             var setFlags: Boolean,
             var rd: ARMRegister,
             var rn: ARMRegister,
             var rm: ARMRegister,
             var rs: ARMRegister,
             var shiftT: SRType):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn, rm) {

    override val mnem = "CMN$mcnd"

    override fun execute() {
        val shiftN = rs.value(core)[7..0].asInt
        val shifted = Shift(rm.value(core), rm.dtyp.bits, shiftT, shiftN, core.cpu.flags.c.asInt)
        val (result, carry, overflow) = AddWithCarry(rn.dtyp.bits, rn.value(core), shifted, 0)
        FlagProcessor.processArithmFlag(core, result, carry, overflow)
    }
}