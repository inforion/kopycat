package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.CTRLR
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 27.05.17.
 */

class Callt(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.IND_CALL, size, *operands) {
    override val mnem = "callt"

    // Format II - imm, reg2
    override fun execute() {
        // size add in CPU execute
        CTRLR.CTPC.value(core, GPR.pc.value(core) + size)
        CTRLR.CTPSW.value(core, CTRLR.PSW.value(core))
        val address = CTRLR.CTBP.value(core) + (op1.value(core) shl 1)
        val base = core.inw(address)
        GPR.pc.value(core, CTRLR.CTBP.value(core) + base - size)
    }
}