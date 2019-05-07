package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

abstract class ADecoder<out T: AV850ESInstruction>(val core: v850ESCore): ITableEntry {
    abstract fun decode(s: Long): T
}
