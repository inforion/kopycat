package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack

import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 22.09.16.
 */

class Push(core: x86Core, opcode: ByteArray, prefs: Prefixes, val isSSR: Boolean, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "push"

    override fun execute() {
        val data = if(op1 is Immediate)
            op1.ssext(core) mask prefs.opsize.bits
        else
            op1.value(core)
        x86utils.push(core, data, op1.dtyp, prefs, isSSR = isSSR)
    }
}