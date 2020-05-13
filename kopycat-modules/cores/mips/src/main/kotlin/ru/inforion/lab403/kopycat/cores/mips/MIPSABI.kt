package ru.inforion.lab403.kopycat.cores.mips

import ru.inforion.lab403.common.extensions.UNDEF
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.cores.mips.enums.eGPR
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore




class MIPSABI(core: MipsCore, heap: LongRange, stack: LongRange, bigEndian: Boolean):
        ABI<MipsCore>(core, heap, stack, bigEndian) {
    override fun gpr(index: Int): ARegister<MipsCore> = GPR(index)
    override fun createCpuContext() = MIPSContext(core.cpu)
    override val ssr = UNDEF
    override val sp = GPR(eGPR.SP.id)
    override val ra = GPR(eGPR.RA.id)
    override val v0 = GPR(eGPR.V0.id)
    override val argl = listOf(
            GPR(eGPR.A0.id),
            GPR(eGPR.A1.id),
            GPR(eGPR.A2.id),
            GPR(eGPR.A3.id),
            GPR(eGPR.T0.id),
            GPR(eGPR.T1.id))
}