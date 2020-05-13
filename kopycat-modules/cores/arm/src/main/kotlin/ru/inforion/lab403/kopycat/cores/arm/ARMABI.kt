package ru.inforion.lab403.kopycat.cores.arm

import ru.inforion.lab403.common.extensions.UNDEF
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR as eGPR
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.AContext
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class ARMABI(cpu: AARMCore, heap: LongRange, stack: LongRange, bigEndian: Boolean):
        ABI<AARMCore>(cpu, heap, stack, bigEndian) {

    override fun gpr(index: Int): ARegister<AARMCore> = GPRBank.Operand(index)
    override fun createCpuContext(): AContext<*> = ARMContext(core.cpu)
    override val ssr = UNDEF
    override val sp = GPRBank.Operand(eGPR.SPMain.id) // TODO: refactor
    override val ra = GPRBank.Operand(eGPR.LR.id)
    override val v0 = GPRBank.Operand(eGPR.R0.id)
    override val argl = listOf(
            GPRBank.Operand(eGPR.R0.id),
            GPRBank.Operand(eGPR.R1.id),
            GPRBank.Operand(eGPR.R2.id),
            GPRBank.Operand(eGPR.R3.id))

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