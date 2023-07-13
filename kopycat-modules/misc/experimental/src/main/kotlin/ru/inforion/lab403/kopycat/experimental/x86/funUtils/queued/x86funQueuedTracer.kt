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
package ru.inforion.lab403.kopycat.experimental.x86.funUtils.queued

import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SKIP
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS
import ru.inforion.lab403.kopycat.experimental.runtime.DataUtils
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.abi.x64AbiSystemV
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.x86funUtils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x86funQueuedTracer(
    parent: Module?,
    name: String,
    val queued: x86funQueuedUtils
) : ATracer<x86Core>(parent, name) {
    val x86 by lazy { core as x86Core }
    val data by lazy { DataUtils(x86) }
    val funUtils by lazy { x86funUtils(x86, x64AbiSystemV(x86)) }

    override fun preExecute(core: x86Core): ULong {
        queued.checkAndRestoreTheState()

        val somethingCalled = queued.callEverythingAvailable()
        return if (somethingCalled) TRACER_STATUS_SKIP else TRACER_STATUS_SUCCESS
    }

    override fun postExecute(core: x86Core, status: Status): ULong = TRACER_STATUS_SUCCESS
}