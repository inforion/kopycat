package ru.inforion.lab403.kopycat.cores.mips.operands

import ru.inforion.lab403.kopycat.cores.mips.enums.Designation
import ru.inforion.lab403.kopycat.cores.mips.enums.eGPR
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

open class GPR(desc: eGPR) : MipsRegister<eGPR>(ProcType.CentralProc, Designation.General, desc) {
    constructor(id: Int) : this(eGPR.values()[id])

    // NOTE: for performance sake it must be final!
    final override fun value(core: MipsCore, data: Long) {
        if (reg != zero.reg)
            core.cpu.regs.writeIntern(reg, data)
    }

    final override fun value(core: MipsCore): Long = core.cpu.regs.readIntern(reg)

    object zero : GPR(eGPR.ZERO)

    object at : GPR(eGPR.AT)

    object a0 : GPR(eGPR.A0)
    object a1 : GPR(eGPR.A1)
    object a2 : GPR(eGPR.A2)
    object a3 : GPR(eGPR.A3)

    object k0 : GPR(eGPR.K0)
    object k1 : GPR(eGPR.K1)

    object v0 : GPR(eGPR.V0)
    object v1 : GPR(eGPR.V1)

    object t0 : GPR(eGPR.T0)
    object t1 : GPR(eGPR.T1)
    object t2 : GPR(eGPR.T2)
    object t3 : GPR(eGPR.T3)
    object t4 : GPR(eGPR.T4)
    object t5 : GPR(eGPR.T5)
    object t6 : GPR(eGPR.T6)
    object t7 : GPR(eGPR.T7)
    object t8 : GPR(eGPR.T8)
    object t9 : GPR(eGPR.T9)

    object s0 : GPR(eGPR.S0)
    object s1 : GPR(eGPR.S1)
    object s2 : GPR(eGPR.S2)
    object s3 : GPR(eGPR.S3)
    object s4 : GPR(eGPR.S4)
    object s5 : GPR(eGPR.S5)
    object s6 : GPR(eGPR.S6)
    object s7 : GPR(eGPR.S7)

    object ra : GPR(eGPR.RA)
    object gp : GPR(eGPR.GP)
    object fp : GPR(eGPR.FP)
    object sp : GPR(eGPR.SP)
}