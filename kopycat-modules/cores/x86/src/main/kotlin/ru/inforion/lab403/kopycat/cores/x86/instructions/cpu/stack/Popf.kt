package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Popf(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "popf"

    override fun execute() {
        // Dunno what to do with it...
        if (x86Register.eflags.vm(core)) {
            val iopl = x86Register.eflags.iopl(core)
            if (iopl != 0) throw GeneralException("GP(0) IOPLH = ${iopl[1]} IOPLL = ${iopl[0]}")
        }

        if (!prefs.is16BitOperandMode) {
            val eflags = x86utils.pop(core, Datatype.DWORD, prefs)
            x86Register.eflags.value(core, eflags)
        } else {
            val flags = x86utils.pop(core, Datatype.WORD, prefs)
            x86Register.flags.value(core, flags)
        }
    }
}