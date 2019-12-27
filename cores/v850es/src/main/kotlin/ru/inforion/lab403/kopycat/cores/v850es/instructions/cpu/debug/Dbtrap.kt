package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.debug

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.CTRLR
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 29.05.17.
 */

class Dbtrap(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "dbtrap"

    // Format I - reg1, reg2
    override fun execute() {
        // insnSize add in CPU execute
        CTRLR.DBPC.value(core, GPR.pc.value(core) + size)
        CTRLR.DBPSW.value(core, CTRLR.PSW.value(core))
        core.cpu.flags.np = true
        core.cpu.flags.ep = true
        core.cpu.flags.id = true
        GPR.pc.value(core, 0x60L - size)
    }
}