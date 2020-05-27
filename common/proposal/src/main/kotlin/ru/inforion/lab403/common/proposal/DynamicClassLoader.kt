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
package ru.inforion.lab403.common.proposal

import ru.inforion.lab403.common.logging.logger
import java.io.File
import java.net.URLClassLoader
import java.util.logging.Level.FINE


object DynamicClassLoader : URLClassLoader(arrayOf(), ClassLoader.getSystemClassLoader()) {

    private const val libraryPathKey = "java.library.path"

    val log = logger(FINE)

    /**
     * {EN} Unset sys_paths to make JVM reload it from java.library.path {EN}
     */
    private fun unsetSystemPath() {
        val sysPathsField = ClassLoader::class.java.getDeclaredField("sys_paths")
        sysPathsField.isAccessible = true
        sysPathsField.set(null, null)
    }

    /**
     * {EN} Add new class path for e.i. JAR-file {EN}
     */
    fun loadIntoClasspath(classpath: File) {
        val newUrl = classpath.toURI().toURL()

        if (newUrl !in urLs) {
            addURL(newUrl)
            log.finer { "Added to classloader: $newUrl" }
        }
    }

    /**
     * {EN} Add path to java.library.path and then unset sys_paths to make JVM reload path {EN}
     *
     * TODO: An illegal reflective access operation has occurred here and it will be denied in a future JVM release
     */
    fun addLibraryPath(path: String) {
        val javaLibraryPath = System.getProperty(libraryPathKey).split(":")
        if (path in javaLibraryPath) {
            log.warning { "Path $path already in $libraryPathKey..." }
            return
        }

        val newPaths = (javaLibraryPath + path).joinToString(":")

        System.setProperty(libraryPathKey, newPaths)

        log.fine { "Added $path to $libraryPathKey" }

        unsetSystemPath()
    }
}
