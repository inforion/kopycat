/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.runtime.analyzer.stack

import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS

class StackAnalyzerTracer<R : AGenericCore>(
    parent: Module?,
    name: String,
    val stackAnalyzer: StackAnalyzer
) : ATracer<R>(parent, name) {
    override fun preExecute(core: R): ULong = TRACER_STATUS_SUCCESS

    override fun postExecute(core: R, status: Status): ULong {
        stackAnalyzer.captureShot()
        return TRACER_STATUS_SUCCESS
    }
}