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
package ru.inforion.lab403.kopycat.modules.virtmips

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.common.ComponentTracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SKIP
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS
import ru.inforion.lab403.kopycat.cores.mips.enums.GPR
import ru.inforion.lab403.kopycat.cores.mips.enums.InstructionSet
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.cores.MipsDebugger
import java.io.InputStream


/**
 * Created by shiftdj on 18.06.2021.
 */


class VirtMIPS(
        parent: Module?,
        name: String,
        baseAddress: ULong,
        memorySize: Int,
        binary: InputStream
) : Module(parent, name) {

    companion object {
        const val ramOffset = 0x10000000L
        const val ramSize =   0x10000000
    }

    constructor(
            parent: Module?,
            name: String,
            baseAddress: ULong = 0x10000000u,
            memorySize: Int = 0x08000000,
            binary: String = "binaries/md1rom"
    ) : this(
            parent,
            name,
            baseAddress,
            memorySize,
            Resource(binary).openStream()
    )

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS32)
    }

    override val buses = Buses()

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }

    override val ports = Ports()

    val mips = MipsCore(this,
            "mips",
            frequency = 50.MHz,
            multiplier = 9,
            ArchitectureRevision = 1,
            countOfShadowGPR = 0,
            ipc = 1.0,
            PABITS = 32,
            PRId = 0x55ABCC01u, // PRId from my imagination
            useMMU = true)

//    val rom = RAM(this, "rom", memorySize, binary)
//    val ram = RAM(this, "ram", ramSize)
//    val shm = RAM(this, "shm", 0x400_0000)
//    val unk = RAM(this, "unk", 0x400_0000)

    private val dbg = MipsDebugger(this, "dbg")

    fun writeTlbEntry(index: Int, vAddress: ULong, pAddress: ULong, size: Int) =
        mips.mmu.writeTlbEntry(index, vAddress, pAddress, size)

    override fun reset() {
        super.reset()

        mips.mmu.writeTlbEntry(0, 0x0000_0000u, 0x7000_0000u, 0x1000_0000)
//        mips.mmu.writeTlbEntry(1, 0x6000_0000, 0x6000_0000, 0x1000_0000)

//        shm.write(0x023444E4, 0, 4, 0x50)

        mips.cpu.iset = InstructionSet.MIPS16
        mips.pc = 0x90CE1600u
        mips.reg(GPR.SP.id, 0x90000000uL - 4u)

        mips.reg(GPR.A0.id, 0x101u)
        mips.reg(GPR.A2.id, 1u)
        mips.reg(GPR.A3.id, 0x20u)
    }

    inner class Interceptor(parent: Module?, name: String, vararg addresses: Pair<ULong, () -> Unit>) : ATracer<MipsCore>(parent, name) {
        val handlers = addresses.toMap()

        override fun preExecute(core: MipsCore): ULong {
            val handler = handlers[mips.pc]
            if (handler != null) {
                handler()
                mips.cpu.branchCntrl.setIp(mips.cpu.reg(31))
                mips.cpu.iset = InstructionSet.MIPS16
//                mips.cpu.resetFault()
                return TRACER_STATUS_SKIP
            }
            return TRACER_STATUS_SUCCESS
        }

        override fun postExecute(core: MipsCore, status: Status) = TRACER_STATUS_SUCCESS
    }

//    fun handler_0x941C039C() {
//        core.reg(GPR.V0.id, 0x6000_0000) // Idk, wtf is the address it wants
//    }

    val interceptor = Interceptor(this, "interceptor",
            0x941C0000uL to {},
            0x941C039CuL to {},
            0x908CDB34uL to { mips.reg(GPR.V0.id, 1u) }, // 0x908C729E
            0x90AE4750uL to { mips.reg(GPR.V0.id, 1u) }, // 0x908CDBBA
    )

    val trc = ComponentTracer<MipsCore>(this, "trc")

    init {
//        shm.ports.mem.connect(buses.mem, 0x6000_0000)
//        unk.ports.mem.connect(buses.mem, 0x7000_0000)
//        ram.ports.mem.connect(buses.mem, baseAddress - ramOffset)
//        rom.ports.mem.connect(buses.mem, baseAddress)

        mips.ports.mem.connect(buses.mem)

        dbg.ports.breakpoint.connect(mips.buses.virtual)
        dbg.ports.reader.connect(mips.buses.virtual)

//        interceptor.ports.mem.connect(buses.mem)

        trc.addTracer(interceptor)

        buses.connect(trc.ports.trace, dbg.ports.trace)

        ports.mem.connect(buses.mem)
    }
}


