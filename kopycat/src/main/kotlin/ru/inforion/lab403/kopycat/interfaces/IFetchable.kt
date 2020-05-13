package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.kopycat.cores.base.MasterPort

interface IFetchable {
    fun beforeFetch(from: MasterPort, ea: Long): Boolean = true
    fun fetch(ea: Long, ss: Int, size: Int): Long
}