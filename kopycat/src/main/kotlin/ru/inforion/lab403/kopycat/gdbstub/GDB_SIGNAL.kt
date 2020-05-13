package ru.inforion.lab403.kopycat.gdbstub


enum class GDB_SIGNAL(val id: Int) {
    SIGSEGV(0x0B),
    SIGSTOP(0x11),
    SIGBUS(0x0A),
    SIGINT(0x02),
    SIGSYS(0x0C),
    SIGTRAP(0x05),
}