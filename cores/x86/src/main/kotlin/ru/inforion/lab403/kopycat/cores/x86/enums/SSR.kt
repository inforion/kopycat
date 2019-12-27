package ru.inforion.lab403.kopycat.cores.x86.enums

/**
 * Created by a.gladkikh on 07/10/16.
 */
enum class SSR(val id: Int) {
    ES(0),
    CS(1),
    SS(2),
    DS(3),
    FS(4),
    GS(5);

    companion object {
        val COUNT: Int get() = SSR.values().size
        fun from(id: Int): SSR = SSR.values().first { it.id == id }
    }
}