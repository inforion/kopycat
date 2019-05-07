package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 13.01.18.
 */

abstract class ADecoder<out T: AARMInstruction>(val core: AARMCore): ITableEntry {
    abstract fun decode(data: Long): T
}
