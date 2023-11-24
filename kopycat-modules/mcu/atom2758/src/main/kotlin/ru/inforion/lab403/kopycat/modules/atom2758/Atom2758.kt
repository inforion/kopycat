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
@file:Suppress("MemberVisibilityCanBePrivate")

package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.*
import ru.inforion.lab403.kopycat.cores.x86.config.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.BrandIndex.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.ECXFeatures.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.EDXFeatures.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.MemoryType.*
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.BUS36
import ru.inforion.lab403.kopycat.modules.atom2758.e1000.E1000
import ru.inforion.lab403.kopycat.modules.atom2758.sata.SATA
import ru.inforion.lab403.kopycat.modules.common.NS16550
import ru.inforion.lab403.kopycat.modules.common.PIC8259
import ru.inforion.lab403.kopycat.modules.common.PIT8254
import ru.inforion.lab403.kopycat.modules.common.RTC
import ru.inforion.lab403.kopycat.modules.common.pci.PciHost
import ru.inforion.lab403.kopycat.modules.common.pci.pci_bus
import ru.inforion.lab403.kopycat.modules.common.pci.pci_connect
import ru.inforion.lab403.kopycat.modules.common.pci.pci_proxy
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.memory.VOID


class Atom2758(
    parent: Module,
    name: String,
    val virtualBusSize: ULong = BUS32,
    val physicalBusSize: ULong = BUS36,
    val enableACPI: Boolean = true,
    val enableRAM: Boolean = true,
    val enableSATA2: Boolean = true,
    val enableSATA3: Boolean = true,
    
) : Module(parent, name), IAutoSerializable {

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem", physicalBusSize)
        val io = Proxy("io", BUS16)
        val tx = Proxy("tx", BUS32)
        val rx = Proxy("rx", BUS32)
        val pci = pci_proxy("pci")
    }

    @DontAutoSerialize
    override val ports = Ports()

    inner class Buses : ModuleBuses(this) {
        val x86_mem = Bus("x86_mem", physicalBusSize)
        val x86_io = Bus("x86_io", BUS16)

        val nb_mem = Bus("nb_mem", physicalBusSize)  // north bridge
        val nb_io = Bus("nb_io", BUS16)  // north bridge

        val irq = Bus("irq", 255)  // north bridge

        val rcrb = Bus("rcrb", RCRB.BUS_SIZE)
        val spi = Bus("spi", SPI.BUS_SIZE)

        val pci = pci_bus("pci")
        val rx_bus = Bus("rx_bus", BUS32)
        val tx_bus = Bus("tx_bus", BUS32)

        val mapper = Bus("mapper")

        val msg = Bus("msg", MESSAGE_BUS_SIZE)
    }

    @DontAutoSerialize
    override val buses = Buses()


    val uio = UnknownIO(this, "uio")

//    val mem_conv = RAM(this, "mem_conv", 0x100000)  // 1 MB

    val mem_00 = if (enableRAM) RAM(this, "mem_0", 0x4000000) else null // 64 MB
    val mem_04 = if (enableRAM) RAM(this, "mem_4", 0x4000000) else null
    val mem_08 = if (enableRAM) RAM(this, "mem_8", 0x4000000) else null
    val mem_0c = if (enableRAM) RAM(this, "mem_c", 0x4000000) else null

    val bridge = BRIDGE(this, "bridge", physicalBusSize)
    val pci_host = PciHost(this, "pci")

    val bunit = BUNIT(this, "bunit")
    val cunit = CUNIT(this, "cunit")
    val punit = PUNIT(this, "punit")
    val dunit0 = DUNIT0(this, "dunit0")
    val dunit1 = DUNIT1(this, "dunit1")
    val mbd18 = MBD18(this, "mbd18")
    val mbd64 = MBD64(this, "mbd64")

    val pic = PIC8259(this, "pic")
    val pit = PIT8254(this, "pit")
    val kb = KB8042(this, "kb")
    val rtc = RTC(this, "rtc")
    val lpc = LPC(this, "lpc")
    val gbe0 = GbE(this, "gbe0", 0x1F40)
    val gbe1 = GbE(this, "gbe1", 0x1F41)
    val gbe2 = GbE(this, "gbe2", 0x1F45)
    val gbe3 = GbE(this, "gbe3", -1) // stub
    val smb_pcu = SMB_PCU(this, "smb_pcu")
    val smb_20_0 = SMB_20(this, "smb_20_0", 0)
    val smb_20_1 = SMB_20(this, "smb_20_1", 1)  // fake
    val ioapic = IOAPIC(this, "ioapic");
    val hpet = HPET(this, "hpet")
    val l_apic = L_APIC(this, "l_apic")
    val abort = ABORT(this, "abort")
    val acpi = if (enableACPI) ACPI(this, "acpi") else null
    val spi = SPI(this, "spi")
    val pmc = PMC(this, "pmc")
    val gpio = GPIO(this, "gpio")
    val ioc = IOC(this, "ioc")
    val ilb = ILB(this, "ilb")
    val mphy = MPHY(this, "mphy")
    val rcrb = RCRB(this, "rcrb")
    val uart0 = NS16550(this, "uart0", Datatype.BYTE)
    val uart1 = UART(this, "uart1", 1)
    val uart3 = VOID(this, "uart3", 0x10)
    val uart4 = VOID(this, "uart4", 0x10)
    val post = POST(this, "post")
    val smm = SMM(this, "smm")

    val pcie1 = PCIe(this, "pcie1", 0x1F10)
    val pcie2 = PCIe(this, "pcie2", 0x1F11)
    val pcie3 = PCIe(this, "pcie3", 0x1F12)
    val pcie4 = PCIe(this, "pcie4", 0x1F13)

    val ras = RAS(this, "ras")

    val usb20 = USB20(this, "usb20")

    val sata2 = SATA(this, "sata2", 2)
    val sata3 = SATA(this, "sata3", 3)

    val e1000 = E1000(
        this,
        "e1000",
        mac = byteArrayOf(
            0xE4.byte,
            0x40.byte,
            0x41.byte,
            0xE2.byte,
            0xED.byte,
            0xFE.byte,
        )
    )

    

    val x86 = x86Core(
        this,
        "x86",
        frequency = 2.GHz,
        Generation.Pentium,
        ipc = 1.0,
        virtualBusSize = virtualBusSize,
        physicalBusSize = physicalBusSize,
    )

    override fun reset() {
        super.reset()

        with(x86.config) {
            cpuid(0x00u, CPUID0(10u, VENDOR.INTEL))
            cpuid(
                0x01u,
                CPUID1EAX(0u, 4u, ProcType.OEM, 6u, 13u, 0u),
                CPUID1EBX(NotSupport, 8u, 0u, L_APIC.LAPIC_ID.uint),
                CPUIDECX(pni, dtes64, monitor, ds_cpl, tm2, /*ssse3,*/ cx16, xtpr, pdcm, movbe),
                CPUIDEDX(
                    fpu, vme, de, pse, tsc, EDXFeatures.msr, pae, mce, cx8,
                    apic, sep, mtrr, pge, mca, cmov, pat, pse36, clflush,
                    dts, EDXFeatures.acpi, /*mmx,*/ fxsr, sse, sse2, ss, ht, tm, pbe
                ),
            )

//            cpuid(0x01u, 0x0004_06D0u, 0x0010_0800u, 0x43D8_E3BFu, 0xBFEB_FBFFu)

            cpuid4(0u, 0u, 0u, 0u, 0u)
            cpuid4(1u, 0u, 0u, 0u, 0u)
            cpuid4(4u, 0u, 0u, 0u, 0u)

            cpuid(0x02u, 0u, 0u, 0u, 0u)
            cpuid(0x03u, 0u, 0u, 0u, 0u)
            cpuid(0x05u, 0u, 0u, 0u, 0u)
            cpuid(0x08u, 0u, 0u, 0u, 0u)
            cpuid(0x09u, 0u, 0u, 0u, 0u)
            cpuid(0x0Au, 0u, 0u, 0u, 0u)
            cpuid(0x21u, 0u, 0u, 0u, 0u)

            cpuid(0x8000_0001u, 0u, 0u, 0u, 0u)
            cpuid(0x8000_0002u, 0u, 0u, 0u, 0u)
            cpuid(0x8000_0003u, 0u, 0u, 0u, 0u)
            cpuid(0x8000_0004u, 0u, 0u, 0u, 0u)
            cpuid(0x8000_0005u, 0u, 0u, 0u, 0u)
            cpuid(0x8000_0006u, 0u, 0u, 0u, 0u)
            cpuid(
                0x8000_0008u,
                insert(physicalBusSize.log2(), 7..0).insert(virtualBusSize.log2(), 15..8).uint,
                0u,
                0u,
                0u,
            )
            cpuid(0x8000_0009u, 0u, 0u, 0u, 0u)
            cpuid(0x8000_000Au, 0u, 0u, 0u, 0u)

            cpuid(0x06u, 0u, 0u, 0u, 0u)
            cpuid(0x07u, 0u, 0u, 0u, 0u)
            cpuid(0x0Au, 0u, 0u, 0u, 0u)
            // https://www.ti.uni-bielefeld.de/html/teaching/WS1920/techinf1/64-ia-32-architectures-software-developers-manual.pdf
            cpuid(0x0Bu, 0x0000_0001u, 0x0000_0002u, 0x0001_0000u, 0x0000_0000u)
            cpuid(0x0Du, 0u, 0u, 0u, 0u)

            // Linux-defined
            // constant_tsc, arch_perfmon, pebs, bts, rep_good, nopl, aperfmperf,a
            // Digital thermal sensor: dts
            cpuid(0x8000_0000u, 0x8000_0008u, 0u, 0u, 0u)
            cpuid(0x8000_0001u, 0u, 0u, CPUIDECX(lahf_lm), CPUIDEDX(syscall, nx, lm))
            // 0x8000_0002u - 0x8000_0004u
            setModelName("Intel(R) Atom(TM) CPU D525   @ 1.80GHz")
            cpuid(0x8000_0007u, 0u, 0u, 0u, 0u)
            cpuid(
                0x8000_0008u,
                insert(48u, 15..8).insert(36u, 7..0),
                0u,
                0u,
                0u,
            )
//            cpuid(0x80000008u, 0x0000_2424u, 0u, 0u, 0u)

            msr(IA32_PLATFORM_ID, insert(1uL, 52..50))
            msr(IA32_APIC_BASE, 0xFEE0_0000u)
            msr(IA32_FEATURE_CONTROL, 0u)
            msr(IA32_BIOS_SIGN_ID, 0u)
            msr(MSR_BBL_CR_CTL3, 0u)
            msr(MSR_FSB_FREQ, 800_000_000u)
            msr(MSR_PLATFORM_INFO, 0x01_00u)

            msr(IA32_PERF_STATUS, 0u)

            msr(IA32_THERM_INTERRUPT, 0u)
            msr(MSR_TEMPERATURE_TARGET, 0u)
            msr(MSR_PKG_POWER_LIMIT, 0u)
            msr(MSR_PKG_POWER_SKU_UNIT, 0u)

            // IA32_MTRRCAP: 0xFEu have_fixed = 1
            msr(IA32_MTRRCAP, 0x108u)

            msr(IA32_MISC_ENABLE, 0u)

            // Linux checks 22'nd bit
            msr(IA32_PERFEVTSEL0, 0u)
            msr(IA32_PERFEVTSEL1, 0u)
            msr(IA32_PERFEVTSEL2, 0u)
            msr(IA32_PERFEVTSEL3, 0u)

            msr(MSR_TURBO_RATIO_LIMIT, 0x0101_0101_0101_0101u)
            msr(IA32_MCG_CAP, 0u)
            msr(MSR_POWER_CTL, 0u)
            msr(IA32_ENERGY_PERF_BIAS, 0u)
            msr(IA32_CLOCK_MODULATION, 0u)
            msr(MSR_PKG_CST_CONFIG_CONTROL, 0u)
            msr(0x120u, 0u)  // unknown

            msr(IA32_MTRR_PHYSBASE0, IA32_MTRR_PHYSBASE(0x000000000u, Writeback))
            msr(IA32_MTRR_PHYSBASE1, IA32_MTRR_PHYSBASE(0x080000000u, Writeback))
            msr(IA32_MTRR_PHYSBASE2, IA32_MTRR_PHYSBASE(0x100000000u, Writeback))
            msr(IA32_MTRR_PHYSBASE3, IA32_MTRR_PHYSBASE())
            msr(IA32_MTRR_PHYSBASE4, IA32_MTRR_PHYSBASE())
            msr(IA32_MTRR_PHYSBASE5, IA32_MTRR_PHYSBASE())
            msr(IA32_MTRR_PHYSBASE6, IA32_MTRR_PHYSBASE())
            msr(IA32_MTRR_PHYSBASE7, IA32_MTRR_PHYSBASE())

            msr(IA32_MTRR_PHYSMASK0, IA32_MTRR_PHYSMASK(0xF80000000u))
            msr(IA32_MTRR_PHYSMASK1, IA32_MTRR_PHYSMASK(0xFC0000000u))
            msr(IA32_MTRR_PHYSMASK2, IA32_MTRR_PHYSMASK(0xFC0000000u))
            msr(IA32_MTRR_PHYSMASK3, IA32_MTRR_PHYSMASK(valid = false))
            msr(IA32_MTRR_PHYSMASK4, IA32_MTRR_PHYSMASK(valid = false))
            msr(IA32_MTRR_PHYSMASK5, IA32_MTRR_PHYSMASK(valid = false))
            msr(IA32_MTRR_PHYSMASK6, IA32_MTRR_PHYSMASK(valid = false))
            msr(IA32_MTRR_PHYSMASK7, IA32_MTRR_PHYSMASK(valid = false))

            // 00000-9FFFF write-back
            // A0000-BFFFF uncachable
            // C0000-FFFFF write-back

            msr(IA32_MTRR_FIX64K_00000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX16K_80000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX16K_A0000, IA32_MTRR_FIX(Uncachable))

            msr(IA32_MTRR_FIX4K_C0000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX4K_C8000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX4K_D0000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX4K_D8000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX4K_E0000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX4K_E8000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX4K_F0000, IA32_MTRR_FIX(Writeback))
            msr(IA32_MTRR_FIX4K_F8000, IA32_MTRR_FIX(Writeback))

            msr(IA32_PAT, IA32_PAT(6u, 4u, 7u, 0u, 6u, 4u, 7u, 0u))

            msr(MSR_EVICT_CTL, 0u)

            // Default Memory Type = 0
            // Fixed Range MTRR Enable = 1
            // MTRR Enable = 1
            msr(IA32_MTRR_DEF_TYPE, 0x600u)

            msr(MSR_IACORE_RATIOS, 0u)
            msr(MSR_PKG_TURBO_CFG1, 0u)
            msr(MSR_CPU_TURBO_WKLD_CFG2, 0u)
            msr(MSR_CPU_THERM_CFG1, 0u)
            msr(MSR_CPU_THERM_CFG2, 0u)

            efer = 0u
        }
    }

    init {
        ports.mem.connect(buses.nb_mem)
        ports.io.connect(buses.nb_io)
        ports.tx.connect(buses.tx_bus)
        ports.rx.connect(buses.rx_bus)
        ports.pci.connect(buses.pci)

        x86.ports.mem.connect(buses.x86_mem)
        x86.ports.io.connect(buses.x86_io)

        bridge.ports.mem_in.connect(buses.x86_mem)
        bridge.ports.io_in.connect(buses.x86_io)
        bridge.ports.mem_out.connect(buses.nb_mem)
        bridge.ports.io_out.connect(buses.nb_io)

        pci_host.ports.io.connect(buses.nb_io)

        ioapic.ports.mem.connect(buses.nb_mem, 0xFEC0_0000u)
        hpet.ports.mem.connect(buses.nb_mem, 0xFED0_0000u)
        l_apic.ports.mem.connect(buses.nb_mem, 0xFEE0_0000u)
        abort.ports.mem.connect(buses.nb_mem, 0xFEB0_0000u)

        bridge.ports.rcrb.connect(buses.rcrb)
        rcrb.ports.mem.connect(buses.rcrb)
        spi.ports.mem.connect(buses.rcrb, 0x3800u)

        bridge.ports.spi.connect(buses.spi)
        spi.ports.mem.connect(buses.spi)

        if (acpi != null) buses.connect(bridge.ports.acpi, acpi.ports.io)
        buses.connect(bridge.ports.pmc, pmc.ports.mem)
        buses.connect(bridge.ports.gpio, gpio.ports.io)
        buses.connect(bridge.ports.ioc, ioc.ports.io)
        buses.connect(bridge.ports.ilb, ilb.ports.mem)
//        buses.connect(bridge.ports.spi, spi.ports.mem)
        buses.connect(bridge.ports.mphy, mphy.ports.mem)
        buses.connect(bridge.ports.punit, punit.ports.mem)

        buses.connect(bridge.ports.usb20, usb20.mem)
        if (enableSATA2) buses.connect(bridge.ports.sata2, sata2.mem)
        if (enableSATA3) buses.connect(bridge.ports.sata3, sata3.mem)
        buses.connect(bridge.ports.e1000, e1000.mem)

        

        buses.connect(bridge.ports.smb_mem, smb_pcu.mem)
        buses.connect(bridge.ports.smb_io, smb_pcu.io)

        buses.connect(bridge.ports.smb20_0_mem, smb_20_0.mem)
        buses.connect(bridge.ports.smb20_1_mem, smb_20_1.mem)

        // should be moved out ASAP
        mem_00?.ports?.mem?.connect(buses.nb_mem, 0x60000000u)
        mem_04?.ports?.mem?.connect(buses.nb_mem, 0x64000000u)
        mem_08?.ports?.mem?.connect(buses.nb_mem, 0x68000000u)
        mem_0c?.ports?.mem?.connect(buses.nb_mem, 0x6C000000u)

        cunit.msg.msg_connect(buses.msg, 0)
        bunit.ports.msg.msg_connect(buses.msg, 3)
        punit.ports.msg.msg_connect(buses.msg, 4)
        dunit0.ports.msg.msg_connect(buses.msg, 16)
        mbd18.ports.msg.msg_connect(buses.msg, 18)
        dunit1.ports.msg.msg_connect(buses.msg, 19)
        mbd64.ports.msg.msg_connect(buses.msg, 64)

        bridge.ports.mapper.connect(buses.mapper)
        bunit.ports.mapper.connect(buses.mapper)
        lpc.ports.mapper.connect(buses.mapper)
        smb_pcu.ports.mapper.connect(buses.mapper)
        usb20.ports.mapper.connect(buses.mapper)
        if (enableSATA2) sata2.ports.mapper.connect(buses.mapper)
        e1000.ports.mapper.connect(buses.mapper)
        if (enableSATA3) sata3.ports.mapper.connect(buses.mapper)
        
        smb_20_0.ports.mapper.connect(buses.mapper)
        smb_20_1.ports.mapper.connect(buses.mapper)

        pic.ports.io.connect(buses.nb_io)
        pit.ports.io.connect(buses.nb_io)
        rtc.ports.io.connect(buses.nb_io)
        kb.ports.io.connect(buses.nb_io)
        uio.ports.io.connect(buses.nb_io)
        post.ports.io.connect(buses.nb_io)
        smm.ports.io.connect(buses.nb_io)

        uart0.ports.mem.connect(buses.nb_io, 0x300uL + 0x00F8u)
        uart0.ports.tx.connect(buses.tx_bus)
        uart0.ports.rx.connect(buses.rx_bus)

        uart1.ports.io.connect(buses.nb_io, 0x200u)

        uart3.ports.mem.connect(buses.nb_io, 0x3E8u)
        uart4.ports.mem.connect(buses.nb_io, 0x2E8u)

        pic.ports.irq.connect(buses.irq)
        pit.ports.irq.connect(buses.irq, 0u) // IRQ 0
        uart0.ports.irq.connect(buses.irq, 4u) // IRQ 4
        l_apic.ports.irq.connect(buses.irq)
        l_apic.ports.irqMaster.connect(buses.irq)

        bridge.ports.ecam.connect(buses.pci)
        pci_host.ports.pci.connect(buses.pci)

        cunit.ports.pci.pci_connect(buses.pci, 0, 0, 0)

        pcie1.ports.pci.pci_connect(buses.pci, 0, 1, 0)
        pcie2.ports.pci.pci_connect(buses.pci, 0, 2, 0)
        pcie3.ports.pci.pci_connect(buses.pci, 0, 3, 0)
        pcie4.ports.pci.pci_connect(buses.pci, 0, 4, 0)

        ras.ports.pci.pci_connect(buses.pci, 0, 14, 0)

        smb_20_0.ports.pci.pci_connect(buses.pci, 0, 19, 0)
        smb_20_1.ports.pci.pci_connect(buses.pci, 0, 19, 1)  // fake

        gbe0.ports.pci.pci_connect(buses.pci, 0, 20, 0)
        gbe1.ports.pci.pci_connect(buses.pci, 0, 20, 1)
        gbe2.ports.pci.pci_connect(buses.pci, 0, 20, 2)
        gbe3.ports.pci.pci_connect(buses.pci, 0, 20, 3)

        /**
         * Intel® Atom™ Processor C2000 Product Family for Microserver
         * Variable I/O Map, page 182
         */

        usb20.ports.pci.pci_connect(buses.pci, 0, 22, 0)

        if (enableSATA2) sata2.ports.pci.pci_connect(buses.pci, 0, 23, 0)
        if (enableSATA3) sata3.ports.pci.pci_connect(buses.pci, 0, 24, 0)
        e1000.ports.pci.pci_connect(buses.pci, 0, 25, 0)
        

        if (enableSATA2) {
            sata2.irq.connect(buses.irq)
            sata2.dmam.connect(buses.nb_mem)
        }

        if (enableSATA3) {
            sata3.irq.connect(buses.irq)
            sata3.dmam.connect(buses.nb_mem)
        }

        e1000.irq.connect(buses.irq)
        e1000.dmam.connect(buses.nb_mem)

        lpc.ports.pci.pci_connect(buses.pci, 0, 31, 0)

        /**
         * **Intel Atom Processor E3800 Product Family**
         * 33.7  PCU SMBUS Memory Mapped I/O Registers, page 4466
         *
         * > <any register>: [MBRAL] + 0xXX
         * > MBARL Type: PCI Configuration Register (Size: 32 bits)
         * > MBARL Reference: [B:0, D:31, F:3] + 10h
         */
        smb_pcu.ports.pci.pci_connect(buses.pci, 0, 31, 3)
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }
}
