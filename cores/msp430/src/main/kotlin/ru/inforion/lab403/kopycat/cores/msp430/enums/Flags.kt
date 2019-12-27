package ru.inforion.lab403.kopycat.cores.msp430.enums

/**
 * Created by a.kemurdzhian on 5/02/18.
 */

enum class Flags (val bit: Int) {
    C(0),
    Z(1),
    N(2),
    GIE(3),
    CPUOFF(4),
    OSCOFF(5),
    SCG0(6),
    SCG1(7),
    V(8);

}