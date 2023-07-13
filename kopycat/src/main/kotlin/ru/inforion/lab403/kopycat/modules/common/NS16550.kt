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
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.logging.Level

// Based on https://linux-sunxi.org/images/d/d2/Dw_apb_uart_db.pdf
class NS16550(parent: Module, name: String, val regDtype: Datatype) : Module(parent, name) {
    companion object {
        @Transient private val log = logger(SEVERE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x100)
        val tx = Master("tx", UART_MASTER_BUS_SIZE)
        val rx = Slave("rx", UART_SLAVE_BUS_SIZE)
        val irq = Master("irq", PIN)
    }

    override val ports = Ports()

    /**
     * Таймер для троттлинга байтов хост->эмулируемая система.
     * Без него эмулируемая система не успевает обрабатывать входящий поток.
     *
     * Пример симптома: `ttyS0: 3 input overrun(s)`.
     */
    private val t2mTimer = object : SystemClock.PeriodicalTimer("terminal 2 machine timer") {
        override fun trigger() {
            if (RBR_THR_DLL.bytesIn.isNotEmpty()) {
                if (FCR_IIR.fcr[0].untruth && LSR.DR.truth) {
                    LSR.OE = 1
                }

                LSR.DR = 1
                log.info { "T2M: ${RBR_THR_DLL.bytesIn[0].ulong_z8.hex2}" }
                timeoutIntr = true
                checkIrq()
            }
        }
    }

    /** Флаг прерывания "THR пуст" */
    private var thrIntr = false

    /** Флаг прерывания таймаута ("character timeout") */
    private var timeoutIntr = false

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
        "thrIntr" to thrIntr,
        "timeoutIntr" to timeoutIntr,
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        thrIntr = loadValue(snapshot, "thrIntr") { false }
        timeoutIntr = loadValue(snapshot, "timeoutIntr") { false }
    }

    private fun checkIrq() {
        val iir = when {
            IER_DLH.ier[2].truth && (LSR.data and 0x1EuL).truth -> 0x06uL
            IER_DLH.ier[0].truth && timeoutIntr -> 0x0CuL
            IER_DLH.ier[0].truth && LSR.DR.truth && FCR_IIR.fcr[0].untruth -> 0x04uL
            IER_DLH.ier[1].truth && thrIntr -> {
                // MSR.data = MSR.data or 0b1011uL
                // log.severe { "MSR update interrupt sent" }
                // 0x00uL
                0x02uL
            }
            IER_DLH.ier[3].truth && (MSR.data and 0x0FuL).truth -> 0x00uL
            else -> 0x01uL
        }

        FCR_IIR.iir = iir or (FCR_IIR.iir and 0xF0uL)

        if (iir != 0x01uL) {
            log.info { "Interrupt, IIR = ${iir.hex2}" }
            ports.irq.request(0)
        }
    }

    /** Регистры RBR, THR, DLL */
    private inner class RBR_THR_DLL_Register : Register(ports.mem, 0x00u, regDtype, "RBR_THR_DLL") {
        /**
         * Divisor Latch (Low) (DWORD, R/W).
         *
         * Dependencies: LCR[7] = 1
         */
        var dll: ULong = 0x00u

        /** Буфер с данными хост->эмулируемая система. */
        val bytesIn = mutableListOf<Char>()

        override fun read(ea: ULong, ss: Int, size: Int) = when {
            LCR.DLAB.truth -> {
                // Driven by DDL
                log.info { "Read from DLL: ${dll.hex8}" }
                dll
            }
            else -> {
                val ret = if (bytesIn.isEmpty()) 0uL else bytesIn.removeAt(0).ulong_z8

                log.info { "Read from RBR: ${ret.hex2}" }

                if (FCR_IIR.fcr[0].truth) {
                    if (bytesIn.isEmpty()) {
                        LSR.DR = 0
                        LSR.BI = 0
                    }
                    timeoutIntr = false
                } else {
                    LSR.DR = 0
                    LSR.BI = 0
                }

                checkIrq()
                ret
            }
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            if (LCR.DLAB.truth) {
                // Driven by DLL
                log.info { "Write to DLL: ${value.hex8}" }
                dll = value
            } else {
                // Driven by THR
                log.info { "Write to THR: ${value.hex2}" }

                if (value.char == '\n') {
                    writeData('\r')
                }

                thrIntr = true
                LSR.THRE = 1
                LSR.TEMT = 1
                checkIrq()

                writeData(value.char)
            }
        }

        override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
            "dll" to dll,
            "bytesIn" to bytesIn,
        )

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super.deserialize(ctxt, snapshot)
            dll = loadValue(snapshot, "dll")

            bytesIn.clear()
            bytesIn.addAll((snapshot["bytesIn"] as ArrayList<String>).map { it[0] })
        }
    }

    /** Регистры RBR, THR, DLL */
    private val RBR_THR_DLL = RBR_THR_DLL_Register()

    /** Регистры IER, DLH */
    private inner class IER_DLH_Register : Register(
        ports.mem,
        regDtype.bytes.ulong_z * 1u,
        regDtype,
        "IER_DLH",
    ) {
        /**
         * Interrupt Enable Register (DWORD, R/W).
         *
         * Dependencies: LCR[7] = 0
         */
        var ier = 0x00uL

        /**
         * Divisor Latch (High) (DWORD, R/W).
         *
         * Dependencies: LCR[7] = 1
         */
        var dlh = 0x00uL

        override fun read(ea: ULong, ss: Int, size: Int) = when {
            LCR.DLAB.truth -> {
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
            LCR.DLAB.truth -> {
                // Driven by DLH
                log.info { "Write to DLH: ${value.hex8}" }
                dlh = value
            }
            else -> {
                // Driven by IER
                log.info { "Write to IER: ${value.hex8}" }

                val changed = ((ier xor value) and 0x0fuL)
                ier = value and 0x0fuL

                if (changed[1].truth) {
                    thrIntr = ier[1].truth && LSR.THRE.truth
                }

                if (changed.truth) {
                    checkIrq()
                }

                Unit
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

    /** Регистры IER, DLH */
    private val IER_DLH = IER_DLH_Register()

    /** Регистры FCR, IIR */
    inner class FCR_IIR_Register : Register(ports.mem, regDtype.bytes.ulong_z * 2u, regDtype, "FCR_IIR") {
        /** Interrupt Identification Register (DWORD, R) */
        var iir: ULong = 0x01u

        /** FIFO Control Register (DWORD, W) */
        var fcr: ULong = 0x00u

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            // Driven by IIR
            log.info { "Read from IIR: ${iir.hex2}" }

            // if (iir and 0x06uL == 0x02uL) {
                // thrIntr = false
                // checkIrq()
            // }

            return iir
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            // Driven by FCR
            log.info { "Write to FCR: ${value.hex2}" }

            var newValue = value

            if ((newValue xor fcr)[0].truth) {
                newValue = newValue or 0x04uL or 0x02uL
            }

            if (newValue[1].truth) {
                LSR.DR = 0
                LSR.BI = 0
                timeoutIntr = false
                RBR_THR_DLL.bytesIn.clear()
            }

            if (newValue[2].truth) {
                LSR.THRE = 1
                thrIntr = true
            }

            fcr = newValue and 0xC9uL
            iir = if (fcr[0].truth) iir or 0xC0uL else iir and 0xC0uL.inv()
            checkIrq()
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

    /** Регистры FCR, IIR */
    private val FCR_IIR = FCR_IIR_Register()

    /** Line Control Register (DWORD, R/W) */
    private inner class LCR_Register : Register(ports.mem, regDtype.bytes.ulong_z * 3u, regDtype, "LCR") {
        var DLAB by bit(7)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Read from LCR: ${data.hex8}" }
            return super.read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.info { "Write to LCR: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }

    /** Line Control Register (DWORD, R/W) */
    private val LCR = LCR_Register()

    /** Modem Control Register (DWORD, R/W) */
    private val MCR = object : Register(
        ports.mem,
        regDtype.bytes.ulong_z * 4u,
        regDtype,
        "MCR",
        0x08uL,
    ) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Read from MCR: ${data.hex8}" }
            return super.read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.info { "Write to MCR: ${value.hex8}" }
            super.write(ea, ss, size, value)
            data = data and 0x1fuL
        }
    }

    /** Line Status Register (DWORD, R) */
    private inner class LSR_Register : Register(
        ports.mem,
        regDtype.bytes.ulong_z * 5u,
        regDtype,
        "LSR",
        0x60u,
        writable = false
    ) {
        var RFE by bit(7)
        var TEMT by bit(6)
        var THRE by bit(5)
        var BI by bit(4)
        var FE by bit(3)
        var PE by bit(2)
        var OE by bit(1)
        var DR by bit(0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            val ret = super.read(ea, ss, size)
            log.info { "Read from LSR: ${ret.hex2}" }

            if (BI.truth || OE.truth) {
                BI = 0
                OE = 0
                checkIrq()
            }

            return ret
        }
    }

    /** Line Status Register (DWORD, R) */
    private val LSR = LSR_Register()

    /** Modem Status Register (DWORD, R) */
    private val MSR = object : Register(
        ports.mem,
        regDtype.bytes.ulong_z * 6u,
        regDtype,
        "MSR",
        writable = false,
    ) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Read from MSR: ${data.hex2}" }

            val ret = super.read(ea, ss, size)

            data = data or 0xb0uL
            if ((data and 0x0fuL).truth) {
                data = data and 0xf0uL
                checkIrq()
            }

            return ret
        }
    }

    /** Scratchpad Register (DWORD, R/W) */
    private val SCR = Register(ports.mem, regDtype.bytes.ulong_z * 7u, regDtype, "SCR")

    private fun writeData(value: Int) = ports.tx.write(UART_MASTER_BUS_DATA, 0, 1, value.ulong_z)
    private fun readData() = ports.tx.read(UART_MASTER_BUS_DATA, 0, 1).int

    private fun writeData(value: Char) = writeData(value.int_z8)

    private val TERMINAL_REQUEST_REG = object : Register(
        ports.rx,
        UART_SLAVE_BUS_REQUEST,
        Datatype.DWORD,
        "TERMINAL_REQUEST_REG",
        readable = false,
        level = Level.SEVERE,
    ) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            when (ss) {
                UART_SLAVE_DATA_RECEIVED -> {
                    RBR_THR_DLL.bytesIn.add(
                        when (val x = readData().char) {
                            '\r' -> '\n'
                            else -> x
                        }
                    )
                }
                UART_SLAVE_DATA_TRANSMITTED -> { }
            }
        }
    }

    fun sendText(string: String) = RBR_THR_DLL.bytesIn.addAll(string.toCharArray().toList())

    override fun initialize(): Boolean {
        super.initialize()
        core.clock.connect(t2mTimer, 1_000, Time.ns, true)
        return true
    }
}
