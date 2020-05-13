package ru.inforion.lab403.kopycat.cores.base.enums

enum class Datatype(val id: Int, val bits: Int, val bytes: Int, val msb: Int, val lsb: Int) {
    UNKNOWN(0, 0, 0, 0, 0),

    BYTE(0, 8, 1, 7, 0),
    WORD(1, 16, 2, 15, 0),
    DWORD(2, 32, 4, 31, 0),
    QWORD(3, 64, 8, 63, 0),

    TRIBYTE(20, 24, 3, 23, 0),
    FWORD(21, 48, 6, 47, 0),   // LGDT, LIDT of x86 processor
    FPU80(22, 80, 10, 79, 0),  // FRU of x86 processor

    BYTES5(23, 40, 5, 39, 0),
    BYTES7(25, 56, 7, 55, 0);
}