package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.bext
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * SWR rt, offset(base)
 */
class swr(core: MipsCore,
          data: Long,
          rt: GPR,
          off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

    override val mnem = "swr"

    override fun execute() {
        // I hate mips...
        val dataword = vrt

        val vAddr = address

        val alignAddr = vAddr and 0xFFFFFFFC
        val byte = (vAddr[1..0] xor core.cpu.bigEndianCPU.bext(2)).toInt()

        val memword = core.inl(alignAddr)

        val hi = dataword[31 - 8 * byte..0]
        val lo = memword[8 * byte - 1..0]

        val result = hi.shl(8 * byte) or lo

        core.outl(alignAddr, result)
    }
}
