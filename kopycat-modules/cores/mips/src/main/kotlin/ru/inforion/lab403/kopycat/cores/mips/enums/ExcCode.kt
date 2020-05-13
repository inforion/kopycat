package ru.inforion.lab403.kopycat.cores.mips.enums



enum class ExcCode(val id: Long) {
    INT(0),         // Interrupt
    MOD(1),         // TLB modification exception
    TLBL_INVALID(2),     // TLB exception (load or instruction fetch)
    TLBS_INVALID(3),     // TLB exception (store)
    TLBL_MISS(2),        // TLB exception (load or instruction fetch)
    TLBS_MISS(3),        // TLB exception (store)
    ADEL(4),        // Address error exception (load or instruction fetch)
    ADES(5),        // Address error exception (store)
    IBE(6),         // Bus error exception (instruction fetch)
    DBE(7),         // Bus error exception (data reference: load or store)
    SYS(8),         // Syscall exception
    BP(9),          // Breakpoint exception
    RI(10),         // Reserved instruction exception
    CPU(11),        // Coprocessor Unusable exception
    OV(12),         // Arithmetic Overflow exception
    TR(13),         // Trap exception
    FPE(15),        // Floating point exception
    C2E(18),        // Reserved for precise Coprocessor 2 exceptions
    MDMX(22),       // Reserved for MDMX Unusable Exception in MIPS64 implementations.
    WATCH(23),      // Reference to WatchHi/WatchLo address
    MCHECK(24),     // Machine check
    CACHEERR(30)    // Cache error
}