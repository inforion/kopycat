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
package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.gzipInputStreamIfPossible
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.library.types.Resource
import java.io.File
import java.io.InputStream

/**
 * Random access memory with debugger break on access (R/W).
 * Can be used in debugging purposes.
 *
 * Use-case: the one would like to break on access to ANY address in the defined range.
 */
class RAMCustom(
    parent: Module,
    name: String,
    size: Int,
    access: ACCESS = ACCESS.R_W,
    verbose: Boolean = false,
    vararg items: Pair<Any, Int>
) :
    AMemory(parent, name, size, access, verbose, *items) {

    @Suppress("RemoveRedundantSpreadOperator")
    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS = ACCESS.R_W,
        verbose: Boolean = false
    ) :
            this(parent, name, size, access, verbose, *emptyArray())

    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS = ACCESS.R_W,
        verbose: Boolean = false,
        data: ByteArray
    ) :
            this(parent, name, size, access, verbose, data to 0)

    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS = ACCESS.R_W,
        verbose: Boolean = false,
        data: InputStream
    ) :
            this(parent, name, size, access, verbose, data.readBytes())

    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS = ACCESS.R_W,
        verbose: Boolean = false,
        data: File
    ) :
            this(parent, name, size, access, verbose, gzipInputStreamIfPossible(data.path))

    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS = ACCESS.R_W,
        verbose: Boolean = false,
        data: Resource
    ) :
            this(parent, name, size, access, verbose, data.openStream())
}
