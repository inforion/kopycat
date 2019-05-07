package ru.inforion.lab403.kopycat.cores.mips.enums

/**
 * Created by batman on 16/06/17.
 */
enum class IntCtl(val range: IntRange) {
    IPTI(31..29),
    IPPCI(28..26),
    IPFDC(25..23),
    MCU_ASE(22..14),
    VS(9..5)
}