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
package ru.inforion.lab403.kopycat.modules.veos

import ru.inforion.lab403.common.extensions.splitBy
import ru.inforion.lab403.common.extensions.whitespaces
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.veos.VEOS


abstract class AApplication <C: AGenericCore, T: VEOS<C>>(
        parent: Module?,
        name: String,
        val exec: String,
        val args: String,
        val ldPreload: String = ""
) : Module(parent, name), IAutoSerializable {

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
    }

    abstract val veos: T

    @DontAutoSerialize
    override val buses = Buses()

    private var isConfigured = false

    private fun loadLibraries(libraries: String) {
        libraries.splitBy(whitespaces).forEach { veos.loadLibrary(it) }
    }

    override fun reset() {
        if (!isConfigured) {
            super.reset()
            veos.initProcess(exec, *args.splitBy(whitespaces).toTypedArray())
            loadLibraries(ldPreload)
            isConfigured = true
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }
}