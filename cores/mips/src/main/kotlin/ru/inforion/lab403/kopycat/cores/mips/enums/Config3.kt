package ru.inforion.lab403.kopycat.cores.mips.enums

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get

/**
 * Created by batman on 18/06/17.
 */
enum class Config3(val pos: Int) {
    ULRI(13), // UserLocal register implemented
    VEIC(6),  // Support for an external interrupt controller is implemented.
    VInt(5);  // Vectored interrupts implemented

    infix fun from(value: Long): Long = value[this.pos]

    companion object {
        fun extract(value: Long): Map<Cause, Int> = Cause.values().associate { Pair(it, (it from value).asInt) }
    }
}