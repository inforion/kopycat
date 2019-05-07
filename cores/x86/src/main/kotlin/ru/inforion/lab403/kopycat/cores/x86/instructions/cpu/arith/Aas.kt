package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 26.09.16.
 */

class Aas(core: x86Core, opcode: ByteArray, prefs: Prefixes): AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "aas"
    override fun execute() {
        var al = core.cpu.regs.al
        val isOverflow: Boolean

        if ((al and 0xF > 9) or core.cpu.flags.af){
            al -= 6
            core.cpu.regs.ah = core.cpu.regs.ah - 1
            isOverflow = true
        } else
            isOverflow = false
        FlagProcessor.processAsciiAdjustFlag(core, isOverflow)
        core.cpu.regs.al = al and 0xF
    }
}