/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
@file:Suppress("PropertyName", "unused", "MemberVisibilityCanBePrivate")

package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.SEVERE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.serializer.NotDeserializableObjectException
import ru.inforion.lab403.kopycat.serializer.NotSerializableObjectException
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.logging.Level


// Based on https://linux-sunxi.org/images/d/d2/Dw_apb_uart_db.pdf
class NS16550(parent: Module, name: String, val regDtype: Datatype) : Module(parent, name) {

    companion object {
        @Transient val log = logger(SEVERE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x100)
        val tx = Master("tx", UART_MASTER_BUS_SIZE)
        val rx = Slave("rx", UART_SLAVE_BUS_SIZE)
        val irq = Master("irq", PIN)
    }

    override val ports = Ports()


    private inner class DelayTimer(/*val delay: ULong*//*, val action: (() -> Unit)? = null*/):
        SystemClock.OneshotTimer("delay timer") {
        override fun trigger() {
            log.finer { "$name triggered at %,d us".format(core.clock.time()) }
            super.trigger()
//            action?.invoke()
            ports.irq.request(0)
        }

    }
    private val delayTimer = DelayTimer()

    inner class RBR_THR_DLL_Register : Register(ports.mem, 0x00u, regDtype, "UART_RBR_THR_DLL") {
        var dll: ULong = 0x00u
        val bytesOut = mutableListOf<Char>() // to terminal
        val bytesIn = mutableListOf<Char>() // from terminal

        override fun read(ea: ULong, ss: Int, size: Int) = when {
            LCR.data[7].truth -> {
                // Driven by DDL
                log.info { "Read from DLL: ${dll.hex8}" }
                dll
            }
            bytesIn.isEmpty() -> 0uL // Driven by RBR
            else -> bytesIn.removeAt(0).ulong_z8
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            if (LCR.data[7].truth) {
                // Driven by DLL
                log.info { "Write to DLL: ${value.hex8}" }
                dll = value
            } else {
                // Driven by THR
                if (value.char == '\n') {
                    log.finest { "${this.name}:\n${bytesOut.joinToString("")}" }
                    bytesOut.clear()
                    writeData('\r')
                } else bytesOut.add(value.char)

                writeData(value.char)

                if (IER.ier and 0x2uL != 0uL) {
                    IIR.iir = 0b0000u
                    delayTimer.enabled = true
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
    val DLL get() = RBR


    inner class UART_IER_DLH: Register(ports.mem, regDtype.bytes.ulong_z * 1u, regDtype, "UART_IER_DLH") {
        var ier = 0x00uL
        var dlh = 0x00uL

        override fun read(ea: ULong, ss: Int, size: Int) = when {
            LCR.data[7].truth -> {
                // Driven by DLH
                log.info { "Read from DLH: ${dlh.hex8}" }
                dlh
            }
            else -> {
                // Driven by IER
                log.info { "Read from IER: ${ier.hex8}" }
                ier
            }
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = when {
            LCR.data[7].truth -> {
                // Driven by DLH
                log.info { "Write to DLH: ${value.hex8}" }
                dlh = value
            }
            else -> {
                // Driven by IER
                log.info { "Write to IER: ${value.hex8}" }
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
    val DLH get() = IER

    inner class UART_FCR_IIR : Register(ports.mem, regDtype.bytes.ulong_z * 2u, regDtype, "UART_FCR_IIR") {
        var iir: ULong = 0x01u
        var fcr: ULong = 0x00u

        // Driven by IIR
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Read from IIR: ${iir.hex8}" }
            return iir
        }

        // Driven by FCR
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.info { "Write to FCR: ${value.hex8}" }
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
    val IIR get() = FCR

    // Line Control Register (DWORD, R/W)
    // Reset value: 0x0
    val LCR = object : Register(ports.mem, regDtype.bytes.ulong_z * 3u, regDtype, "UART_LCR") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }

    // Modem Control Register (DWORD, R/W)
    // Reset value: 0x0
    val MCR = object : Register(ports.mem, regDtype.bytes.ulong_z * 4u, regDtype, "UART_MCR") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }

     inner class LSR_Register : Register(ports.mem, regDtype.bytes.ulong_z * 5u, regDtype, "UART_LSR", 0x60u, writable = false) {
        var RFE by bit(7)
        var TEMT by bit(6)
        var THRE by bit(5)
        var BI by bit(4)
        var FE by bit(3)
        var PE by bit(2)
        var OE by bit(1)
        var DR by bit(0)


        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            DR = RBR.bytesIn.isNotEmpty().int
            return super.read(ea, ss, size)
        }
    }
    // Line Status Register (DWORD, R)
    // Reset value: 0x60
    val LSR = LSR_Register()

    // Modem Status Register (DWORD, R)
    // Reset value: 0x0
    val MSR = object : Register(ports.mem, regDtype.bytes.ulong_z * 6u, regDtype, "UART_MSR", writable = false) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
    }

    // Scratchpad Register (DWORD, R/W)
    // Reset value: 0x0
    val SPR = object : Register(ports.mem, regDtype.bytes.ulong_z * 7u, regDtype, "UART_SPR") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }

    private fun writeData(value: Int) = ports.tx.write(UART_MASTER_BUS_DATA, 0, 1, value.ulong_z)
    private fun readData() = ports.tx.read(UART_MASTER_BUS_DATA, 0, 1).int

    private fun writeData(value: Char) = writeData(value.int_z8)

    val TERMINAL_REQUEST_REG = object : Register(
            ports.rx,
            UART_SLAVE_BUS_REQUEST,
            Datatype.DWORD,
            "TERMINAL_REQUEST_REG",
            readable = false,
            level = Level.SEVERE
    ) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            when (ss) {
                UART_SLAVE_DATA_RECEIVED -> {
                    when (val x = readData().char) {
                        '\r' -> RBR.bytesIn.add('\n')
                        else -> RBR.bytesIn.add(x)
                    }

                    if (IER.ier and 0x1uL != 0uL) {
                        IIR.iir = 0b0100u
                        delayTimer.enabled = true
                    }
                }

                UART_SLAVE_DATA_TRANSMITTED -> {

                }
            }
        }
    }

    fun sendText(string: String) = RBR.bytesIn.addAll(string.toCharArray().toList())

    override fun initialize(): Boolean {
        super.initialize()
        core.clock.connect(delayTimer, 1000, false)
        return true
    }
}
