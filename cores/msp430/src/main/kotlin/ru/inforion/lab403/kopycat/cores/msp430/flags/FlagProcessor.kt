package ru.inforion.lab403.kopycat.cores.msp430.flags

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.msp430.MSP430Operand
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 13/02/18.
 */

@Suppress("NOTHING_TO_INLINE")
object FlagProcessor {

    inline fun processShiftFlag(core: MSP430Core, result : Variable<MSP430Core>, carry : Boolean) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = carry
        core.cpu.flags.v = false
    }

    inline fun processLogicalFlag(core: MSP430Core, result : Variable<MSP430Core>) {
        core.cpu.flags.n = result.msb(core).toBool()
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = result.isNotZero(core)
        core.cpu.flags.v = false
    }

    inline fun processXorFlag(core: MSP430Core, result : Variable<MSP430Core>, op1 : MSP430Operand, op2 : MSP430Operand) {
        core.cpu.flags.n = result.msb(core).toBool()
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = result.isNotZero(core)
        core.cpu.flags.v = op1.isNegative(core) and op2.isNegative(core)
    }

    inline fun processArithmFlag(core: MSP430Core, result : Variable<MSP430Core>, op1 : MSP430Operand, op2 : MSP430Operand, isSubtract: Boolean) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = result.isCarry(core)
        core.cpu.flags.v = result.isIntegerOverflow(core, op1, op2, isSubtract)
    }

    inline fun processDaddFlag(core: MSP430Core, result : Variable<MSP430Core>, carry : Boolean) {
        core.cpu.flags.n = result.msb(core).toBool()
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = carry
    }

    inline fun processSxtFlag(core: MSP430Core, result : Variable<MSP430Core>) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = result.isNotZero(core)
        core.cpu.flags.v = false
    }
}