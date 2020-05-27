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

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.memory.ROM

class PIC32MZ2048EFH144(parent: Module, name: String) : Module(parent, name) {

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val buses = Buses()

    val mips = MipsCore(
            this,
            "mips",
            frequency = 200.MHz,
            multiplier = 1,
            ArchitectureRevision = 6,
            ipc = 1.0,
            PABITS = 30,
            PRId = 0x000000A7,
            EIC_option1 = true,
            syncSupported = true,
            countOfShadowGPR = 8)

    val ram0 = RAM(this, "ram0", 0x8_0000)
    val prog = ROM(this, "prog", 0x20_0000)
    val boot = ROM(this, "boot ", 0x7_4000)
//    val ebi = VOID(this, "ebi", 0x400_0000)
//    val sqi = VOID(this, "sqi", 0x400_0000)

    val pic = PIC(this, "pic")
    val osc = OSC(this, "osc")
    val system = SYS(this, "system")
    val timer1 = Timer(this, "timer1")
    val timer2 = Timer(this, "timer2")
    val timer3 = Timer(this, "timer3")
    val timer4 = Timer(this, "timer4")
    val timer5 = Timer(this, "timer5")
    val timer6 = Timer(this, "timer6")
    val timer7 = Timer(this, "timer7")
    val timer8 = Timer(this, "timer8")
    val timer9 = Timer(this, "timer9")
    val gpioa = GPIO(this, "gpioa")
    val gpiob = GPIO(this, "gpiob")
    val gpioc = GPIO(this, "gpioc")
    val gpiod = GPIO(this, "gpiod")
    val gpioe = GPIO(this, "gpioe")
    val gpiof = GPIO(this, "gpiof")
    val gpiog = GPIO(this, "gpiog")
    val gpioh = GPIO(this, "gpioh")
    val gpioj = GPIO(this, "gpioj")
    val gpiok = GPIO(this, "gpiok")

    init {
        ram0.ports.mem.connect(buses.mem, 0xA000_0000)
        prog.ports.mem.connect(buses.mem, 0xBD00_0000)
        boot.ports.mem.connect(buses.mem, 0xBFC0_0000)
//        TODO ebi.ports.mem.connect(buses.mem, 0x2000_0000) phy?
//        TODO sqi.ports.mem.connect(buses.mem, 0x3000_0000) phy?

        // SFR base at 0x1F800000 [phy]
        pic.ports.mem.connect(buses.mem, 0xBF81_0000)
        osc.ports.mem.connect(buses.mem, 0xBF80_0000)

        system.ports.mem.connect(buses.mem, 0xBF80_0000)

        timer1.ports.mem.connect(buses.mem, 0xBF84_0000)
        timer2.ports.mem.connect(buses.mem, 0xBF84_0200)
        timer3.ports.mem.connect(buses.mem, 0xBF84_0400)
        timer4.ports.mem.connect(buses.mem, 0xBF84_0600)
        timer5.ports.mem.connect(buses.mem, 0xBF84_0800)
        timer6.ports.mem.connect(buses.mem, 0xBF84_0A00)
        timer7.ports.mem.connect(buses.mem, 0xBF84_0C00)
        timer8.ports.mem.connect(buses.mem, 0xBF84_0E00)
        timer9.ports.mem.connect(buses.mem, 0xBF84_1000)

        gpioa.ports.mem.connect(buses.mem, 0xBF86_0000)
        gpiob.ports.mem.connect(buses.mem, 0xBF86_0100)
        gpioc.ports.mem.connect(buses.mem, 0xBF86_0200)
        gpiod.ports.mem.connect(buses.mem, 0xBF86_0300)
        gpioe.ports.mem.connect(buses.mem, 0xBF86_0400)
        gpiof.ports.mem.connect(buses.mem, 0xBF86_0500)
        gpiog.ports.mem.connect(buses.mem, 0xBF86_0600)
        gpioh.ports.mem.connect(buses.mem, 0xBF86_0700)
        gpioj.ports.mem.connect(buses.mem, 0xBF86_0800)
        gpiok.ports.mem.connect(buses.mem, 0xBF86_0900)
    }
}