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
package ru.inforion.lab403.gradle.dokkaMultilang

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.jetbrains.dokka.gradle.GradleSourceRootImpl
import ru.inforion.lab403.gradle.common.forEachExtension
import ru.inforion.lab403.gradle.common.getExtensionsAsList
import ru.inforion.lab403.gradle.common.getSourceSets
import java.io.File
import java.nio.file.Files


open class DokkaMultilangTask : DefaultTask() {

    companion object {
        const val taskIdentifier = "dokkaMultilang"
    }

    @Input var targets = listOf("kotlin")
    @Input var excludes = listOf<String>()

    private val invokedGradleTasks get() = project.gradle.taskGraph.allTasks.map { it.name }
    private val taskRequestedDirectly get() = name in invokedGradleTasks

    private data class Source(val file: File, val base: File, val classpath: File)

    private fun filterWorkingSources(sourceSet: SourceSet): List<Source> {
        val sources = sourceSet.allSource

        // get full path of include directories
        val includeDirectories = sources.srcDirs.filter { dir -> targets.any { dir.endsWith(it) } }

        return sources.mapNotNull { src ->
            val base = includeDirectories.find { dir -> src.startsWith(dir) } ?: return@mapNotNull null
            // get the path without starting part: kotlin, java ...
            val classpath = File(src.path.removePrefix(base.path).removePrefix("/"))
            // check if path started with 'ru/inforion...' in excludes
            if (excludes.any { exclude -> classpath.startsWith(exclude) }) null else Source(src, base, classpath)
        }
    }

    private fun prepareGeneratedDirectory(prefix: String): File {
        val generatedTemporaryDir = File(temporaryDir, prefix)

        if (generatedTemporaryDir.exists()) {
            check(generatedTemporaryDir.deleteRecursively()) {
                "Generated previous directory deletion failed: '${generatedTemporaryDir.path}'"
            }
        }

        logger.lifecycle("Creating directory '${generatedTemporaryDir.path}'")
        check(generatedTemporaryDir.mkdirs()) { "Can't create '${generatedTemporaryDir.path}'" }

        return generatedTemporaryDir
    }

    private fun copyFiles(files: List<Source>, destination: File) = files.map {
        val output = File(destination, it.classpath.path)
        val dir = output.parentFile
        if (!dir.exists()) {
            logger.debug("Creating directory '${dir.path}'")
            dir.mkdirs()
        }
        logger.debug("Coping '${it.file.path}' -> '${output.path}'")
        Files.copy(it.file.toPath(), output.toPath())
        output
    }

    private fun makeComment(file: File, languages: List<Language>, selected: Language) {
        var result = file.readText()
        languages.forEach { language ->
            result = if (language.name != selected.name) {
                val escapedMarker = "\\{${language.marker}}".toRegex()
                val regex = Regex("(?s)$escapedMarker.*?$escapedMarker")
                result.replace(regex, "")
            } else {
                result.replace("{${language.marker}}", "")
            }
        }
        val regex = Regex("(\r\n|[\r\n]).*\\* Created by .*")
        result = result.replace(regex, "")
        logger.info("Output file: '${file.path}'")
        file.writeText(result)
    }

    private fun configureLanguageFiles(sources: List<Source>, languages: List<Language>, selected: Language) {
        val generatedTemporaryDir = prepareGeneratedDirectory(selected.marker)

        logger.lifecycle("Coping files into temporary directory '${generatedTemporaryDir.path}'")
        val result = copyFiles(sources, generatedTemporaryDir)

        logger.lifecycle("Fixing comments for language: '${selected.name}'")
        result.forEach { makeComment(it, languages, selected) }

        logger.lifecycle("Setup ${selected.task} sourceRoots to '${generatedTemporaryDir.path}'")
        val dokkaRoot = GradleSourceRootImpl().also { it.path = generatedTemporaryDir.path }
        selected.task.configuration.sourceRoots = mutableListOf(dokkaRoot)
    }

    fun afterProjectEvaluate() {
        forEachExtension(DokkaMultilangPlugin.languagesExtension) { language: Language ->
            language.task.also { dokkaTask -> dokkaTask.dependsOn(this) }
        }
    }

    @TaskAction
    fun dokkaMultilang() {
        require(targets.isNotEmpty()) { "Specify at least one target sources: kotlin|java (default: kotlin)" }

        val languages: List<Language> = getExtensionsAsList(DokkaMultilangPlugin.languagesExtension)

        val workingSources = filterWorkingSources(project.getSourceSets(SourceSet.MAIN_SOURCE_SET_NAME))

        val foundLanguageTasks = languages.filter { it.task.name in invokedGradleTasks }

        val requestedLanguages = if (foundLanguageTasks.isEmpty()) {
            check(taskRequestedDirectly) { "Something weird happen: '$name' wasn't run directly and no dokka task run... but I'm here!" }
            logger.lifecycle("Task '$name' was requested directly -> generate all languages")
            languages
        } else {
            foundLanguageTasks
        }

        logger.lifecycle("Requested languages: ${requestedLanguages.joinToString { it.name }}")
        requestedLanguages.forEach { configureLanguageFiles(workingSources, languages, it) }
    }
}