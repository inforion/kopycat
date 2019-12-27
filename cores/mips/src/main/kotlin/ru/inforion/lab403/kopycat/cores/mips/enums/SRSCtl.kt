package ru.inforion.lab403.kopycat.cores.mips.enums

/**
 * Created by a.gladkikh on 16/06/17.
 */
enum class SRSCtl(val range: IntRange) {
    HSS(29..26),
    EICSS(21..18),
    ESS(15..12),
    PSS(9..6),
    CSS(3..0)
}