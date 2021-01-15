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
@file:Suppress("unused", "SpellCheckingInspection", "PrivatePropertyName", "LocalVariableName", "PropertyName")

package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.logging.Level
import java.util.logging.Level.*

/**
 * {RU}
 *
 * @param parent родительский модуль, в который будет вставлен данный
 * @param name имя инстанциированного модуля (объекта)
 * @param index номер интерфейса USART
 * {RU}
 */
class USARTx(parent: Module, name: String, val index: Int) : Module(parent, name) {

    companion object {
        @Transient val log = logger(INFO)
        const val CORE_FREQ = 8_000_000
    }

    enum class RegisterType(val offset: Long) {
        USART_CR1   (0x00),
        USART_CR2   (0x04),
        USART_CR3   (0x08),
        USART_BRR   (0x0C),
        USART_GTPR  (0x10),
        USART_RTOR  (0x14),
        USART_RQR   (0x18),
        USART_ISR   (0x1C),
        USART_ICR   (0x20),
        USART_RDR   (0x24),
        USART_TDR   (0x28)
    }

    inner class Ports : ModulePorts(this) {
        /**
         * Configuration registers port
         */
        val mem = Slave("mem", 0x400)

        /**
         * Interrupt request port pin on data transmitted
         */
        val irq_tx = Master("irq_tx", PIN)

        /**
         * Interrupt request port pin on data received
         */
        val irq_rx = Master("irq_rx", PIN)

        /**
         * DMA request on data transmitted
         */
        val drq_tx = Master("drq_tx", PIN)

        /**
         * DMA request on data received
         */
        val drq_rx = Master("drq_rx", PIN)

        /**
         * UART master / Terminal slave port
         */
        val usart_m = Master("usart_m", UART_MASTER_BUS_SIZE)

        /**
         * UART slave / Terminal master port
         */
        val usart_s = Slave("usart_s", UART_SLAVE_BUS_SIZE)
    }

    override val ports = Ports()

    private open inner class RegisterBase(
            register: RegisterType,
            default: Long = 0x0000_0000,
            writable: Boolean = true,
            readable: Boolean = true,
            level: Level = FINE
    ) : Register(ports.mem, register.offset, DWORD, "USART${index}_${register.name}", default, writable, readable, level)

    private fun readParam(index: Int) = ports.usart_m.read(UART_MASTER_BUS_PARAM, index, 1)
    private fun writeData(value: Int) = ports.usart_m.write(UART_MASTER_BUS_DATA, 0, 1, value.asULong)
    private fun readData() = ports.usart_m.read(UART_MASTER_BUS_DATA, 0, 1).asInt

    private fun dmaRequestReceived() = ports.drq_rx.request(0)
    private fun dmaRequestTransmitted() = ports.drq_tx.request(0)
    private fun intRequestReceived() = ports.irq_rx.request(0)
    private fun intRequestTransmitted() = ports.irq_tx.request(0)

    private var usartEnabled = false
    private var transmitEnabled = false
    private var receiveEnabled = false

    private var baudRate = 0

    val TERMINAL_REQUEST_REG = object : Register(
            ports.usart_s,
            UART_SLAVE_BUS_REQUEST,
            DWORD,
            "TERMINAL_REQUEST_REG",
            readable = false,
            level = SEVERE) {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (ss) {
                UART_SLAVE_DATA_RECEIVED -> {
                    USART_ISR.RXNE = 1

                    if (USART_CR3.DMAR == 1)
                        dmaRequestReceived()

                    if (USART_CR1.RXNEIE == 1)
                        intRequestReceived()
                }

                UART_SLAVE_DATA_TRANSMITTED -> {
                    USART_ISR.TC = 1
                    USART_ISR.TXE = 1

                    if (USART_CR3.DMAT == 1)
                        dmaRequestTransmitted()

                    if (USART_CR1.TXEIE == 1)
                        intRequestTransmitted()
                }
            }
        }
    }

    private val USART_CR1   = object : RegisterBase(RegisterType.USART_CR1) {
        var OVER8 by bit(15)
        var TXEIE by bit(7)
        var TCIE by bit(6)
        var RXNEIE by bit(5)
        var TE by bit(3)
        var RE by bit(2)
        var UE by bit(0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            if (!usartEnabled && UE == 1) {
                usartEnabled = readParam(UART_MASTER_ENABLE).toBool()

                if (!usartEnabled) {
                    transmitEnabled = false
                    receiveEnabled = false
                    log.severe { "USART$index can't enable usart something goes wrong..." }
                    return
                }

                if (TE == 1) {
                    transmitEnabled = readParam(UART_MASTER_TX_ENABLE).toBool()
                    if (transmitEnabled) {
                        USART_ISR.TEACK = 1
                    } else {
                        log.severe { "USART$index transmit request but not supported by terminal" }
                    }
                }

                if (RE == 1) {
                    receiveEnabled = readParam(UART_MASTER_RX_ENABLE).toBool()
                    if (receiveEnabled) {
                        USART_ISR.REACK = 1
                    } else {
                        log.severe { "USART$index receive requested but not supported by terminal" }
                    }
                }

                log.info { "USART$index enabled TE=${transmitEnabled.toInt()} RE=${receiveEnabled.toInt()} baud=$baudRate" }
            } else if (usartEnabled && UE == 0) {
                usartEnabled = false
                USART_ISR.TEACK = 0
                USART_ISR.REACK = 0
                RE = 0
                TE = 0
                transmitEnabled = false
                receiveEnabled = false
//              TODO for version 0.3.30 https://youtrack.lab403.inforion.ru/issue/KC-1575
                log.severe { "USART disable not implemented, see https://youtrack.lab403.inforion.ru/issue/KC-1575" }
            }
        }
    }

    private val USART_CR2   = object : RegisterBase(RegisterType.USART_CR2) {

    }

    private val USART_CR3   = object : RegisterBase(RegisterType.USART_CR3) {
        var WUFIE by bit(22)
        var WUS by field(21..20)
        var SCARCNT by field(19..17)
        var DEP by bit(15)
        var DEM by bit(14)
        var DDRE by bit(13)
        var OVRDIS by bit(12)
        var ONEBIT by bit(11)
        var CTSIE by bit(10)
        var CTSE by bit(9)
        var RTSE by bit(8)
        var DMAT by bit(7)
        var DMAR by bit(6)
        var SCEN by bit(5)
        var NACK by bit(4)
        var HDSEL by bit(3)
        var IRLP by bit(2)
        var IREN by bit(1)
        var EIE by bit(0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val prevDMAR = DMAR
            val prevDMAT = DMAT

            super.write(ea, ss, size, value)

            if (prevDMAR == 0 && DMAR == 1)
                dmaRequestReceived()

            if (prevDMAT == 0 && DMAT == 1)
                dmaRequestTransmitted()
        }
    }

    private val USART_BRR   = object : RegisterBase(RegisterType.USART_BRR) {
        var BRR by field(15..0)

        fun calcBaudRate(clock: Int, div: Int): Int = clock / div + 1

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            if (USART_CR1.UE != 0) {
                log.severe { "USART$index not disabled but BRR written -> this operation has not effect!" }
                return
            }

            val USARTDIVH = BRR[15..4]
            val USARTDIVL = if (USART_CR1.OVER8 == 0) BRR[3..0] else BRR[2..0] shl 1

            val USARTDIV = insert(USARTDIVH, 15..4)
                    .insert(USARTDIVL, 3..0)

            baudRate = calcBaudRate(CORE_FREQ, USARTDIV)
        }
    }
    private val USART_GTPR  = object : RegisterBase(RegisterType.USART_GTPR) {}
    private val USART_RTOR  = object : RegisterBase(RegisterType.USART_RTOR) {}
    private val USART_RQR   = object : RegisterBase(RegisterType.USART_RQR) {}
    private val USART_ISR   = object : RegisterBase(RegisterType.USART_ISR, 0x0200_00C0, writable = false) {
        var REACK by bit(22)
        var TEACK by bit(21)
        var TXE by bit(7)
        var TC by bit(6)
        var RXNE by bit(5)
    }
    private val USART_ICR   = object : RegisterBase(RegisterType.USART_ICR) {}
    private val USART_RDR   = object : RegisterBase(RegisterType.USART_RDR, writable = false) {
        var RDR by field(7..0)

        override fun read(ea: Long, ss: Int, size: Int): Long {
            USART_ISR.RXNE = 0
            RDR = readData()
            return super.read(ea, ss, size)
        }
    }
    private val USART_TDR   = object : RegisterBase(RegisterType.USART_TDR) {
        var TDR by field(7..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            USART_ISR.TC = 0
            USART_ISR.TXE = 0
            writeData(TDR)
        }
    }

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
            "usartEnabled" to usartEnabled,
            "transmitEnabled" to transmitEnabled,
            "receiveEnabled" to receiveEnabled,
            "baudRate" to baudRate)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        usartEnabled = loadValue(snapshot, "usartEnabled")
        transmitEnabled = loadValue(snapshot, "transmitEnabled")
        receiveEnabled = loadValue(snapshot, "receiveEnabled")
        baudRate = loadValue(snapshot, "baudRate")
    }
}