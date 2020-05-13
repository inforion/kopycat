package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap

import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code20bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * SYSCALL
 *
 * To cause a System Call exception
 *
 * A system call exception occurs, immediately and unconditionally transferring control to the exception handler.
 * The code field is available for use as software parameters, but is retrieved by the exception handler only by
 * loading the contents of the memory word containing the instruction.
 */
class syscall(core: MipsCore,
              data: Long,
              code: MipsImmediate) : Code20bitInsn(core, data, Type.VOID, code) {

    override val mnem = "syscall"

    override fun execute() {
        throw MipsHardwareException.SYS(core.pc)
    }
}