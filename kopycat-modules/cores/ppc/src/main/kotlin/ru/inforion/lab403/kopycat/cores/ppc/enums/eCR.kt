package ru.inforion.lab403.kopycat.cores.ppc.enums



enum class eCR(val bit: Int) {
    //CR0
    CR0_LT(31), //Negative
    CR0_GT(30), //Positive
    CR0_EQ(29), //Zero
    CR0_SO(28), //Summary overflow

    //CR1
    CR1_FX(27), //Floating-point exception
    CR1_FEX(26), //Floating-point enabled exception
    CR1_VX(25), //Floating-point invalid exception
    CR1_OX(24); //Floating-point overflow exception

    companion object {
        fun LTbit(ind: Int): Int = CR0_LT.bit - 4 * ind
        fun GTbit(ind: Int): Int = CR0_GT.bit - 4 * ind
        fun EQbit(ind: Int): Int = CR0_EQ.bit - 4 * ind
        fun SObit(ind: Int): Int = CR0_SO.bit - 4 * ind
        fun msb(ind: Int): Int = LTbit(ind)
        fun lsb(ind: Int): Int = SObit(ind)
    }
}