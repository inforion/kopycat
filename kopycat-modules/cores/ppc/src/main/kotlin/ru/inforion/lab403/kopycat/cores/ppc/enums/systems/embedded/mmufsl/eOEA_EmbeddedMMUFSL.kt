package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl

enum class eOEA_EmbeddedMMUFSL(val id: Int) {

    //MMU assist registers
    MAS0(624),
    MAS1(625),
    MAS2(626),
    MAS3(627),
    MAS4(628),
    MAS6(630),
    MAS7(944),

    //Process ID registers
    PID1(633),
    PID2(634),

    //TLB Configuration Registers
    TLB0CFG(688),
    TLB1CFG(689),
    TLB2CFG(690),
    TLB3CFG(691),

    //MMU control and status(Read/Write)
    MMUCSR0(1012),

    MMUCFG(1015)

}