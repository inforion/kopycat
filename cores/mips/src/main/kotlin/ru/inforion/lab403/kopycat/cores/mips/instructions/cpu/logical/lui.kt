package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.logical

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * LUI rt, immediate
 *
 * To load a constant into the upper half of a word
 *
 * The 16-bit immediate is shifted left 16 bits and concatenated with 16 bits of low-order zeros.
 * The 32-bit result is placed into GPR rt.
 */
class lui(
        core: MipsCore,
        data: Long,
        rt: GPR,
        imm: MipsImmediate) : RtImmInsn(core, data, Type.VOID, rt, imm) {

    override val mnem = "lui"

    override fun execute() {
        vrt = imm.zext shl 16
    }
}