package ru.inforion.lab403.kopycat.cores.mips.enums


enum class SRSMap(val range: IntRange) {
    SSV7(31..28),
    SSV6(27..24),
    SSV5(23..20),
    SSV4(19..16),
    SSV3(15..12),
    SSV2(11..8),
    SSV1(7..4),
    SSV0(3..0)
}