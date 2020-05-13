package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class FdFsFt(
        core: MipsCore,
        val construct: (MipsCore, Long, FPR, FPR, FPR) -> AMipsInstruction
) : ADecoder(core) {

//    val fpu:FPU get() = dev.fpu
//
//    var fd: Int by IntOperandField(1)
//    var fs: Int by IntOperandField(2)
//    var ft: Int by IntOperandField(3)
//
//    var dfd : Long by DoubleRegister(1)
//    var dfs : Long by DoubleRegister(2)
//    var dft : Long by DoubleRegister(3)

    override fun decode(data: Long): AMipsInstruction {
        val fd = data[10..6].toInt()
        val fs = data[15..11].toInt()
        val ft = data[20..16].toInt()
        return construct(core, data,
                FPR(fd),
                FPR(fs),
                FPR(ft))
    }
}
