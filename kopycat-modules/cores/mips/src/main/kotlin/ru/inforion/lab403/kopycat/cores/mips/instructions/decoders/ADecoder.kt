package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

abstract class ADecoder(val core: MipsCore): ITableEntry {
    abstract fun decode(data: Long): AMipsInstruction
}