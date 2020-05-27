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
@file:Suppress("UnstableApiUsage", "NOTHING_TO_INLINE")

package ru.inforion.lab403.gradle.common

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.extensibility.DefaultConvention
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

@Suppress("UNCHECKED_CAST")
inline fun <T: Task>Project.getTaskByNameOrNull(name: String): T? {
    val task = tasks.getByName(name) ?: return null
    return task as T
}

@Suppress("UNCHECKED_CAST")
inline fun <T: Task>Project.getTaskByName(name: String): T =
        getTaskByNameOrNull(name) ?: throw UnknownTaskException("$name not found!")

inline val Project.processResourcesTaskOrNull: ProcessResources? get() = getTaskByNameOrNull(processResourcesTaskIdentifier)
inline val Project.jarTaskOrNull: Jar? get() = getTaskByNameOrNull(jarTaskIdentifier)
inline val Project.compileKotlinTaskOrNull: KotlinCompile? get() = getTaskByNameOrNull(compileKotlinTaskIdentifier)
inline val Project.cleanTaskOrNull: Delete? get() = getTaskByNameOrNull(cleanTaskIdentifier)

inline val Project.processResourcesTask: ProcessResources get() = getTaskByName(processResourcesTaskIdentifier)
inline val Project.jarTask: Jar get() = getTaskByName(jarTaskIdentifier)
inline val Project.compileKotlinTask: KotlinCompile get() = getTaskByName(compileKotlinTaskIdentifier)
inline val Project.cleanTask: Delete get() = getTaskByName(cleanTaskIdentifier)


@Suppress("UNCHECKED_CAST")
inline fun <reified T: Any>Project.getConventionByType(): T = convention.getPlugin(T::class.java)

inline fun Project.getSourceSets(name: String): SourceSet =
        getConventionByType<JavaPluginConvention>().sourceSets.getByName(name)


fun Project.addKotlinSourceDir(dir: File) {
    val mainSourceSet = getSourceSets(SourceSet.MAIN_SOURCE_SET_NAME)
    val ext = mainSourceSet.extensions as DefaultConvention
    val kotlinSourceSet = ext.plugins["kotlin"] as DefaultKotlinSourceSet
    if (dir !in kotlinSourceSet.kotlin.sourceDirectories) {
        logger.info("Added source directory: $dir")
        kotlinSourceSet.kotlin.srcDir(dir)
    }
}