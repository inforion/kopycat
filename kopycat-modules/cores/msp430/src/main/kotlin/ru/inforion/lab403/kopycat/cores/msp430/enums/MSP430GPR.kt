package ru.inforion.lab403.kopycat.cores.msp430.enums



enum class MSP430GPR(val id: Int, val regName : String) {
    r0(0, "PC"),
    r1(1, "SP"),
    r2(2, "SR"),
    r3(3, "CG"),
    r4(4, "R4"),
    r5(5, "R5"),
    r6(6, "R6"),
    r7(7, "R7"),
    r8(8, "R8"),
    r9(9, "R9"),
    r10(10, "R10"),
    r11(11, "R11"),
    r12(12, "R12"),
    r13(13, "R13"),
    r14(14, "R14"),
    r15(15, "R15");

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): MSP430GPR = MSP430GPR.values().first { it.id == id }
    }
}