package ru.inforion.lab403.kopycat.cores.arm.enums

/**
 * Created by r.valitov on 16.01.18
 */

enum class Flags(val msb: Int, val lsb: Int = msb) {
    V(28),
    C(29),
    Z(30),
    N(31)
}