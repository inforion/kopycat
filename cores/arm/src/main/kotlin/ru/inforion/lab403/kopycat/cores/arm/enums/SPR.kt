package ru.inforion.lab403.kopycat.cores.arm.enums

/**
 * Created by r.valitov on 19.01.18
 */

enum class SPR(val id: Int) {
    PRIMASK(0),
    CONTROL(1);

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): SPR = values().first { it.id == id }
    }
}