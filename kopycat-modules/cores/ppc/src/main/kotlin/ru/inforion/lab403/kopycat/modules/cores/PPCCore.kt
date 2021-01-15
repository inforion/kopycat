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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.ppc.enums.eSystem
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.IPPCExceptionHolder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.APPCMMU
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.PPCCOP
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.PPCCPU
import ru.inforion.lab403.kopycat.modules.BUS32


abstract class PPCCore(
        parent: Module,
        name: String,
        frequency: Long,
        val exceptionHolder: IPPCExceptionHolder,
        optionalCpu: ((PPCCore, String) -> PPCCPU)? = null,
        optionalCop: ((PPCCore, String) -> PPCCOP)? = null
) : ACore<PPCCore, PPCCPU, PPCCOP>(parent, name, frequency, 1.0) {

    inner class Buses : ModuleBuses(this) {
        val physical = Bus("physical", BUS32)
        val virtual = Bus("virtual", BUS32)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }
    //override val internalHardwareExceptions = eIrq.values().toSet()

    override val buses = Buses()
    override val ports = Ports()

    @Suppress("LeakingThis")
    final override val cpu = optionalCpu?.invoke(this, "cpu") ?: PPCCPU(this, "cpu", eSystem.Base)

    @Suppress("LeakingThis")
    final override val cop = optionalCop?.invoke(this, "cop") ?: PPCCOP(this, "cop")

    override val fpu = null //TODO("FPU")
    abstract override val mmu: APPCMMU //TODO: change this

    //Because buses, cpu and ports aren't final
    open fun initRoutine() {
        //cpu.ports.mem.connect(buses.physical)
        cpu.ports.mem.connect(buses.virtual)
        mmu.ports.inp.connect(buses.virtual)

        mmu.ports.outp.connect(buses.physical)
        ports.mem.connect(buses.physical)
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "cpu" to cpu.serialize(ctxt),
                "cop" to cop.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        cpu.deserialize(ctxt, snapshot["cpu"] as Map<String, String>)
        cop.deserialize(ctxt, snapshot["cop"] as Map<String, String>)
    }

}