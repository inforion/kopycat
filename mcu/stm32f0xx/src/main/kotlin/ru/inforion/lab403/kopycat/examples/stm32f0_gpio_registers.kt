package ru.inforion.lab403.kopycat.examples

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.PIN
import ru.inforion.lab403.kopycat.modules.stm32f042.LED
import ru.inforion.lab403.kopycat.modules.stm32f042.STM32F042
import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal

@Suppress("PrivatePropertyName", "PropertyName")
class stm32f0_gpio_registers(parent: Module?, name: String) : Module(parent, name) {
    private val stm32f042 = STM32F042(this, "stm32f042", Resource("binaries/gpiox_registers.bin"))
    private val term = UartSerialTerminal(this, "term", null)

    class PinConnector(parent: Module?) : Module(parent, "controller_test_module") {
        inner class Ports : ModulePorts(this) {
            val pin_input = Slave("pin_input", PIN)
            val pin_output = Master("pin_output", PIN)
        }

        override val ports = Ports()
    }

    private val pinConnector = PinConnector(this)

    private val leds_gpio1 = Array(16) { LED(this, "led${it}_gpio1") }
    private val leds_gpio2 = Array(16) { LED(this, "led${it}_gpio2") }

    init {
        leds_gpio1.forEachIndexed { k, led -> led.ports.pin.connect(stm32f042.buses.pin_output_a, k.asLong) }
        leds_gpio2.forEachIndexed { k, led -> led.ports.pin.connect(stm32f042.buses.pin_output_a, k.asLong) }

        buses.connect(stm32f042.ports.usart1_m, term.ports.term_s)
        buses.connect(stm32f042.ports.usart1_s, term.ports.term_m)

        pinConnector.ports.pin_input.connect(stm32f042.buses.pin_input_a, 13)
        pinConnector.ports.pin_output.connect(stm32f042.buses.pin_output_a, 13)
    }
}