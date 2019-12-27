package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 19.01.17.
 */

class Leave(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "leave"

    override fun execute() {
        if (prefs.is16BitAddressMode) core.cpu.regs.sp = core.cpu.regs.bp else core.cpu.regs.esp = core.cpu.regs.ebp

        if(prefs.is16BitOperandMode)
            core.cpu.regs.bp = x86utils.pop(core, prefs.opsize, prefs)
        else
            core.cpu.regs.ebp = x86utils.pop(core, prefs.opsize, prefs)
    }
}