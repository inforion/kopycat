/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Non-free licenses may also be purchased from INFORION, LLC,
 * for users who do not want their programs protected by the GPL.
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.cortexm0.CORTEXM0
import ru.inforion.lab403.kopycat.modules.cores.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.io.File
import java.io.InputStream

class STM32F042 constructor(parent: Module, name: String, vararg parts: Pair<Any, Int>) : Module(parent, name) {

    constructor(parent: Module, name: String, firmware: ByteArray) : this(parent, name, firmware to 0)
    constructor(parent: Module, name: String, firmware: InputStream) : this(parent, name, firmware.readBytes())
    constructor(parent: Module, name: String, firmware: Resource) : this(parent, name, firmware.openStream())
    constructor(parent: Module, name: String, firmware: File) : this(parent, name, firmware.inputStream())

    inner class Ports : ModulePorts(this) {
        val usart1_m = Proxy("usart1_m")
        val usart1_s = Proxy("usart1_s")

        val usart2_m = Proxy("usart2_m")
        val usart2_s = Proxy("usart2_s")

        val gpioa_in = Proxy("gpioa_in")
        val gpioa_out = Proxy("gpioa_out")

        val gpiob_in = Proxy("gpiob_in")
        val gpiob_out = Proxy("gpiob_out")
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
        val irq = Bus("irq")
    }

    override val buses = Buses()

    // Creates ROM before CORTEXM0 because CORTEX require ROM memory for correct initialization
    // and order of initialization correspond order of module definition
    private val rom = RAM(this, "rom", 0x8000, *parts)
    private val sram = RAM(this, "sram", 0x1800)

    private val cortex = CORTEXM0(this, "cortexm0")

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

    val dbg = ARMDebugger(this, "dbg")

    private fun sysInit() {
        cortex.ports.mem.connect(buses.mem)
        rom.ports.mem.connect(buses.mem, 0x0800_0000u)
        sram.ports.mem.connect(buses.mem, 0x2000_0000u)
    }

    private fun bootInit() {
        // TODO for version unknown https://youtrack.lab403.inforion.ru/issue/KC-1567
        rom.ports.mem.connect(buses.mem, 0x0000_0000u)
    }

    private fun gpioInit() {
        gpioa.ports.mem.connect(buses.mem, 0x4800_0000u)
        buses.connect(gpioa.ports.pin_input, ports.gpioa_in)
        buses.connect(gpioa.ports.pin_output, ports.gpioa_out)

        gpiob.ports.mem.connect(buses.mem, 0x4800_0400u)
        buses.connect(gpiob.ports.pin_input, ports.gpiob_in)
        buses.connect(gpiob.ports.pin_output, ports.gpiob_out)
    }

    private fun miscInit() {
        tim2.ports.mem.connect(buses.mem, 0x4000_0000u)
        tim3.ports.mem.connect(buses.mem, 0x4000_0400u)
        iwdg.ports.mem.connect(buses.mem, 0x4000_3000u)
        syscfg.ports.mem.connect(buses.mem, 0x4001_0000u)
        exti.ports.mem.connect(buses.mem, 0x4001_0400u)
        tim1.ports.mem.connect(buses.mem, 0x4001_2C00u)
        rcc.ports.mem.connect(buses.mem, 0x4002_1000u)
//        fmi.ports.mem.connect(buses.mem, 0x4002_2000u)
        flash.ports.mem.connect(buses.mem, 0x4002_2000u)
        tsc.ports.mem.connect(buses.mem, 0x4002_4000u)
    }

    private fun usartInit() {
        usart1.ports.mem.connect(buses.mem, 0x4001_3800u)
        usart2.ports.mem.connect(buses.mem, 0x4000_4400u)

        buses.connect(ports.usart1_m, usart1.ports.usart_m)
        buses.connect(ports.usart1_s, usart1.ports.usart_s)

        buses.connect(ports.usart2_m, usart2.ports.usart_m)
        buses.connect(ports.usart2_s, usart2.ports.usart_s)
    }

    private fun dmacInit() {
        dmac.ports.mem.connect(buses.mem, 0x4002_0000u)
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
        dmac.ports.irq_ch1.connect(buses.irq, 9u)
        dmac.ports.irq_ch2_3.connect(buses.irq, 10u)
        dmac.ports.irq_ch4_5_6_7.connect(buses.irq, 11u)

        tim1.ports.irq_ut.connect(buses.irq, 13u)
        tim1.ports.irq_cc.connect(buses.irq, 14u)
        tim2.ports.irq_cc.connect(buses.irq, 15u)
        tim3.ports.irq_cc.connect(buses.irq, 16u)

        usart1.ports.irq_rx.connect(buses.irq, 27u)
        usart2.ports.irq_rx.connect(buses.irq, 28u)

        cortex.ports.irq.connect(buses.irq)
    }

    private fun dbgInit() {
        dbg.ports.breakpoint.connect(buses.mem)
        dbg.ports.reader.connect(buses.mem)
    }

    init {
        sysInit()
        bootInit()
        gpioInit()
        miscInit()
        usartInit()
        dmacInit()
        irqInit()
        dbgInit()
    }
}