package ru.inforion.lab403.kopycat.cores.mips.enums

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get

/**
 * Created by a.gladkikh on 15/06/16.
 */
enum class Status(val pos: Int) {
    IE(0),
    EXL(1),
    ERL(2),
    R0(3),
    UM(4),
    UX(5),
    SX(6),
    KX(7),
//    IPL_L(10),
//    IPL_H(15),
    IM0(8),
    IM1(9),
    IM2(10),
    IM3(11),
    IM4(12),
    IM5(13),
    IM6(14),
    IM7(15),
    NMI(19),
    SR(20),
    TS(21),
    BEV(22),
    PX(23),
    MX(24),
    RE(25),
    FR(26),
    RP(27),
    CU0(28),
    CU1(29),
    CU2(30),
    CU3(31);

    infix fun from(value: Long): Long = value[this.pos]

    companion object {
        fun extract(value: Long): Map<Status, Int> = values().associate { Pair(it, (it from value).asInt) }
    }
}