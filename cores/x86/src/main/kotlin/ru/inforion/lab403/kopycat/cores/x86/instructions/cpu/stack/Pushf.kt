package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.CTRLR.cr0
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.eflags
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 22.09.16.
 */
class Pushf(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "pushf"

    override fun execute() {
        val pe = cr0.pe(core)
        val vm = eflags.vm(core)
        val iopl = eflags.iopl(core)
        if (pe || pe && (!vm || vm && iopl == 3)) {
            val eflags = eflags.value(core) or 0x0002L
            if (!prefs.is16BitOperandMode) {
                x86utils.push(core, eflags and 0xFCFFFF, Datatype.DWORD, prefs)
            } else {
                x86utils.push(core, eflags[15..0], Datatype.WORD, prefs)
            }
        } else throw GeneralException("GP(0)")
    }
}