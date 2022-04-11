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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.abstracts.APIC
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.modules.*


@Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")
class PIC8259(parent: Module, name: String) : APIC(parent, name) {
    companion object {
        @Transient val log = logger(FINE)

        const val SLCT_ICW1_BIT = 4
        const val IS_OCW3_BIT = 3
        const val PIC_INTERRUPT_COUNT = 16
    }

    inner class Ports : ModulePorts(this) {
        val io = Slave("io", BUS16)
        val irq = Slave("master", PIC_INTERRUPT_COUNT)
    }

    override val ports = Ports()

    private val FPUERRCLR = Register(ports.io, 0x00F0u, DWORD, "FPUERRCLR")
    private val ECLR0 = Register(ports.io, 0x04D0u, BYTE, "ECLR0")
    private val ECLR1 = Register(ports.io, 0x04D1u, BYTE, "ECLR1")

    enum class CR_LOW_READ_STATE { IR, ISR, NONE }
    enum class CR_HIGH_WRITE_STATE { MSK, ICW2, ICW3, ICW4 }

    class Pin(
            val num: Int,
            var inService: Boolean = false,
            var pending: Boolean = false,
            var masked: Boolean = false): IAutoSerializable, IConstructorSerializable

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

    inner class I8259(val offset: ULong) : IMultiplexer, IAutoSerializable {
        override var vectorOffset: Int = 0
        override val pins = Array(8) { Pin(it) }
        protected var portLowReadState = CR_LOW_READ_STATE.NONE
        protected var portHighWriteState = CR_HIGH_WRITE_STATE.MSK
        protected var omitICW3 = false
        protected var omitICW4 = false

        override fun masked(num: Int): Boolean = pins[num].masked
        override fun pending(num: Int): Boolean {
            return pins[num].pending
        }

        override fun inService(num: Int): Boolean {
            return pins[num].inService
        }

        private val IR = object : Register(ports.io, offset, BYTE, "IR", writable = false) {
            override fun beforeRead(from: MasterPort, ea: ULong) = portLowReadState == CR_LOW_READ_STATE.IR

            override fun read(ea: ULong, ss: Int, size: Int): ULong {
                data = pins.filter { it.pending }.fold(0) { result, it -> result or (1 shl it.num) }.ulong_z
                portLowReadState = CR_LOW_READ_STATE.NONE
                return super.read(ea, ss, size)
            }
        }

        private val ISR = object : Register(ports.io, offset, BYTE, "ISR", writable = false) {
            override fun beforeRead(from: MasterPort, ea: ULong) = portLowReadState == CR_LOW_READ_STATE.ISR

            override fun read(ea: ULong, ss: Int, size: Int): ULong {
                data = pins.filter { it.inService }.fold(0) { result, it -> result or (1 shl it.num) }.ulong_z
                portLowReadState = CR_LOW_READ_STATE.NONE
                return super.read(ea, ss, size)
            }
        }

        private val INTMSK = object : Register(ports.io, offset + 1u, BYTE, "INTMSK") {
            override fun beforeRead(from: MasterPort, ea: ULong) = portHighWriteState == CR_HIGH_WRITE_STATE.MSK
            override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) = portHighWriteState == CR_HIGH_WRITE_STATE.MSK

            override fun read(ea: ULong, ss: Int, size: Int): ULong {
                data = pins.filter { it.masked }.fold(0) { result, it -> result or (1 shl it.num) }.ulong_z
                return super.read(ea, ss, size)
            }

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                super.write(ea, ss, size, value)
                pins.forEach {
                    val bit = data[it.num].truth
                    if (it.masked xor bit) {
                        it.masked = bit
                        log.finer { "$name WR <- IM${it.num}=${it.masked}" }
                    }
                }
            }
        }

        private val ICW1 = object : Register(ports.io, offset, BYTE, "ICW1", readable = false) {
            val SLCT_ICW1 by bit(SLCT_ICW1_BIT)
            val LTIM by bit(3)
            var ADI by bit(2)
            val SNGL by bit(1)
            val IC4 by bit(0)

            override fun stringify() = "${super.stringify()} [SLCT_ICW1=$SLCT_ICW1 LTIM=$LTIM ADI=$ADI SNGL=$SNGL IC4=$IC4]"

            override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) = value[SLCT_ICW1_BIT] == 1uL

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                super.write(ea, ss, size, value)
                omitICW4 = !IC4.truth
                portHighWriteState = CR_HIGH_WRITE_STATE.ICW2
            }
        }


        private val ICW2 = object : Register(ports.io, offset + 1u, BYTE, "ICW2", readable = false) {
            val T7T3 by field(7..3)
            var A10A8 by field(2..0)

            override fun stringify() = "${super.toString()} [T7T3=$T7T3 A10A8=$A10A8]"

            override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) =
                    portHighWriteState == CR_HIGH_WRITE_STATE.ICW2

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                super.write(ea, ss, size, value)
                A10A8 = 0u
                vectorOffset = (T7T3 shl 3).int
                portHighWriteState = when {
                    !omitICW3 -> CR_HIGH_WRITE_STATE.ICW3
                    !omitICW4 -> CR_HIGH_WRITE_STATE.ICW4
                    else -> CR_HIGH_WRITE_STATE.MSK
                }
            }
        }

        private val ICW3 = object : Register(ports.io, offset + 1u, BYTE, "ICW3", readable = false) {
            var S7 by bit(7)
            var S6 by bit(6)
            var S5 by bit(5)
            var S4 by bit(4)

            var S3 by bit(3)
            var S2 by bit(2)
            var S1 by bit(1)
            var S0 by bit(0)

            override fun stringify() = "${super.stringify()} [S2=$S2 S5=$S5]"

            override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) =
                    portHighWriteState == CR_HIGH_WRITE_STATE.ICW3

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                super.write(ea, ss, size, value)
                S0 = 0; S1 = 0; S3 = 0; S4 = 0; S6 = 0; S7 = 0
                portHighWriteState = when {
                    !omitICW4 -> CR_HIGH_WRITE_STATE.ICW4
                    else -> CR_HIGH_WRITE_STATE.MSK
                }
            }
        }

        private val ICW4 = object : Register(ports.io, offset + 1u, BYTE, "ICW4", readable = false) {
            val SFNM by bit(4)
            val BUF_MS by field(3..2)
            val AEOI by bit(1)
            var PM by bit(0)

            override fun stringify() = "${super.toString()} [SFNM=$SFNM BUF_MS=$BUF_MS AEOI=$AEOI PM=$PM]"

            override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) =
                    portHighWriteState == CR_HIGH_WRITE_STATE.ICW4

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                super.write(ea, ss, size, value)
                PM = 1 // In the ÉlanSC520 microcontroller design, this PC/AT-compatible bit is internally fixed to 1
                portHighWriteState = CR_HIGH_WRITE_STATE.MSK
            }
        }

        private val OCW2 = object : Register(ports.io, offset, BYTE, "OCW2", readable = false) {
            val R_SL_EOI by field(7..5)
            val SLCT_ICW1 by bit(SLCT_ICW1_BIT)
            val IS_OCW3 by bit(IS_OCW3_BIT)
            val LS by field(2..0)

            override fun stringify() = "${super.stringify()} [R_SL_EOI=$R_SL_EOI SLCT_ICW1=$SLCT_ICW1 IS_OCW3=$IS_OCW3 LS=$LS]"

            override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) = value[SLCT_ICW1_BIT] == 0uL && value[IS_OCW3_BIT] == 0uL

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                super.write(ea, ss, size, value)
                // FIXME: Oh, it's gonna be fack up here ... quite a strange EOI types: Rotate on nonspecific EOI command
                pins[LS.int].inService = !R_SL_EOI.truth
            }
        }

        private val OCW3 = object : Register(ports.io, offset, BYTE, "OCW3", readable = false) {
            val ESMM_SMM by field(6..5)
            val SLCT_ICW1 by bit(SLCT_ICW1_BIT)
            val IS_OCW3 by bit(IS_OCW3_BIT)
            val P by bit(2)
            val RR_RIS by field(1..0)

            override fun stringify() = "${super.stringify()} [ESMM_SMM=$ESMM_SMM SLCT_ICW1=$SLCT_ICW1 IS_OCW3=$IS_OCW3 P=$P RR_RIS=$RR_RIS]"

            override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) = value[SLCT_ICW1_BIT] == 0uL && value[IS_OCW3_BIT] == 1uL

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                super.write(ea, ss, size, value)
                when (RR_RIS) {
                    2uL -> portLowReadState = CR_LOW_READ_STATE.IR
                    3uL -> portLowReadState = CR_LOW_READ_STATE.ISR
                }
            }
        }
    }

    inner class Interrupt(irq: Int, val pic: I8259, val postfix: String) : AInterrupt(irq, postfix), IAutoSerializable {
        override val cop get() = core.cop

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

        override val vector: Int
            get() = pic.vectorOffset or irq

        override val priority: Int get() = irq + 1

        override val masked: Boolean
            get() {
                return !enabled && pic.masked(irq)
            }

        override fun onInterrupt() = Unit

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return super<IAutoSerializable>.serialize(ctxt)
        }

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super<IAutoSerializable>.deserialize(ctxt, snapshot)
        }
    }

    private val master = I8259(0x20u)
    private val slave = I8259(0xA0u)


    private val IRQ = Interrupts(ports.irq, "IRQ",
        Interrupt(0, master,"IRQ0"), // IRQ 0 — system timer
        Interrupt(1, master,"IRQ1"), // IRQ 1 — keyboard controller
        // 2 is a cascade of slave
        Interrupt(3, master,"IRQ3"), // IRQ 3 — serial port COM2, COM4
        Interrupt(4, master,"IRQ4"), // IRQ 4 — serial port COM1, COM3
        Interrupt(5, master,"IRQ5"), // IRQ 5 — parallel port 2 and 3 or sound card
        Interrupt(6, master,"IRQ6"), // IRQ 6 — floppy controller
        Interrupt(7, master,"IRQ7"), // IRQ 7 — parallel port 1

        Interrupt(8, slave,"IRQ8"),  // IRQ 8 — RTC timer
        Interrupt(9, slave,"IRQ9"),  // IRQ 9 — ACPI
        Interrupt(10, slave,"IRQ10"),// IRQ 10 — open/SCSI/NIC
        Interrupt(11, slave,"IRQ11"),// IRQ 11 — open/SCSI/NIC
        Interrupt(12, slave,"IRQ12"),// IRQ 12 — mouse controller
        Interrupt(13, slave,"IRQ13"),// IRQ 13 — math co-processor
        Interrupt(14, slave,"IRQ14"),// IRQ 14 — ATA channel 1
        Interrupt(15, slave,"IRQ15") // IRQ 15 — ATA channel 2
    )

}