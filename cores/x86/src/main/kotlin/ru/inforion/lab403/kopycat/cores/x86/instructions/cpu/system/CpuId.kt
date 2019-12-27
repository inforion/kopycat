package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 13.06.17.
 */
class CpuId(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "cpuid"

    override fun execute() {
        // TODO: Implement configuration of AX86 device and CPUID
        val case = core.cpu.regs.eax
        when (case) {
            0L -> {
                core.cpu.regs.eax = 0x1
                core.cpu.regs.ebx = 0x756e6547
                core.cpu.regs.edx = 0x49656e69
                core.cpu.regs.ecx = 0x6c65746e
            }
            1L -> {
                log.severe { "CPUID instruction not implemented -> execution may be wrong!" }
                core.cpu.regs.eax = 0x0  // Original OEM processor
                core.cpu.regs.ebx = 0x0
                core.cpu.regs.ecx = 0x0
                core.cpu.regs.edx = 0x4F4 // For amd elan 520
            }
            else -> throw GeneralException("Incorrect argument in CpuId insn")
        }
    }
}