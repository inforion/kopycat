package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2

enum class eHID1(val bit: Int) {

    PLL_CFGHigh(31),        // Reflected directly from the PLL_CFG input pins (read-only)
    PLL_CFGLow(26),

    //25..18 (38..46 in PPC notation) - reserved

    RFXE(17),               // Read fault exception enable

    //16 (47 in PPC notation) - reserved

    R1DPE(15),              // R1 data bus parity enable
    R2DPE(14),              // R2 data bus parity enable

    ASTME(13),              // Address bus streaming mode enable

    ABE(12),                // Address broadcast enable

    //11 (52 in PPC notation) - reserved

    MPXTT(10),              // MPX re-map transfer type

    //9..8 (54..55 in PPC notation) - reserved

    ATS(7),                 // Atomic status (read-only)

    //6..4 (57..59 in PPC notation) - reserved

    MIDHigh(3),             // Reflected directly from the MID input pins (read-only)
    MIDLow(0);

    companion object {
        val PLL_CFG = PLL_CFGHigh.bit..PLL_CFGLow.bit
        val MID = MIDHigh.bit..MIDLow.bit
    }



}