package ru.inforion.lab403.kopycat.cores.x86.enums


enum class CR0(val bit: Int) {
    PE(0),
    MP(1),
    EM(2),
    TS(3),
    ET(4),
    NE(5),
    WP(16),
    AM(18),
    NW(29),
    CD(30),
    PG(31);
}