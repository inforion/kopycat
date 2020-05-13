package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl



enum class eMAS0(val bit: Int) {

    //31..30 (32..33 in PPC notation) - reserved

    TLBSELHigh(29),     //TLB select
    TLBSELLow(28),

    ESELHigh(27),       //Entry select
    ESELLow(16),

    //15..12 (48..51 in PPC notation) - reserved

    NVHigh(11),         //Next victim
    NVLow(0);

    companion object {
        val TLBSEL = TLBSELHigh.bit..TLBSELLow.bit
        val ESEL = ESELHigh.bit..ESELLow.bit
        val NV = NVHigh.bit..NVLow.bit
    }

}