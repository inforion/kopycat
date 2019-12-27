package ru.inforion.lab403.kopycat.cores.msp430.enums

/**
 * Created by a.kemurdzhian on 13/02/18.
 */

enum class Condition(val opcode: Int, val mnem: String) {
    NZ(0b000, "nz"),
    Z(0b001, "z"),
    NC(0b010, "nc"),
    C(0b011, "c"),
    N(0b100, "n"),
    GE(0b101, "ge"),
    L(0b110, "l"),
    MP(0b111, "mp"),
}