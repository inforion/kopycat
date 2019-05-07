package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.ecx
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.cx
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by batman on 23/10/16.
 */
abstract class AStringInstruction(core: x86Core, opcode: ByteArray, prefs: Prefixes, val isRepeOrRepne:Boolean, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {

    abstract protected fun executeStringInstruction()

    final override fun execute() {
        if (prefs.string != StringPrefix.NO) {
            val isRepz = prefs.string == StringPrefix.REPZ
            val isRepnz = prefs.string == StringPrefix.REPNZ
            val counter = if (prefs.is16BitAddressMode) cx else ecx
            while (counter.value(core) != 0L) {
                // TODO: ServiceInterrupts()
                executeStringInstruction()
                counter.minus(core, 1L)
//                val zf = counter.value(cpu) == 0L
                if(isRepeOrRepne)
                    if (isRepz && !core.cpu.flags.zf || isRepnz && core.cpu.flags.zf) break
            }
        } else executeStringInstruction()
//        executeStringInstruction()
    }
}