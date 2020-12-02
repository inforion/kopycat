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

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.modules.common.pci.pci_proxy
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.cores.x86Core.Generation
import ru.inforion.lab403.kopycat.modules.cores.x86Core.Generation.Am5x86


class AMDElanSC520(
        parent: Module,
        name: String,

// FIXME: Must be Am5x86 but something wrong here 0000B8F7 with flags and DMA cache
// Perhaps error occurred in early boot and hides after loading from snapshot.
// If cacheArchLibInitDone != 0 then software try to go insane and satisfy mutually exclusive conditions:
// Software setup loadingString for i82551 with "-1:..." (perhaps from early boot perhaps not, where not know)
// and specify dmaCacheFlushRtn => all these conditions lead to error in x_fei82557InitMem[00013F6C]
// "fei82557EndLoad: shared \n\t\t\t\t\t    memory not cache coherent"
// Variable cacheArchLibInitDone == 0 only if AC flag is insensible (always = 0) but it is true only
// for prior i486SX processors (Elan is Am5x86)
//
// But for eth module it should be as it is...
        generation: Generation = Am5x86,

        RSTLD: Int = 0
) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val gpio = Proxy("gpio", BUS16)

        val gpcs = proxies(8, "gpcs")
        val bootcs = Proxy("bootcs", BUS32)
        val romcs = proxies(2, "romcs", BUS16)
        val sdram = Proxy("sdram", BUS28)

        val irq_pci = Proxy("irq_pci", PCI_INTERRUPTS_COUNT)
        val irq_gp = Proxy("irq_gp", PIC.GP_INTERRUPT_COUNT)

        val pci = pci_proxy("pci")
        val map = Proxy("map")
    }

    override val ports = Ports()

    inner class Buses : ModuleBuses(this) {
        val gpio = Bus("gpio", BUS16)
        val mmcr = Bus("mmcr", BUS12)
    }

    override val buses = Buses()

    val x86 = x86Core(
            this,
            "x86",
            frequency = 133.MHz,
            generation = generation,
            ipc = 0.388)

    val pic = PIC(this, "pic")

    val gpio = GPIO(this, "gpio")
    val scp = SCP(this, "scp", RSTLD)
    val dmac = DMAC(this, "dmac")
    val gpbus = GPBUS(this, "gpbus")
    val sam = SAM(this, "sam")
    val sac = SAC(this, "sac")  // system arbiter control
    val uart1 = UART(this, "uart1", 1)
    val uart2 = UART(this, "uart2", 2)
    val am5x86 = Am5X86(this, "am5x86")
    val pci = PCI(this, "pci")
    val pit = PIT(this, "pit")
    val rtc = RTC(this, "rtc")
    val sdram_ctrl = SDRAM(this, "sdram_ctrl")
    val boot_ctrl = BOOT(this, "boot_ctrl")

    init {
        // x86 connect to internal x5 mem/io bus and
        // ElanSC520 system address mapping controller connect to x5 mem/io bus
        buses.connect(x86.ports.mem, sam.ports.x5mem)
        buses.connect(x86.ports.io, sam.ports.x5io)

        pic.ports.io.connect(buses.gpio)
        pit.ports.io.connect(buses.gpio)
        gpio.ports.io.connect(buses.gpio)
        scp.ports.io.connect(buses.gpio)
        dmac.ports.io.connect(buses.gpio)
        gpbus.ports.io.connect(buses.gpio)
        uart1.ports.io.connect(buses.gpio, 0x300)
        uart2.ports.io.connect(buses.gpio, 0x200)
//        am5x86.ports.io.connect(buses.gpio)
//        pci.ports.io.connect(buses.gpio)
        sac.ports.io.connect(buses.gpio)
        rtc.ports.io.connect(buses.gpio)
//        sdram.ports.io.connect(buses.io)

        // SAM input MMCR and GP I/O buses
        sam.ports.gpio_s.connect(buses.gpio)  // connect MMCR slave port of SAM to bus
        sam.ports.mmcr_s.connect(buses.mmcr)  // connect GP I/O slave port of SAM to bus

        buses.connect(sam.ports.pci_mem, pci.ports.mem)
        buses.connect(sam.ports.pci_io, pci.ports.io)

        pic.ports.mmcr.connect(buses.mmcr)
        gpio.ports.mmcr.connect(buses.mmcr)
        scp.ports.mmcr.connect(buses.mmcr)
        dmac.ports.mmcr.connect(buses.mmcr)
        gpbus.ports.mmcr.connect(buses.mmcr)
        uart1.ports.mmcr.connect(buses.mmcr, 0xCC0)
        uart2.ports.mmcr.connect(buses.mmcr, 0xCC4)
        am5x86.ports.mmcr.connect(buses.mmcr)
        sac.ports.mmcr.connect(buses.mmcr)
        sdram_ctrl.ports.mmcr.connect(buses.mmcr)
        boot_ctrl.ports.mmcr.connect(buses.mmcr)

        pci.ports.mmcr.connect(buses.mmcr)

        // internal interrupts connect to inner ElanSC520 buses
        buses.connect(pit.ports.irq, pic.ports.pit)

        // SAM output MMCR and GP I/O buses
        sam.ports.mmcr_m.connect(buses.mmcr)  // connect MMCR master port of SAM to bus
        sam.ports.gpio_m.connect(buses.gpio)  // connect GP I/O master port of SAM to bus

        ports.gpio.connect(buses.gpio)

        buses.connect(ports.gpcs, sam.ports.gpcs)
        buses.connect(ports.bootcs, sam.ports.bootcs)
        buses.connect(ports.romcs, sam.ports.romcs)
        buses.connect(ports.sdram, sam.ports.sdram)

        // PCI output buses connect
        buses.connect(ports.pci, pci.ports.pci)
        buses.connect(ports.map, pci.ports.map)

        // PIC output buses connect
        buses.connect(ports.irq_gp, pic.ports.gp)
        buses.connect(ports.irq_pci, pic.ports.pci)
    }
}