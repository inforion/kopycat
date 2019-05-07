package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRBL.al
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.eax
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.ax
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 29.09.16.
 */
class Imul(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "imul"

    override fun execute() {
        when (operands.size) {
            1 -> {
                when (op1.dtyp) {
                    Datatype.BYTE -> {
                        core.cpu.regs.ax = al.ssext(core) * op1.ssext(core)
                    }
                    Datatype.WORD -> {
                        val result = ax.ssext(core) * op1.ssext(core)
                        core.cpu.regs.ax = result
                        core.cpu.regs.dx = result ushr 16
                    }
                    Datatype.DWORD -> {
                        val result = eax.ssext(core) * op1.ssext(core)
                        core.cpu.regs.eax = result
                        core.cpu.regs.edx = result ushr 32
                    }
                    else -> throw GeneralException("Incorrect datatype")
                }
                FlagProcessor.processOneOpImulFlag()
            }
            2 -> {
                val result = op1.ssext(core) * op2.ssext(core)
                FlagProcessor.processTwoThreeOpImulFlag()
                op1.value(core, result)
            }
            3 -> {
                val result = op2.ssext(core) * op3.ssext(core)
                FlagProcessor.processTwoThreeOpImulFlag()
                op1.value(core, result)
            }
        }
    }
}