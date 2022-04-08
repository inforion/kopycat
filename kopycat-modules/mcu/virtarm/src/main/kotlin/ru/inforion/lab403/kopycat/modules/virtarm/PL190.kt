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
@file:Suppress("PropertyName", "unused")

package ru.inforion.lab403.kopycat.modules.virtarm

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.abstracts.APIC
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.serializer.storeValues


class PL190(parent: Module, name: String) : APIC(parent, name) {
    companion object {
        const val INTERRUPT_COUNT = 16
    }

    inner class Ports : ModulePorts(this) {
        val irq = Slave("irq", INTERRUPT_COUNT)
        val mem = Slave("mem", 0x1000)
    }

    override val ports = Ports()


    inner class Interrupt(
            irq: Int,
            postfix: String) : AInterrupt(irq, postfix) {

        override var priority: Int
            get() = vector
            set(value) = throw IllegalAccessException("Can't change priority")

        override val vector: Int get() = VICVECTCNTL.first { it.intSource == irq.ulong_z}.index
        override val cop get() = core.cop

        // Interrupt serialization is unnecessary, because all data gets by VICVECTCNTL which is array of registers,
        // and all registers in VICVECTCNTL have their own serialize method. But this is left for info in snapshot to user.
        override fun serialize(ctxt: GenericSerializer) = storeValues(
                "irq" to irq,
                "name" to name,
                "nmi" to nmi,
                "pending" to pending,
                "enabled" to enabled,
                "priority" to try { priority } catch (e: NoSuchElementException) { -1 }
        )
    }

    val interrupts = Interrupts(ports.irq,  "IRQ", *Array(INTERRUPT_COUNT) { Interrupt(it, "IRQ$it")})

    /**
     * See 2.1.6
     * A vectored interrupt is only generated if the following are true:
     * > you enable it in the interrupt enable register, [VICINTENABLE]
     * > you set it to generate an IRQ interrupt in the interrupt select register, [VICINTSELECT]
     * > you enable it in the relevant vector control register, [VICVECTCNTL] [0-15].
     */
    fun updateEnabled(index: Int) {
        interrupts[index].enabled = VICINTENABLE.data[index].truth
                && !VICINTSELECT.data[index].truth
                && VICVECTCNTL[index].e.truth

        // Clear interrupt on mask
        if (interrupts[index].pending && !interrupts[index].enabled)
            interrupts[index].pending = false
    }

    fun updateEnabled() = repeat(INTERRUPT_COUNT) { updateEnabled(it) }


    // ===== Registers =====

    open inner class DEBUG_REGISTER(address: ULong, name: String) : Register(ports.mem, address, Datatype.DWORD, name) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            TODO("Not implemented")
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            TODO("Not implemented")
        }
    }

    // IRQ Status Register, see 3.3.1
    // 1 - interrupt is active
    // 0 - otherwise
    val VICIRQSTATUS = object : Register(ports.mem, 0x000u, Datatype.DWORD, "VICIRQSTATUS") {

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            var data = 0uL
            repeat(16) { i ->
                if (interrupts[i].pending)
                    data = data or (1uL shl i)
            }
            return data
        }

        // It's read-only
        // See Linux 2.6.31.12 sources, vic.c:
        // writel(0, base + VIC_IRQ_STATUS);
        // So we can't just use "readable = false"
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit

    }

    val VICFIQSTATUS = DEBUG_REGISTER(0x004u, "VICFIQSTATUS")
    val VICRAWINTR = DEBUG_REGISTER(0x008u, "VICRAWINTR")

    // Interrupt Select Register, see 3.3.4
    // 1 - FIQ
    // 0 - IRQ
    val VICINTSELECT = object : Register(ports.mem, 0x00Cu, Datatype.DWORD,"VICINTSELECT") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            updateEnabled()
        }
    }

    // Interrupt Enable Register, see 3.3.5
    // 1 - enabled
    // 0 - disabled
    val VICINTENABLE = object : Register(ports.mem, 0x010u, Datatype.DWORD, "VICINTENABLE") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            data = data or value // A LOW bit has no effect.
            updateEnabled()
        }
    }

    // Interrupt Enable Clear Register, see 3.3.6
    // 1 - clear
    // 0 - stay
    val VICINTENCLEAR = object : Register(ports.mem, 0x014u, Datatype.DWORD, "VICINTENCLEAR", readable = false) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            VICINTENABLE.data = VICINTENABLE.data and value.inv() // A LOW bit has no effect.
            updateEnabled()
        }
    }

    val VICSOFTINT = DEBUG_REGISTER(0x018u, "VICSOFTINT")

    // Software Interrupt Clear Register, see 3.3.8
    // 1 - clear
    // 0 - stay
    val VICSOFTINTCLEAR = object : Register(ports.mem, 0x01Cu, Datatype.DWORD, "VICSOFTINTCLEAR") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            VICSOFTINT.data = VICSOFTINT.data and value.inv() // A LOW bit has no effect.
        }
    }

    val VICPROTECTION = DEBUG_REGISTER(0x020u, "VICPROTECTION")

    // Vector Address Register, see 3.3.10
    // Contains the Interrupt Service Routine (ISR) address of the currently active interrupt.
    val VICVECTADDR = Register(ports.mem, 0x030u, Datatype.DWORD, "VICVECTADDR")

    // Default Vector Address Register, see 3.3.11
    // Contains the address of the default ISR handler
    val VICDEFVECTADDR = Register(ports.mem, 0x034u, Datatype.DWORD, "VICDEFVECTADDR")

    val VICVECTADDR0 = DEBUG_REGISTER(0x100u, "VICVECTADDR0")
    val VICVECTADDR1 = DEBUG_REGISTER(0x104u, "VICVECTADDR1")
    val VICVECTADDR2 = DEBUG_REGISTER(0x108u, "VICVECTADDR2")
    val VICVECTADDR3 = DEBUG_REGISTER(0x10Cu, "VICVECTADDR3")
    val VICVECTADDR4 = DEBUG_REGISTER(0x110u, "VICVECTADDR4")
    val VICVECTADDR5 = DEBUG_REGISTER(0x114u, "VICVECTADDR5")
    val VICVECTADDR6 = DEBUG_REGISTER(0x118u, "VICVECTADDR6")
    val VICVECTADDR7 = DEBUG_REGISTER(0x11Cu, "VICVECTADDR7")
    val VICVECTADDR8 = DEBUG_REGISTER(0x120u, "VICVECTADDR8")
    val VICVECTADDR9 = DEBUG_REGISTER(0x124u, "VICVECTADDR9")
    val VICVECTADDR10 = DEBUG_REGISTER(0x128u, "VICVECTADDR10")
    val VICVECTADDR11 = DEBUG_REGISTER(0x12Cu, "VICVECTADDR11")
    val VICVECTADDR12 = DEBUG_REGISTER(0x130u, "VICVECTADDR12")
    val VICVECTADDR13 = DEBUG_REGISTER(0x134u, "VICVECTADDR13")
    val VICVECTADDR14 = DEBUG_REGISTER(0x138u, "VICVECTADDR14")
    val VICVECTADDR15 = DEBUG_REGISTER(0x13Cu, "VICVECTADDR15")


    // Vector Control Registers, see 3.3.13
    inner class VICVECTCNTLN(val index: Int) : Register(ports.mem, 0x200u + index.uint * 4uL, Datatype.DWORD, "VICVECTCNTL$index") {
        var intSource by field(4..0)
        var e by bit(5)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            updateEnabled(index)
        }
    }
    val VICVECTCNTL = Array(16) { VICVECTCNTLN(it) }

    // Test Control Register, see 4.3.1
    // Integration test enable:
    // 1 - test mode
    // 0 - normal mode
    val VICITCR = object : Register(ports.mem, 0x300u, Datatype.DWORD, "VICITCR") {
        var iten by bit(0)
    }

    val VICITIP1 = DEBUG_REGISTER(0x304u, "VICITIP1")
    val VICITIP2 = DEBUG_REGISTER(0x308u, "VICITIP2")
    val VICITOP1 = DEBUG_REGISTER(0x30Cu, "VICITOP1")
    val VICITOP2 = DEBUG_REGISTER(0x310u, "VICITOP2")

    val VICPERIPHID0 = DEBUG_REGISTER(0xFE0u, "VICPERIPHID0")
    val VICPERIPHID1 = DEBUG_REGISTER(0xFE4u, "VICPERIPHID1")
    val VICPERIPHID2 = DEBUG_REGISTER(0xFE8u, "VICPERIPHID2")
    val VICPERIPHID3 = DEBUG_REGISTER(0xFECu, "VICPERIPHID3")
    val VICPCELLID0 = DEBUG_REGISTER(0xFF0u, "VICPCELLID0")
    val VICPCELLID1 = DEBUG_REGISTER(0xFF4u, "VICPCELLID1")
    val VICPCELLID2 = DEBUG_REGISTER(0xFF8u, "VICPCELLID2")
    val VICPCELLID3 = DEBUG_REGISTER(0xFFCu, "VICPCELLID3")
}