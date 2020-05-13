package ru.inforion.lab403.kopycat.cores.v850es.enums



enum class GPR(val id: Int) {
    r0(0),
    r1(1),
    r2(2),
    r3(3),
    r4(4),
    r5(5),
    r6(6),
    r7(7),
    r8(8),
    r9(9),
    r10(10),
    r11(11),
    r12(12),
    r13(13),
    r14(14),
    r15(15),
    r16(16),
    r17(17),
    r18(18),
    r19(19),
    r20(20),
    r21(21),
    r22(22),
    r23(23),
    r24(24),
    r25(25),
    r26(26),
    r27(27),
    r28(28),
    r29(29),
    r30(30),
    r31(31),
    pc(32);

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): GPR = GPR.values().first { it.id == id }
    }
}