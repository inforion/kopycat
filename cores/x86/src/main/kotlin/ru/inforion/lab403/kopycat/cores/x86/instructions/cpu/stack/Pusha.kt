package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack

import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 22.09.16.
 */
class Pusha(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "pusha"

    override fun execute() {
        val eax = x86Register.gpr(prefs.opsize, x86GPR.EAX).value(core)
        val ecx = x86Register.gpr(prefs.opsize, x86GPR.ECX).value(core)
        val edx = x86Register.gpr(prefs.opsize, x86GPR.EDX).value(core)
        val ebx = x86Register.gpr(prefs.opsize, x86GPR.EBX).value(core)
        val esp = x86Register.gpr(prefs.opsize, x86GPR.ESP).value(core)
        val ebp = x86Register.gpr(prefs.opsize, x86GPR.EBP).value(core)
        val esi = x86Register.gpr(prefs.opsize, x86GPR.ESI).value(core)
        val edi = x86Register.gpr(prefs.opsize, x86GPR.EDI).value(core)
        x86utils.push(core, eax, prefs.opsize, prefs)
        x86utils.push(core, ecx, prefs.opsize, prefs)
        x86utils.push(core, edx, prefs.opsize, prefs)
        x86utils.push(core, ebx, prefs.opsize, prefs)
        x86utils.push(core, esp, prefs.opsize, prefs)
        x86utils.push(core, ebp, prefs.opsize, prefs)
        x86utils.push(core, esi, prefs.opsize, prefs)
        x86utils.push(core, edi, prefs.opsize, prefs)
    }
}