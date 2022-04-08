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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.x86.IA32_MTRRCAP
import ru.inforion.lab403.kopycat.cores.x86.config.*
import ru.inforion.lab403.kopycat.cores.x86.config.CPUID0
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.ECXFeatures.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.EDXFeatures.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.MemoryType.*
import ru.inforion.lab403.kopycat.cores.x86.hardware.extensions.SSE
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86COP
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86MMU
import ru.inforion.lab403.kopycat.cores.x86.x86ABI
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

    val sse = SSE(this)

    val config = Configuration()

    val is16bit get() = cpu.mode == x86CPU.Mode.R16
    val is32bit get() = cpu.mode == x86CPU.Mode.R32
    val is64bit get() = cpu.mode == x86CPU.Mode.R64

    override fun abi() = x86ABI(this, false)

    private fun String.getUInt(ind: Int): UInt {
        val remaining = length - ind * 4
        if (remaining <= 0) return 0u
        val size = if (remaining > 4) 4 else remaining
        return bytes.getUInt(ind * 4, size).uint
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
    }

    override fun reset() {
        super.reset()
        with (config) {
            cpuid(0x00u, CPUID0(1u, VENDOR.INTEL))
        }
    }
}