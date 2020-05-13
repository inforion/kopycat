package ru.inforion.lab403.kopycat.cores.msp430.flags

import ru.inforion.lab403.kopycat.cores.msp430.enums.Condition
import ru.inforion.lab403.kopycat.cores.msp430.enums.Condition.*
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



@Suppress("NOTHING_TO_INLINE")

object FlagCondition {
    inline fun CheckCondition(core: MSP430Core, cond: Condition): Boolean = when (cond) {
        NZ -> !core.cpu.flags.z
        Z -> core.cpu.flags.z
        NC -> !core.cpu.flags.c
        C -> core.cpu.flags.c
        N -> core.cpu.flags.n
        GE -> !(core.cpu.flags.n xor core.cpu.flags.v)
        L -> core.cpu.flags.n xor core.cpu.flags.v
        MP -> true
    }
}