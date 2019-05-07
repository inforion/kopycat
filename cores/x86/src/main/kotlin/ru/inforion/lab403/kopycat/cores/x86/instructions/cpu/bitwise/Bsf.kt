package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 26.06.17.
 */
class Bsf(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "bsf"

    override fun execute() {
        val src = op2.value(core)
        if (src == 0L) {
            core.cpu.flags.zf = true
            op1.value(core, 0L)
        } else {
            core.cpu.flags.zf = false
            val counter = (0..prefs.opsize.bits).takeWhile { src[it] == 0L }.count().toLong()
            op1.value(core,counter)
        }
    }
}