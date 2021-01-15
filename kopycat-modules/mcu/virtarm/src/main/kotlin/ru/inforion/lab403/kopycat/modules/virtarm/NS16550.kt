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
package ru.inforion.lab403.kopycat.modules.virtarm

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.logging.Level



// Based on https://linux-sunxi.org/images/d/d2/Dw_apb_uart_db.pdf
class NS16550(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient val log = logger(Level.SEVERE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x100)
        val tx = Master("tx", UART_MASTER_BUS_SIZE)
        val rx = Slave("rx", UART_SLAVE_BUS_SIZE)
        val irq = Master("irq", PIN)
    }
    override val ports = Ports()

    inner class RBR_THR_DLL_Register : Register(ports.mem, 0x00, Datatype.DWORD, "UART_RBR_THR_DLL") {
        var dll: Long = 0x00
        val bytesOut = mutableListOf<Char>() // to terminal
        val bytesIn = mutableListOf<Char>() // from terminal

        override fun read(ea: Long, ss: Int, size: Int): Long {
            // Driven by DDL
            return if (LCR.data[7].toBool()) {
                NS16550.log.info { "Read from DLL: ${dll.hex8}" }
                dll
            }
            // Driven by RBR
            else if (bytesIn.isEmpty())
                0L
            else
                bytesIn.removeAt(0).toLong()
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            // Driven by DLL
            if (LCR.data[7].toBool()) {
                NS16550.log.info { "Write to DLL: ${value.hex8}" }
                dll = value
            }
            // Driven by THR
            else {
                if (value.toChar() == '\n') {
                    log.finest { "${this.name}:\n${bytesOut.joinToString("")}" }
                    bytesOut.clear()
                    writeData('\r'.toInt())
                }
                else
                    bytesOut.add(value.toByte().toChar())
                writeData(value.toByte().toInt())
                if (IER.ier and 0x2L != 0L) {
                    IIR.iir = 0b0000
                    ports.irq.request(0)
                }
            }
        }

        override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
                "dll" to dll,
                "bytesOut" to bytesOut,
                "bytesIn" to bytesIn
        )

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super.deserialize(ctxt, snapshot)
            dll = loadValue(snapshot, "dll")

            bytesOut.clear()
            bytesIn.clear()
            bytesOut.addAll((snapshot["bytesOut"] as ArrayList<String>).map { it[0] })
            bytesIn.addAll((snapshot["bytesIn"] as ArrayList<String>).map { it[0] })
        }
    }
    // Receive Buffer Register (DWORD, R)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 0
    val RBR = RBR_THR_DLL_Register()
    // Transmit Holding Register (DWORD, W)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 0
    val THR
        get() = RBR
    // Divisor Latch (Low) (DWORD, R/W)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 1
    val DLL
        get() = RBR



    inner class UART_IER_DLH: Register(ports.mem, 0x04, Datatype.DWORD, "UART_IER_DLH") {
        var ier: Long = 0x00
        var dlh: Long = 0x00

        override fun read(ea: Long, ss: Int, size: Int): Long {
            // Driven by DLH
            return if (LCR.data[7].toBool()) {
                NS16550.log.info { "Read from DLH: ${dlh.hex8}" }
                dlh
            }
            // Driven by IER
            else {
                NS16550.log.info { "Read from IER: ${ier.hex8}" }
                ier
            }
        }
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            // Driven by DLH
            if (LCR.data[7].toBool()) {
                NS16550.log.info { "Write to DLH: ${value.hex8}" }
                dlh = value
            }
            // Driven by IER
            else {
                NS16550.log.info { "Write to IER: ${value.hex8}" }
                ier = value
            }
        }

        override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
                "ier" to ier,
                "dlh" to dlh
        )

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super.deserialize(ctxt, snapshot)
            ier = loadValue(snapshot, "ier")
            dlh = loadValue(snapshot, "dlh")
        }
    }

    // Interrupt Enable Register (DWORD, R/W)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 0
    val IER = UART_IER_DLH()
    // Divisor Latch (High) (DWORD, R/W)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 1
    val DLH
        get() = IER

    inner class UART_FCR_IIR : Register(ports.mem, 0x08, Datatype.DWORD, "UART_FCR_IIR") {
        var iir: Long = 0x01
        var fcr: Long = 0x00

        // Driven by IIR
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from IIR: ${iir.hex8}" }
            return iir
        }

        // Driven by FCR
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            NS16550.log.info { "Write to FCR: ${value.hex8}" }
            fcr = value
        }

        override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
                "iir" to iir,
                "fcr" to fcr
        )

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super.deserialize(ctxt, snapshot)
            iir = loadValue(snapshot, "iir")
            fcr = loadValue(snapshot, "fcr")
        }
    }


    // FIFO Control Register (DWORD, W)
    // Reset value: 0x0
    val FCR = UART_FCR_IIR()

    // Interrupt Identification Register (DWORD, R)
    // Reset value: 0x01
    val IIR
        get() = FCR

    // Line Control Register (DWORD, R/W)
    // Reset value: 0x0
    val LCR = object : Register(ports.mem, 0x0C, Datatype.DWORD, "UART_LCR") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            NS16550.log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }

    // Modem Control Register (DWORD, R/W)
    // Reset value: 0x0
    val MCR = object : Register(ports.mem, 0x10, Datatype.DWORD, "UART_MCR") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            NS16550.log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }

     inner class LSR_Register : Register(ports.mem, 0x14, Datatype.DWORD, "UART_LSR", 0x60, writable = false) {
        var RFE by bit(7)
        var TEMT by bit(6)
        var THRE by bit(5)
        var BI by bit(4)
        var FE by bit(3)
        var PE by bit(2)
        var OE by bit(1)
        var DR by bit(0)


        override fun read(ea: Long, ss: Int, size: Int): Long {
            DR = RBR.bytesIn.isNotEmpty().toInt()
            return super.read(ea, ss, size)
        }
    }
    // Line Status Register (DWORD, R)
    // Reset value: 0x60
    val LSR = LSR_Register()

    // Modem Status Register (DWORD, R)
    // Reset value: 0x0
    val MSR = object : Register(ports.mem, 0x18, Datatype.DWORD, "UART_MSR", writable = false) {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
    }

    // Scratchpad Register (DWORD, R/W)
    // Reset value: 0x0
    val SPR = object : Register(ports.mem, 0x1C, Datatype.DWORD, "UART_SPR") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            NS16550.log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }



    private fun writeData(value: Int) = ports.tx.write(UART_MASTER_BUS_DATA, 0, 1, value.asULong)
    private fun readData() = ports.tx.read(UART_MASTER_BUS_DATA, 0, 1).asInt

    val TERMINAL_REQUEST_REG = object : Register(
            ports.rx,
            UART_SLAVE_BUS_REQUEST,
            Datatype.DWORD,
            "TERMINAL_REQUEST_REG",
            readable = false,
            level = Level.SEVERE
    ) {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (ss) {
                UART_SLAVE_DATA_RECEIVED -> {
                    val x = readData().toChar()

                    when (x) {
                        '\r' -> {
                            RBR.bytesIn.add('\n')
                        }
                        else -> RBR.bytesIn.add(x)
                    }

                    if (IER.ier and 0x1L != 0L) {
                        IIR.iir = 0b0100
                        ports.irq.request(0)
                    }
                }

                UART_SLAVE_DATA_TRANSMITTED -> {
                }
            }
        }
    }


    fun sendText(string: String) = RBR.bytesIn.addAll(string.toCharArray().toList())
}
