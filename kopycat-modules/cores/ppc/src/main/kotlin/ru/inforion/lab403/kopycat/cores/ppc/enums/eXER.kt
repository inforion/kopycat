package ru.inforion.lab403.kopycat.cores.ppc.enums



enum class eXER(val bit: Int) {
    SO(31),
    OV(30),
    CA(29),
    BCmsb(6),   //Byte count field
    BClsb(0);

}