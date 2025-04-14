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
package ru.inforion.lab403.gradle.kopycat

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import java.io.File

object CopyExternalDeps {
    const val TASK_ID = "copyExternalDeps"
    const val EXTERNAL_LIBRARY_NAME = "external"

    fun createTask(project: Project, productionDirPath: File): Task =
        project.tasks.register(TASK_ID, Copy::class.java) { copy ->
            copy.group = "kopycat"
            val externalDependencies = project.objects.setProperty(File::class.java)
            project.subprojects { proj ->
                proj.tasks.withType(BuildKopycatModuleTask::class.java).all { buildKopycatModuleTask ->
                    copy.mustRunAfter(buildKopycatModuleTask)
                    copy.inputs.files(buildKopycatModuleTask.externalDependencies) // rebuild when any of them change
                    externalDependencies.addAll(buildKopycatModuleTask.externalDependencies)
                }
            }

            copy.from(externalDependencies)
            copy.destinationDir = File(productionDirPath, EXTERNAL_LIBRARY_NAME)
        }.get()
}
