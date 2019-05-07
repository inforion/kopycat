package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition.*
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR
import ru.inforion.lab403.kopycat.cores.arm.enums.Mode
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.PC
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet

/**
 * Created by the bat on 13.01.18.
 */

abstract class AARMCPU(val arm: AARMCore, name: String) : ACPU<AARMCPU, AARMCore, AARMInstruction, GPR>(arm, name) {
    var pipelineRefillRequired = false

    override fun reg(index: Int): Long = regs[index].value(arm)
    override fun reg(index: Int, value: Long) = regs[index].value(arm, value)
    override fun count() = regs.count()

    override var pc: Long
        get() = PC.value(arm)
        set(value) = PC.value(arm, value)

    inline fun ConditionPassed(cond: Condition): Boolean = when (cond) {
        EQ -> flags.z
        NE -> !flags.z
        CS -> flags.c
        CC -> !flags.c
        MI -> flags.n
        PL -> !flags.n
        VS -> flags.v
        VC -> !flags.v
        HI -> flags.c && !flags.z
        LS -> !flags.c || flags.z
        GE -> flags.n == flags.v
        LT -> flags.n != flags.v
        GT -> !flags.z && (flags.n == flags.v)
        LE -> flags.z || (flags.n != flags.v)
        AL,
        UN -> true
    }

    fun BranchTo(address: Long) {
        pipelineRefillRequired = true
        pc = address
    }

    abstract fun BranchWritePC(address: Long)
    abstract fun BXWritePC(address: Long)
    abstract fun LoadWritePC(address: Long)
    abstract fun ALUWritePC(address: Long)
    abstract fun CurrentInstrSet(): InstructionSet
    abstract fun CurrentModeIsPrivileged(): Boolean
    abstract fun CurrentMode(): Mode
    abstract fun SelectInstrSet(target: InstructionSet)
    abstract fun StackPointerSelect(): Int

    open fun InITBlock(): Boolean = false
    open fun LastInITBlock(): Boolean = false

    fun ArchVersion(): Int = arm.version
    fun CurrentModeIsHyp(): Boolean = false
    fun CurrentModeIsUserOrSystem(): Boolean = true
    fun CurrentModeIsNotUser(): Boolean = false
    fun UnalignedSupport(): Boolean = true
    fun PCStoreValue(): Long = pc

    fun SPSRWriteByInstr(value: Long, mask: Long){

    }

    val regs = GPRBank(arm)
    val flags = APSRBank(arm)
    val sregs = PSRBank(arm)
    val spr = SPRBank(arm)
    val status = CPSRBank(arm)
}