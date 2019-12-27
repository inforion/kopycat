package ru.inforion.lab403.kopycat.cores.v850es.enums

/**
 * Created by a.gladkikh on 22/07/17.
 */
enum class CONDITION(val bits: Int) {
    BGE(0b1110),
    BGT(0b1111),
    BLE(0b0111),
    BLT(0b0110),

    BH(0b1011),
    BL(0b0001),
    BNH(0b0011),
    BNL(0b1001),

    BE(0b0010),
    BNE(0b1010),

    BC(0b0001),
    BN(0b0100),
    BNC(0b1001),
    BNV(0b1000),
    BNZ(0b1010),
    BP(0b1100),
    BR(0b0101),
    BSA(0b1101),
    BV(0b0000),
    BZ(0b0010),

    NONE(0b0000)
}