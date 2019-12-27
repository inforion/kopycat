package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 08.02.17.
 */

class Lahf(core: x86Core, opcode: ByteArray, prefs: Prefixes): AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "lahf"
    override fun execute() {
        val flags = core.cpu.flags
        val value = (if(flags.sf) 1L.shl(7) else 0) or
                    (if(flags.zf) 1L.shl(6) else 0) or
                    (if(flags.af) 1L.shl(4) else 0) or
                    (if(flags.pf) 1L.shl(2) else 0) or
                    (if(flags.cf) 1L.shl(0) else 0) or
                    0b00000010
        core.cpu.regs.ah = value
    }
}