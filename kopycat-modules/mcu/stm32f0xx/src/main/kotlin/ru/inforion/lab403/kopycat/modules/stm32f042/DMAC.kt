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
@file:Suppress("PropertyName", "PrivatePropertyName")

package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.bits
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareNotReadyException
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.PIN
import java.io.Serializable
import java.util.logging.Level.*


class DMAC(parent: Module, name: String, val channels: Int) : Module(parent, name) {
    companion object {
        @Transient val log = logger(ALL)

        private fun ch2ea(base: Long, ch: Int) = base + 20L * (ch - 1)

        const val CHANNEL1 = 0L
        const val CHANNEL2 = 1L
        const val CHANNEL3 = 2L
        const val CHANNEL4 = 3L
        const val CHANNEL5 = 4L
        const val CHANNEL6 = 5L
        const val CHANNEL7 = 6L
    }

    inner class Ports : ModulePorts(this) {
        /**
         * Configuration registers port
         */
        val mem = Slave("mem", 0x400)

        /**
         * Direct memory access input/output port
         */
        val io = Master("io")

        /**
         * Direct memory access channels
         */
        val drq = Slave("drq", channels)

        /**
         * Interrupt request port pin for channel 1
         */
        val irq_ch1 = Master("irq_ch1", PIN)

        /**
         * Interrupt request port pin for channel 2 and 3
         */
        val irq_ch2_3 = Master("irq_ch2_3", PIN)

        /**
         * Interrupt request port pin for channel 4, 5, 6 and 7
         */
        val irq_ch4_5_6_7 = Master("irq_ch4_5_6_7", PIN)
    }

    override val ports = Ports()

    /**
     * DMA interrupt status register (DMA_ISR and DMA2_ISR)
     */
    inner class DMA_ISR_REG : Register(ports.mem, 0x00, DWORD, "DMA_ISR", 0x0000_0000, writable = false, level = WARNING) {
        val TEIF by bits(3, 7, 11, 15, 19, 23, 27)
        val HTIF by bits(2, 6, 10, 14, 18, 22, 26)
        val TCIF by bits(1, 5,  9, 13, 17, 21, 25)
        val GIF  by bits(0, 4,  8, 12, 16, 20, 24)
    }

    /**
     * DMA interrupt flag clear register (DMA_IFCR and DMA2_IFCR)
     */
    inner class DMA_IFCR_REG : Register(ports.mem, 0x04, DWORD, "DMA_IFCR", 0x0000_0000, readable = false, level = WARNING) {
        val CTEIF by bits(3, 7, 11, 15, 19, 23, 27)
        val CHTIF by bits(2, 6, 10, 14, 18, 22, 26)
        val CTCIF by bits(1, 5,  9, 13, 17, 21, 25)
        val CGIF  by bits(0, 4,  8, 12, 16, 20, 24)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            // clear interrupts flags
            DMA_ISR.write(DMA_ISR.address, ss, size, DMA_ISR.data and value)
        }
    }

    /**
     * DMA channel x configuration register (DMA_CCRx and DMA2_CCRx)
     * (x = 1..7 for DMA and x = 1..5 for DMA2, where x = channel number)
     */
    inner class DMA_CCRx_REG(ch: Int) : Register(ports.mem, ch2ea(0x08, ch), DWORD, "DMA_CCRx$ch", level = WARNING) {
        private val idx = ch - 1

        var MEM2MEM by bit(14)
        var PL by field(13..12)
        var MSIZE by field(11..10)
        var PSIZE by field(9..8)
        var MINC by bit(7)
        var PINC by bit(6)
        var CIRC by bit(5)
        var DIR by bit(4)
        var TEIE by bit(3)
        var HTIE by bit(2)
        var TCIE by bit(1)
        var EN by bit(0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val prev = EN
            super.write(ea, ss, size, value)
            if (prev == 0 && EN == 1) {
                log.finer { "DMAC${idx}: CCR transfer -> [EN=$EN TCIE=$TCIE HTIE=$HTIE TEIE=$TEIE DIR=$DIR CIRC=$CIRC]" }
                dmaChannels[idx].enable()
            }
        }
    }

    /**
     * DMA channel x number of data register (DMA_CNDTRx and
     * DMA2_CNDTRx) (x = 1..7 for DMA and x = 1..5 for DMA2,
     * where x = channel number)
     */
    inner class DMA_CNDTRx_REG(ch: Int) : Register(ports.mem, ch2ea(0x0C, ch), DWORD, "DMA_CNDTRx$ch", level = WARNING) {
        var NDT by field(15..0)
    }

    /**
     * DMA channel x peripheral address register (DMA_CPARx and
     * DMA2_CPARx) (x = 1..7 for DMA and x = 1..5 for DMA2,
     * where x = channel number)
     */
    inner class DMA_CPARx_REG(ch: Int) : Register(ports.mem, ch2ea(0x10, ch), DWORD, "DMA_CPARx$ch", level = WARNING) {
        var PA by field(31..0)
    }

    /**
     * DMA channel x memory address register (DMA_CMARx and
     * DMA2_CMARx) (x = 1..7 for DMA and x = 1..5 for DMA2,
     * where x = channel number)
     */
    inner class DMA_CMARx_REG(ch: Int) : Register(ports.mem, ch2ea(0x14, ch), DWORD, "DMA_CMARx$ch", level = WARNING) {
        var MA by field(31..0)
    }

    /**
     * DMA requests channels handler register
     */
    inner class DMA_CHx_REQ_REG(ch: Int) :
            Register(ports.drq, ch - 1L, DWORD, "CH${ch}_REQ_REG", readable = false, level = SEVERE) {

        private val idx = ch - 1

        override fun write(ea: Long, ss: Int, size: Int, value: Long) = dmaChannels[idx].hardwareRequest()
    }

    class Location(val start: Int, val size: Int, val increment: Int): Serializable {
        private fun getSize(code: Int) = when(code) {
            0b00 -> 1
            0b01 -> 2
            0b10 -> 4
            else -> throw NotImplementedError("Unknown DMA transfer size: $code")
        }

        private fun align(address: Long, size: Int): Long = (address ushr size) shl size

        fun reload() {
            address = align(start.asULong, size)
        }

        fun next() {
            address += itemSize * increment
        }

        var address = align(start.asULong, size)  // aligned address
            private set

        val itemSize = getSize(size)
    }

    inner class Channel(ch: Int): Serializable {

        private val idx = ch - 1

        private val irq = when (ch) {
            1 -> ports.irq_ch1
            2, 3 -> ports.irq_ch2_3
            4, 5, 6, 7 -> ports.irq_ch4_5_6_7
            else -> throw IllegalArgumentException("That should not be happen")
        }

        private val CCRx = DMA_CCRx[idx]
        private val CNDTRx = DMA_CNDTRx[idx]
        private val CPARx = DMA_CPARx[idx]
        private val CMARx = DMA_CMARx[idx]

        private var hardwareRequestPending = false

        // recursion prevention
        private var nestedRequestCount = 0

        // latched start value of NDT
        private var dmaTransferSize = 0

        private lateinit var src: Location
        private lateinit var dst: Location

        // Misc flags helpers
        private val circularMode get() = CCRx.CIRC == 1
        private val memory2PeripheralDir get() = CCRx.DIR == 1

        private var dmaTransferEnabled
            get() = CCRx.EN == 1
            set(value) { CCRx.EN = value.asInt }

        // Condition helpers
        private val transferError get() = false
        private val halfTransferComplete get() = CNDTRx.NDT == dmaTransferSize / 2
        private val transferComplete get() = CNDTRx.NDT == 0

        private val transferJustStarted get() = CNDTRx.NDT == dmaTransferSize

        // Interrupt enabled helpers
        private val transferErrorInterruptEnabled get() = CCRx.TEIE == 1
        private val halfTransferInterruptEnabled get() = CCRx.HTIE == 1
        private val transferCompleteInterruptEnabled get() = CCRx.TCIE == 1

        // Interrupt flags helpers
        private var transferErrorInterruptFlag
            get() = DMA_ISR.TEIF[idx] == 1
            set(value) { DMA_ISR.TEIF[idx] = value.asInt }

        private var halfTransferInterruptFlag
            get() = DMA_ISR.HTIF[idx] == 1
            set(value) { DMA_ISR.HTIF[idx] = value.asInt }

        private var transferCompleteInterruptFlag
            get() = DMA_ISR.TCIF[idx] == 1
            set(value) { DMA_ISR.TCIF[idx] = value.asInt }

        private var globalInterruptFlag
            get() = DMA_ISR.GIF[idx] == 1
            set(value) { DMA_ISR.GIF[idx] = value.asInt }

        // Process and set interrupts flags
        private fun processInterruptsFlags() {
            transferErrorInterruptFlag = transferErrorInterruptEnabled && transferError
            halfTransferInterruptFlag = halfTransferInterruptEnabled && halfTransferComplete
            transferCompleteInterruptFlag = transferCompleteInterruptEnabled && transferComplete
            globalInterruptFlag = transferErrorInterruptFlag || halfTransferInterruptFlag || transferCompleteInterruptFlag
        }

        /**
         * {EN}
         * Functions return whether or not interrupt DMA transfer and give control to firmware.
         * Firmware must get control in case error or complete data transfer.
         * WARN: When half-transfer complete DMAC doesn't stop because in current architecture we can't get it back.
         *
         * see https://youtrack.lab403.inforion.ru/issue/KC-1595
         * {EN}
         */
        private val transferInterruptRequired get() = transferErrorInterruptFlag || transferCompleteInterruptFlag

        /**
         * {EN} Transfer one item {EN}
         */
        private fun transfer() {
            // due to possible recursion at port.io.read first decrement NDT
            CNDTRx.NDT = CNDTRx.NDT - 1

            // make data exchange
            val data = ports.io.read(src.address, 0, src.itemSize)
            ports.io.write(dst.address, 0, dst.itemSize, data)

            // move pointers
            src.next()
            dst.next()
        }

        private fun hardwareTransferRequestAvailable(): Boolean {
            hardwareRequestPending = true

            if (!dmaTransferEnabled)
                return false

            if (!transferJustStarted || nestedRequestCount == 0) {
                // prevent deep recursion call for dataReceivedRequest
                // see https://youtrack.lab403.inforion.ru/issue/KC-1572
                nestedRequestCount++
                if (nestedRequestCount != 1)
                    return false
            }

            return true
        }

        /**
         * {EN} Called at softwareRequest or hardwareRequest end {EN}
         */
        private fun onTransferRequestDone() {
            if (globalInterruptFlag) {
                log.finer { "DMAC${idx}: request IRQ" }
                irq.request(0)
            }

            if (transferComplete) {

                if (nestedRequestCount == 0) {
                    // request has been done
                    log.finer { "DMAC${idx}: request pending reset" }
                    hardwareRequestPending = false
                }

                // reload remain transfer count
                if (circularMode) {
                    CNDTRx.NDT = dmaTransferSize
                    src.reload()
                    dst.reload()
                    log.finer { "DMAC${idx}: circular transfer reload done" }
                } else {
                    // disable transfer (new transfer started only after EN = 1)
                    dmaTransferEnabled = false
                    log.finer { "DMAC${idx}: transfer disabled" }
                }
            }
        }

        /**
         * {EN} Called when CCRx.EN latched from 0 to 1 {EN}
         */
        fun enable() {
            dmaTransferSize = CNDTRx.NDT

            if (memory2PeripheralDir) {
                // Copy from memory to periph
                src = Location(CMARx.MA, CCRx.MSIZE, CCRx.MINC)
                dst = Location(CPARx.PA, CCRx.PSIZE, CCRx.PINC)

                // in this case the most probably scenario that firmware wants to send data
                // force DMA request
                firmwareRequest()
            } else {
                // Copy from periph to memory
                src = Location(CPARx.PA, CCRx.PSIZE, CCRx.PINC)
                dst = Location(CMARx.MA, CCRx.MSIZE, CCRx.MINC)

                log.finest { "DMAC${idx}: requested = $hardwareRequestPending" }
                if (hardwareRequestPending) hardwareRequest()
            }
        }

        /**
         * {EN}
         * DMAC request for cases memory-to-memory or memory-to-peripheral when transfer initiated by firmware.
         * {EN}
         */
        private fun firmwareRequest() {
            log.finer { "DMAC${idx}: handling firmware request" }

            while (!transferComplete && !transferInterruptRequired) {
                try {
                    transfer()
                    processInterruptsFlags()
                } catch (error: MemoryAccessError) {
                    log.severe { "Can't access to DMA region: $error" }
                    // I think bad fate wait for me
                    CNDTRx.NDT = CNDTRx.NDT + 1
                }
            }

            onTransferRequestDone()
        }

        /**
         * {EN}
         * DMAC request from [Ports.drq] port. This request goes from peripheral not in case when
         * transfer made memory-to-memory or memory-to-peripheral and initiated by firmware.
         * {EN}
         */
        fun hardwareRequest() {
            if (!hardwareTransferRequestAvailable())
                return

            log.finer { "DMAC${idx}: handling hardware request -> $nestedRequestCount" }

            while (!transferComplete && nestedRequestCount != 0 && !transferInterruptRequired) {
                try {
                    transfer()
                    processInterruptsFlags()
                } catch (error: HardwareNotReadyException) {
                    log.severe { "Can't access to DMA region: $error" }
                    hardwareRequestPending = false
                    // I think bad fate wait for me
                    CNDTRx.NDT = CNDTRx.NDT + 1
                } finally {
                    nestedRequestCount--
                }
            }

            log.finer {
                "DMAC${idx}: Transfer finished TEIF=$transferErrorInterruptFlag " +
                        "HTIF=$halfTransferInterruptFlag " +
                        "TCIF=$transferCompleteInterruptFlag " +
                        "GIF=$globalInterruptFlag"
            }

            onTransferRequestDone()
        }
    }

    val DMA_ISR = DMA_ISR_REG()
    val DMA_IFCR = DMA_IFCR_REG()

    val DMA_CCRx = Array(channels) { DMA_CCRx_REG(it + 1) }
    val DMA_CNDTRx = Array(channels) { DMA_CNDTRx_REG(it + 1) }
    val DMA_CPARx = Array(channels) { DMA_CPARx_REG(it + 1) }
    val DMA_CMARx = Array(channels) { DMA_CMARx_REG(it + 1) }

    val DMA_CHx_REQ = Array(channels) { DMA_CHx_REQ_REG(it + 1) }

    val dmaChannels = Array(channels) { Channel(it + 1) }
}