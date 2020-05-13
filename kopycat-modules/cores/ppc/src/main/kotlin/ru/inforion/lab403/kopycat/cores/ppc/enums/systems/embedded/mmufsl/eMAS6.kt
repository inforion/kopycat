package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl


 
 enum class eMAS6(val bit: Int) {

    SPID0High(23),
    SPID0Low(16),

    SAS(0);

    companion object {
        val SPID0 = SPID0High.bit..SPID0Low.bit
    }

}