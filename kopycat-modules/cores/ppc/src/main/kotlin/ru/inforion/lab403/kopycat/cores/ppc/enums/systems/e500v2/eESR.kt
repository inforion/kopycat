package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2



enum class eESR(val bit: Int) {

    PIL(27),
    PPR(26),
    PTR(25),

    ST(23),

    DLK(21),
    ILK(20),

    BO(17),

    SPE(7);

}