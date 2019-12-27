package ru.inforion.lab403.kopycat.cores.arm.enums

/**
 * Created by a.gladkikh on 13.01.18.
 */

enum class GPR(val id: Int) {
    R0(0),
    R1(1),
    R2(2),
    R3(3),
    R4(4),
    R5(5),
    R6(6),
    R7(7),
    R8(8),
    R9(9),
    R10(10),
    R11(11),
    R12(12),
    SPMain(13),
    LR(14),
    PC(15),
    SPProcess(16);

    companion object {
        val COUNT: Int get() = values().size - 1
        fun from(id: Int): GPR = values().first { it.id == id }
    }
}