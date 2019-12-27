package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_BUS_SIZE
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.UART_MASTER_BUS_SIZE
import ru.inforion.lab403.kopycat.modules.UART_SLAVE_BUS_SIZE
import ru.inforion.lab403.kopycat.modules.cortexm0.CORTEXM0
import ru.inforion.lab403.kopycat.modules.cortexm0.NVIC
import ru.inforion.lab403.kopycat.modules.debuggers.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.io.File
import java.io.InputStream

class STM32F042 constructor(parent: Module, name: String, firmware: ByteArray) : Module(parent, name) {

    constructor(parent: Module, name: String, firmware: InputStream) : this(parent, name, firmware.readBytes())
    constructor(parent: Module, name: String, firmware: Resource) : this(parent, name, firmware.inputStream())
    constructor(parent: Module, name: String, firmware: File) : this(parent, name, firmware.inputStream())

    inner class Ports : ModulePorts(this) {
        val usart1_m = Proxy("usart1_m", UART_MASTER_BUS_SIZE)
        val usart1_s = Proxy("usart1_s", UART_SLAVE_BUS_SIZE)

        val usart2_m = Proxy("usart2_m", UART_MASTER_BUS_SIZE)
        val usart2_s = Proxy("usart2_s", UART_SLAVE_BUS_SIZE)

        val trace = Proxy("trace", TRACER_BUS_SIZE)
    }

    override val ports = Ports()

    inner class Buses : ModuleBuses(this) {
        /**
         * System memory bus
         */
        val mem = Bus("mem")

        /**
         * DMA requests bus
         */
        val drq = Bus("drq")

        /**
         * Interrupts request bus
         */
        val irq = Bus("irq", NVIC.INTERRUPT_COUNT)

        /**
         * Physical pins bus gpio a
         * */
        val pin_input_a = Bus("pin_input_a", GPIOx.PIN_COUNT)
        val pin_output_a = Bus("pin_output_a", GPIOx.PIN_COUNT)

        /**
         * Physical pins bus gpio b
         * */
        val pin_input_b = Bus("pin_input_b", GPIOx.PIN_COUNT)
        val pin_output_b = Bus("pin_output_b", GPIOx.PIN_COUNT)
    }

    override val buses = Buses()

    private val cortex = CORTEXM0(this, "cortexm0")

    private val rom = RAM(this, "rom", 0x8000, firmware)
    private val sram = RAM(this, "sram", 0x1800)

    private val usart1 = USARTx(this, "usart1", 1)
    private val usart2 = USARTx(this, "usart2", 2)

    private val gpioa = GPIOx(this, "gpioa", 1)
    private val gpiob = GPIOx(this, "gpiob", 2)

    private val tim1 = TIMx(this, "tim1", 1)
    private val tim2 = TIMx(this, "tim2", 2)
    private val tim3 = TIMx(this, "tim3", 3)

    private val rcc = RCC(this, "rcc")
    private val fmi = FMI(this, "fmi")
    private val syscfg = SYSCFG(this, "syscfg")
    private val exti = EXTI(this, "exti")
    private val dmac = DMAC(this, "dmac", 5)
    private val tsc = TSC(this, "tsc")
    private val iwdg = IWDG(this, "iwdg")
    private val flash = FLASH(this, "flash")

    private val dbg = ARMDebugger(this, "dbg")

    private fun sysInit() {
        cortex.ports.mem.connect(buses.mem)
        rom.ports.mem.connect(buses.mem, 0x0800_0000)
        sram.ports.mem.connect(buses.mem, 0x2000_0000)
    }

    private fun gpioInit() {
        gpioa.ports.mem.connect(buses.mem, 0x4800_0000)
        gpioa.ports.pin_input.connect(buses.pin_input_a)
        gpioa.ports.pin_output.connect(buses.pin_output_a)

        gpiob.ports.mem.connect(buses.mem, 0x4800_0400)
        gpiob.ports.pin_input.connect(buses.pin_input_b)
        gpiob.ports.pin_output.connect(buses.pin_output_b)
    }

    private fun miscInit() {
        tim2.ports.mem.connect(buses.mem, 0x4000_0000)
        tim3.ports.mem.connect(buses.mem, 0x4000_0400)
        iwdg.ports.mem.connect(buses.mem, 0x4000_3000)
        syscfg.ports.mem.connect(buses.mem, 0x4001_0000)
        exti.ports.mem.connect(buses.mem, 0x4001_0400)
        tim1.ports.mem.connect(buses.mem, 0x4001_2C00)
        rcc.ports.mem.connect(buses.mem, 0x4002_1000)
//        fmi.ports.mem.connect(buses.mem, 0x4002_2000)
        flash.ports.mem.connect(buses.mem, 0x4002_2000)
        tsc.ports.mem.connect(buses.mem, 0x4002_4000)
    }

    private fun usartInit() {
        usart1.ports.mem.connect(buses.mem, 0x4001_3800)
        usart2.ports.mem.connect(buses.mem, 0x4000_4400)

        buses.connect(ports.usart1_m, usart1.ports.usart_m)
        buses.connect(ports.usart1_s, usart1.ports.usart_s)

        buses.connect(ports.usart2_m, usart2.ports.usart_m)
        buses.connect(ports.usart2_s, usart2.ports.usart_s)
    }

    private fun dmacInit() {
        dmac.ports.mem.connect(buses.mem, 0x4002_0000)
        dmac.ports.io.connect(buses.mem)
        dmac.ports.drq.connect(buses.drq)

        // =============================================================================================================

//        adc.ports.drq.connect(buses.drq, DMAC.CHANNEL1)
//        adc.ports.drq.connect(buses.drq, DMAC.CHANNEL2)

        // =============================================================================================================

//        spi1.ports.drq_rx.connect(buses.drq, DMAC.CHANNEL2)
//        spi1.ports.drq_tx.connect(buses.drq, DMAC.CHANNEL3)

//        spi2.ports.drq_rx.connect(buses.drq, DMAC.CHANNEL4)
//        spi2.ports.drq_tx.connect(buses.drq, DMAC.CHANNEL5)

        // =============================================================================================================

        usart1.ports.drq_tx.connect(buses.drq, DMAC.CHANNEL2)
        usart1.ports.drq_rx.connect(buses.drq, DMAC.CHANNEL3)

        usart1.ports.drq_tx.connect(buses.drq, DMAC.CHANNEL4)
        usart2.ports.drq_tx.connect(buses.drq, DMAC.CHANNEL4)

        usart1.ports.drq_rx.connect(buses.drq, DMAC.CHANNEL5)
        usart2.ports.drq_rx.connect(buses.drq, DMAC.CHANNEL5)

        // =============================================================================================================

//        i2c1.ports.drq_tx.connect(buses.drq, DMAC.CHANNEL2)
//        i2c1.ports.drq_rx.connect(buses.drq, DMAC.CHANNEL3)
//
//        i2c1.ports.drq_tx.connect(buses.drq, DMAC.CHANNEL4)
//        i2c1.ports.drq_rx.connect(buses.drq, DMAC.CHANNEL5)

        // =============================================================================================================

        tim1.ports.drq.connect(buses.drq, DMAC.CHANNEL2)
        tim1.ports.drq.connect(buses.drq, DMAC.CHANNEL3)
//        tim1.ports.drq.connect(buses.drq, DMAC.CHANNEL4)
//        tim1.ports.drq.connect(buses.drq, DMAC.CHANNEL4)
        tim1.ports.drq.connect(buses.drq, DMAC.CHANNEL4)
        tim1.ports.drq.connect(buses.drq, DMAC.CHANNEL5)
//        tim1.ports.drq.connect(buses.drq, DMAC.CHANNEL5)

        // =============================================================================================================

        tim2.ports.drq.connect(buses.drq, DMAC.CHANNEL1)
        tim2.ports.drq.connect(buses.drq, DMAC.CHANNEL2)
        tim2.ports.drq.connect(buses.drq, DMAC.CHANNEL3)
        tim2.ports.drq.connect(buses.drq, DMAC.CHANNEL4)
        tim2.ports.drq.connect(buses.drq, DMAC.CHANNEL5)

        // =============================================================================================================

        tim3.ports.drq.connect(buses.drq, DMAC.CHANNEL2)
        tim3.ports.drq.connect(buses.drq, DMAC.CHANNEL3)
//        tim3.ports.drq.connect(buses.drq, DMAC.CHANNEL3)
//        tim3.ports.drq.connect(buses.drq, DMAC.CHANNEL4)
        tim3.ports.drq.connect(buses.drq, DMAC.CHANNEL4)

        // =============================================================================================================

//        tim6.ports.drq_up.connect(buses.drq, DMAC.CHANNEL3)
//        dac.ports.drq_ch1.connect(buses.drq, DMAC.CHANNEL3)

        // =============================================================================================================

//        tim15.ports.drq_ch1.connect(buses.drq, DMAC.CHANNEL5)
//        tim15.ports.drq_up.connect(buses.drq, DMAC.CHANNEL5)
//        tim15.ports.drq_trig.connect(buses.drq, DMAC.CHANNEL5)
//        tim15.ports.drq_com.connect(buses.drq, DMAC.CHANNEL5)

        // =============================================================================================================

//        tim16.ports.drq_ch1.connect(buses.drq, DMAC.CHANNEL3)
//        tim16.ports.drq_up.connect(buses.drq, DMAC.CHANNEL3)
//
//        tim16.ports.drq_ch1.connect(buses.drq, DMAC.CHANNEL4)
//        tim16.ports.drq_up.connect(buses.drq, DMAC.CHANNEL4)

        // =============================================================================================================

//        tim17.ports.drq_ch1.connect(buses.drq, DMAC.CHANNEL1)
//        tim17.ports.drq_up.connect(buses.drq, DMAC.CHANNEL1)
//
//        tim17.ports.drq_ch1.connect(buses.drq, DMAC.CHANNEL2)
//        tim17.ports.drq_up.connect(buses.drq, DMAC.CHANNEL2)
    }

    private fun irqInit() {
        dmac.ports.irq_ch1.connect(buses.irq, 9)
        dmac.ports.irq_ch2_3.connect(buses.irq, 10)
        dmac.ports.irq_ch4_5_6_7.connect(buses.irq, 11)

        usart1.ports.irq_rx.connect(buses.irq, 27)
        usart2.ports.irq_rx.connect(buses.irq, 28)

        tim1.ports.irq.connect(buses.irq, 14)
        tim2.ports.irq.connect(buses.irq, 15)
        tim3.ports.irq.connect(buses.irq, 16)

        cortex.ports.irq.connect(buses.irq)
    }

    private fun dbgInit() {
        buses.connect(ports.trace, dbg.ports.trace)
        dbg.ports.breakpoint.connect(buses.mem)
        dbg.ports.reader.connect(buses.mem)
    }

    init {
        sysInit()
        gpioInit()
        miscInit()
        usartInit()
        dmacInit()
        irqInit()
        dbgInit()
    }
}