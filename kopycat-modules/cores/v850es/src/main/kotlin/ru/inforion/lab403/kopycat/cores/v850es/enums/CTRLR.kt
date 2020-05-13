package ru.inforion.lab403.kopycat.cores.v850es.enums



enum class CTRLR (val id: Int) {
    EIPC(0),
    EIPSW(1),
    FEPC(2),
    FEPSW(3),
    ECR(4),
    PSW(5),
    CTPC(6),
    CTPSW(7),
    DBPC(8),
    DBPSW(9),
    CTBP(10),
    DIR(11);

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): CTRLR = CTRLR.values().first { it.id == id }
    }
}
