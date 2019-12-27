package ru.inforion.lab403.kopycat.cores.x86.enums

/**
 * Created by v.davydov on 09.02.18.
 */

enum class CWR(val bit: Int) {
    I(0),
    D(1),
    Z(2),
    O(3),
    U(4),
    P(5),
    PC(8),
    RC(10)
}