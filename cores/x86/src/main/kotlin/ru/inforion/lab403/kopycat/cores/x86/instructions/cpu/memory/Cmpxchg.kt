package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.toUInt
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 27.06.18.
 */

class Cmpxchg(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "cmpxchg"

    private val isB0 = opcode.last().toUInt() == 0xB0
    private val dtype = if (isB0) Datatype.BYTE else prefs.opsize
    private val acc = x86Register.gpr(dtype, x86GPR.EAX)

    override fun execute() {
        // is F0 B0 opcode then al used otherwise ax/eax
        val temp = op1.value(core)
        if (acc.value(core) == temp) {
            core.cpu.flags.zf = true
            op1.value(core, op2)
        } else {
            core.cpu.flags.zf = false
            acc.value(core, temp)
            op1.value(core, temp)
        }
    }
}