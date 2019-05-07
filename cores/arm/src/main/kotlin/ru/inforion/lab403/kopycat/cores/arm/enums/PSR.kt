package ru.inforion.lab403.kopycat.cores.arm.enums

/**
 * Created by r.valitov on 19.01.18
 */

enum class PSR(val id: Int) {
    APSR(0),
    IPSR(1),
    EPSR(2),
    CPSR(3),
    SPSR(4);

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): PSR = values().first { it.id == id }
    }
}