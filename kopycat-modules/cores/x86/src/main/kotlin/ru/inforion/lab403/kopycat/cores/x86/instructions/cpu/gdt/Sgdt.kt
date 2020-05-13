package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt

import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Sgdt(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "sgdt"

    override fun execute() {
        var data = 0L
        if (prefs.is16BitOperandMode) {
            data = data.insert(core.mmu.gdtr.limit, 15..0)
            data = data.insert(core.mmu.gdtr.base, 39..16)
        } else {
            data = data.insert(core.mmu.gdtr.limit, 15..0)
            data = data.insert(core.mmu.gdtr.base, 47..16)
        }

        val ssr = op1.ssr
        val ea = op1.effectiveAddress(core)

        core.write(Datatype.FWORD, ea, data, ssr.reg)
    }
}