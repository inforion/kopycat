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
package ru.inforion.lab403.kopycat.library.types

import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.utils.DynamicClassLoader
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.library.exceptions.ResourceGzipError
import java.io.*
import java.net.JarURLConnection
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import kotlin.io.path.*


class ResourceNotFoundException(val path: String) : FileNotFoundException(
    "Can't open resource within path '$path'. " +
            "Searched in resource runtime directory and JAR resource"
) {
    constructor(path: Path) : this(path.pathString)
}

/**
 * Searches the resource within JAR resources and runtime provided resource path
 *
 * Search priority:
 * 1. Runtime path
 * 2. JAR resources
 *
 * @throws FileNotFoundException Is the resource not found
 */
class Resource(val path: Path) {
    constructor(path: String) : this(Path(path))

    companion object {
        @Transient
        val log = logger(INFO)
    }

    /**
     * Path string with POSIX-like separators
     */
    val pathString: String = path.invariantSeparatorsPathString

    fun jarFileListing() = sequence {
        val e1 = DynamicClassLoader.getResources(pathString)
        while (e1.hasMoreElements()) {
            val e2 = (e1.nextElement().openConnection() as? JarURLConnection)?.jarFile?.entries()
            while (e2?.hasMoreElements() == true) {
                val entry = e2.nextElement()
                if (entry.name.startsWith(pathString)) {
                    val relativeName = entry.name.substring(pathString.length + 1)
                    if (relativeName.isNotEmpty()) {
                        yield(relativeName)
                    }
                }
            }
        }
    }

    fun openStream(): InputStream {
        return let {
            // WARNING: don't change -> with function not working
            val runtimePath = Path(Kopycat.resourceDir) / path
            if (runtimePath.exists()) {
                return@let File(runtimePath.toString()).inputStream()
            }
            log.trace { "Runtime file '${runtimePath}' not found, trying to load JAR resource" }

            return@let DynamicClassLoader.getResourceAsStream(pathString)
        }.let { resource ->
            if (resource == null) {
                throw ResourceNotFoundException(pathString)
            }
            resource
        }.let { stream ->
            if (path.extension == "gz") try {
                GZIPInputStream(stream)
            } catch (e: IOException) {
                throw ResourceGzipError(path, e)
            } else stream
        }
    }

    fun readBytes(): ByteArray = openStream().readBytes()
}
