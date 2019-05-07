package ru.inforion.lab403.kopycat.cores.x86.enums

/**
 * Created by davydov_vn on 09.02.18.
 */

enum class FWR(val id: Int) {
    // https://studfiles.net/preview/1402551/
    SWR(0),
    CWR(1),
    TWR(2),
    DPR(2),
    IPR(2),
    LIO(2);

    companion object {
        val COUNT: Int get() = FWR.values().size
        fun from(id: Int): FWR = FWR.values().first { it.id == id }
    }
}