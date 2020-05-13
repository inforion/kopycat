package ru.inforion.lab403.kopycat.cores.x86

import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class x86ABI(core: x86Core, heap: LongRange, stack: LongRange, bigEndian: Boolean):
        ABI<x86Core>(core, heap, stack, bigEndian) {
    override fun gpr(index: Int): ARegister<x86Core> = x86Register.gpr(Datatype.DWORD, index)
    override fun createCpuContext() = x86Context(core.cpu)
    override val ssr = SSR.SS.id
    override val sp = x86Register.GPRDW.esp
    override val ra by lazy { throw IllegalAccessError("x86 has no return address register!") }
    override val v0 = x86Register.GPRDW.eax
    override val argl = emptyList<x86Register>()

    override var returnAddressValue: Long
        get() = readPointer(stackPointerValue)
        set(value) { push(value) }

    override fun ret() {
        super.ret()
        stackPointerValue += types.pointer.bytes
    }

    override fun getArgs(args: Array<ArgType>): Array<Long> {
        var res = argl.map { it.value(core) }

        if (args.size > argl.size) {
            val ss = stackStream(where = stackPointerValue + types.pointer.bytes)
            res += args.drop(argl.size).map {  // !!!!!!!!!!!!!!!!!!!!!  Не все аргументы !!!!!!!!!!!!!!1
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