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
package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.gzipInputStreamIfPossible
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.library.types.Resource
import java.io.File
import java.io.InputStream

class ROM(parent: Module, name: String, size: Int, vararg items: Pair<Any, Int>) :
        AMemory(parent, name, size, ACCESS.R_I, *items) {

    @Suppress("RemoveRedundantSpreadOperator")
    constructor(parent: Module, name: String, size: Int) :
            this(parent, name, size, *emptyArray())

    constructor(parent: Module, name: String, size: Int, data: ByteArray) :
            this(parent, name, size, data to 0)

    constructor(parent: Module, name: String, size: Int, data: InputStream) :
            this(parent, name, size, data.readBytes())

    constructor(parent: Module, name: String, size: Int, data: File) :
            this(parent, name, size, gzipInputStreamIfPossible(data.path))

    constructor(parent: Module, name: String, size: Int, data: Resource) :
            this(parent, name, size, data.inputStream())
}