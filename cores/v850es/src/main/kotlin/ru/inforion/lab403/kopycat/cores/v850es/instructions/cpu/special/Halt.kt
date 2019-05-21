package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 29.05.17.
 */

class Halt(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "halt"

    // Format X - imm
    override fun execute() {
        throw GeneralException("Not implemented HALT")
    }
}