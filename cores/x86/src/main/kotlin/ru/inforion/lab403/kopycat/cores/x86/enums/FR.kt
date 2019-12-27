package ru.inforion.lab403.kopycat.cores.x86.enums

/**
 * Created by a.gladkikh on 07/10/16.
 */
enum class FR(val id: Int) {
    EFLAGS(0);

    companion object {
        val COUNT: Int get() = FR.values().size
        fun from(id: Int): FR = FR.values().first { it.id == id }
    }
}