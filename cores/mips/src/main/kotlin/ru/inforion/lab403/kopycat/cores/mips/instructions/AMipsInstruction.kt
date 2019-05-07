package ru.inforion.lab403.kopycat.cores.mips.instructions

import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.AMipsOperand
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

abstract class AMipsInstruction(core: MipsCore, val data: Long, type: Type, vararg operands: AMipsOperand) :
        AInstruction<MipsCore>(core, type, *operands), ITableEntry {

    var hi: Long
        get() = core.cpu.hi
        set(value) { core.cpu.hi = value }

    var lo: Long
        get() = core.cpu.lo
        set(value) { core.cpu.lo = value }

    final override val size: Int = 4
}
