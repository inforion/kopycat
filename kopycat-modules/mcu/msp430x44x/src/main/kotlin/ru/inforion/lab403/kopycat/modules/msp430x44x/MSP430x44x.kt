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
package ru.inforion.lab403.kopycat.modules.msp430x44x

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core
import ru.inforion.lab403.kopycat.modules.cores.MSP430Debugger
import ru.inforion.lab403.kopycat.modules.memory.RAM


// : ADebuggableMSP430(freq = 16.MHz, debugger = MSP430Debugger())
class MSP430x44x(parent: Module?, name: String): Module(parent, name) {

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS16)
        val irq = Bus("irq", BUS16)
    }

    override val buses = Buses()

    val msp430 = MSP430Core(this, "msp430", 16.MHz)
    
//    override val memory = MSP430SystemMemory(this).apply {
//        PeripheralIO("sfr",     start = 0x0000_0000, end = 0x0000_000F, access = ACCESS.R_W, extbus = 0, io = false)
//        PeripheralIO("per8",    start = 0x0000_0010, end = 0x0000_00FF, access = ACCESS.R_W, extbus = 0, io = false)
//        PeripheralIO("per16",   start = 0x0000_0100, end = 0x0000_01FF, access = ACCESS.R_W, extbus = 0, io = false)
//        Segment("ram",          start = 0x0000_0200, end = 0x0000_FFDF, access = ACCESS.R_W)
//        Segment("ivt",          start = 0x0000_FFE0, end = 0x0000_FFFF, access = ACCESS.R_W)
//        Segment("rom",          start = 0x0001_0000, end = 0x0001_FFFF, access = ACCESS.R_W)
//    }

    val ram = RAM(this,"RAM", 0x800)
    val ivt = RAM(this,"IVT", 0x20)
//    val rom = RAM(this,"ROM", 0x10000)

    val ic = PIC(this, "PIC")
    val watchdog = Watchdog(this, "Watchdog")
    val timerA = TimerA(this, "TimerA")
    val usart1 = USART1(this, "USART1")
    val hardwareMultiplier = HardwareMultiplier(this, "HMUL")

    private val dbg = MSP430Debugger(this, "dbg")

    init {
        msp430.ports.mem.connect(buses.mem, 0x0000_0000L)

        ram.ports.mem.connect(buses.mem, 0x0000_0200)
        ivt.ports.mem.connect(buses.mem, 0x0000_FFE0)
//        rom.ports.mem.connect(buses.mem, 0x0001_0000)

        ic.ports.mem.connect(buses.mem)
        watchdog.ports.mem.connect(buses.mem)

        timerA.ports.mem.connect(buses.mem)
        timerA.ports.irq_taccr0.connect(buses.irq, 0)
        timerA.ports.irq_reg.connect(buses.irq, 1)
        usart1.ports.irqTX.connect(buses.irq, 2)
        usart1.ports.irqRX.connect(buses.irq, 3)

        usart1.ports.mem.connect(buses.mem)
        hardwareMultiplier.ports.mem.connect(buses.mem)

        dbg.ports.breakpoint.connect(buses.mem)
        dbg.ports.reader.connect(buses.mem)
    }
}
