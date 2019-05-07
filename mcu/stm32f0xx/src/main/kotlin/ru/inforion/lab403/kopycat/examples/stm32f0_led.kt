package ru.inforion.lab403.kopycat.examples

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.stm32f042.LED
import ru.inforion.lab403.kopycat.modules.stm32f042.STM32F042

@Suppress("PrivatePropertyName")
class stm32f0_led(parent: Module?, name: String) : Module(parent, name) {
    val stm32f042 = STM32F042(this, "u1_stm32", Resource("binaries/gpiox_led.bin"))
    private val led_0 = LED(this, "led_0")

    init {
        led_0.ports.pin.connect(stm32f042.buses.pin_output_a, 0)
        (1..15).forEach { LED(this, "led_$it").ports.pin.connect(stm32f042.buses.pin_output_a, it.toLong()) }
    }
}