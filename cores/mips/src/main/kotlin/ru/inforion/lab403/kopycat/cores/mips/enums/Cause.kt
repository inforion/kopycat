package ru.inforion.lab403.kopycat.cores.mips.enums

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get

/**
 * Created by batman on 15/06/16.
 */
enum class Cause(val pos: Int) {

    EXC_L(2),
    EXC_H(6), // Exception code
    IP0(8),   // Request software interrupt 0
    IP1(9),   // Request software interrupt 1
    IP2(10),  // Hardware interrupt 0
    IP3(11),  // Hardware interrupt 1
    IP4(12),  // Hardware interrupt 2
    IP5(13),  // Hardware interrupt 3
    IP6(14),  // Hardware interrupt 4
    IP7(15),  // Hardware interrupt 5, timer or performance counter interrupt
    WP(22),
    IV(23),   // Use the general exception vector or a special interrupt vector
    CE_L(28),
    CE_H(29),
    TI(30),
    BD(31);

    infix fun from(value: Long): Long = value[this.pos]

    companion object {
        fun extract(value: Long): Map<Cause, Int> = Cause.values().associate { Pair(it, (it from value).asInt) }
    }
}