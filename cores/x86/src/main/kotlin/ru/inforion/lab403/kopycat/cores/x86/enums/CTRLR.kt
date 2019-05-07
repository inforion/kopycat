package ru.inforion.lab403.kopycat.cores.x86.enums

/**
 * Created by batman on 09/10/16.
 */
enum class CTRLR(val id: Int) {
    CR0(0),
    CR1(1),
    CR2(2),
    CR3(3),
    CR4(4),

    ; companion object {
        val COUNT: Int get() = DBGR.values().size
        fun from(id: Int): DBGR = DBGR.values().first { it.id == id }
    }
}