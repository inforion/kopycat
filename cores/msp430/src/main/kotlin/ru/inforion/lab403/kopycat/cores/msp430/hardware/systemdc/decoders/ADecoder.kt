package ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 6/02/18.
 */

abstract class ADecoder<out T: AMSP430Instruction>(val core: MSP430Core): ITableEntry {
    abstract fun decode(s: Long): T
}
