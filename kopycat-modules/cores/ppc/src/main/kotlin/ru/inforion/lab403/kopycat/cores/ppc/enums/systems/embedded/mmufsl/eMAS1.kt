package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl



enum class eMAS1(val bit: Int) {

    V(31),          // TLB valid bit

    IPROT(30),      // Invalid protect

    TIDHigh(29),    // Translation identity
    TIDLow(16),

    //15..13 (48..50 in PPC notation) - reserved

    TS(12),         // Translation space

    TSIZEHigh(11),  // Translation size
    TSIZELow(8);

    //7..0 (56..63 in PPC notation) - reserved

    companion object {
        val TID = TIDHigh.bit..TIDLow.bit
        val TSIZE = TSIZEHigh.bit..TSIZELow.bit
    }
}