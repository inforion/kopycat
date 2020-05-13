package ru.inforion.lab403.kopycat.benchmarks

import org.junit.Test
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.modules.virtarm.VirtARM

class VirtARMSerializationTest {
    companion object {
        val log = logger()
    }

    private fun execute() {
        val exitPoint = 0xAFFCC7C0

        val kopycat = Kopycat(null, "temp").apply {
            val top = VirtARM(null, "top")
            open(top, false, null)
        }

        kopycat.reset()

        kopycat.save("VirtARMSerializationTest.zip")
        kopycat.run { step, core -> core.pc != exitPoint }
        assert(!kopycat.hasException()) { "fault = ${kopycat.exception()}" }

        kopycat.load("VirtARMSerializationTest.zip")
        kopycat.run { step, core -> core.pc != exitPoint }
        assert(!kopycat.hasException()) { "fault = ${kopycat.exception()}" }

        kopycat.save("VirtARMSerializationTest_TLB.zip")
        kopycat.load("VirtARMSerializationTest_TLB.zip")
        kopycat.load("VirtARMSerializationTest.zip")

        kopycat.run { step, core -> core.pc != exitPoint }
        assert(!kopycat.hasException()) { "fault = ${kopycat.exception()}" }
    }

    @Test
    fun virtARMSerializationTest() = execute()
}