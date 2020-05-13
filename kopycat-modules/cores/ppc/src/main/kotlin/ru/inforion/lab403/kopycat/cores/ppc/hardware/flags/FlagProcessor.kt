package ru.inforion.lab403.kopycat.cores.ppc.flags

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import ru.inforion.lab403.kopycat.cores.base.operands.Variable


@Suppress("NOTHING_TO_INLINE")
object FlagProcessor {

    inline fun processCR0(core: PPCCore, result : Variable<PPCCore>) {
        core.cpu.crBits.CR0.field = 0
        when {
            result.isZero(core) -> core.cpu.crBits.CR0.EQ = true
            result.isNegative(core) -> core.cpu.crBits.CR0.LT = true
            else -> core.cpu.crBits.CR0.GT = true
        }
        core.cpu.crBits.CR0.SO = core.cpu.xerBits.SO
    }

    inline fun processCarry(core: PPCCore, result: Variable<PPCCore>) {
        core.cpu.xerBits.CA = result.isCarry(core)
    }

    inline fun processCarryAlgShift(core: PPCCore, data: Variable<PPCCore>, n: Int) {
        core.cpu.xerBits.CA = data.isCarry(core) && (data.value(core) mask n != 0L) //Won't work on 64 bit system
    }

    inline fun processOverflow(core: PPCCore, result: Variable<PPCCore>) {
        //result.isOverflow() - no works
        core.cpu.xerBits.OV = result.bit(core, result.msb(core)).toBool() xor result.isCarry(core)
        if (core.cpu.xerBits.OV)
            core.cpu.xerBits.SO = true
    }

    inline fun processOverflowDiv(core: PPCCore, ovr: Boolean) {
        core.cpu.xerBits.OV = ovr
    }

}