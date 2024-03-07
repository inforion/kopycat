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
package ru.inforion.lab403.kopycat.modules.tracer

import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS

class DynamicTracer<R: AGenericCore>(parent: Module?, name: String) : ATracer<R>(parent, name) {
    override fun postExecute(core: R, status: Status): ULong {
        return TRACER_STATUS_SUCCESS
    }

    val hooks = mutableMapOf<ULong, () -> ULong>()
    val dynamicHooks = mutableListOf<() -> ULong>()

    override fun preExecute(core: R): ULong = when (core.pc) {
        in hooks -> hooks[core.pc]!!()

        else -> let {
            for (hook in dynamicHooks) {
                val result = hook()
                if (result != TRACER_STATUS_SUCCESS) {
                    return@let result
                }
            }

            TRACER_STATUS_SUCCESS
        }
    }
}