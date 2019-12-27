package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * MFLO rd
 */
class mflo(core: MipsCore,
           data: Long,
           rd: GPR) : RdInsn(core, data, Type.VOID, rd) {

    override val mnem = "mflo"

    override fun execute() {
        vrd = lo
    }
}