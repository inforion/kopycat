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
package ru.inforion.lab403.kopycat.modules.demolinux

import ru.inforion.lab403.common.extensions.toFile
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.SEVERE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.ComponentTracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.*
import ru.inforion.lab403.kopycat.cores.x86.config.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.BrandIndex
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.ECXFeatures
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.EDXFeatures
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.ProcType
import ru.inforion.lab403.kopycat.experimental.common.ExternalDisk
import ru.inforion.lab403.kopycat.experimental.common.PacketSourceData
import ru.inforion.lab403.kopycat.cores.base.classResourcePath
import ru.inforion.lab403.kopycat.experimental.common.parsePacketSourceData
import ru.inforion.lab403.kopycat.runtime.funcall.FunQueuedTracer
import ru.inforion.lab403.kopycat.runtime.funcall.FunQueuedUtils
import ru.inforion.lab403.kopycat.runtime.abi.x64AbiSystemV
import ru.inforion.lab403.kopycat.experimental.linux.biosless.BioslessDevice
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.BUS36
import ru.inforion.lab403.kopycat.modules.BUS64
import ru.inforion.lab403.kopycat.modules.atom2758.Atom2758
import ru.inforion.lab403.kopycat.modules.atom2758.L_APIC
import ru.inforion.lab403.kopycat.modules.common.e1000.sources.EthernetOverTcpSource
import ru.inforion.lab403.kopycat.modules.common.sata.DiskInfo
import ru.inforion.lab403.kopycat.modules.common.pci.pci_bus
import ru.inforion.lab403.kopycat.modules.cores.x64Debugger
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.cores.x86Debugger
import ru.inforion.lab403.kopycat.modules.demolinux.linux.Linux040302Top
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.terminals.UartNetworkTerminal
import ru.inforion.lab403.kopycat.modules.tracer.DynamicTracer
import ru.inforion.lab403.kopycat.runtime.analyzer.stack.StackAnalyzer
import ru.inforion.lab403.kopycat.runtime.analyzer.stack.tracer
import ru.inforion.lab403.kopycat.runtime.analyzer.stack.x86StackAnalyzerCore
import java.io.RandomAccessFile
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.invariantSeparatorsPathString


class DemoLinux(
    parent: Module?,
    name: String,

    /**
     * Default CPU terminal on TX/RX bus
     */
    port: Int? = null,

    /**
     * Use x86Debugger
     */
    x32dbg: Boolean = false,

    bzImageName: String = "bzImage",
    initRdName: String = "rootfs.cpio",

    /**
     * E1000 packet source string.
     * The format `host:port`. Example: `127.0.0.1:30000`
     */
    packetSource: String? = null,


    /**
     * Use tracer for network hooking
     */
    val strictExecution: Boolean = false,
) : BioslessDevice(parent, name) {
    companion object {
        @Transient
        val log = logger(INFO)

        const val BUSSIZE = BUS36

        val resourceRootPath = this::class.classResourcePath
    }

    inner class Ports : ModulePorts(this) {
        val io = Port("io")
    }

    @DontAutoSerialize
    override val ports = Ports()

    @DontAutoSerialize
    private val packetSourceData: PacketSourceData? = parsePacketSourceData(packetSource, Module.log)

    val atom2758 = Atom2758(this, "atom2758", BUS64, BUSSIZE)
    override val x86 = atom2758.x86
    override val bzImage: ByteArray = Resource(resourceRootPath / "binaries/$bzImageName").readBytes()
    override val cmdline: String = "debug nohpet notsc noapictimer acpi=off noapic " +
            "lpj=4005888 console=uart8250,io,0x3f8,115200n8 " +
            "norandmaps ignore_loglevel initcall_debug=1 nokaslr " +
            "noibrs noibpb nopti nospectre_v2 nospectre_v1 l1tf=off nospec_store_bypass_disable " +
            "no_stf_barrier mds=off tsx=on tsx_async_abort=off mitigations=off" +
            "\u0000"

    override val ramdisk: ByteArray = Resource(resourceRootPath / "binaries/$initRdName").readBytes()

    // Allow large bzImages
    override val ramdiskAddress: ULong = 0x800_0000uL

    val term = UartNetworkTerminal(this, "term", port, dummy=strictExecution)

    val dbg = if (x32dbg) x86Debugger(this, "dbg") else x64Debugger(this, "dbg", BUS64)
    private val trc = ComponentTracer<x86Core>(this, "trc")

    @DontAutoSerialize
    val dynamicTracer = DynamicTracer<x86Core>(this, "this-dyna")

    val queueApi = FunQueuedUtils(x64AbiSystemV(atom2758.x86))
    private val queueTracer = FunQueuedTracer<x86Core>(this, "q-trc-demolinux", queueApi)

    val demoLinuxTracer = DemoLinuxTracer(this, "trc-demolinux")

    val fintek8250 = Fintek8250(this, "Fintek_8250")

    private val diskPathList = listOf(
        "disks/demo.bin",
        (Path(Kopycat.resourceDir) / "demo/demo.hda3").invariantSeparatorsPathString,
        (Path(Kopycat.resourceDir) / "demo/demo.bin").invariantSeparatorsPathString,
        (Path(Kopycat.resourceDir) / resourceRootPath / "binaries/demo.hda3").invariantSeparatorsPathString
    )

    private val diskPathOrNull = diskPathList
        .map { it.toFile() }
        .find { it.exists() }

    @DontAutoSerialize
    val disk = diskPathOrNull?.let { path ->
        ExternalDisk(
            this,
            "disk_external",
            RandomAccessFile(path, "rw"),
        )
    } ?: let {
        log.warning {
            val diskPaths = diskPathList.joinToString("\n") { "- $it" }
            "No disk at found. No disk will be connected. Searched in:\n$diskPaths"
        }
        null
    }

    private val zerodisk = RAM(
        this,
        "zerodisk",
        0x1_0000
    )

    @DontAutoSerialize
    val linux by lazy { Linux040302Top(atom2758.x86) }

    val stackAnalyzer = StackAnalyzer(x86StackAnalyzerCore(atom2758.x86.cpu), captureAll = false) {
        set(0, 0xFFFF_FFFF_FFFF_FFFFuL .. 0x1000_0000_0000_0000uL)
        set(3, 0x0FFF_FFFF_FFFF_FFFFuL .. 0x0000_0000_0000_0000uL)
    }
    val stackAnalyzerTracer = stackAnalyzer.tracer<x86Core>(this, "stack-analyzer-trc")

    private val memoryLayout = buildMemoryLayout()

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
        val io = Bus("io")
        val pci = pci_bus("pci")

        val rx_bus = Bus("rx_bus")
        val tx_bus = Bus("tx_bus")
    }

    override val buses = Buses()

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        require(!queueApi.isInProcessing) { "Unable to snapshot while the queueApi in processing." }

        return super.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)

        queueApi.forceClearState()
    }

    // Used by SeaBIOS
    private val fastA20Gate = Register(ports.io, 0x92uL, Datatype.BYTE, "fastA20Gate", level = SEVERE)
    private val qemuPortSel = Register(ports.io, 0x510uL, Datatype.BYTE, "qemuPortSel", level = SEVERE)
    private val qemuPortData = Register(ports.io, 0x511uL, Datatype.BYTE, "qemuPortData", level = SEVERE)

    // Linux: arch/x86/boot/compressed/misc.c
    private val vidPort1 = Register(ports.io, 0x3d4uL, Datatype.BYTE, "vidPort1", level = SEVERE)
    private val vidPort2 = Register(ports.io, 0x3d5uL, Datatype.BYTE, "vidPort2", level = SEVERE)

    init {
        dbg.ports.breakpoint.connect(atom2758.x86.buses.virtual)
        dbg.ports.reader.connect(atom2758.x86.buses.virtual)

        atom2758.ports.mem.connect(buses.mem)
        atom2758.ports.io.connect(buses.io)
        atom2758.ports.tx.connect(buses.tx_bus)
        atom2758.ports.rx.connect(buses.rx_bus)

        atom2758.ports.pci.connect(buses.pci)

        term.ports.term_m.connect(buses.rx_bus)
        term.ports.term_s.connect(buses.tx_bus)

        fintek8250.ports.io.connect(atom2758.buses.nb_io)

        atom2758.pit.divider = 20uL

        ports.io.connect(buses.io)

        atom2758.sata2.connect(
            DiskInfo(
                "Kopycat",
                "1337",
                "Kopycat SATA 2",
                zerodisk.size.ulong_z,
            ),
            zerodisk.ports.mem,
        )
        atom2758.sata3.connect(
            DiskInfo(
                "Kopycat",
                "1337",
                "Kopycat SATA 3",
                disk?.size ?: zerodisk.size.ulong_z,
            ),
            disk?.ports?.mem ?: zerodisk.ports.mem,
        )

        buses.connect(trc.ports.trace, dbg.ports.trace)


        if (!strictExecution) {
            log.warning { "Using tracers and PIT" }
            trc.addTracer(queueTracer)
            trc.addTracer(stackAnalyzerTracer)
            trc.addTracer(dynamicTracer)
            trc.addTracer(demoLinuxTracer)
        } else {
            // No tracers
            // No context switching
            atom2758.pit.irqEnabled = false
        }

        memoryLayout.forEach {
            it.second.connect(buses.mem, it.first)
        }


        log.info { "[DEMO TOP] Loading bzImage: $bzImageName" }
        log.info { "[DEMO TOP] Loading initRd : $initRdName" }

        packetSourceData?.let { data ->
            Module.log.info { "Using EthernetOverTcpSource with host='${data}' as atom2758.e1000.packetSource" }

            val source = EthernetOverTcpSource(data.host, data.port)
            Module.log.info { "Stopping existing atom2758.e1000.packetSource" }
            atom2758.e1000.packetSource.stop(atom2758.e1000)

            source.start(atom2758.e1000)
            atom2758.e1000.packetSource = source
            Module.log.info { "Changed atom2758.e1000.packetSource successfully" }
        }
    }

    override fun reset() {
        super.reset()

        // Used by Linux
        atom2758.x86.config.apply {
            msr(MSR_SMI_COUNT, 0u)
            msr(MSR_CORE_C1_RES, 0u)
            msr(MSR_PKG_C7_RESIDENCY, 0u)
            msr(MSR_CORE_C6_RESIDENCY, 0u)
            msr(MSR_MISC_FEATURES_ENABLES, 0u)
            msr(IA32_MCG_STATUS, 0u)

            msr(MSR_IA32_SPEC_CTRL, 0u)
            msr(MSR_IA32_TSX_CTRL, 0u)
            msr(MSR_TSX_FORCE_ABORT, 0u)
            msr(MSR_IA32_MCU_OPT_CTRL, 0u)
            msr(MSR_AMD64_LS_CFG, 0u)
            msr(MSR_AMD64_DE_CFG, 0u)

            // See https://www.felixcloutier.com/x86/cpuid#fig-3-8
            cpuid(
                0x4u,
                CPUID(
                    0b011111_000000000001_0000_0_1_001_00001u,
                    0b0001000000_0000000001_000000001000u,
                    0x40u,
                    0b100u,
                ),
                0x0u
            )
            cpuid(
                0x4u,
                CPUID(
                    0b011111_000000000001_0000_0_1_001_00010u,
                    0b0001000000_0000000001_000000001000u,
                    0x40u,
                    0b100u,
                ),
                0x1u
            )
            cpuid(
                0x4u,
                CPUID(
                    0b011111_000000000001_0000_0_1_010_00011u,
                    0b0001000000_0000000001_000000001000u,
                    0x80u,
                    0b100u,
                ),
                0x2u
            )
            cpuid(
                0x4u,
                CPUID(
                    0b011111_000000000001_0000_0_1_011_00011u,
                    0b0001000000_0000000001_000000001000u,
                    0x600u,
                    0b100u,
                ),
                0x3u
            )
            cpuid(
                0x4u,
                CPUID(0u, 0u, 0u, 0u),
                0x4u
            )
            cpuid(
                0x2u,
                CPUID(0xFFFFFFFFu, 0xFFFFFFFFu, 0xFFFFFFFFu, 0xFFFFFFFFu),
                0x0u
            )

            // Make MPX enabled again
            cpuid(
                0x01u,
                CPUID1EAX(0u, 4u, ProcType.OEM, 6u, 13u, 0u),
                CPUID1EBX(BrandIndex.NotSupport, 8u, 0u, L_APIC.LAPIC_ID.uint),
                CPUIDECX(ECXFeatures.pni, ECXFeatures.dtes64, ECXFeatures.monitor, ECXFeatures.ds_cpl, ECXFeatures.tm2, /*ssse3,*/ ECXFeatures.cx16, ECXFeatures.xtpr, ECXFeatures.pdcm, ECXFeatures.movbe),
                CPUIDEDX(
                    EDXFeatures.fpu, EDXFeatures.vme, EDXFeatures.de, EDXFeatures.pse, EDXFeatures.tsc, EDXFeatures.msr, EDXFeatures.pae, EDXFeatures.mce, EDXFeatures.cx8,
                    EDXFeatures.apic, EDXFeatures.sep, EDXFeatures.mtrr, EDXFeatures.pge, EDXFeatures.mca, EDXFeatures.cmov, EDXFeatures.pat, EDXFeatures.pse36, EDXFeatures.clflush,
                    EDXFeatures.dts, EDXFeatures.acpi, EDXFeatures.mmx, EDXFeatures.fxsr, EDXFeatures.sse, EDXFeatures.sse2, EDXFeatures.ss, EDXFeatures.ht, EDXFeatures.tm, EDXFeatures.pbe
                ),
            )

            // Used by SeaBIOS to detect VM
            cpuid(0x4000_0000u, 0u, 0u, 0u, 0u)
            for (i in 1u..0xFFu) {
                cpuid(0x40000000u + i * 0x100u, CPUID(0u, 0u, 0u, 0u))
            }
        }
    }
}
