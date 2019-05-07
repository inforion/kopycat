package ru.inforion.lab403.kopycat.cores.arm

import ru.inforion.lab403.common.extensions.UNDEF
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.AContext
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by rusanov_pv on 14.02.18.
 */

class ARMABI(cpu: AARMCore, heap: LongRange, stack: LongRange, bigEndian: Boolean):
        ABI<AARMCore>(cpu, heap, stack, bigEndian) {

    override fun gpr(index: Int): ARegister<AARMCore> = ARMRegister.gpr(index)
    override fun createCpuContext(): AContext<*> = ARMContext(core.cpu)
    override val ssr = UNDEF
    override val sp = ARMRegister.gpr(GPR.SPMain)
    override val ra = ARMRegister.gpr(GPR.LR)
    override val v0 = ARMRegister.gpr(GPR.R0)
    override val argl = listOf(
            ARMRegister.gpr(GPR.R0),
            ARMRegister.gpr(GPR.R1),
            ARMRegister.gpr(GPR.R2),
            ARMRegister.gpr(GPR.R3))

    override fun getArgs(n: Int, type: ArgType): Array<Long>{
        var res = argl.map { it.value(core) }
        val args = Array(n){ type }

        if (n > argl.size) {
            val ss = stackStream(where = argl.last().value(core))
            res.dropLast(1)
            res += args[argl.size until args.size].map {  // !!!!!!!!!!!!!!!!!!!!!  Не все аргументы !!!!!!!!!!!!!!1
                when (it) {
                    ArgType.Pointer -> ss.read(types.pointer)
                    ArgType.Word -> ss.read(types.word)
                    ArgType.Half -> ss.read(types.half)
                    ArgType.Byte -> ss.read(types.half)  // x86 can't push byte but others ...
                }
            }
        }

        return res.toTypedArray()
    }
}