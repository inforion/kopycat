package ru.inforion.lab403.kopycat.cores.x86.enums

/**
 * Created by batman on 07/10/16.
 */
enum class Flags(val bit: Int) {
    CF(0),
    PF(2),
    AF(4),
    ZF(6),
    SF(7),
    TF(8),
    IF(9),
    DF(10),
    OF(11),
    IOPLL(12),
    IOPLH(13),
    NT(14),
    RF(16),
    VM(17),
    AC(18),
    VIF(19),
    VIP(20),
    ID(21)
}