package ru.inforion.lab403.kopycat.benchmarks

import org.junit.Test
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.auxiliary.PerformanceTester
import ru.inforion.lab403.kopycat.cores.base.common.ComponentTracer
import ru.inforion.lab403.kopycat.modules.virtarm.VirtARM
import java.util.logging.Level



class VirtARMPerformanceTest {
    companion object {
        val log = logger(Level.FINE)
    }

    @Test
    fun ubootPerformance() {
        PerformanceTester(0xAFFCC7C0) { VirtARM(null, "top") }.run(50)
    }

    @Test
    fun linuxPerformance() {
        val tester = PerformanceTester(-1, 150_000_000) { VirtARM(null, "top") }
        tester.stopWhenTerminalReceive(tester.top.term.socat!!.pty1, "buildroot login:").run(1, 0)
    }
}