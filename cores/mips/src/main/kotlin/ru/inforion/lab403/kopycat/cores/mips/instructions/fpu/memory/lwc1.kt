package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.mips.instructions.FtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * LWC1 ft, offset(base)
 */
class lwc1(
        core: MipsCore,
        data: Long,
        ct: MipsRegister<*>,
        off: MipsDisplacement
) : FtOffsetInsn(core, data, Type.VOID, ct, off) {

    override val mnem = "lwc1"

    override fun execute() {
        if (address[1..0] != 0L)
            throw MemoryAccessError(core.pc, address, LOAD, "ADEL")
        vct = memword
    }
}
