package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x86FprRegister(reg: Int) : ARegister<x86Core>(reg, Access.ANY) {

    override fun value(core: x86Core): Long = core.fpu[reg]

    override fun value(core: x86Core, data: Long) {
        core.fpu[reg] = data
    }

    fun push(core: x86Core, data: Long){
        core.fpu.push(data)
    }

    override fun toString(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}