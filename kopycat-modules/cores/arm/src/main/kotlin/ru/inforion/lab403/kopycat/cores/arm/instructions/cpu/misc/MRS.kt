package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.misc

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class MRS(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rd: ARMRegister,
          val readSPSR: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd) {
    override val mnem = "MRS$mcnd"

    val result = ARMVariable(Datatype.DWORD)
    override fun execute() {
        if(readSPSR)
            if(core.cpu.CurrentModeIsUserOrSystem())
                throw Unpredictable
            else
                rd.value(core, core.cpu.sregs.spsr.value)
//                rd.value(core, core.cpu.sregs.apsr.value and core.cpu.sregs.cpsr.value)
        else {
            // CPSR is read with execution state bits other than E masked out.
            result.value(core, core.cpu.sregs.cpsr.value and 0b11111000_11111111_00000011_11011111L)
//            rd.value(core, core.cpu.sregs.apsr.value and core.cpu.sregs.cpsr.value and 0b1111_1000_1111_1111_0000_0011_1101_1111L)
            if (!core.cpu.CurrentModeIsNotUser()) {
                // If accessed from User mode return UNKNOWN values for M, bits<4:0>,
                // and for the E, A, I, F bits, bits<9:6>
                result.bits(core, 4..0, 0L)
                result.bits(core, 9..6, 0L)
            }
            rd.value(core, result)
        }
    }
}