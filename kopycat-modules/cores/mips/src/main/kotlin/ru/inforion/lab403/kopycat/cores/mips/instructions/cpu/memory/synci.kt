package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.EmptyInsn
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * SYNCI offset(base)
 */
class synci(core: MipsCore, data: Long) : EmptyInsn(core, data) {

    override val mnem = "synci"

    override fun execute() {
        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}