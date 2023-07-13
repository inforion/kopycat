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
package ru.inforion.lab403.kopycat.library.exceptions

import ru.inforion.lab403.kopycat.library.types.Resource
import java.nio.file.Path
import kotlin.io.path.pathString

class ResourceGzipError : Exception {
    companion object {
        private fun stringToMessage(string: String) = "Gzip error. Resource '${string}'"
        private val Resource.message get() = stringToMessage(this.pathString)
    }

    constructor(resource: Resource) : super(resource.message);
    constructor(resource: Resource, cause: Throwable) : super(resource.message, cause)
    constructor(path: String) : super(stringToMessage(path));
    constructor(path: String, cause: Throwable) : super(stringToMessage(path), cause)
    constructor(path: Path) : super(path.pathString);
    constructor(path: Path, cause: Throwable) : super(path.pathString, cause)
}