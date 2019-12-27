package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException.GeneralProtectionFault
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by a.gladkikh on 12/10/16.
 */
class Loop(core: x86Core, opcode: ByteArray, prefs: Prefixes, val operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "loop"

    override fun execute() {
        val eip = x86Register.gpr(prefs.opsize, x86GPR.EIP.id)
        val ecx = x86Register.gpr(prefs.opsize, x86GPR.ECX.id)
        val count = ecx.value(core) - 1
        ecx.value(core, count)
        if (count != 0L){
            val ip = eip.value(core) + op1.ssext(core)
            if (!x86utils.isWithinCodeSegmentLimits(ip))
                throw GeneralProtectionFault(core.pc, cs.value(core))
            eip.value(core, ip)
        }
    }
}