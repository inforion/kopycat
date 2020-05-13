package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Mul(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "mul"

    override val ofChg = true
    override val cfChg = true

    override fun execute() {
        val upperHalf = when(op1.dtyp){
            Datatype.BYTE -> {
                val result = core.cpu.regs.al * op1.value(core)
                val upperHalf = result ushr 8
                core.cpu.regs.ax = result
                upperHalf
            }
            Datatype.WORD -> {
                val result = core.cpu.regs.ax * op1.value(core)
                val upperHalf = result ushr 16
                core.cpu.regs.ax = result
                core.cpu.regs.dx = upperHalf
                upperHalf
            }
            Datatype.DWORD -> {
                val result = core.cpu.regs.eax * op1.value(core)
                val upperHalf = result ushr 32
                core.cpu.regs.eax = result
                core.cpu.regs.edx = upperHalf
                upperHalf
            }
            else -> throw GeneralException("Incorrect datatype")
        }
        FlagProcessor.processMulFlag(core, upperHalf)
    }
}