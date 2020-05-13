package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU

class TestCPU(core: TestCore, name: String): ACPU<TestCPU, TestCore, TestInstruction, TestGPR>(core, name) {
    override fun reg(index: Int): Long = regs[index].value(core as TestCore)
    override fun reg(index: Int, value: Long) = regs[index].value(core as TestCore, value)
    override fun count() = regs.count()

    override var pc: Long
        get() = regs.pc
        set(value) { regs.pc = value }

    val regs = TestGPRBank(core)

    override fun decode() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun execute(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}