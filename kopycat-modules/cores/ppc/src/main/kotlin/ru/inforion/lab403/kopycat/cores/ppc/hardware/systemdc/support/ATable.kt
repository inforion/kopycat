package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support

import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.APPCDecoder
import ru.inforion.lab403.kopycat.interfaces.ITableEntry

abstract class ATable : ITableEntry {
    abstract fun lookup(data: Long, where: Long): APPCDecoder
}