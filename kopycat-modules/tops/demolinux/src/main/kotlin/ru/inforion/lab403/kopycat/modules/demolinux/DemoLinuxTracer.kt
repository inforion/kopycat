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
package ru.inforion.lab403.kopycat.modules.demolinux

import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS
import ru.inforion.lab403.kopycat.experimental.runtime.DataUtils
import ru.inforion.lab403.kopycat.experimental.tracer.TracerUtils
import ru.inforion.lab403.kopycat.experimental.x86.TracerBypassUtils
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.abi.x64AbiSystemV
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.x86funUtils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class DemoLinuxTracer(parent: Module?, name: String) : ATracer<x86Core>(parent, name) {
    @DontAutoSerialize
    val x86 by lazy { core as x86Core }

//    @DontAutoSerialize
//    val linux by lazy { LinuxTODO(x86) }

    @DontAutoSerialize
    val funUtils by lazy { x86funUtils(x86, x64AbiSystemV(x86)) }

    @DontAutoSerialize
    val data by lazy { DataUtils(x86) }

    @DontAutoSerialize
    val tracerUtils by lazy { TracerUtils(x86) }

    @DontAutoSerialize
    val tracerBypassUtils by lazy { TracerBypassUtils(funUtils) }

    override fun postExecute(core: x86Core, status: Status): ULong {
        return TRACER_STATUS_SUCCESS
    }

    val hooks = mutableMapOf<ULong, () -> ULong>()

    override fun preExecute(core: x86Core): ULong = when (core.pc) {
        in hooks -> hooks[core.pc]!!()

        else -> {
            TRACER_STATUS_SUCCESS
        }
    }
}