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
package ru.inforion.lab403.kopycat.modules.cores

import InterruptHook
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.x86.*
import ru.inforion.lab403.kopycat.cores.x86.config.*
import ru.inforion.lab403.kopycat.cores.x86.config.CPUID0
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.*
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.extensions.SSE
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86COP
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86MMU
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32

class x86Core constructor(
    parent: Module,
    name: String,
    frequency: Long,
    val generation: Generation,
    ipc: Double,
    val useMMU: Boolean = true,
    val virtualBusSize: ULong = BUS32,
    val physicalBusSize: ULong = BUS32
): ACore<x86Core, x86CPU, x86COP>(parent, name, frequency, ipc), IAutoSerializable {

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem", physicalBusSize)
        val io = Master("io", BUS16)
    }

    inner class Buses : ModuleBuses(this) {
        val physical = Bus("physical", physicalBusSize)
        val virtual = Bus("virtual", virtualBusSize)
    }

    @DontAutoSerialize
    override val ports = Ports()

    @DontAutoSerialize
    override val buses = Buses()

    override val cpu = x86CPU(this, "cpu", virtualBusSize)
    override val cop = x86COP(this, "cop")
    override val mmu = x86MMU(this, "mmu")
    override val fpu = x86FPU(this, "fpu")

    @DontAutoSerialize
    val intHooks = InterruptHook();

    val sse = SSE(this)

    val config = Configuration(this)

    val is16bit get() = cpu.mode == x86CPU.Mode.R16
    val is32bit get() = cpu.mode == x86CPU.Mode.R32
    val is64bit get() = cpu.mode == x86CPU.Mode.R64

    val isRing0 get() = cpu.sregs.cs.cpl == 0uL
    val isRing3 get() = cpu.sregs.cs.cpl == 3uL

    override fun abi() = x86ABI(this, false)

    fun updateSnapshot() {
//        config.cpuid4(0u, 0u, 0u, 0u, 0u)
//        config.cpuid(0x2u,0u, 0u, 0u, 0u)
//        config.cpuid(0xau,0u, 0u, 0u, 0u)
//        config.cpuid(0x05u,0u, 0u, 0u, 0u)

//        config.cpuid4(0x0u, 0u, 0u, 0u, 0u)
//        (core.mmu as x86MMU).invalidatePagingCache()
//        with(config) {
//            msr(IA32_PERFEVTSEL0, 0u)
//            msr(IA32_PERFEVTSEL1, 0u)
//            msr(IA32_PERFEVTSEL2, 0u)
//            msr(IA32_PERFEVTSEL3, 0u)
//        }
    }

    private fun setModelName(name: String) {
        config.cpuid(0x80000002u, name.getUInt(0), name.getUInt(1), name.getUInt(2), name.getUInt(3))
        config.cpuid(0x80000004u, name.getUInt(8), name.getUInt(9), name.getUInt(10), name.getUInt(11))
        config.cpuid(0x80000003u, name.getUInt(4), name.getUInt(5), name.getUInt(6), name.getUInt(7))
    }

    init {
        // ToCheck: bad but working solution
        if (useMMU) {
            cpu.ports.mem.connect(buses.virtual)
            mmu.ports.inp.connect(buses.virtual)

            mmu.ports.outp.connect(buses.physical)
            ports.mem.connect(buses.physical)
        } else {
            cpu.ports.mem.connect(buses.virtual)
            ports.mem.connect(buses.virtual)
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
        updateSnapshot()
    }

    override fun reset() {
        super.reset()
        with (config) {
            cpuid(0x00u, CPUID0(1u, VENDOR.INTEL))
        }
    }

    /**
     * Выкидывает page fault если память недоступна для доступа [LorS]
     * @throws x86HardwareException.PageFault
     */
    fun raisePageFault(ea: ULong, ss: Int, size: Int, LorS: AccessAction) {
        if (useMMU) mmu.translate(ea, ss, size, LorS)
    }

    override fun stringify() = buildString {
        appendLine(mmu.stringifyTranslateAll(core.pc))
        appendLine(super.stringify())
    }
}
