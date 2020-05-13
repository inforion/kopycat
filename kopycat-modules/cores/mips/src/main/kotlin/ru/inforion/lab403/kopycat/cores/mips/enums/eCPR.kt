package ru.inforion.lab403.kopycat.cores.mips.enums


enum class eCPR(val id: Int, val sel: Int = 0) {
    INDEX(0),
    RANDOM(1),
    ENTRYLO0(2),
    ENTRYLO1(3),
    CONTEXT(4),
    USER_LOCAL(4, 2),
    PAGEMASK(5),
    WIRED(6),
    HWRENA(7),
    BADVADDR(8),
    COUNTR(9),
    ENTRYHI(10),
    COMPARE(11),
    STATUS(12),
    IMPLSPEC0(12, 1),  // IntCtl
    IMPLPIC32(12, 2),  // SRSCtl
    IMPLSPEC1(12, 3),  // SRSMap
    CAUSE(13),
    EPC(14),
    PRID(15),
    IMPLSPEC2(15, 1),  // EBase
    CONFIG0(16, 0),
    CONFIG1(16, 1),
    CONFIG2(16, 2),
    CONFIG3(16, 3),
    IMPLSPEC3(16, 7),
    LLADR(17),
    WATCHLO0(18, 0),
    WATCHHI0(19, 0),
    WATCHLO1(18, 1),
    WATCHHI1(19, 1),
    WATCHLO2(18, 2),
    WATCHHI2(19, 2),
    WATCHLO3(18, 3),
    WATCHHI3(19, 3),
    XCONTEXT(20),
    DEBUG(23),
    DEPC0(24, 0),
    DEPC6(24, 6),
    PERFCNT(25),
    ERRCNT(26),  // ERRCTL, ECC
    CACHEERR0(27, 0),
    CACHEERR1(27, 1),
    CACHEERR2(27, 2),
    CACHEERR3(27, 3),
    TAGLO0(28, 0),
    TAGLO2(28, 2),
    TAGLO4(28, 4),
    DATALO1(28, 1),
    DATALO3(28, 3),
    TAGHI0(29, 0),
    TAGHI2(29, 2),
    TAGHI4(29, 4),
    DATAHI1(29, 1),
    DATAHI3(29, 3),
    ERROREPC(30),
    DESAVE(31);

    companion object {
        private val map = java.util.HashMap<Pair<Int, Int>, eCPR>().apply {
            for (it in eCPR.values()) {
                put(Pair(it.id, it.sel), it)
            }
        }

        fun from(id: Int, sel: Int = 0): eCPR = map[Pair(id, sel)]!!
        val COUNT: Int get() = values().size
    }
}