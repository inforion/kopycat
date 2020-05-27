/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.msp430x44x

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.abstracts.APIC
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.BUS16

@Suppress("PropertyName", "unused")


class PIC(parent: Module, name: String) : APIC(parent, name) {
    companion object {
        const val INTERRUPT_COUNT = 256
    }

    inner class Ports : ModulePorts(this) {
        val irq = Slave("irq", INTERRUPT_COUNT)
        val mem = Slave("mem", BUS16)
    }

    override val ports = Ports()

    inner class Interrupt(
            irq: Int,
            override val vector: Int,
            override var priority: Int,
            postfix: String) : AInterrupt(irq, postfix) {
        override val cop get() = core.cop
    }

    val TACCR0_INT = Interrupt(0,0xFFEC, 6, "TimerA TACCR0")
    val TIMERA_INT = Interrupt(1,0xFFEA, 5, "TimerA Regular")
    val USART1TX_INT = Interrupt(2,0xFFE6, 3, "USART1 TX")
    val USART1RX_INT = Interrupt(3,0xFFE4, 2, "USART1 RX")

    val interrupts = Interrupts(ports.irq,  "IRQ", TACCR0_INT, TIMERA_INT, USART1TX_INT, USART1RX_INT)

    val IE1 = object : Register(ports.mem, 0x00, BYTE, "IE1") {
        var WDTIE by bit(0)   //Watchdog-timer interrupt enable
        var OFIE by bit(1)   //Oscillator-fault-interrupt enable
        var NMIIE by bit(4)   //Nonmaskable-interrupt enable
        var ACCVIE by bit(5)   //Flash access violation interrupt enable
        var URXIE0 by bit(6)   //USART0: UART and SPI receive-interrupt enable
        var UTXIE0 by bit(7)   //USART0: UART and SPI transmit-interrupt enable

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            throw GeneralException("IE1 register isn't implemented in InterruptController")
        }
    }

    val IE2 = object : Register(ports.mem, 0x01, BYTE, "IE2") {
        var URXIE1 by bit(4)   //USART0: UART and SPI receive-interrupt enable
        var UTXIE1 by bit(5)   //USART0: UART and SPI transmit-interrupt enable
        var BTIE by bit(7)   //Basic timer interrupt enable

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            USART1RX_INT.enabled = URXIE1.toBool()
            USART1TX_INT.enabled = UTXIE1.toBool()

            if (BTIE == 1)
                throw GeneralException("BTIE isn't implemented in InterruptController")
        }
    }

    val IFG1 = object : Register(ports.mem, 0x02, BYTE, "IFG1") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            throw GeneralException("IFG1 register isn't implemented in InterruptController")
        }
    }

    val IFG2 = object : Register(ports.mem, 0x03, BYTE, "IFG2") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            throw GeneralException("IFG2 register isn't implemented in InterruptController")
        }
    }

    val ME1 = object : Register(ports.mem, 0x04, BYTE, "ME1") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            throw GeneralException("ME1 register isn't implemented in InterruptController")
        }
    }

    val ME2 = object : Register(ports.mem, 0x05, BYTE, "ME2") {
        var URXSPIE1 by bit(4)   //USART1: UART mode receive enable and SPI mode transmit and receive enable
        var UTXE1 by bit(5)   //USART1: UART mode transmit enable
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            //TODO: enable and disable USART
        }
    }

    override fun command(): String = "ic"
}