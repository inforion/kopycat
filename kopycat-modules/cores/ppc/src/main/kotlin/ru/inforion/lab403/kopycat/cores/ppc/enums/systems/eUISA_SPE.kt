package ru.inforion.lab403.kopycat.cores.ppc.enums.systems



enum class eUISA_SPE(val id: Int, val regName : String) {
    Accumulator(0, "ACC"),
    SPEFSCR(1, "SPEFSCR");

    companion object {
        fun from(id: Int): eUISA_SPE = eUISA_SPE.values().first { it.id == id }
    }
}