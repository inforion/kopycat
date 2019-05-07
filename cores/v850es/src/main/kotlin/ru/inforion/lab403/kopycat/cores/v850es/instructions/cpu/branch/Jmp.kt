package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.enums.GPR
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR.pc
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 29.05.17.
 */

class Jmp(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.IND_JUMP, size, *operands) {
    override val mnem = "jmp"
    override val isRet: Boolean get() = (op1 as v850esRegister).reg == GPR.r31.id

    // Format I - reg1, reg2
    override fun execute() {
        val addr = op1.value(core) mask 63..1
        pc.value(core, addr - size)
    }
}