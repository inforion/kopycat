package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.device.TestGPR as eGPR

abstract class TestRegister(reg: Int, access: AOperand.Access = ANY)
    : ARegister<TestCore>(reg, access, DWORD) {
    companion object {
        fun gpr(id: Int): TestRegister = when(id) {
                eGPR.r0.id -> GPR.r0
                eGPR.r1.id -> GPR.r1
                eGPR.pc.id -> GPR.pc
                else -> throw GeneralException("Unknown GPR id = $id")
        }
    }

    sealed class GPR(id: Int) : TestRegister(id) {
        override fun value(core: TestCore, data: Long) = core.cpu.regs.writeIntern(reg, data)
        override fun value(core: TestCore): Long = core.cpu.regs.readIntern(reg)
        object r0 : GPR(eGPR.r0.id)
        object r1 : GPR(eGPR.r1.id)
        object pc : GPR(eGPR.pc.id)
    }

    override fun toString(): String = eGPR.from(reg).name.toLowerCase()
}
