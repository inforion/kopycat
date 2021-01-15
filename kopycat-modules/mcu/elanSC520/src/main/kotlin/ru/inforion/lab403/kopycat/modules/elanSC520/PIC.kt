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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.abstracts.APIC
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.modules.*
import java.util.logging.Level
import java.util.logging.Level.*
import kotlin.collections.set


/**
 * Modified by the bat on 19.07.17.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")
class PIC(parent: Module, name: String) : APIC(parent, name) {
    companion object {
        @Transient val log = logger(FINE)

        const val SLCT_ICW1_BIT = 4
        const val IS_OCW3_BIT = 3
        const val GP_INTERRUPT_COUNT = 10
    }

    inner class Ports : ModulePorts(this) {
        // control ports
        val mmcr = Slave("mmcr", BUS12)
        val io = Slave("io", BUS16)

        // external ports
        val gp = Slave("gp", GP_INTERRUPT_COUNT)
        val pci = Slave("pci", PCI_INTERRUPTS_COUNT)
        val uart = Slave("uart", UART.INTERRUPT_COUNT)

        // internal ports
        val pit = Slave("pit", PIT.INTERRUPT_COUNT)
    }

    override val ports = Ports()

    private val FPUERRCLR = Register(ports.io, 0x00F0, DWORD, "FPUERRCLR")

    enum class CR_LOW_READ_STATE { IR, ISR, NONE }
    enum class CR_HIGH_WRITE_STATE { MSK, ICW2, ICW3, ICW4 }

    enum class IC_TYPE(val prefix: String, val portLo: Long, val portHi: Long) {
        MASTER("MPIC", 0x0020, 0x0021),
        SLAVE1("S1PIC", 0x00A0, 0x00A1),
        SLAVE2("S2PIC", 0x0024, 0x0025),
        NONE("NONE", -1, -1)
    }

    class Pin(
            val num: Int,
            var inService: Boolean = false,
            var pending: Boolean = false,
            var masked: Boolean = false)

    interface IMultiplexer {
        val pins: Array<Pin>
        fun pending(num: Int): Boolean
        fun inService(num: Int): Boolean
        fun inService(num: Int, value: Boolean) {
            pins[num].inService = false
        }

        fun masked(num: Int): Boolean
        var vectorOffset: Int
    }

    private abstract inner class AElanIC(val desc: IC_TYPE, val size: Int) : IMultiplexer {
        override var vectorOffset: Int = 0
        override val pins = Array(size) { Pin(it) }
        protected var portLowReadState = CR_LOW_READ_STATE.NONE
        protected var portHighWriteState = CR_HIGH_WRITE_STATE.MSK
        protected var omitICW3 = false
        protected var omitICW4 = false

        private val xIR = object : Register(ports.io, desc.portLo, BYTE, "${desc.prefix}IR", writable = false) {
            override fun beforeRead(from: MasterPort, ea: Long) = portLowReadState == CR_LOW_READ_STATE.IR

            override fun read(ea: Long, ss: Int, size: Int): Long {
                data = pins.filter { it.pending }.fold(0) { result, it -> result or (1 shl it.num) }.asULong
                portLowReadState = CR_LOW_READ_STATE.NONE
                return super.read(ea, ss, size)
            }
        }

        private val xISR = object : Register(ports.io, desc.portLo, BYTE, "${desc.prefix}ISR", writable = false) {
            override fun beforeRead(from: MasterPort, ea: Long) = portLowReadState == CR_LOW_READ_STATE.ISR

            override fun read(ea: Long, ss: Int, size: Int): Long {
                data = pins.filter { it.inService }.fold(0) { result, it -> result or (1 shl it.num) }.asULong
                portLowReadState = CR_LOW_READ_STATE.NONE
                return super.read(ea, ss, size)
            }
        }

        private val xINTMSK = object : Register(ports.io, desc.portHi, BYTE, "${desc.prefix}INTMSK") {
            override fun beforeRead(from: MasterPort, ea: Long) = portHighWriteState == CR_HIGH_WRITE_STATE.MSK
            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) = portHighWriteState == CR_HIGH_WRITE_STATE.MSK

            override fun read(ea: Long, ss: Int, size: Int): Long {
                data = pins.filter { it.masked }.fold(0) { result, it -> result or (1 shl it.num) }.asULong
                return super.read(ea, ss, size)
            }

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                pins.forEach {
                    val bit = data[it.num].toBool()
                    if (it.masked xor bit) {
                        it.masked = bit
                        log.finer { "$name WR <- IM${it.num}=${it.masked}" }
                    }
                }
            }
        }

        private val xICW2 = object : Register(ports.io, desc.portHi, BYTE, "${desc.prefix}ICW2", readable = false) {
            val T7T3 by field(7..3)
            var A10A8 by field(2..0)

            override fun stringify() = "${super.toString()} [T7T3=$T7T3 A10A8=$A10A8]"

            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) =
                    portHighWriteState == CR_HIGH_WRITE_STATE.ICW2

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                A10A8 = 0
                vectorOffset = T7T3 shl 3
                portHighWriteState = when {
                    !omitICW3 -> CR_HIGH_WRITE_STATE.ICW3
                    !omitICW4 -> CR_HIGH_WRITE_STATE.ICW4
                    else -> CR_HIGH_WRITE_STATE.MSK
                }
            }
        }

        private val xICW4 = object : Register(ports.io, desc.portHi, BYTE, "${desc.prefix}ICW4", readable = false) {
            val SFNM by bit(4)
            val BUF_MS by field(3..2)
            val AEOI by bit(1)
            var PM by bit(0)

            override fun stringify() = "${super.toString()} [SFNM=$SFNM BUF_MS=$BUF_MS AEOI=$AEOI PM=$PM]"

            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) =
                    portHighWriteState == CR_HIGH_WRITE_STATE.ICW4

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                PM = 1 // In the ÉlanSC520 microcontroller design, this PC/AT-compatible bit is internally fixed to 1
                portHighWriteState = CR_HIGH_WRITE_STATE.MSK
            }
        }

        private val xOCW2 = object : Register(ports.io, desc.portLo, BYTE, "${desc.prefix}OCW2", readable = false) {
            val R_SL_EOI by field(7..5)
            val SLCT_ICW1 by bit(SLCT_ICW1_BIT)
            val IS_OCW3 by bit(IS_OCW3_BIT)
            val LS by field(2..0)

            override fun stringify() = "${super.stringify()} [R_SL_EOI=$R_SL_EOI SLCT_ICW1=$SLCT_ICW1 IS_OCW3=$IS_OCW3 LS=$LS]"

            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) = value[SLCT_ICW1_BIT] == 0L && value[IS_OCW3_BIT] == 0L

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                // FIXME: Oh, it's gonna be fack up here ... quite a strange EOI types: Rotate on nonspecific EOI command
                pins[LS].inService = !R_SL_EOI.toBool()
            }
        }

        private val xOCW3 = object : Register(ports.io, desc.portLo, BYTE, "${desc.prefix}OCW3", readable = false) {
            val ESMM_SMM by field(6..5)
            val SLCT_ICW1 by bit(SLCT_ICW1_BIT)
            val IS_OCW3 by bit(IS_OCW3_BIT)
            val P by bit(2)
            val RR_RIS by field(1..0)

            override fun stringify() = "${super.stringify()} [ESMM_SMM=$ESMM_SMM SLCT_ICW1=$SLCT_ICW1 IS_OCW3=$IS_OCW3 P=$P RR_RIS=$RR_RIS]"

            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) = value[SLCT_ICW1_BIT] == 0L && value[IS_OCW3_BIT] == 1L

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                when (RR_RIS) {
                    2 -> portLowReadState = CR_LOW_READ_STATE.IR
                    3 -> portLowReadState = CR_LOW_READ_STATE.ISR
                }
            }
        }
    }

    private inner class MasterElanIC : AElanIC(IC_TYPE.MASTER, 8) {
        val selectors = Array(8) { false }
        val slaves = Array<SlaveElanIC?>(8) { null }
        var single = false

        override fun masked(num: Int): Boolean = pins[num].masked
        override fun pending(num: Int): Boolean {
            val slave = slaves[num]
            if (selectors[num] && slave != null)
                return slave.pins.any { it.pending }
            return pins[num].pending
        }

        override fun inService(num: Int): Boolean {
            val slave = slaves[num]
            if (selectors[num] && slave != null)
                return slave.pins.any { it.inService }
            return pins[num].inService
        }

        fun connect(id: Int, slave: SlaveElanIC) {
            if (!single) {
                slaves[id] = slave
                slave.id = id
                slave.master = this
            }
        }

        private val xICW1 = object : Register(ports.io, desc.portLo, BYTE, "${desc.prefix}ICW1", readable = false) {
            val SLCT_ICW1 by bit(SLCT_ICW1_BIT)
            val LTIM by bit(3)
            var ADI by bit(2)
            val SNGL by bit(1)
            val IC4 by bit(0)

            override fun stringify() = "${super.stringify()} [SLCT_ICW1=$SLCT_ICW1 LTIM=$LTIM ADI=$ADI SNGL=$SNGL IC4=$IC4]"

            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) = value[SLCT_ICW1_BIT] == 1L

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                ADI = 1  // In the ÉlanSC520 microcontroller design, this PC/AT-compatible bit (ADI) is internally fixed to 1.
                if (SNGL == 1) {
                    omitICW3 = true
                    selectors.indices.forEach { selectors[it] = false }
                    slaves.indices.forEach { slaves[it] = null }
                    single = true
                    interruptRouter[ROUTE.P3] = Entry(master, 2)
                    interruptRouter[ROUTE.P13] = Entry(master, 5)
                } else {
                    interruptRouter[ROUTE.P3] = Entry(slave1, 0)
                    interruptRouter[ROUTE.P13] = Entry(slave2, 0)
                }
                omitICW4 = !IC4.toBool()
                portHighWriteState = CR_HIGH_WRITE_STATE.ICW2
            }
        }

        private val xICW3 = object : Register(ports.io, desc.portHi, BYTE, "${desc.prefix}ICW3", readable = false) {
            var S7 by bit(7)
            var S6 by bit(6)
            var S5 by bit(5)
            var S4 by bit(4)

            var S3 by bit(3)
            var S2 by bit(2)
            var S1 by bit(1)
            var S0 by bit(0)

            override fun stringify() = "${super.stringify()} [S2=$S2 S5=$S5]"

            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) =
                    portHighWriteState == CR_HIGH_WRITE_STATE.ICW3

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                S0 = 0; S1 = 0; S3 = 0; S4 = 0; S6 = 0; S7 = 0  // Fixed in Elan
                selectors[2] = S2.toBool()
                selectors[5] = S5.toBool()
                interruptRouter[ROUTE.P3] = if (selectors[2]) Entry(master, 2) else Entry(slave1, 0)
                interruptRouter[ROUTE.P13] = if (selectors[5]) Entry(master, 5) else Entry(slave2, 0)
                portHighWriteState = when {
                    !omitICW4 -> CR_HIGH_WRITE_STATE.ICW4
                    else -> CR_HIGH_WRITE_STATE.MSK
                }
            }
        }
    }

    private inner class SlaveElanIC(desc: IC_TYPE) : AElanIC(desc, 8) {
        var master: MasterElanIC? = null
        var id: Int = -1

        override fun masked(num: Int): Boolean {
            val local = master
            if (local != null && id != -1 && local.selectors[id] && local.pins[id].masked)
                return true
            return pins[num].masked
        }

        override fun pending(num: Int) = pins[num].pending
        override fun inService(num: Int) = pins[num].inService

        private val xICW1 = object : Register(ports.io, desc.portLo, BYTE, "${desc.prefix}ICW1", readable = false) {
            val SLCT_ICW1 by bit(SLCT_ICW1_BIT)
            val LTIM by bit(3)
            var ADI by bit(2)
            var SNGL by bit(1)
            val IC4 by bit(0)

            override fun stringify() = "${super.stringify()} [SLCT_ICW1=$SLCT_ICW1 LTIM=$LTIM ADI=$ADI SNGL=$SNGL IC4=$IC4]"

            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) = value[SLCT_ICW1_BIT] == 1L

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                ADI = 1  // In the ÉlanSC520 microcontroller design, this PC/AT-compatible bit (ADI) is internally fixed to 1.
                SNGL = 0
                omitICW3 = false
                omitICW4 = !IC4.toBool()
                portHighWriteState = CR_HIGH_WRITE_STATE.ICW2
            }
        }

        private val xICW3 = object : Register(ports.io, desc.portHi, BYTE, "${desc.prefix}ICW3", readable = false) {
            override fun beforeWrite(from: MasterPort, ea: Long, value: Long) =
                    portHighWriteState == CR_HIGH_WRITE_STATE.ICW3

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                super.write(ea, ss, size, value)
                data = 0b101  // fixed
                portHighWriteState = when {
                    !omitICW4 -> CR_HIGH_WRITE_STATE.ICW4
                    else -> CR_HIGH_WRITE_STATE.MSK
                }
            }
        }
    }

    enum class ROUTE(val map: Int) {
        P1(1), P2(2),
        P3(3), P4(4),
        P5(5), P6(6),
        P7(7), P8(8),
        P9(9), P10(10),
        P11(11), P12(12),
        P13(13), P14(14),
        P15(15), P16(16),
        P17(17), P18(18),
        P19(19), P20(20),
        P21(21), P22(22),
        NMI(0b11111), DISABLED(-1)
    }

    data class Entry(val ic: IMultiplexer, val num: Int)

    private val slave1 = SlaveElanIC(IC_TYPE.SLAVE1)
    private val slave2 = SlaveElanIC(IC_TYPE.SLAVE2)
    private val master = MasterElanIC().apply {
        connect(2, slave1)
        connect(5, slave2)
    }

    inner class Interrupt(irq: Int, val control: MAP_REG, val postfix: String) : AInterrupt(irq, postfix) {
        override val cop get() = core.cop

        var route = ROUTE.DISABLED

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + postfix.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Interrupt) return false
            if (!super.equals(other)) return false

            if (postfix != other.postfix) return false

            return true
        }

        override var inService: Boolean
            get() {
                val entry = interruptRouter[route] ?: return false
                return entry.ic.inService(entry.num)
            }
            set(value) {
                val entry = interruptRouter[route] ?: return
                entry.ic.inService(entry.num, value)
            }

        override val vector: Int
            get() {
                val entry = interruptRouter[route] ?: return 0
                return entry.ic.vectorOffset or entry.num
            }

        override val priority: Int get() = route.map
        override val nmi: Boolean get() = route == ROUTE.NMI

        override val masked: Boolean
            get() {
                val entry = interruptRouter[route] ?: return false
                return !enabled && entry.ic.masked(entry.num)
            }

        override fun onInterrupt() = Unit

        override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + mapOf("route" to route)

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super.deserialize(ctxt, snapshot)
            route = snapshot["route"] as ROUTE
        }

        init {
            control.interrupt = this
        }
    }

    private val interruptRouter = mutableMapOf(
            ROUTE.P1 to Entry(master, 0),
            ROUTE.P2 to Entry(master, 1),

            ROUTE.P3 to Entry(slave1, 0),
            ROUTE.P4 to Entry(slave1, 1),
            ROUTE.P5 to Entry(slave1, 2),
            ROUTE.P6 to Entry(slave1, 3),
            ROUTE.P7 to Entry(slave1, 4),
            ROUTE.P8 to Entry(slave1, 5),
            ROUTE.P9 to Entry(slave1, 6),
            ROUTE.P10 to Entry(slave1, 7),

            ROUTE.P11 to Entry(master, 3),
            ROUTE.P12 to Entry(master, 4),

            ROUTE.P13 to Entry(slave2, 0),
            ROUTE.P14 to Entry(slave2, 1),
            ROUTE.P15 to Entry(slave2, 2),
            ROUTE.P16 to Entry(slave2, 3),
            ROUTE.P17 to Entry(slave2, 4),
            ROUTE.P18 to Entry(slave2, 5),
            ROUTE.P19 to Entry(slave2, 6),
            ROUTE.P20 to Entry(slave2, 7),

            ROUTE.P21 to Entry(master, 6),
            ROUTE.P22 to Entry(master, 7))

    inner class MAP_REG(port: SlavePort, address: Long, name: String) : Register(port, address, BYTE, name) {

        var interrupt: Interrupt? = null

        val INT_MAP by field(4..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            val localInt = interrupt

            if (localInt == null) {
                log.warning { "Configuring not implemented interrupt: $this" }
                return
            }

            val route = find<ROUTE> { it.map == INT_MAP } ?: ROUTE.DISABLED
            if (localInt.route != route) {
                localInt.route = route
                log.fine { "${localInt.name} routed to -> ${localInt.irq}" }
            }
        }
    }

    val PICICR = Register(ports.mmcr, 0xD00, BYTE, "PICICR")
    val MPICMODE = Register(ports.mmcr, 0xD01, BYTE, "MPICMODE")
    val SL1PICMODE = Register(ports.mmcr, 0xD02, BYTE, "SL1PICMODE")
    val SL2PICMODE = Register(ports.mmcr, 0xD03, BYTE, "SL2PICMODE")

    val SWINT16_1 = Register(ports.mmcr, 0xD08, WORD, "SWINT16_1")
    val SWINT22_17 = Register(ports.mmcr, 0xD0A, BYTE, "SWINT22_17")
    val INTPINPOL = Register(ports.mmcr, 0xD10, WORD, "INTPINPOL")

    val PCIHOSTMAP = Register(ports.mmcr, 0xD14, WORD, "PCIHOSTMAP")

    val ECCMAP = Register(ports.mmcr, 0xD18, WORD, "ECCMAP")

    val GPTMR0MAP = MAP_REG(ports.mmcr, 0xD1A, "GPTMR0MAP")
    val GPTMR1MAP = MAP_REG(ports.mmcr, 0xD1B, "GPTMR1MAP")
    val GPTMR2MAP = MAP_REG(ports.mmcr, 0xD1C, "GPTMR2MAP")

    val PIT0MAP = MAP_REG(ports.mmcr, 0xD20, "PIT0MAP")
    val PIT1MAP = MAP_REG(ports.mmcr, 0xD21, "PIT1MAP")
    val PIT2MAP = MAP_REG(ports.mmcr, 0xD22, "PIT2MAP")

    val UART1MAP = MAP_REG(ports.mmcr, 0xD28, "UART1MAP")
    val UART2MAP = MAP_REG(ports.mmcr, 0xD29, "UART2MAP")

    val PCIINTAMAP = MAP_REG(ports.mmcr, 0xD30, "PCIINTAMAP")
    val PCIINTBMAP = MAP_REG(ports.mmcr, 0xD31, "PCIINTBMAP")
    val PCIINTCMAP = MAP_REG(ports.mmcr, 0xD32, "PCIINTCMAP")
    val PCIINTDMAP = MAP_REG(ports.mmcr, 0xD33, "PCIINTDMAP")

    val DMABCINTMAP = MAP_REG(ports.mmcr, 0xD40, "DMABCINTMAP")
    val SSIMAP = MAP_REG(ports.mmcr, 0xD41, "SSIMAP")
    val WDTMAP = MAP_REG(ports.mmcr, 0xD42, "WDTMAP")
    val RTCMAP = MAP_REG(ports.mmcr, 0xD43, "RTCMAP")
    val WPVMAP = MAP_REG(ports.mmcr, 0xD44, "WPVMAP")
    val ICEMAP = MAP_REG(ports.mmcr, 0xD45, "ICEMAP")
    val FERRMAP = MAP_REG(ports.mmcr, 0xD46, "FERRMAP")

    val GP0IMAP = MAP_REG(ports.mmcr, 0xD50, "GP0IMAP")
    val GP1IMAP = MAP_REG(ports.mmcr, 0xD51, "GP1IMAP")
    val GP2IMAP = MAP_REG(ports.mmcr, 0xD52, "GP2IMAP")
    val GP3IMAP = MAP_REG(ports.mmcr, 0xD53, "GP3IMAP")
    val GP4IMAP = MAP_REG(ports.mmcr, 0xD54, "GP4IMAP")
    val GP5IMAP = MAP_REG(ports.mmcr, 0xD55, "GP5IMAP")
    val GP6IMAP = MAP_REG(ports.mmcr, 0xD56, "GP6IMAP")
    val GP7IMAP = MAP_REG(ports.mmcr, 0xD57, "GP7IMAP")
    val GP8IMAP = MAP_REG(ports.mmcr, 0xD58, "GP8IMAP")
    val GP9IMAP = MAP_REG(ports.mmcr, 0xD59, "GP9IMAP")
    val GP10IMAP = MAP_REG(ports.mmcr, 0xD5A, "GP10IMAP")

    private val GP_IRQ = Interrupts(ports.gp, "GP_IRQ",
            Interrupt(0, GP0IMAP, "GPIRQ0"),
            Interrupt(1, GP1IMAP, "GPIRQ1"),
            Interrupt(2, GP2IMAP, "GPIRQ2"),
            Interrupt(3, GP3IMAP, "GPIRQ3"),
            Interrupt(4, GP4IMAP, "GPIRQ4"),
            Interrupt(5, GP5IMAP, "GPIRQ5"),
            Interrupt(6, GP6IMAP, "GPIRQ6"),
            Interrupt(7, GP7IMAP, "GPIRQ7"),
            Interrupt(8, GP8IMAP, "GPIRQ8"),
            Interrupt(9, GP9IMAP, "GPIRQ9"),
            Interrupt(10, GP10IMAP, "GPIRQ10"))

    private val PIT_IRQ = Interrupts(ports.pit, "PIT_IRQ",
            Interrupt(0, PIT0MAP, "PIT0IRQ"),
            Interrupt(1, PIT1MAP, "PIT1IRQ"),
            Interrupt(2, PIT2MAP, "PIT2IRQ"))

    private val PCI_IRQ = Interrupts(ports.pci, "PCI_IRQ",
            Interrupt(0, PCIINTAMAP, "INTA"),
            Interrupt(1, PCIINTBMAP, "INTB"),
            Interrupt(2, PCIINTCMAP, "INTC"),
            Interrupt(3, PCIINTDMAP, "INTD"))

    private val UART_IRQ = Interrupts(ports.uart, "UART_IRQ",
            Interrupt(0, UART1MAP, "UART1IRQ"),
            Interrupt(1, UART2MAP, "UART1IRQ"))

    override fun command(): String = "ic"
}