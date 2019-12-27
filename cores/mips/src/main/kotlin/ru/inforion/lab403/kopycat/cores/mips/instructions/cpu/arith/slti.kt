package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 * Created by a.gladkikh on 03/06/16.
 *
 * SLTI rt, rs, immediate
 */
class slti(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rs: GPR,
        imm: MipsImmediate) : RtRsImmInsn(core, data, Type.VOID, rt, rs, imm)  {

//    override val isSigned = true
    override val mnem = "slti"

    override fun execute() {
        // ancient bug awaken here
        // 50026BC4 slti   $v0, $s2, 50 ; WTF??? 500267B4 addiu  $s2, $zero, -1
        vrt = (vrs.asInt < imm.ssext.asInt).asLong
    }
}