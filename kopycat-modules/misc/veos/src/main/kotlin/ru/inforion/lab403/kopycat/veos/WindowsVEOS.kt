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
package ru.inforion.lab403.kopycat.veos

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.memory.VirtualMemory
import ru.inforion.lab403.kopycat.veos.api.impl.WindowsAPI
import ru.inforion.lab403.kopycat.veos.loader.WindowsLoader
import ru.inforion.lab403.kopycat.veos.ports.windows.WindowsProcess


class WindowsVEOS<C: AGenericCore>(parent: Module, name: String, bus: Long = BUS32): VEOS<C>(parent, name, bus) {

    override val loader = WindowsLoader(this)

    inner class Buses : VEOS<C>.Buses() {
        val outp = Bus("outp", bus)
    }

    @DontAutoSerialize
    override val buses = Buses()

    val x86mmu = object : AddressTranslator(this@WindowsVEOS, "x86mmu") {

        override fun translate(ea: Long, ss: Int, size: Int, LorS: AccessAction): Long {
            return when (ss) {
                SSR.ES.id,
                SSR.CS.id,
                SSR.SS.id,
                SSR.DS.id -> ea
                SSR.FS.id -> {
                    log.severe { "Access to TID at ${abi.programCounterValue.hex8}, offset ${ea.hex8}" }
                    (currentProcess as WindowsProcess).segmentFS or (ea and 0xFFFL)
                }

                else -> TODO("Not implemented ss: $ss")
            }
        }

    }

    override fun initialize(): Boolean {
        if (!super.initialize())
            return false

        addApi(WindowsAPI(this))

        x86mmu.ports.inp.connect(buses.mem)
        x86mmu.ports.outp.connect(buses.outp)

        return true
    }

    override val memBus get() = buses.outp

    override fun newProcess(memory: VirtualMemory) = WindowsProcess(sys, processIds.allocate(), memory)
}