package ru.inforion.lab403.kopycat.cores.arm.enums

/**
 * Created by r.valitov on 16.01.18
 */

enum class StatusReg(val msb: Int, val lsb: Int = msb) {
    M(4,0),
    T(5),
    F(6),
    I(7),
    A(8),
    E(9),
    IT1(15, 10),
    GE(19, 16),
    J(24),
    IT2(26, 25),
    Q(27),
    V(28),
    C(29),
    Z(30),
    N(31)
}