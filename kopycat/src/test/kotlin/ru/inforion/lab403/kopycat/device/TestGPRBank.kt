package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank

class TestGPRBank(core: TestCore) : ARegistersBank<TestCore, TestGPR>(core, TestGPR.values(), bits = 32) {
    override val name: String = "Test Registers"
    var r0 by valueOf(TestRegister.GPR.r0)
    var r1 by valueOf(TestRegister.GPR.r1)
    var pc by valueOf(TestRegister.GPR.pc)
}