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

class ComponentTracer<R: AGenericCore> constructor(parent: Module, name: String): ATracer<R>(parent, name) {
    private val tracers = mutableListOf<ITracer<R>>()

    fun addTracer(vararg newTracers: ITracer<R>) =
            tracers.addAll(newTracers).also { working = tracers.isNotEmpty() }

    fun removeTracer(tracer: ITracer<R>) = tracers.remove(tracer).also { working = tracers.isNotEmpty() }

    // Quite a strange serialization -> check required
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf()
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = Unit

    // Get the minimal status when executing, see TRACER_STATUS_STOP and other status
    override fun preExecute(core: R): Long = tracers.minOf { it.preExecute(core) }
    override fun postExecute(core: R, status: Status) = tracers.minOf { it.postExecute(core, status) }
    override fun onStart(core: R) = tracers.forEach { it.onStart(core) }
    override fun onStop() = tracers.forEach { it.onStop() }

    override fun reset() {
        super.reset()
        tracers.forEach { it.reset() }
    }

    init {
        // disabled until subtracer added
        working = false
    }
}
