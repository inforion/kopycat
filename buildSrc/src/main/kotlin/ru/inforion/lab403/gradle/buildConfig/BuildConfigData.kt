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

import org.gradle.api.tasks.Input

data class BuildConfigData(
    /**
     * Overrides kcFullTopClass from the task
     */
    @Input var fullTopClass: String,

    /**
     * Configuration name
     */
    @Input var name: String,

    /**
     * Configuration description
     */
    @Input var description: String,

    /**
     * Kopycat Starter class full name
     */
    @Input var starterClass: String,

    /**
     * KOPYCAT arguments.
     * Except `-p`, it should be passed using kcConstructorArguments
     */
    @Input var kcArguments: HashMap<String, String?> = hashMapOf(),

    /**
     * Will be passed into the `-p` argument.
     */
    @Input var kcConstructorArguments: HashMap<String, String> = hashMapOf(),

    /**
     * Passes `-kts` argument
     */
    @Input var withKotlinConsole: Boolean = true,

    /**
     * Passes `-ci` argument
     */
    @Input var withConnectionInfo: Boolean = false,

    /**
     * Passes default arguments, like init script, log file, etc.
     * See [BuildConfigTask]
     */
    @Input var withDefaultArguments: Boolean = true,
) {

    val kcConstructorArgumentsString by lazy {
        kcConstructorArguments
            .map { (key, value) -> "$key=$value" }
            .joinToString(",")
    }
}
