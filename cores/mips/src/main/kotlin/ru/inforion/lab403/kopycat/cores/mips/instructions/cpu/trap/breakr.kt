package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap

import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code20bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * BREAK
 *
 * To cause a Breakpoint exception
 *
 * A breakpoint exception occurs, immediately and unconditionally transferring control to the exception handler.
 * The code field is available for use as software parameters, but is retrieved by the exception handler only by
 * loading the contents of the memory word containing the instruction.
 */
class breakr(core: MipsCore,
             data: Long,
             code: MipsImmediate) : Code20bitInsn(core, data, Type.VOID, code) {

    override val mnem = "break"

    override fun execute() {
        throw MipsHardwareException.BP(core.pc)
    }
}