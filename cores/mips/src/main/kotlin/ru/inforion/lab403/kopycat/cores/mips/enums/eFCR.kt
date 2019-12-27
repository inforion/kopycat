package ru.inforion.lab403.kopycat.cores.mips.enums

/**
 * Created by a.gladkikh on 05/06/16.
 */
enum class eFCR(val id: Int) {
    FIR(0),
    FCCR(25),
    FEXR(26),
    FENR(28),
    FCSR(31);
}