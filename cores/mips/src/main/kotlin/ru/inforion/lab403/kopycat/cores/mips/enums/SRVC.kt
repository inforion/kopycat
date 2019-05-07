package ru.inforion.lab403.kopycat.cores.mips.enums


/**
 * Created by batman on 05/06/16.
 */
enum class SRVC(val id: Int) {
    lo(0x20),
    hi(0x21),
    pc(0x25);
}