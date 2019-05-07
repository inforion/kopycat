package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.edx
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.ax
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.dx
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 31.01.17.
 */

class Idiv(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "idiv"

    override fun execute() {
        val a2 = op1.ssext(core)
        if (a2 == 0L)
            throw x86HardwareException.DivisionByZero(core.pc)

        when (op1.dtyp) {
            Datatype.BYTE -> {
                val a1 = ax.ssext(core)
                val quotient = a1 / a2
                val remainder = a1 % a2
                core.cpu.regs.al = quotient
                core.cpu.regs.ah = remainder
            }
            Datatype.WORD -> {
//                val a1 = cpu.regs.dx.shl(16) or cpu.regs.ax
                val a1 = dx.ssext(core).shl(16) or core.cpu.regs.ax
                val quotient = a1 / a2
                val remainder = a1 % a2
                core.cpu.regs.ax = quotient
                core.cpu.regs.dx = remainder
            }
            Datatype.DWORD -> {
                val a1 = edx.ssext(core).shl(32) or core.cpu.regs.eax
                val quotient = a1 / a2
                val remainder = a1 % a2
                core.cpu.regs.eax = quotient
                core.cpu.regs.edx = remainder
            }
            else -> throw GeneralException("Wrong datatype!")
        }
    }
}