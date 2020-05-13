package ru.inforion.lab403.kopycat.gdbstub


enum class GDB_BPT(val code: Int) {
    SOFTWARE(0),
    HARDWARE(1),
    WRITE(2),
    READ(3),
    ACCESS(4)
}