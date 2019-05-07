package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.base.exceptions.UnsupportedInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.EmptyInsn
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * SYNC (stype = 0 implied)
 */
class sync(core: MipsCore, data: Long) : EmptyInsn(core, data) {

    override val mnem = "sync"

    override fun execute() {
        if (!core.syncSupported)
            throw UnsupportedInstruction(this)
//        log.severe { "[${core.cpu.pc.hex8}] Sync command not implemented... ra = ${core.cpu.regs.ra.hex8}" }
    }
}