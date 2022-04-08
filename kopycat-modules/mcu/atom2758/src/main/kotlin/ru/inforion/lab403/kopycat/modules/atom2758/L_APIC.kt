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

import ru.inforion.lab403.common.extensions.enum
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.util.logging.Level.CONFIG

class L_APIC(parent: Module, name: String) : Module(parent, name) {
    enum class DeliveryMode {
        FIXED,  // 0
        RESERVED1,  // 1
        SMI,  // 2
        RESERVED3, // 3
        NMI,  // 4
        INIT,  // 5
        START_UP,  // 6
        RESERVED7  // 7
    }

    enum class DestinationShorthand {
        NO_SHORTHAND, // 0
        SELF,  // 1
        ALL_INCLUDING_SELF, // 2
        ALL_EXCLUDING_SELF  // 3
    }

    enum class DestinationMode {
        Physical, // 0
        Logica,  // 1
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x10_0000)
        val lio = Slave("lio", 0x100)  // local i/o
    }

    override val ports = Ports()

    //  FEE0_0000 - FEEF_FFFF

    // https://wiki.osdev.org/APIC

    private val x86 get() = core as x86Core

    private val APIC_ID = Register(ports.mem, 0x020u, DWORD, "APIC_ID", 0x4655434Bu, level = CONFIG)
    private val APIC_VER = Register(ports.mem, 0x030u, DWORD, "APIC_VER", 0x494E544Cu, level = CONFIG)

    private val TPR = Register(ports.mem, 0x080u, DWORD, "TPR", level = CONFIG)
    private val PPR = Register(ports.mem, 0x0A0u, DWORD, "PPR", level = CONFIG)
    private val EOI = Register(ports.mem, 0x0B0u, DWORD, "EOI", level = CONFIG)
    private val LDR = Register(ports.mem, 0x0D0u, DWORD, "LDR", level = CONFIG)

    private val SIVR = Register(ports.mem, 0x0F0u, DWORD, "SIVR", level = CONFIG)

    private val ISR0 = Register(ports.mem, 0x100u, DWORD, "ISR0", level = CONFIG)
    private val ISR1 = Register(ports.mem, 0x110u, DWORD, "ISR1", level = CONFIG)
    private val ISR2 = Register(ports.mem, 0x120u, DWORD, "ISR2", level = CONFIG)
    private val ISR3 = Register(ports.mem, 0x130u, DWORD, "ISR3", level = CONFIG)
    private val ISR4 = Register(ports.mem, 0x140u, DWORD, "ISR4", level = CONFIG)
    private val ISR5 = Register(ports.mem, 0x150u, DWORD, "ISR5", level = CONFIG)
    private val ISR6 = Register(ports.mem, 0x160u, DWORD, "ISR6", level = CONFIG)
    private val ISR7 = Register(ports.mem, 0x170u, DWORD, "ISR7", level = CONFIG)

    private val TMR0 = Register(ports.mem, 0x180u, DWORD, "TMR0", level = CONFIG)
    private val TMR1 = Register(ports.mem, 0x190u, DWORD, "TMR1", level = CONFIG)
    private val TMR2 = Register(ports.mem, 0x1A0u, DWORD, "TMR2", level = CONFIG)
    private val TMR3 = Register(ports.mem, 0x1B0u, DWORD, "TMR3", level = CONFIG)
    private val TMR4 = Register(ports.mem, 0x1C0u, DWORD, "TMR4", level = CONFIG)
    private val TMR5 = Register(ports.mem, 0x1D0u, DWORD, "TMR5", level = CONFIG)
    private val TMR6 = Register(ports.mem, 0x1E0u, DWORD, "TMR6", level = CONFIG)
    private val TMR7 = Register(ports.mem, 0x1F0u, DWORD, "TMR7", level = CONFIG)

    private val IRR0 = Register(ports.mem, 0x200u, DWORD, "IRR0", level = CONFIG)
    private val IRR1 = Register(ports.mem, 0x210u, DWORD, "IRR1", level = CONFIG)
    private val IRR2 = Register(ports.mem, 0x220u, DWORD, "IRR2", level = CONFIG)
    private val IRR3 = Register(ports.mem, 0x230u, DWORD, "IRR3", level = CONFIG)
    private val IRR4 = Register(ports.mem, 0x240u, DWORD, "IRR4", level = CONFIG)
    private val IRR5 = Register(ports.mem, 0x250u, DWORD, "IRR5", level = CONFIG)
    private val IRR6 = Register(ports.mem, 0x260u, DWORD, "IRR6", level = CONFIG)
    private val IRR7 = Register(ports.mem, 0x270u, DWORD, "IRR7", level = CONFIG)

    private val ESR = Register(ports.mem, 0x280u, DWORD, "ESR", level = CONFIG)

    private val ICR0 = object : Register(ports.mem, 0x300u, DWORD, "ICR0", level = CONFIG) {
        var vector by field(7..0)
        var deliveryMode by field(10..8)
        var destinationMode by bit(11)
        var status by bit(12)
        var assert by bit(14)
        var trigger by bit(15)
        var destinationShorthand by field(19..18)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            status = 0
            val mode = deliveryMode.enum<DeliveryMode>()
            val shorthand = destinationShorthand.enum<DestinationShorthand>()
            val destination = destinationMode.enum<DestinationMode>()

            log.warning { "[${core.cpu.pc.hex}] L_APIC requested delivery mode=$mode vector=0x${vector.hex} assert=$assert shorthand=$shorthand destination=$destination" }

            when (mode) {
                DeliveryMode.SMI -> x86.cpu.enterSmmMode(vector)
                else -> {
                    log.severe { "[${core.cpu.pc.hex}] unsupported delivery mode=$mode -> ignored" }
                }
            }
        }
    }

    private val ICR1 = Register(ports.mem, 0x304u, DWORD, "ICR1", level = CONFIG)
    private val ICR2 = Register(ports.mem, 0x308u, DWORD, "ICR2", level = CONFIG)
    private val ICR3 = Register(ports.mem, 0x30Cu, DWORD, "ICR3", level = CONFIG)
    private val ICR4 = Register(ports.mem, 0x310u, DWORD, "ICR4", level = CONFIG)

    private val LVT_TIM = Register(ports.mem, 0x320u, DWORD, "LVT_TIM", level = CONFIG)
    private val LVT_SEN = Register(ports.mem, 0x330u, DWORD, "LVT_SEN", level = CONFIG)
    private val LVT_PER = Register(ports.mem, 0x340u, DWORD, "LVT_PER", level = CONFIG)
    private val LVT_LINT0 = Register(ports.mem, 0x350u, DWORD, "LVT_LINT0", level = CONFIG)
    private val LVT_LINT1 = Register(ports.mem, 0x360u, DWORD, "LVT_LINT1", level = CONFIG)
    private val LVT_ERR = Register(ports.mem, 0x370u, DWORD, "LVT_ERR", level = CONFIG)

    private var count: ULong = 0u

    // Initial Count Register
    private val LVT_INIT = object : Register(ports.mem, 0x380u, DWORD, "LVT_INIT", level = CONFIG) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            count = data
        }
    }

    // Current Count Register
    private val LVT_CURRENT = object : Register(ports.mem, 0x390u, DWORD, "LVT_CURRENT", level = CONFIG) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            if (count != 0uL) count--
            data = count
            return super.read(ea, ss, size)
        }
    }

    // Divide Configuration Register
    private val LVT_DIV_CR = object : Register(ports.mem, 0x3E0u, DWORD, "LVT_DIV_CR", level = CONFIG) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong = 2u
    }

    private val IOAPIC_IDX = Register(ports.lio, 0x00u, DWORD, "IOAPIC_IDX", level = CONFIG)
    private val IOAPIC_WDW = Register(ports.lio, 0x10u, DWORD, "IOAPIC_WDW", level = CONFIG)
    private val IOAPIC_EOI = Register(ports.lio, 0x40u, DWORD, "IOAPIC_EOI", level = CONFIG)
}