/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.cores.E500v2
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.terminals.UartNetworkTerminal
import java.nio.ByteOrder


class P2020(parent: Module?, name: String, port: Int? = null) : Module(parent, name) {

    inner class Buses : ModuleBuses(this) {
        val cbus = Bus("cbus")
        val ctrl = Bus("ctrl")
        val mem = Bus("mem")
        val ddr = Bus("ddr")
        val l2sdram = Bus("l2sdram")
        val flash = Bus("flash")
        val rx_bus = Bus("rx_bus")
        val tx_bus = Bus("tx_bus")
        val i2c_bus = Bus("i2c_bus")
    }

    override val buses = Buses()

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }
    override val ports = Ports()

    val e500v2 = E500v2(this, "ppc", 1200.MHz)


    val bridge = CoherencyModule(this, "bridge")
    val ecm = eCoherencyModule(this, "ECM")             // 0x0_1000
    val ddrc = DDRController(this, "DDRController")     // 0x0_2000
    val i2c = I2C(this, "I2C")                          // 0x0_3000
    val duart = DUART(this, "DUART")                    // 0x0_4000
    val elbc = EnhancedLocalBusCtrl(this, "eLBC")       // 0x0_5000

    val gpio = GPIO(this, "GPIO")                       // 0x0_F000

    val l2c = L2Cache(this, "L2Cache")                  // 0x2_0000

    val etsec1 = ETSEC(this, "ETSEC1", 1)            // 0x2_4000

    val pic = PIC(this, "PIC")                          // 0x4_0000

    val guts = GlobalUtilities(this, "GUTS")            // 0xE_0000?

    val l2sdram = RAM(this, "L2SDRAM", 1024*1024).apply { endian = ByteOrder.BIG_ENDIAN }
    val ddr0 = DDRMemory(this, "DDR0", 0x1000_0000u)
    val ddr1 = DDRMemory(this, "DDR1", 0x1000_0000u)

//    val boot = RAM(this, "boot", 0x200_0000, Resource("binaries/patched.bin"))
//    val flash0 = RAM(this, "flash0", 0x8_0000, Resource("binaries/spi_flash.bin")) // 512 Kb
//    val flash1 = RAM(this, "flash1", 0x8000, Resource("binaries/i2c_flash.bin"))   // 32 Kb

    val term = UartNetworkTerminal(this, "term", port)

    init {
        // === CORE ===
        // Core
        e500v2.ports.mem.connect(buses.cbus)

        // === DEBUGGER ===
        // Debugger to mem
//        dbg.ports.breakpoint.connect(buses.mem)
//        dbg.ports.reader.connect(buses.mem)

        // === MODULES ===
        // Bridge
        bridge.ports.inp.connect(buses.cbus)
        bridge.ports.ctrl.connect(buses.ctrl)
        bridge.ports.outp.connect(buses.mem)

        // Coherency module to ctrl
        ecm.ports.ctrl.connect(buses.ctrl)

        // DDRController
        ddrc.ports.ctrl.connect(buses.ctrl)
        ddrc.ports.inp.connect(buses.mem)
        ddrc.ports.outp.connect(buses.ddr)

        // I2C
        i2c.ports.ctrl.connect(buses.ctrl)
        i2c.ports.outp.connect(buses.i2c_bus)

        // DUART
        duart.ports.ctrl.connect(buses.ctrl)
        duart.ports.tx1.connect(buses.tx_bus)
        duart.ports.rx1.connect(buses.rx_bus)
        duart.ports.tx2.connect(buses.tx_bus)
        duart.ports.rx2.connect(buses.rx_bus)
        // (Terminal)
        term.ports.term_m.connect(buses.rx_bus)
        term.ports.term_s.connect(buses.tx_bus)

        // Enhanced local bus
        elbc.ports.ctrl.connect(buses.ctrl)
        elbc.ports.inp.connect(buses.mem)
        elbc.ports.outp.connect(buses.flash)

        // GPIO
        gpio.ports.ctrl.connect(buses.ctrl)

        // L2 Cache
        l2c.ports.ctrl.connect(buses.ctrl)
        l2c.ports.inp.connect(buses.mem)
        l2c.ports.outp.connect(buses.l2sdram)

        // Enhanced three-speed Ethernet controller
        etsec1.ports.ctrl.connect(buses.ctrl)

        // Programmable interrupt controller
        pic.ports.ctrl.connect(buses.ctrl)

        // Global Utilities
        guts.ports.ctrl.connect(buses.ctrl)

        // === MEMORY ===
        // DDR0
        ddr0.ports.inp.connect(buses.ddr, DDRController.CS0_BASE)

        // DDR1
        ddr1.ports.inp.connect(buses.ddr, DDRController.CS1_BASE)

        // L2 SDRAM
        l2sdram.ports.mem.connect(buses.l2sdram)

        // TODO: rename bus
        ports.mem.connect(buses.flash)
//        // Boot ROM to flash
//        boot.ports.mem.connect(buses.flash, 0x0000_0000)
//
//        // Flash 0 to flash
//        flash0.ports.mem.connect(buses.flash, 0x2000_0000)
//
//        // Flash 1 to flash
//        flash1.ports.mem.connect(buses.flash, 0x4000_0000)
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "e500v2" to e500v2.serialize(ctxt),
//                "dbg" to dbg.serialize(ctxt),
                "cmod" to bridge.serialize(ctxt),
                "ddrc" to ddrc.serialize(ctxt),
                "duart" to duart.serialize(ctxt),
                "elbc" to elbc.serialize(ctxt),
                "guts" to guts.serialize(ctxt),
                "ddr0" to ddr0.serialize(ctxt),
//                "boot" to boot.serialize(ctxt),
//                "flash0" to flash0.serialize(ctxt),
//                "flash1" to flash1.serialize(ctxt),
                "term" to term.serialize(ctxt)
                )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        e500v2.deserialize(ctxt, snapshot["e500v2"] as Map<String, String>)
//        dbg.deserialize(ctxt, snapshot["dbg"] as Map<String, String>)
        bridge.deserialize(ctxt, snapshot["cmod"] as Map<String, String>)
        ddrc.deserialize(ctxt, snapshot["ddrc"] as Map<String, String>)
        duart.deserialize(ctxt, snapshot["duart"] as Map<String, String>)
        elbc.deserialize(ctxt, snapshot["elbc"] as Map<String, String>)
        guts.deserialize(ctxt, snapshot["guts"] as Map<String, String>)
        ddr0.deserialize(ctxt, snapshot["ddr0"] as Map<String, String>)
//        boot.deserialize(ctxt, snapshot["boot"] as Map<String, String>)
//        flash0.deserialize(ctxt, snapshot["flash0"] as Map<String, String>)
//        flash1.deserialize(ctxt, snapshot["flash1"] as Map<String, String>)
        term.deserialize(ctxt, snapshot["term"] as Map<String, String>)
    }
}