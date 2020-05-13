package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Fsave(core: x86Core, opcode: ByteArray, prefs: Prefixes, val dst: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, dst) {
    override val mnem = "fsave"

    override fun execute() {
        val address = dst.effectiveAddress(core)

        if (!core.cpu.cregs.vpe || core.cpu.mode != x86CPU.Mode.R32)
            TODO("Only for PE and 32-bit mode implemented!")

        core.outl(address +  0, core.fpu.fwr.FPUControlWord)
        core.outl(address +  4, core.fpu.fwr.FPUStatusWord)
        core.outl(address +  8, core.fpu.fwr.FPUTagWord)
        core.outl(address + 12, core.fpu.fwr.FPUInstructionPointer)
        core.outl(address + 16, 0)  // FPUInstructionPointer Selector
        core.outl(address + 20, core.fpu.fwr.FPUDataPointer)
        core.outl(address + 24, 0)  // FPUDataPointer Selector

        (0 until x86FPU.FPU_STACK_SIZE).forEach {
            core.outl(address + 28 + 10 * it, core.fpu[it])
        }
        // occupied 0x6C bytes (108)

        core.fpu.fwr.FPUControlWord = 0x37F
        core.fpu.fwr.FPUStatusWord = 0
        core.fpu.fwr.FPUTagWord = 0xFFFF
        core.fpu.fwr.FPUDataPointer = 0
        core.fpu.fwr.FPUInstructionPointer = 0
        core.fpu.fwr.FPULastInstructionOpcode = 0
    }
}