package ru.inforion.lab403.kopycat.cores.arm.hardware.flags

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 19.01.18
 */

@Suppress("NOTHING_TO_INLINE")
object FlagProcessor {
    inline fun processArithmFlag(core: AARMCore, result: Long, carry: Int, overflow: Int) {
        core.cpu.flags.n = result[31] == 1L
        core.cpu.flags.z = result == 0L
        core.cpu.flags.c = carry == 1
        core.cpu.flags.v = overflow == 1
    }

    inline fun processLogicFlag(core: AARMCore, result: Variable<AARMCore>, shifterCarryOut: Boolean) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = shifterCarryOut
    }

    inline fun processMulFlag(core: AARMCore, result: Variable<AARMCore>) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
    }

    inline fun processHMulFlag(core: AARMCore, result: Long) {
        core.cpu.status.q = result != SInt(result[31..0], 32)
    }

    inline fun processHMulRegFlag(core: AARMCore, result: Long, rd: ARMRegister) {
        core.cpu.status.q = result.shr(16) != rd.ssext(core)
    }

    inline fun processSatFlag(core: AARMCore) {
        core.cpu.status.q = true
    }
}