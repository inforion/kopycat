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
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.interfaces.ITracer

class ComponentTracer<R: AGenericCore>(
        parent: Module,
        name: String,
        vararg val args: ATracer<R>
): ATracer<R>(parent, name) {
    private val tracers = ArrayList<ITracer<R>>(args.toList())

    fun addTracer(vararg newTracers: ITracer<R>): Boolean = tracers.addAll(newTracers)
    fun removeTracer(tracer: ITracer<R>): Boolean = tracers.remove(tracer)

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> = HashMap()
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = Unit

    override fun preExecute(core: R): Boolean = tracers.all { it.preExecute(core) }
    override fun postExecute(core: R, status: Status): Boolean = tracers.all { it.postExecute(core, status) }
    override fun onStart() = tracers.forEach { it.onStart() }
    override fun onStop() = tracers.forEach { it.onStop() }
}
