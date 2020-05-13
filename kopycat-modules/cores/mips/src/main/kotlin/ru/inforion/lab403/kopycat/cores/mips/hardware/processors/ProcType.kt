package ru.inforion.lab403.kopycat.cores.mips.hardware.processors



enum class ProcType(val id: Int) {
    SystemControlCop(0),
    FloatingPointCop(1),
    ImplementSpecCop(2),
    CentralProc(-1)
}