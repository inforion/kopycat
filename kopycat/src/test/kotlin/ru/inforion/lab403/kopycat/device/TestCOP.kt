package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException

class TestCOP(core: TestCore, name: String): ACOP<TestCOP, TestCore>(core, name) {
    override fun processInterrupts() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun handleException(exception: GeneralException?): GeneralException? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}