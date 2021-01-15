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
package ru.inforion.lab403.kopycat

/**
 * {EN}Settings for build configuration of Kopycat core{EN}
 */
object settings {
    /**
     * {EN}When set to true enables some restriction on emulator connected with usage of host system resources{EN}
     */
    const val hasConstraints = false

    /**
     * {EN}
     * Maximum possible guest RAM size after which exception will be generated during instantiate
     * NOTE: Only make sense when [hasConstraints] is true
     * {EN}
     */
    const val maxPossibleRamSize = 0x1000_0000

    /**
     * {EN}
     * Maximum possible modules in whole guest system
     * NOTE: Only make sense when [hasConstraints] is true
     * {EN}
     */
    const val maxPossibleModules = 0x80

    /**
     * {EN}Enable or not REST API compilation{EN}
     */
    const val enableRestApi = true

    // the following variables used as extensions when plugins loading

    /**
     * {EN}Jar-file plugin extension{EN}
     */
    const val jarFileExt = "jar"

    /**
     * {EN}Zip-file plugin extension{EN}
     */
    const val zipFileExt = "zip"

    /**
     * {EN}Class-file plugin extension{EN}
     */
    const val classFileExt = "class"

    /**
     * {EN}Json-file plugin extension{EN}
     */
    const val jsonFileExt = "json"

    /**
     * {EN}How module parameters (arguments) section name for JSON {EN}
     */
    const val jsonParamsSectionName = "params."

    /**
     * {EN}Name of file for plugins and modules to define which of class export as loadable modules{EN}
     */
    const val exportFilename = "export.txt"

    /**
     * {EN}Where in classpath placed embedded into Kopycat core modules{EN}
     */
    const val internalModulesClasspath = "ru.inforion.lab403.kopycat.modules"

    const val libraryPathSeparator = ":"

    const val registriesSeparator = ","

    const val librariesSeparator = ","

    /**
     * {EN}Available types in json for variables, parameters and arguments{EN}
     */
    val availableTypes = listOf("String", "char", "int", "long", "float", "double", "boolean", "File", "Resource")

    /**
     * {EN}Path relative to working directory of emulator with modules{EN}
     */
    const val modulesDirectoryPath = "modules"

    const val systemModulesMainPath = "classes/kotlin/main/"

    const val systemModulesTestPath = "classes/kotlin/test/"

    /**
     * {EN}Kopycat home environment variable name{EN}
     */
    const val envHomeVariableName = "KOPYCAT_HOME"

    /**
     * {EN}Use or not directed [ByteBuffer] for emulator memory object{EN}
     */
    const val directedMemory = false
}