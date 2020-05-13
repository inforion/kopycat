package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded

enum class eOEA_Embedded(val id: Int) {

    // Interrupt registers

    // Interrupt vector prefix
    IVPR(63),

    // Process ID register 0
    PID0(48),

    // Decrementer auto-reload register
    DECAR(54),

    // Timer status register
    TSR(336),

    // Timer control
    TCR(340);


}