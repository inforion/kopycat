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
package ru.inforion.lab403.kopycat.library.types

import ru.inforion.lab403.common.extensions.DynamicClassLoader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.util.zip.GZIPInputStream

class Resource(private val path: String) {
    private fun getResource(path: String) = DynamicClassLoader.getResource(path)

    private fun openResourceStream(resource: String): InputStream {
        // WARNING: don't change -> with function not working
        return DynamicClassLoader.getResourceAsStream(resource)
                ?: throw FileNotFoundException("Can't open resource $resource within path ${getResource("")}")
    }

    fun exists(): Boolean {
        val stream = DynamicClassLoader.getResourceAsStream(path)
        return stream == null
    }

    fun inputStream(): InputStream {
        val stream = openResourceStream(path)
        return if (File(path).extension == "gz") GZIPInputStream(stream) else stream
    }

    val url get() = getResource(path) ?: throw FileNotFoundException("Can't open resource $path within path ${getResource("")}")

    fun readBytes(): ByteArray = inputStream().readBytes()
}