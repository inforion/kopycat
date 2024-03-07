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
package ru.inforion.lab403.kopycat.experimental.tracer

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS

class TracerUtils(val core: ACore<*, *, *>) {
    fun safeTraceSuccess(functionName: String? = null, block: (functionName: String) -> Unit) =
        safeTrace(functionName, TRACER_STATUS_SUCCESS) { funName ->
            block(funName)
            TRACER_STATUS_SUCCESS
        }

    fun safeTrace(functionName: String? = null, fallback: ULong, block: (functionName: String) -> ULong): ULong {
        functionName?.also {
            TracerBypassUtils.log.fine { "[0x${core.pc.hex}] Called $it" }
        }

        return runCatching {
            block(functionName ?: "__no__name__")
        }.onFailure { e ->
            TracerBypassUtils.log.warning { "[0x${core.pc.hex}] Trace $functionName failed. Exception: '${e.message ?: e}'" }
        }.getOrDefault(fallback)
    }
}