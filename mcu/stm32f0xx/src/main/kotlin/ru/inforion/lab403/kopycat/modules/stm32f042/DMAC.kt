package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.bits
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level.SEVERE
import java.util.logging.Level.WARNING

/**
 * Created by the bat on 14.12.18.
 */
class DMAC(parent: Module, name: String, val channels: Int) : Module(parent, name) {
    companion object {
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
        val HTIF by bits(2, 6, 12, 14, 18, 22, 26)
        val TCIF by bits(1, 5, 11, 13, 17, 21, 25)
        val GIF  by bits(0, 4, 10, 12, 16, 20, 24)
    }

    /**
     * DMA interrupt flag clear register (DMA_IFCR and DMA2_IFCR)
     */
    inner class DMA_IFCR_REG : Register(ports.mem, 0x04, DWORD, "DMA_IFCR", 0x0000_0000, readable = false, level = WARNING) {
        val CTEIF by bits(3, 7, 11, 15, 19, 23, 27)
        val CHTIF by bits(2, 6, 12, 14, 18, 22, 26)
        val CTCIF by bits(1, 5, 11, 13, 17, 21, 25)
        val CGIF  by bits(0, 4, 10, 12, 16, 20, 24)

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
            super.write(ea, ss, size, value)
            if (EN == 1)
                dmaChannels[idx].enable()
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

        override fun write(ea: Long, ss: Int, size: Int, value: Long) = dmaChannels[idx].request()
    }

    class Location(val start: Int, val size: Int, val increment: Int) {
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

    inner class Channel(ch: Int) {

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

        private var requested = false

        // latched start value of NDT
        private var dmaTransferSize = 0

        private lateinit var src: Location
        private lateinit var dst: Location

        fun enable() {
            if (CCRx.DIR == 1) {
                // Copy from memory to periph
                src = Location(CMARx.MA, CCRx.MSIZE, CCRx.MINC)
                dst = Location(CPARx.PA, CCRx.PSIZE, CCRx.PINC)
            } else {
                // Copy from periph to memory
                src = Location(CPARx.PA, CCRx.PSIZE, CCRx.PINC)
                dst = Location(CMARx.MA, CCRx.MSIZE, CCRx.MINC)
            }

            dmaTransferSize = CNDTRx.NDT

            if (requested)
                request()
        }

        fun request() {
            if (CCRx.EN == 0) {
                requested = true
                return
            }

            if (CNDTRx.NDT != 0) {
                val data = ports.io.read(src.address, 0, src.itemSize)
                ports.io.write(dst.address, 0, dst.itemSize, data)
                src.next()
                dst.next()
                CNDTRx.NDT = CNDTRx.NDT - 1
            }

            val transferError = false  // stub
            val halfTransferComplete = CNDTRx.NDT == dmaTransferSize / 2
            val transferComplete = CNDTRx.NDT == 0

            var interrupt = false

            if (CCRx.TEIE == 1 && transferError) {
                DMA_ISR.TEIF[idx] = 1
                interrupt = true
            }

            if (CCRx.HTIE == 1 && halfTransferComplete) {
                DMA_ISR.HTIF[idx] = 1
                interrupt = true
            }

            if (CCRx.TCIE == 1 && transferComplete) {
                DMA_ISR.TCIF[idx] = 1
                interrupt = true
            }

            if (interrupt) {
                DMA_ISR.GIF[idx] = 1
                irq.request(0)
            }

            // reload remain transfer count
            if (CCRx.CIRC == 1 && CNDTRx.NDT == 0) {
                CNDTRx.NDT = dmaTransferSize
                src.reload()
                dst.reload()
            }

            requested = false
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