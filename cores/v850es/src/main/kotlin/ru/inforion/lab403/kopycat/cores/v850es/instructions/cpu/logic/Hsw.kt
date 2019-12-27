package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.logic

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esVariable
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 29.05.17.
 */

class Hsw(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "hsw"

    override val cyChg = true
    override val ovChg = true
    override val sChg = true
    override val zChg = true

    private val result = v850esVariable(Datatype.DWORD)

    // Format XII - imm, reg2, reg3
    override fun execute() {
        val a1 = op2.value(core)
        val res = a1[31..16].insert(a1[15..0], 31..16)
        FlagProcessor.processSwapFlag(core, result) { it.word(core, 0) == 0L || it.word(core, 1) == 0L }
        op3.value(core, res)
    }
}