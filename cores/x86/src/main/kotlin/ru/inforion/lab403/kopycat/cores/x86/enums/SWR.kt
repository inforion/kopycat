package ru.inforion.lab403.kopycat.cores.x86.enums

/**
 * Created by davydov_vn on 09.02.18.
 */

enum class SWR(val bit: Int) {
    IE(0),
    DE(1),
    XE(2),
    OE(3),
    UE(4),
    PE(5),
    SF(6),
    ES(7),
    C0(8),
    C1(9),
    C2(10),
    TOP(11),
    C3(14),
    B(15),
}