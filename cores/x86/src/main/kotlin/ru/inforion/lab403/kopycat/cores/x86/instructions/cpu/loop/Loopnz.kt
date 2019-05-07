package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by batman on 12/10/16.
 */
class Loopnz(core: x86Core, opcode: ByteArray, prefs: Prefixes, val operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "loopnz"

    override fun execute() {
        val ecx = x86Register.gpr(prefs.opsize, x86GPR.ECX.id)
        val count = ecx.value(core) - 1
        ecx.value(core, count)
        if (!core.cpu.flags.zf && count != 0L){
            val eip = core.cpu.regs.eip + op1.value(core)
            if (!x86utils.isWithinCodeSegmentLimits(eip))
                throw x86HardwareException.GeneralProtectionFault(core.pc, cs.value(core))
            // TODO: always write to eip?
            core.cpu.regs.eip = eip
        }
    }
}