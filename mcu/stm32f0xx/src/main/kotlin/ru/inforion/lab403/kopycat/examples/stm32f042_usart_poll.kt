package ru.inforion.lab403.kopycat.examples

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.stm32f042.STM32F042
import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal

@Suppress("PrivatePropertyName")
class stm32f042_usart_poll(parent: Module?, name: String, tty1: String, tty2: String) : Module(parent, name) {
//    private val u1_stm32 = STM32F042(this, "u1_stm32", Resource("binaries/usart_poll.bin"))
    private val u1_stm32 = STM32F042(this, "u1_stm32", Resource("binaries/usart_dma.bin"))
    private val u2_term = UartSerialTerminal(this, "u2_term", tty1)
    private val u3_term = UartSerialTerminal(this, "u3_term", tty2)

    init {
        buses.connect(u1_stm32.ports.usart1_m, u2_term.ports.term_s)
        buses.connect(u1_stm32.ports.usart1_s, u2_term.ports.term_m)

        buses.connect(u1_stm32.ports.usart2_m, u3_term.ports.term_s)
        buses.connect(u1_stm32.ports.usart2_s, u3_term.ports.term_m)
    }
}