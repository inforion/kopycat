package ru.inforion.lab403.kopycat.cores.x86.enums


enum class DBGR(val id: Int) {
    DR0(0),
    DR1(1),
    DR2(2),
    DR3(3),
    DR4(4),
    DR5(5),
    DR6(6),
    DR7(7);

    companion object {
        val COUNT: Int get() = DBGR.values().size
        fun from(id: Int): DBGR = DBGR.values().first { it.id == id }
    }
}