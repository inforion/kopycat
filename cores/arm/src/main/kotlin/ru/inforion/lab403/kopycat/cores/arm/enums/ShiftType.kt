package ru.inforion.lab403.kopycat.cores.arm.enums

enum class ShiftType(val id: Long) {
    LSL(0),
    LSR(1),
    ASR(2),
    ROR(3),
    RRX(4),
    NONE(-1)
}