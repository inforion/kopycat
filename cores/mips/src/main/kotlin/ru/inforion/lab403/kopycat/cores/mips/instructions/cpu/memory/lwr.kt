package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.bext
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * LWR rt, offset(base)
 */
class lwr(core: MipsCore,
          data: Long,
          rt: GPR,
          off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

//    override val checked: Boolean = true
//    override val store = false
//    override val dtyp = DWORD
//    override val core = ProcType.CentralProc
//    override val construct = ::lwr
    override val mnem = "lwr"

    override fun execute() {
        // I hate mips...
        val dataword = vrt

        val vAddr = address

        val byte = (vAddr[1..0] xor core.cpu.bigEndianCPU.bext(2)).toInt()
        // Can't use operand value because to specific handler required
        val memword = core.inl(vAddr and 0xFFFFFFFC)

        val hi = dataword[31..32 - 8 * byte]
        val lo = memword[31..8 * byte]

        vrt = hi.shl(32 - 8 * byte) or lo
    }
}