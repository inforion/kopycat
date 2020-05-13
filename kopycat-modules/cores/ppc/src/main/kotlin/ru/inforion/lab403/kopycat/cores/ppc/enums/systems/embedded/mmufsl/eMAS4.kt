package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl


 
enum class eMAS4(val bit: Int) {

    TLBSELDHigh(30),
    TLBSELDLow(29),

    TIDSELDHigh(17),
    TIDSELDLow(16),

    TSIZEDHigh(11),
    TSIZEDLow(8),

    X0D(6),
    X1D(5),
    WD(4),
    ID(3),
    MD(2),
    GD(1),
    ED(0);

    companion object {
        val TLBSELD = TLBSELDHigh.bit..TLBSELDLow.bit
        val TIDSELD = TIDSELDHigh.bit..TIDSELDLow.bit
        val TSIZED = TSIZEDHigh.bit..TSIZEDLow.bit
    }

}