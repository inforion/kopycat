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
package ru.inforion.lab403.kopycat.modules.pic32mz

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.convertBooleanArrayToNumber
import ru.inforion.lab403.common.extensions.convertNumberToBooleanArray
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.abstracts.APIC
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.field
import java.util.logging.Level
import kotlin.properties.Delegates

@Suppress("PrivatePropertyName", "PropertyName", "unused")
/**
 *
 * Hardware peripherals interrupt controller
 */
class PIC(parent: Module, name: String) : APIC(parent, name) {
    companion object {
        @Transient val log = logger(Level.INFO)

        const val INTERRUPT_COUNT = 214
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
        val irq = Slave("irq", INTERRUPT_COUNT)
    }

    override val ports = Ports()

    /* ============================ Registers ============================ */

    inner class Interrupt(irq: Int, postfix: String) : AInterrupt(irq, postfix) {
        override val cop get() = core.cop

        var subPriority: Int by Delegates.observable(0) { _, old, new ->
            if (old != new) {
                log.config { "$name subPriority changed $old -> $new" }
            }
        }
        var mainPriority: Int by Delegates.observable(0) { _, old, new ->
            if (old != new) {
                log.config { "$name mainPriority changed $old -> $new" }
            }
        }

        override var vector: Int = 0
        override val priority: Int get() = (mainPriority shl 2) or subPriority
        override val cause: Int get() = mainPriority

        override fun onInterrupt() {
            // pending = false  // TODO: Seems something wrong but software in FreeRTOS won't clear it by itself
            INTSTAT.SIRQ = vector
            INTSTAT.SRIPL = cause
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private val INTCON = object : ComplexRegister(ports.mem,0xBF81_0000, name = "INTCON") {
        var NMIKEY by field(7..0)
        var MVEC by bit(12)
        var TPC by field(10..8)
        var INT4EP by bit(4)
        var INT3EP by bit(3)
        var INT2EP by bit(2)
        var INT1EP by bit(1)
        var INT0EP by bit(0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.config { "INTCON[NMIKEY=$NMIKEY MVEC=$MVEC TPC=$TPC INT4EP=$INT4EP INT3EP=$INT3EP INT2EP=$INT2EP INT1EP=$INT1EP INT0EP=$INT0EP]" }
        }
    }

    private val PRISS = object : ComplexRegister(ports.mem,0xBF81_0010, name = "PRISS") {
        var PRI7SS by field(31..28)
        var PRI6SS by field(27..24)
        var PRI5SS by field(23..20)
        var PRI4SS by field(19..16)
        var PRI3SS by field(15..12)
        var PRI2SS by field(11..8)
        var PRI1SS by field(7..4)
        var SS0 by bit(0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.config { "PRISS[PRI7SS=$PRI7SS PRI6SS=$PRI6SS PRI5SS=$PRI5SS PRI4SS=$PRI4SS PRI3SS=$PRI3SS PRI2SS=$PRI2SS PRI1SS=$PRI1SS SS0=$SS0]" }
        }
    }

    private val INTSTAT = object : ComplexRegister(ports.mem,0x0020, name = "INTSTAT") {
        var SRIPL by field(10..8)
        var SIRQ by field(7..0)
//        var VEC by field(5..0)
    }

    private val IPTMR = ComplexRegister(ports.mem,0x0030, name = "IPTMR")

    private inner class IFSx(val ord: Int) : ComplexRegister(ports.mem,0x0040L + 0x10 * ord, name = "IFS$ord") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            return convertBooleanArrayToNumber { interrupts[ord * 32 + it].pending }
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            convertNumberToBooleanArray(value) { k, v -> interrupts[ord * 32 + k].pending = v }
        }
    }

    private inner class IECx(val ord: Int) : ComplexRegister(ports.mem,0x00C0L + 0x10 * ord, name = "IEC$ord") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            return convertBooleanArrayToNumber { interrupts[ord * 32 + it].enabled }
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            convertNumberToBooleanArray(value) { k, v -> interrupts[ord * 32 + k].enabled = v }
        }
    }

    private inner class IPCx(val ord: Int) : ComplexRegister(ports.mem,0x0140L + 0x10 * ord, name = "IPC$ord") {
        var IS0 by field(1..0)
        var IP0 by field(4..2)

        var IS1 by field(9..8)
        var IP1 by field(12..10)

        var IS2 by field(17..16)
        var IP2 by field(20..18)

        var IS3 by field(25..24)
        var IP3 by field(28..26)

        override fun read(ea: Long, ss: Int, size: Int): Long {
            IS0 = interrupts[ord * 4 + 0].subPriority
            IP0 = interrupts[ord * 4 + 0].mainPriority

            IS1 = interrupts[ord * 4 + 1].subPriority
            IP1 = interrupts[ord * 4 + 1].mainPriority

            IS2 = interrupts[ord * 4 + 2].subPriority
            IP2 = interrupts[ord * 4 + 2].mainPriority

            IS3 = interrupts[ord * 4 + 3].subPriority
            IP3 = interrupts[ord * 4 + 3].mainPriority
            return super.read(ea, ss, size)
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            interrupts[ord * 4 + 0].subPriority = IS0.asInt
            interrupts[ord * 4 + 0].mainPriority = IP0.asInt

            interrupts[ord * 4 + 1].subPriority = IS1.asInt
            interrupts[ord * 4 + 1].mainPriority = IP1.asInt

            interrupts[ord * 4 + 2].subPriority = IS2.asInt
            interrupts[ord * 4 + 2].mainPriority = IP2.asInt

            interrupts[ord * 4 + 3].subPriority = IS3.asInt
            interrupts[ord * 4 + 3].mainPriority = IP3.asInt
        }
    }

    private inner class OFF(val irq: Int) : ByteAccessRegister(ports.mem,0x0540L + 4 * irq, WORD, "OFF$irq") {
        var OFF by field(17..1)
        override fun read(ea: Long, ss: Int, size: Int): Long {
            OFF = interrupts[irq].vector ushr 1
            return super.read(ea, ss, size)
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            interrupts[irq].vector = OFF.asInt shl 1
        }
    }

    private val IFS_TABLE = Array(INTERRUPT_COUNT / 32 + 1) { IFSx(it) }
    private val IEC_TABLE = Array(INTERRUPT_COUNT / 32 + 1) { IECx(it) }
    private val IPC_TABLE = Array(INTERRUPT_COUNT / 4 + 1) { IPCx(it) }
    private val OFF_TABLE = Array(INTERRUPT_COUNT) { OFF(it) }

    val interrupts = Interrupts(ports.irq, "IRQ",
            Interrupt(1, "_CORE_SOFTWARE_0_VECTOR"),
            Interrupt(2, "_CORE_SOFTWARE_1_VECTOR"),

            Interrupt(4, "_TIMER_1_VECTOR"),
            Interrupt(9, "_TIMER_2_VECTOR"),
            Interrupt(14, "_TIMER_3_VECTOR"),
            Interrupt(19, "_TIMER_4_VECTOR"),
            Interrupt(24, "_TIMER_5_VECTOR"),
            Interrupt(28, "_TIMER_6_VECTOR"),
            Interrupt(32, "_TIMER_7_VECTOR"),
            Interrupt(36, "_TIMER_8_VECTOR"),
            Interrupt(40, "_TIMER_9_VECTOR"))

    override fun command(): String = "ic"
}