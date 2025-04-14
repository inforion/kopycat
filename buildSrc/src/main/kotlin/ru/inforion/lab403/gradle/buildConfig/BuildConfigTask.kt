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

import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import ru.inforion.lab403.common.extensions.div
import ru.inforion.lab403.common.extensions.removeIf
import ru.inforion.lab403.gradle.buildConfig.creator.*
import ru.inforion.lab403.gradle.buildConfig.creator.scriptgen.ScriptGeneratorData
import ru.inforion.lab403.gradle.common.*
import java.io.File


open class BuildConfigTask : DefaultTask() {
    companion object {
        const val TASK_IDENTIFIER = "createKopycatConfig"

        private fun getKcTopClassFromFull(full: String): String = full.split(".").run {
            getOrElse(this.size - 1) {
                throw IllegalStateException("Unable to find -1 part of the top class full name '$this'")
            }
        }
    }

    override fun getGroup() = "kopycat"

    @Internal
    val rootProjectDir: File = project.rootProject.projectDir

    /**
     * Directory for codegen files
     */
    @Input
    var configDir: String = "${rootProjectDir / "temp/config"}"

    @get:Internal
    val configDirPath by lazy { File(configDir) }

    /**
     * Full top class name.
     * For example, `ru.inforion.lab403.kopycat.modules.demolinux.DemoLinux`
     */
    @Input
    lateinit var kcFullTopClass: String

    @get:Internal
    val kcPackageName by lazy {
        kcFullTopClass
            .split(".").run {
                getOrElse(this.size - 2) {
                    throw IllegalStateException("Unable to find -2 part of the top class full name '$this'")
                }
            }
    }

    /**
     * Actual library for the module
     * (name of directory in the e.g. `production/modules` directory)
     */
    @Input
    lateinit var kcLibraryDirectory: String

    @get:Internal
    val defaults by lazy {
        BuildConfigDefaults(
            rootProjectDir = rootProjectDir,
            kcPackageName = kcPackageName,
            kcLibraryDirectory = kcLibraryDirectory,
            kcFullTopClass = kcFullTopClass
        )
    }

    @Internal
    val dataList = mutableListOf<BuildConfigData>()

    fun addConfig(closure: Closure<BuildConfigData>) {
        val nonameString = defaults.unsetArgument()
        val data = BuildConfigData(
            fullTopClass = kcFullTopClass,
            name = nonameString,
            description = "",
            starterClass = defaults.starter(),
        )
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = data
        closure.call()

        if (data.name == nonameString) {
            project.logger.error("[BuildConfig] Configuration in `addConfig` name is unset")
            throw IllegalStateException("Configuration in `addConfig` name is unset")
        }

        dataList.add(data)
    }

    /**
     * Java classpath for the generated scripts
     */
    private fun getRuntimeClasspath() = project
        .getSourceSets("main")
        .runtimeClasspath
        .map { it.absolutePath }

    /**
     * Prepares arguments for the script generator
     *
     * Sources:
     * 1. Default arguments
     * 2. Special variables (e.g. kcTopClass)
     * 3. kcArguments
     */
    private fun prepareArguments(data: BuildConfigData) = linkedMapOf<String, String?>().also { arguments ->
        if (data.withDefaultArguments) {
            arguments["-g"] = defaults.gdbPort().toString()
            arguments["-r"] = defaults.httpPort().toString()
            arguments["-w"] = defaults.tempDir()
            arguments["-sd"] = defaults.scriptsDir()
            arguments["-rd"] = defaults.resourcesDir()
            arguments["-is"] = defaults.initScript()
            arguments["-lf"] = defaults.logFilePath(data.name)
            arguments["-hf"] = defaults.historyFilePath()
        }

        arguments["-n"] = getKcTopClassFromFull(data.fullTopClass)
        if (data.kcConstructorArgumentsString.isNotEmpty()) {
            arguments["-p"] = data.kcConstructorArgumentsString
        }

        // Creates (key exists), but the value is null
        if (data.withKotlinConsole) {
            arguments["-kts"] = null
        }
        if (data.withConnectionInfo) {
            arguments["-ci"] = null
        }
    }.also { defaultArguments ->
        data.kcArguments
            .filter { (key, _) -> key in defaultArguments }
            .keys
            .joinToString(", ")
            .also { keys ->
                if (keys.isNotEmpty()) {
                    logger.warn("[WARN] Keys collision in '${data.name}' between default arguments and kcArguments: '$keys'")
                }
            }
    }.let { defaultArguments ->
        LinkedHashMap<String, String?>().also { copy ->
            copy.putAll(defaultArguments)
            copy.putAll(data.kcArguments)
            copy.removeIf { (_, value) -> value == defaults.deleteArgument() }
        }
    }

    /**
     * Is being called after all project tasks has been collected
     */
    internal fun afterProjectEvaluate() {
        for (data in dataList) {
            // just check
            prepareArguments(data)
        }
    }

    /**
     * Task itself
     */
    @TaskAction
    fun createKopycatConfig() {
        configDirPath.dirCheckOrCreate()

        val runtimeClasspath = getRuntimeClasspath()
        val parentProjectFiles = generateSequence(project) { it.parent }
            .map { it.name }
            .toList()

        for (data in dataList) {
            val genData = ScriptGeneratorData(
                data.name,
                data.description,
                runtimeClasspath,
                data.starterClass,
                rootProjectDir.path,
                "${project.path}:buildKopycatModule",
                kcPackageName,
                parentProjectFiles
            )

            val args = prepareArguments(data)

            listOf(
                BashScriptCreator(genData),
                PowerShellScriptCreator(genData),
                IntelliJScriptCreator(genData),
            ).forEach { creator ->
                val generator = creator.generator
                generator.arguments.putAll(args)

                creator.preHook()
                val text = generator.generate()

                val innerConfigDirPath = File(configDirPath, generator.dirName())
                innerConfigDirPath.dirCheckOrCreate()
                val config = File(innerConfigDirPath, generator.fileName())
                    .apply { writeText(text) }

                creator.postHook(config, logger)
            }
        }
    }
}