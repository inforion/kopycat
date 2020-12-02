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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.cores.E500v2
import ru.inforion.lab403.kopycat.modules.cores.PPCDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal
import java.nio.ByteOrder



class P2020(parent: Module?, name: String) : Module(parent, name) {

    inner class Buses : ModuleBuses(this) {
        val cbus = Bus("cbus", BUS32)
        val ctrl = Bus("ctrl", BUS32)
        val mem = Bus("mem", BUS32)
        val ddr = Bus("ddr", BUS32)
        val flash = Bus("flash", BUS32)
        val rx_bus = Bus("rx_bus", BUS32)
        val tx_bus = Bus("tx_bus", BUS32)
    }

    override val buses = Buses()

    val e500v2 = E500v2(this, "ppc", 1200.MHz)
    val dbg = PPCDebugger(this, "dbg")

    val cmod = CoherencyModule(this, "ECM")
    val ddrc = DDRController(this, "DDRController")
    // I2C
    val duart = DUART(this, "DUART")
    val elbc = EnhancedLocalBusCtrl(this, "eLBC")

    val l2c = L2Cache(this, "L2Cache")

    val pic = PIC(this, "PIC")

    val guts = GlobalUtilities(this, "GUTS")

    val ddr0 = DDRMemory(this, "DDR0", 0,0x1000_0000)
    val boot = RAM(this, "boot", 0x200_0000, Resource("binaries/patched.bin"))
    val flash0 = RAM(this, "flash0", 0x8_0000, Resource("binaries/spi_flash.bin")) // 512 Kb
    val flash1 = RAM(this, "flash1", 0x8000, Resource("binaries/i2c_flash.bin"))   // 32 Kb

    val term = UartSerialTerminal(this, "term", "socat:")

    override fun reset() {
        super.reset()

        //FIXME: [CODEHAZARD] NOT CUTE!!!
        //FIXME: [CODEHAZARD] NOT CUTE!!!
        //FIXME: [CODEHAZARD] NOT CUTE!!!
        boot.endian = ByteOrder.BIG_ENDIAN
        flash0.endian = ByteOrder.BIG_ENDIAN
        flash1.endian = ByteOrder.BIG_ENDIAN
        //FIXME: [CODEHAZARD] NOT CUTE!!!
        //FIXME: [CODEHAZARD] NOT CUTE!!!
        //FIXME: [CODEHAZARD] NOT CUTE!!!

        e500v2.mmu.createEntry(0, 0, mapOf(
                "V" to 1L,
                "IPROT" to 1L,
                "EPN" to 0xFFFF_F000L,
                "RPN" to 0xFFFF_F000L,
                "SIZE" to 1L,
                "UX" to 1L,
                "SX" to 1L,
                "UR" to 1L,
                "SR" to 1L
        ))
    }

    init {
        // === CORE ===
        // Core to Coherency module bus
        e500v2.ports.mem.connect(buses.cbus)

        // === DEBUGGER ===
        // Debugger to mem
        dbg.ports.breakpoint.connect(buses.mem)
        dbg.ports.reader.connect(buses.mem)

        // === MODULES ===
        // Coherency module to ctrl and mem
        cmod.ports.inp.connect(buses.cbus)
        cmod.ports.ctrl.connect(buses.ctrl)
        cmod.ports.outp.connect(buses.mem)

        // DDRController to ctrl, mem and ddr
        ddrc.ports.ctrl.connect(buses.ctrl)
        ddrc.ports.inp.connect(buses.mem)
        ddrc.ports.outp.connect(buses.ddr)

        // DUART to ctrl and mem (also tx and rx)
        duart.ports.ctrl.connect(buses.ctrl)
        duart.ports.inp.connect(buses.mem)
        duart.ports.tx.connect(buses.tx_bus)
        duart.ports.rx.connect(buses.rx_bus)

        // Term to rx and tx
        term.ports.term_m.connect(buses.rx_bus)
        term.ports.term_s.connect(buses.tx_bus)

        // Enhanced local bus to ctrl, mem and flash
        elbc.ports.ctrl.connect(buses.ctrl)
        elbc.ports.inp.connect(buses.mem)
        elbc.ports.outp.connect(buses.flash)

        // L2 Cache to ctrl and mem
        l2c.ports.ctrl.connect(buses.ctrl)
        l2c.ports.inp.connect(buses.mem)

        // Programmable interrupt controller to ctrl and mem
        pic.ports.ctrl.connect(buses.ctrl)
        pic.ports.inp.connect(buses.mem)

        // Global Utilities to ctrl and mem
        guts.ports.ctrl.connect(buses.ctrl)
        guts.ports.inp.connect(buses.mem)

        // === MEMORY ===
        // DDR0 to ddr
        ddr0.ports.inp.connect(buses.ddr)

        // Boot ROM to flash
        boot.ports.mem.connect(buses.flash, 0x0000_0000)

        // Flash 0 to flash
        flash0.ports.mem.connect(buses.flash, 0x2000_0000)

        // Flash 1 to flash
        flash1.ports.mem.connect(buses.flash, 0x4000_0000)
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "e500v2" to e500v2.serialize(ctxt),
                "dbg" to dbg.serialize(ctxt),
                "cmod" to cmod.serialize(ctxt),
                "ddrc" to ddrc.serialize(ctxt),
                "duart" to duart.serialize(ctxt),
                "elbc" to elbc.serialize(ctxt),
                "guts" to guts.serialize(ctxt),
                "ddr0" to ddr0.serialize(ctxt),
                "boot" to boot.serialize(ctxt),
                "flash0" to flash0.serialize(ctxt),
                "flash1" to flash1.serialize(ctxt),
                "term" to term.serialize(ctxt)
                )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        e500v2.deserialize(ctxt, snapshot["e500v2"] as Map<String, String>)
        dbg.deserialize(ctxt, snapshot["dbg"] as Map<String, String>)
        cmod.deserialize(ctxt, snapshot["cmod"] as Map<String, String>)
        ddrc.deserialize(ctxt, snapshot["ddrc"] as Map<String, String>)
        duart.deserialize(ctxt, snapshot["duart"] as Map<String, String>)
        elbc.deserialize(ctxt, snapshot["elbc"] as Map<String, String>)
        guts.deserialize(ctxt, snapshot["guts"] as Map<String, String>)
        ddr0.deserialize(ctxt, snapshot["ddr0"] as Map<String, String>)
        boot.deserialize(ctxt, snapshot["boot"] as Map<String, String>)
        flash0.deserialize(ctxt, snapshot["flash0"] as Map<String, String>)
        flash1.deserialize(ctxt, snapshot["flash1"] as Map<String, String>)
        term.deserialize(ctxt, snapshot["term"] as Map<String, String>)
    }
}