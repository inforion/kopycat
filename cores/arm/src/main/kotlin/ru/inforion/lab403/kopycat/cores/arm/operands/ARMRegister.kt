package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.kopycat.cores.arm.enums.Regtype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR as eGPR
import ru.inforion.lab403.kopycat.cores.arm.enums.PSR as ePSR
import ru.inforion.lab403.kopycat.cores.arm.enums.SPR as eSPR

/**
 * Created by a.gladkikh on 13.01.18.
 */

abstract class ARMRegister(
        reg: Int,
        private val rtyp: Regtype,
        access: AOperand.Access = ANY
) : ARegister<AARMCore>(reg, access, DWORD) {

    override fun toString(): String = when (rtyp) {
        Regtype.GPR -> first<eGPR> { it.id == reg }.name
        Regtype.PSR -> first<ePSR> { it.id == reg }.name
        Regtype.SPR -> first<eSPR> { it.id == reg }.name
    }.toLowerCase()

    companion object {
        fun gpr(e: eGPR) = gpr(e.id)

        fun gpr(id: Int): ARMRegister = when (id) {
            eGPR.R0.id  -> GPR.R0
            eGPR.R1.id  -> GPR.R1
            eGPR.R2.id  -> GPR.R2
            eGPR.R3.id  -> GPR.R3
            eGPR.R4.id  -> GPR.R4
            eGPR.R5.id  -> GPR.R5
            eGPR.R6.id  -> GPR.R6
            eGPR.R7.id  -> GPR.R7
            eGPR.R8.id  -> GPR.R8
            eGPR.R9.id  -> GPR.R9
            eGPR.R10.id -> GPR.R10
            eGPR.R11.id -> GPR.R11
            eGPR.R12.id -> GPR.R12
            eGPR.SPMain.id -> GPR.SPMain
            eGPR.SPProcess.id -> GPR.SPProcess
            eGPR.LR.id -> GPR.LR
            eGPR.PC.id -> GPR.PC
            else -> throw GeneralException("Unknown GPR id = $id")
        }

        fun psr(id: Int = 0): ARMRegister = when (id) {
            ePSR.APSR.id -> PSR.APSR
            ePSR.IPSR.id -> PSR.IPSR
            ePSR.EPSR.id -> PSR.EPSR
            ePSR.CPSR.id -> PSR.CPSR
            ePSR.SPSR.id -> PSR.SPSR
            else -> throw GeneralException("Unknown PSR id = $id")
        }

        fun spr(id: Int = 0): ARMRegister = when (id) {
            eSPR.PRIMASK.id -> SPR.PRIMASK
            eSPR.CONTROL.id -> SPR.CONTROL
            else -> throw GeneralException("Unknown PSR id = $id")
        }
    }

    sealed class GPR(id: Int) : ARMRegister(id, Regtype.GPR) {
        final override fun value(core: AARMCore, data: Long) =
                if(reg == eGPR.SPMain.id) {
                    val sp = core.cpu.StackPointerSelect()
                    core.cpu.regs.writeIntern(sp, data)
                } else core.cpu.regs.writeIntern(reg, data)

        final override fun value(core: AARMCore): Long =
                if(reg == eGPR.SPMain.id) {
                    val sp = core.cpu.StackPointerSelect()
                    core.cpu.regs.readIntern(sp)
                } else core.cpu.regs.readIntern(reg)

        object R0          : GPR(eGPR.R0.id)
        object R1          : GPR(eGPR.R1.id)
        object R2          : GPR(eGPR.R2.id)
        object R3          : GPR(eGPR.R3.id)
        object R4          : GPR(eGPR.R4.id)
        object R5          : GPR(eGPR.R5.id)
        object R6          : GPR(eGPR.R6.id)
        object R7          : GPR(eGPR.R7.id)
        object R8          : GPR(eGPR.R8.id)
        object R9          : GPR(eGPR.R9.id)
        object R10         : GPR(eGPR.R10.id)
        object R11         : GPR(eGPR.R11.id)
        object R12         : GPR(eGPR.R12.id)
        object SPMain      : GPR(eGPR.SPMain.id)
        object SPProcess   : GPR(eGPR.SPProcess.id)
        object LR          : GPR(eGPR.LR.id)
        object PC          : GPR(eGPR.PC.id)
    }

    sealed class PSR(id: Int) : ARMRegister(id, Regtype.PSR) {
        final override fun value(core: AARMCore, data: Long) = core.cpu.sregs.writeIntern(reg, data)
        final override fun value(core: AARMCore): Long = core.cpu.sregs.readIntern(reg)

        object APSR : PSR(ePSR.APSR.id)
        object IPSR : PSR(ePSR.IPSR.id)
        object EPSR : PSR(ePSR.EPSR.id)
        object CPSR : PSR(ePSR.CPSR.id)
        object SPSR : PSR(ePSR.SPSR.id)
    }

    sealed class SPR(id: Int) : ARMRegister(id, Regtype.SPR) {
        final override fun value(core: AARMCore, data: Long) = core.cpu.spr.writeIntern(reg, data)
        final override fun value(core: AARMCore): Long = core.cpu.spr.readIntern(reg)

        object PRIMASK : SPR(ePSR.APSR.id)
        object CONTROL : SPR(ePSR.IPSR.id)
    }
}