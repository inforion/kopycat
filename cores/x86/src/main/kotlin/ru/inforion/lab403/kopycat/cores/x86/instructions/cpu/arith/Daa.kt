package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 26.09.16.
 */

class Daa(core: x86Core, opcode: ByteArray, prefs: Prefixes): AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "daa"

    override fun execute() {
        var al = core.cpu.regs.al
        if ((al and 0xF > 9) or core.cpu.flags.af){
            al += 6
            core.cpu.flags.cf = core.cpu.flags.cf or (al[8] != 0L)
            core.cpu.flags.af = true
        } else
            core.cpu.flags.af = false
        if ((al and 0xF0 > 0x90) or core.cpu.flags.cf){
            al += 0x60
            core.cpu.flags.cf = true
        } else
            core.cpu.flags.cf = false
        core.cpu.regs.al = al
    }
}