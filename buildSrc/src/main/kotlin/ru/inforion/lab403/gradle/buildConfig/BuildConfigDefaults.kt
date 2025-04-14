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
package ru.inforion.lab403.gradle.buildConfig

import java.io.File

/**
 * Default values, that can be used in gradle files
 * to reduce code duplication
 */
class BuildConfigDefaults(
    val rootProjectDir: File,
    val kcPackageName: String,
    val kcLibraryDirectory: String,
    val kcFullTopClass: String,
) {
    fun scriptsDir(): String {
        val packagePath = kcFullTopClass
            .split(".")
            .run { slice(0 until this.size - 1) }
            .joinToString("/")
        return File(
            rootProjectDir,
            "kopycat-modules/${kcLibraryDirectory}/${kcPackageName}/" +
                    "src/main/resources/${packagePath}/scripts"
        ).absolutePath
    }

    fun tempDir(): String =
        File(rootProjectDir, "temp/${kcPackageName}")
            .absolutePath

    fun resourcesDir(): String =
        File(rootProjectDir, "resources")
            .absolutePath

    fun gdbPort() = 64128

    fun httpPort() = 64129

    fun terminalPort() = 64130

    fun starter() = "ru.inforion.lab403.kopycat.KopycatStarter"

    fun initScript() = "init.kts"

    fun logFilePath(configName: String): String =
        File(rootProjectDir, "temp/log-$kcPackageName-$configName.log").absolutePath

    fun historyFilePath(): String =
        File(rootProjectDir, "temp/$kcPackageName.history").absolutePath

    fun unsetArgument(): String = "__!!!_UNSET_ARGUMENT_!!!__"
    fun deleteArgument(): String = "__!!!_DELETE_ARGUMENT_!!!__"
}