package ru.inforion.lab403.kopycat.cores.ppc.enums


 
enum class eTCR(val bit: Int) {

    WPHigh(31),     // Watchdog Timer Period
    WPLow(30),
    WRCHigh(29),    // Watchdog Timer Reset Control
    WRCLow(28),
    WIE(27),        // Watchdog Timer Interrupt Enable
    DIE(26),        // Decrementer Interrupt Enable
    FPHigh(25),     // Fixed-Interval Timer Period
    FPLow(24),
    FIE(23),        // Fixed-Interval Timer Interrupt Enable
    ARE(22),        // Auto-Reload Enable
    IMPDEP(21);     // Implementation-dependent

    companion object {
        val WP = WPHigh.bit..WPLow.bit
        val WRC = WRCHigh.bit..WRCLow.bit
        val FP = FPHigh.bit..FPLow.bit
    }

}