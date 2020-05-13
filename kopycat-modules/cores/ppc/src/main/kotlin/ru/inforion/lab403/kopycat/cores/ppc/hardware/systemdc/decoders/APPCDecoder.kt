package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



abstract class APPCDecoder(val core: PPCCore): ITableEntry {
    abstract fun decode(s: Long): APPCInstruction
}
