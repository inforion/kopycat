package ru.inforion.lab403.kopycat.cores.mips.enums


enum class eFCR(val id: Int) {
    FIR(0),
    FCCR(25),
    FEXR(26),
    FENR(28),
    FCSR(31);
}