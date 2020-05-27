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
package ru.inforion.lab403.gradle.buildConfig

//import ru.inforion.lab403.common.logging
import ru.inforion.lab403.gradle.common.className
import ru.inforion.lab403.gradle.common.kotlinFileExtension
import ru.inforion.lab403.gradle.common.packageName
import ru.inforion.lab403.gradle.kodegen.Kodegen
import java.io.File

internal object BuildConfig {
//    val log = logger(Level.INFO)

    internal fun generateBuildConfig(configObject: String, properties: Map<String, Any>) = Kodegen {
        pkg(configObject.packageName()) {
            obj(configObject.className()) {
                properties.forEach { (name, value) ->
//                    log.debug("Setup $name: ${value.javaClass.simpleName} = $value")
                    constval(name, value)
                }
            }
        }
    }

    private fun createDirIfNotExists(dir: File) {
        if (!dir.exists()) {
//            log.lifecycle("Creating directory: '$dir'")
            dir.mkdirs()
        }
    }

    fun makeConfigSources(baseDir: File, configObject: String, properties: Map<String, Any>): Boolean {
        val newSourceFile = File(baseDir, configObject.replace(".", "/") + kotlinFileExtension)

        val output = generateBuildConfig(configObject, properties).toString()

        if (newSourceFile.exists() && newSourceFile.readText() == output) {
//            log.info("Nothing changed in '$configObject'")
            return false
        }

        createDirIfNotExists(newSourceFile.parentFile)

//        log.lifecycle("Writing config to '${newSourceFile.path}'\n$output")
        newSourceFile.writeText(output)

        return true
    }
}