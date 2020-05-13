package ru.inforion.lab403.kopycat.cores.ppc.enums



//Virtual environment architecture
enum class eVEA(val id: Int, val regName : String) {

    //Time base facility (for reading)
    TBL(0, "TBL"),
    TBU(1, "TBU");

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): eVEA = eVEA.values().first { it.id == id }
    }
}