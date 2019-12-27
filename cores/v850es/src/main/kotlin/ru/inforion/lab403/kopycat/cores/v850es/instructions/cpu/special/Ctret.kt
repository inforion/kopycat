package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.CTRLR
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 29.05.17.
 */


class Ctret(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.RET, size, *operands) {
    override val mnem = "ctret"

    // Format X - imm
    override fun execute() {
        // insnSize add in CPU execute
        GPR.pc.value(core, CTRLR.CTPC.value(core) - size)
        CTRLR.PSW.value(core, CTRLR.CTPSW.value(core))
    }
}