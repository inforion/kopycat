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
@file:Suppress("UnstableApiUsage")

package ru.inforion.lab403.gradle.dokkaMultilang

import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.inforion.lab403.gradle.common.dokkaPluginString


class DokkaMultilangPlugin : Plugin<Project> {
    companion object {
        const val languagesExtension = "languages"
    }

    override fun apply(project: Project) {
        project.plugins.apply(dokkaPluginString)

        val dokkaMultilangTask = project.tasks.create(
                DokkaMultilangTask.taskIdentifier,
                DokkaMultilangTask::class.java) { task ->
            val languageContainer = project.container(Language::class.java)
            task.extensions.add(languagesExtension, languageContainer)
        }

        project.afterEvaluate { dokkaMultilangTask.afterProjectEvaluate() }
    }
}