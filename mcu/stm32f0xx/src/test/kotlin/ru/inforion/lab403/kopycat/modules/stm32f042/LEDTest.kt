package ru.inforion.lab403.kopycat.modules.stm32f042

import org.junit.Test
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level
import kotlin.test.expect

class LEDTest : Module(null, "LED_test_module") {
    companion object {
        private val log = logger(Level.ALL)
    }

    class SimpleControllerTestModule(parent: Module?) : Module(parent, "controller_test_module") {
        inner class Ports : ModulePorts(this) {
            val pin = Master("pin", PIN)
        }

        override val ports = Ports()

    }

    val controller = SimpleControllerTestModule(this)
    val led = LED(this, "led")

    init {
        buses.connect(led.ports.pin, controller.ports.pin)
        buses.resolveSlaves()
    }

    fun write(value: Long) = controller.ports.pin.write(0, 0, 0, value)
    fun read() = controller.ports.pin.read(0, 0, 0)

    @Test
    fun ledSwitchesCorrectly() {
        write(1)
        expect(LED.Companion.STATE.ON) { led.state }
        expect(1) { read() }

        write(-1)
        expect(LED.Companion.STATE.UNKNOWN) { led.state }
        expect(-1) { read() }

        write(0)
        expect(LED.Companion.STATE.OFF) { led.state }
        expect(0) { read() }

        write(24)
        expect(LED.Companion.STATE.UNKNOWN) { led.state }
        expect(-1) { read() }
    }
}