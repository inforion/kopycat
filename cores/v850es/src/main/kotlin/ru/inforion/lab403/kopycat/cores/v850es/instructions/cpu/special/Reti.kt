package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 29.05.17.
 */

class Reti(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "reti"

    // Format X - imm
    override fun execute() {
        // insnSize add in CPU execute
        if(core.cpu.flags.ep){
            core.cpu.pc = core.cpu.cregs.eipc - size
            core.cpu.cregs.psw = core.cpu.cregs.eipsw
        } else if (core.cpu.flags.np){
            core.cpu.pc = core.cpu.cregs.fepc - size
            core.cpu.cregs.psw = core.cpu.cregs.fepsw
        } else {
            core.cpu.pc = core.cpu.cregs.eipc - size
            core.cpu.cregs.psw = core.cpu.cregs.eipsw
        }
    }
}