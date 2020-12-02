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

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.varags
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.extensions.pending
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level



class USART1(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient private val log = logger(Level.FINE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS16)
        val irqRX = Master("irqRX", PIN)
        val irqTX = Master("irqTX", PIN)
    }

    override val ports = Ports()

    //TODO: To off timer (Clock) while is not using
    private inner class Timer : SystemClock.PeriodicalTimer("USART1 Counter") {

        override fun trigger() {
            super.trigger()

            if (!txBuffer.isEmpty()) {// && (txBuffer.last() == '\n')) {
                //txBuffer.deleteCharAt(txBuffer.lastIndex)
                log.info { "UART: $txBuffer" }
                txBuffer.setLength(0)
            }

            if (rxBuffer.isEmpty() || ports.irqRX.pending(0))
                return

            U1RXBUF.data = rxBuffer[0].toLong()
            rxBuffer.deleteCharAt(0)
            ports.irqRX.request(0)
        }
    }

    private val timer = Timer()
    val rxBuffer = StringBuffer(256)
    val txBuffer = StringBuffer(256)

    //USART Control register
    private val U1CTL = object : Register(ports.mem, 0x78, BYTE, "U1CTL") {
        var SWRST by bit(0)   //Software reset
        var MM by bit(1)   //Multiprocessor mode
        var SYNC by bit(2)   //Sync mode
        var Listen by bit(3)   //Feed back to receiver
        var CHAR by bit(4)   //Character length
        var SP by bit(5)   //Number of stop bits
        var PEV by bit(6)   //Parity odd/even
        var PENA by bit(7)   //Parity enable

        override fun stringify(): String = "${super.stringify()} [SWRST=$SWRST MM=$MM SYNC=$SYNC Listen=$Listen CHAR=$CHAR SP=$SP PEV=$PEV PENA=$PENA]"

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            //Software reset
            if (SWRST == 1) {
                data = 1
                U1TCTL.TXEPT = 1
                U1TCTL.TXWake = 0
                //TODO: Anything else for reset?
            }

            //Multiprocessor mode
            if (MM == 1)
                throw Exception("MM isn't implemented in USART1.kt")

            //Sync mode
            if (SYNC == 1)
                throw Exception("SYNC (SPI mode) isn't implemented in USART1.kt")

            //Parity odd/even
            if (PEV == 1)
                throw Exception("PEV isn't implemented in USART1.kt")

            //Parity enable
            if (PENA == 1)
                throw Exception("PENA isn't implemented in USART1.kt")
        }

    }

    //Transmit Control Register
    private val U1TCTL = object : Register(ports.mem, 0x79, BYTE, "U1TCTL") {
        var TXEPT by bit(0)      //TX buffer empty
        var TXWake by bit(2)     //Wake for multiprocessor mode
        var URXSE by bit(3)      //Interrupt routine
        var SSEL by field(5..4)  //Source select
        var CKPL by bit(6)       //Clock polarity

        override fun stringify(): String = "${super.stringify()} [TXEPT=$TXEPT TXWake=$TXWake URXSE=$URXSE SSEL=$SSEL CKPL=$CKPL]"

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            //Wake for multiprocessor mode
            if (TXWake == 1) throw NotImplementedError("TXWake isn't implemented in USART1")

            //Interrupt routine
            if (URXSE == 1) throw NotImplementedError("URXSE isn't implemented in USART1")

            if (CKPL == 1) throw NotImplementedError("CKPL isn't implemented in USART1")

        }
    }

    override fun initialize(): Boolean {
        if (!super.initialize()) return false
        core.clock.connect(timer, 1)
        return true
    }

    //Receive Control Register
    private val U1RCTL = object : Register(ports.mem,0x7A, BYTE, "U1RCTL") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long): Unit =
                throw NotImplementedError("U1RCTL isn't implemented in USART1")
    }

    //Modulation Control Register
    private val U1MCTL = object : Register(ports.mem,0x7B, BYTE, "U1MCTL") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long): Unit =
                throw NotImplementedError("U1MCTL isn't implemented in USART1")
    }

    //Baud Rate Select Register 0
    private val U1BR0 = Register(ports.mem,0x7C, BYTE, "U1BR0")

    //Baud Rate Select Register 1
    private val U1BR1 = Register(ports.mem,0x7D, BYTE, "U1BR1")

    //Receiver Data buffer
    private val U1RXBUF = Register(ports.mem,0x7E, BYTE, "U1RXBUF")

    //Transmit Data buffer
    private val U1TXBUF = object : Register(ports.mem,0x7F, BYTE, "U1TXBUF") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            txBuffer.append(data.toChar())
            ports.irqTX.request(0)
        }

    }

    override fun command(): String = "uart"

    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent)?.apply {
                varags<String>("message", help = "Transmit message by UART")
            }

    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        val strings: ArrayList<String> = context["message"]
        val message = strings.joinToString(" ")
        rxBuffer.append(message)

        context.result = if (U1CTL.Listen == 1) "uart loopback: $message" else "no uart loopback"

        return true
    }

}