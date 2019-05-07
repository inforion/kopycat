package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 08.09.16.
 */

class Finit(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "finit"

    override fun execute() {
        core.fpu.fwr.FPUControlWord = 0x37F
        core.fpu.fwr.FPUStatusWord = 0
        core.fpu.fwr.FPUTagWord = 0xFFFF
        core.fpu.fwr.FPUDataPointer = 0
        core.fpu.fwr.FPUInstructionPointer = 0
        core.fpu.fwr.FPULastInstructionOpcode = 0
    }
}