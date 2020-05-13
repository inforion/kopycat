package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl



enum class eMAS3(val bit: Int) {

    RPNLHigh(31),       // Real page number
    RPNLLow(12),

    //11..10 (52..53 in PPC notation) - reserved

    U0(9),              // User bits
    U1(8),
    U2(7),
    U3(6),

    // Permissions bits
    UX(5),              // User execute
    SX(4),              // Supervisor execute
    UW(3),              // User write
    SW(2),              // Supervisor write
    UR(1),              // User read
    SR(0);              // Supervisor read

    companion object {
        val RPNL = RPNLHigh.bit..RPNLLow.bit
    }

}