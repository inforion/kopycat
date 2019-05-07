package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.gs
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by the bat on 25.10.16.
 */
class Lgs(core: x86Core, opcode: ByteArray, prefs: Prefixes, op1: AOperand<x86Core>, op2: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, op1, op2) {
    override val mnem = "lgs"

    override fun execute() {
        val a2 = op2.value(core)
        if(prefs.is16BitOperandMode){
            op1.value(core, a2[15..0])
            gs.value(core, a2[31..16])
        } else {
            op1.value(core, a2[31..0])  // always use 31..0 (snapped in value)
            gs.value(core, a2[47..32])
        }
    }
}