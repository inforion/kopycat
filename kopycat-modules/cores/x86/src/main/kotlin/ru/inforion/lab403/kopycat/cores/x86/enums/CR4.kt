package ru.inforion.lab403.kopycat.cores.x86.enums


enum class CR4(val bit: Int) {
    VME(0),
    PVI(1),
    TSD(2),
    DE(3),
    PSE(4),
    PAE(5),
    MCE(6),
    PGE(7),
    PCE(8),
    OSFXSR(9),
    OSXMMEXCPT(10),
    VMXE(13),
    SMXE(14),
    FSGSBASE(16),
    PCIDE(17),
    OSXSAVE(18),
    SMEP(20),
    SMAP(21),
    PKE(22);
}