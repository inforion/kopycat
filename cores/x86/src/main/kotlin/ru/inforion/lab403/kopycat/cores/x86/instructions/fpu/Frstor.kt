package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by a.gladkikh on 03.07.18.
 */

class Frstor(core: x86Core, opcode: ByteArray, prefs: Prefixes, val src: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, src) {
    override val mnem = "frstor"

    override fun execute() {
        val address = src.effectiveAddress(core)

        if (!core.cpu.cregs.vpe || core.cpu.mode != x86CPU.Mode.R32)
            TODO("Only for PE and 32-bit mode implemented!")

        core.fpu.fwr.FPUControlWord = core.inl(address +  0)
        core.fpu.fwr.FPUStatusWord = core.inl(address +  4)
        core.fpu.fwr.FPUTagWord = core.inl(address +  8)
        core.fpu.fwr.FPUInstructionPointer = core.inl(address + 12)
//      FPUInstructionPointer Selector = core.read_word(address + 16)
        core.fpu.fwr.FPUDataPointer = core.inl(address + 20)
//      FPUDataPointer Selector = core.read_word(address + 24)

        (0 until x86FPU.FPU_STACK_SIZE).forEach {
            core.fpu[it] = core.inl(address + 28 + 10 * it)
        }

        // occupied 0x6C bytes (108)
    }
}