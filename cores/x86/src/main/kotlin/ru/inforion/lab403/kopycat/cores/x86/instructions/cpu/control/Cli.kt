package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.CTRLR.cr0
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 03.10.16.
 */
class Cli(core: x86Core, opcode: ByteArray, prefs: Prefixes): AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "cli"
    override fun execute() {
        val pe = cr0.pe(core)
        if (!pe) {
            x86Register.eflags.ifq(core, false)
        } else {
            val vm = x86Register.eflags.vm(core)
            val iopl = x86Register.eflags.iopl(core)
            if (!vm) {
                // TODO: iopl vs cpl
                x86Register.eflags.ifq(core, false)
            } else {
                TODO()
            }
        }
    }
}