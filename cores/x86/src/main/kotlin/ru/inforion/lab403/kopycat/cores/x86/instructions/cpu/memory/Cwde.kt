package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRBL.al
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.eax
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.ax
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 20.01.17.
 */

class Cwde(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "cwde"

    override fun execute() {
        val dst = if (prefs.is16BitOperandMode) ax else eax
        val src = if (prefs.is16BitOperandMode) al else ax
        dst.value(core, src.ssext(core))
    }
}