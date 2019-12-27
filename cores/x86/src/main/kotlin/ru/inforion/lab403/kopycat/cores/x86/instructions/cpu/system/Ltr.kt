package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by a.gladkikh on 02.07.18.
 */
class Ltr(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "ltr"

    override fun execute() {
        val ss = op1.value(core)

        if (ss == 0L) throw x86HardwareException.GeneralProtectionFault(core.pc, 0)

        if (ss > core.mmu.gdtr.limit) throw x86HardwareException.GeneralProtectionFault(core.pc, ss)

        val desc = core.mmu.readSegmentDescriptor(ss)

        if (!desc.isForAnAvailableTSS) throw x86HardwareException.GeneralProtectionFault(core.pc, ss)
        if (!desc.isPresent) throw x86HardwareException.SegmentNotPresent(core.pc, ss)

        // TSSSegmentDescriptor.Busy = 1;

        core.cop.tssr = ss
    }
}