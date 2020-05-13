package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl



//WARNING: 64 bit by PowerISA V2.05
enum class eMAS2(val bit: Int) {

    EPNHigh(31),    // Effective page number
    EPNLow(12),

    //11..8 (52..55 in PPC notation) - reserved

    ACMHigh(7),     // Alternate coherency mode
    ACMLow(6),

    // Category: VLE
    VLE(5),         // VLE mode

    W(4),           // Write through

    I(3),           // Caching inhibited

    M(2),           // Memory coherence required

    G(1),           // Guarded

    E(0);           // Endianness (0 - big-endian, 1 - little-endian)

    companion object {
        val EPN = EPNHigh.bit..EPNLow.bit
        val ACM = ACMHigh.bit..ACMLow.bit
    }
}