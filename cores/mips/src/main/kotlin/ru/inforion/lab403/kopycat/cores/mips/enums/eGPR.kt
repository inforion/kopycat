package ru.inforion.lab403.kopycat.cores.mips.enums


/**
 * Created by a.gladkikh on 04/06/16.
 */
enum class eGPR(val id: Int) {
    ZERO(0),
    AT(1),

    V0(2),
    V1(3),

    A0(4),
    A1(5),
    A2(6),
    A3(7),

    T0(8),
    T1(9),
    T2(10),
    T3(11),
    T4(12),
    T5(13),
    T6(14),
    T7(15),

    S0(16),
    S1(17),
    S2(18),
    S3(19),
    S4(20),
    S5(21),
    S6(22),
    S7(23),

    T8(24),
    T9(25),

    K0(26),
    K1(27),

    GP(28),
    SP(29),
    FP(30),
    RA(31);

    init {
        val q = 1000
    }
}
