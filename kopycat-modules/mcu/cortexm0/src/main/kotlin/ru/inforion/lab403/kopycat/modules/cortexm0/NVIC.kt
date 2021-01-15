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
package ru.inforion.lab403.kopycat.modules.cortexm0

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.abstracts.APIC
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import java.util.logging.Level

@Suppress("unused", "PrivatePropertyName")


class NVIC(parent: Module, name: String) : APIC(parent, name) {
    companion object {
        @Transient private val log = logger(Level.ALL)

        const val INTERRUPT_COUNT = 32
        const val EXCEPTION_COUNT = 16
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x400)
        val irq = Slave("irq", INTERRUPT_COUNT)
        val exc = Slave("exc", EXCEPTION_COUNT)
    }

    override val ports = Ports()

    inner class Exception(private val exceptionNum: Int, name: String) : AInterrupt(exceptionNum, name, "EXC") {
        override val cop get() = core.cop
        override val vector: Int get() = exceptionNum * 4
        override val priority: Int
            get() = when (exceptionNum) {
                1 -> 255
                2 -> 254
                3 -> 253
                else -> 0
            }
    }

    private val exceptions = Interrupts(ports.exc, "EXCEPTIONS",
            Exception(1, "RESET"),
            Exception(2, "NMI"),
            Exception(3, "Hard Fault"),
            Exception(11, "SVCall"),
            Exception(14, "PendSV"),
            Exception(15, "SysTick"))

    private fun Interrupts<Exception>.enableAll() {
        this[1].enabled = true
        this[2].enabled = true
        this[3].enabled = true
        this[11].enabled = true
        this[14].enabled = true
        this[15].enabled = true
    }

    inner class NVICInterrupt(irq: Int) : AInterrupt(irq, irq.toString()) {
        override val cop
            get() = core.cop
        val base = 0x40
        val exceptionNumber = irq + 16
        override val vector: Int
            get() = 4 * irq + 0x40
        override val priority: Int
            get() = when (irq) {
                0 -> IPR0.PRI_0
                1 -> IPR0.PRI_1
                2 -> IPR0.PRI_2
                3 -> IPR0.PRI_3
                4 -> IPR1.PRI_4
                5 -> IPR1.PRI_5
                6 -> IPR1.PRI_6
                7 -> IPR1.PRI_7
                8 -> IPR2.PRI_8
                9 -> IPR2.PRI_9
                10 -> IPR2.PRI_10
                11 -> IPR2.PRI_11
                12 -> IPR3.PRI_12
                13 -> IPR3.PRI_13
                14 -> IPR3.PRI_14
                15 -> IPR3.PRI_15
                16 -> IPR4.PRI_16
                17 -> IPR4.PRI_17
                18 -> IPR4.PRI_18
                19 -> IPR4.PRI_19
                20 -> IPR5.PRI_20
                21 -> IPR5.PRI_21
                22 -> IPR5.PRI_22
                23 -> IPR5.PRI_23
                24 -> IPR6.PRI_24
                25 -> IPR6.PRI_25
                26 -> IPR6.PRI_26
                27 -> IPR6.PRI_27
                28 -> IPR7.PRI_28
                29 -> IPR7.PRI_29
                30 -> IPR7.PRI_30
                31 -> IPR7.PRI_31
                else -> throw GeneralException("WRONG INTERRUPT IRQ")
            }
    }

    private val interrupts = Interrupts(ports.irq, "IRQ", *Array(INTERRUPT_COUNT) { NVICInterrupt(it) })

    /**
     * Interrupt set-enable register
     */
    inner class ISER_TYP : Register(ports.mem, 0x000, DWORD, "ISER") {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            repeat(INTERRUPT_COUNT) {
                if (value[it] == 1L)
                    interrupts[it].enabled = true
            }
        }

        // ICER and ISER read similar, it's ok!
        override fun read(ea: Long, ss: Int, size: Int): Long {
            var enabled = 0L
            repeat(INTERRUPT_COUNT) {
                if (interrupts[it].enabled)
                    enabled = enabled set it
            }
            return enabled
        }
    }

    /**
     * Interrupt clear-enable register
     */
    inner class ICER_TYP : Register(ports.mem, 0x080, DWORD, "ICER") {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            repeat(INTERRUPT_COUNT) {
                if (value[it] == 1L)
                    interrupts[it].enabled = false
            }
        }

        // ICER and ISER read similar, it's ok!
        override fun read(ea: Long, ss: Int, size: Int): Long {
            var enabled = 0L
            repeat(INTERRUPT_COUNT) {
                if (interrupts[it].enabled)
                    enabled = enabled set it
            }
            return enabled
        }
    }

    /**
     * Interrupt set-pending register
     */
    inner class ISPR_TYP : Register(ports.mem, 0x100, DWORD, "ISPR") {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            repeat(INTERRUPT_COUNT) {
                if (value[it] == 1L)
                    interrupts[it].pending = true
            }
        }

        // ICPR and ISPR read similar, it's ok!
        override fun read(ea: Long, ss: Int, size: Int): Long {
            var pending = 0L
            repeat(INTERRUPT_COUNT) {
                if (interrupts[it].pending)
                    pending = pending set it
            }
            return pending
        }
    }

    /**
     * Interrupt clear-pending register
     */
    inner class ICPR_TYP : Register(ports.mem, 0x180, DWORD, "ICPR") {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            repeat(INTERRUPT_COUNT) {
                if (value[it] == 1L)
                    interrupts[it].pending = false
            }
        }

        // ICPR and ISPR read similar, it's ok!
        override fun read(ea: Long, ss: Int, size: Int): Long {
            var pending = 0L
            repeat(INTERRUPT_COUNT) {
                if (interrupts[it].pending)
                    pending = pending set it
            }
            return pending
        }
    }

    private var ISER = ISER_TYP()
    private var ICER = ICER_TYP()
    private var ISPR = ISPR_TYP()
    private var ICPR = ICPR_TYP()

    private var IPR0 = object : Register(ports.mem, 0x300, DWORD, "IPR0") {
        var PRI_0 by field(7..0)
        var PRI_1 by field(15..8)
        var PRI_2 by field(23..16)
        var PRI_3 by field(31..24)
    }

    private var IPR1 = object : Register(ports.mem, 0x304, DWORD, "IPR1") {
        var PRI_4 by field(7..0)
        var PRI_5 by field(15..8)
        var PRI_6 by field(23..16)
        var PRI_7 by field(31..24)
    }

    private var IPR2 = object : Register(ports.mem, 0x308, DWORD, "IPR2") {
        var PRI_8 by field(7..0)
        var PRI_9 by field(15..8)
        var PRI_10 by field(23..16)
        var PRI_11 by field(31..24)
    }

    private var IPR3 = object : Register(ports.mem, 0x30C, DWORD, "IPR3") {
        var PRI_12 by field(7..0)
        var PRI_13 by field(15..8)
        var PRI_14 by field(23..16)
        var PRI_15 by field(31..24)
    }

    private var IPR4 = object : Register(ports.mem, 0x310, DWORD, "IPR4") {
        var PRI_16 by field(7..0)
        var PRI_17 by field(15..8)
        var PRI_18 by field(23..16)
        var PRI_19 by field(31..24)
    }

    private var IPR5 = object : Register(ports.mem, 0x314, DWORD, "IPR5") {
        var PRI_20 by field(7..0)
        var PRI_21 by field(15..8)
        var PRI_22 by field(23..16)
        var PRI_23 by field(31..24)
    }

    private var IPR6 = object : Register(ports.mem, 0x318, DWORD, "IPR6") {
        var PRI_24 by field(7..0)
        var PRI_25 by field(15..8)
        var PRI_26 by field(23..16)
        var PRI_27 by field(31..24)
    }

    private var IPR7 = object : Register(ports.mem, 0x31C, DWORD, "IPR7") {
        var PRI_28 by field(7..0)
        var PRI_29 by field(15..8)
        var PRI_30 by field(23..16)
        var PRI_31 by field(31..24)
    }

    override fun reset() {
        super.reset()
        exceptions.enableAll()
    }
}