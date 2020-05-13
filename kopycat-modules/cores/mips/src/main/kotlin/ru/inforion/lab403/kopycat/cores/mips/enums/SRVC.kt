package ru.inforion.lab403.kopycat.cores.mips.enums



enum class SRVC(val id: Int) {
    lo(0x20),
    hi(0x21),
    pc(0x25);
}