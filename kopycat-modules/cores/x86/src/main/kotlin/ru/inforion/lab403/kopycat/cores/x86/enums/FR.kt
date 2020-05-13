package ru.inforion.lab403.kopycat.cores.x86.enums


enum class FR(val id: Int) {
    EFLAGS(0);

    companion object {
        val COUNT: Int get() = FR.values().size
        fun from(id: Int): FR = FR.values().first { it.id == id }
    }
}