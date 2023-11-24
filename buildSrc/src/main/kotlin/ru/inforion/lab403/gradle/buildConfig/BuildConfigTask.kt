/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import ru.inforion.lab403.common.extensions.div
import ru.inforion.lab403.common.extensions.removeIf
import ru.inforion.lab403.gradle.buildConfig.scriptgen.BashScriptGenerator
import ru.inforion.lab403.gradle.buildConfig.scriptgen.IScriptGenerator
import ru.inforion.lab403.gradle.buildConfig.scriptgen.IntelliJScriptGenerator
import ru.inforion.lab403.gradle.buildConfig.scriptgen.PowerShellScriptGenerator
import ru.inforion.lab403.gradle.common.*
import java.io.File


open class BuildConfigTask : DefaultTask() {
    companion object {
        const val TASK_IDENTIFIER = "createKopycatConfig"
    }

    @Internal
    val rootProjectDir = project.rootProject.projectDir

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

    @get:Internal
    val kcTopClass by lazy {
        kcFullTopClass
            .split(".").run {
                getOrElse(this.size - 1) {
                    throw IllegalStateException("Unable to find -1 part of the top class full name '$this'")
                }
            }
    }

    /**
     * Path to a bunch of prebuild module's libraries.
     * For example, `production/modules`
     */
    @Input
    var kcModuleLibraries = "${rootProjectDir / "production/modules"}"

    /**
     * Actual library for the module
     * (name of directory in the [kcModuleLibraries])
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
        val data = BuildConfigData(nonameString, "", defaults.starter())
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = data
        closure.call()

        if (data.name == nonameString) {
            project.logger.error("Configuration in `addConfig` name is unset")
            throw IllegalStateException("Configuration in `addConfig` name is unset")
        }

        dataList.add(data)
    }

    fun addConfigData(data: BuildConfigData) {
        dataList.add(data)
    }


    /**
     * Is being called after all project tasks has been collected
     */
    internal fun afterProjectEvaluate() {
        if (!configDirPath.exists()) {
            project.logger.info("$configDirPath does not exist. Creating.")
            configDirPath.mkdirs()
        }

        val classpath = project.getSourceSets("main").runtimeClasspath.map { it.absolutePath }

        // TODO: encapsulate script generation
        for (data in dataList) {
            listOf<IScriptGenerator>(
                // TODO: encapsulate constructor arguments
                BashScriptGenerator(
                    data.name,
                    data.description,
                    classpath,
                    data.starterClass,
                    rootProjectDir.path,
                    "${project.path}:buildKopycatModule",
                    kcPackageName
                ),
                PowerShellScriptGenerator(
                    data.name,
                    data.description,
                    classpath,
                    data.starterClass,
                    rootProjectDir.path,
                    "${project.path}:buildKopycatModule",
                    kcPackageName
                ),
                IntelliJScriptGenerator(
                    data.name,
                    data.description,
                    classpath,
                    data.starterClass,
                    rootProjectDir.path,
                    "${project.path}:buildKopycatModule",
                    kcPackageName
                ),
            ).forEach { generator ->
                if (data.withDefaultArguments) {
                    generator.arguments["-g"] = defaults.gdbPort().toString()
                    generator.arguments["-w"] = defaults.tempDir()
                    generator.arguments["-sd"] = defaults.scriptsDir()
                    generator.arguments["-rd"] = defaults.resourcesDir()
                    generator.arguments["-is"] = defaults.initScript()
                    generator.arguments["-lf"] = defaults.logFilePath(data.name)
                }

                generator.arguments["-n"] = kcTopClass
                generator.arguments["-y"] = kcModuleLibraries
                generator.arguments["-l"] = kcLibraryDirectory
                if (data.kcConstructorArgumentsString.isNotEmpty()) {
                    generator.arguments["-p"] = data.kcConstructorArgumentsString
                }

                // Creates (key exists), but the value is null
                if (data.withKotlinConsole) {
                    generator.arguments["-kts"] = null
                }
                if (data.withConnectionInfo) {
                    generator.arguments["-ci"] = null
                }

                // TODO: check arguments collision

                generator.arguments.putAll(data.kcArguments)
                generator.arguments.removeIf { (_, value) -> value == defaults.deleteArgument() }

                val text = generator.generate()
                val innerConfigDirPath = File(configDirPath, generator.dirName())

                if (!innerConfigDirPath.exists()) {
                    project.logger.info("$innerConfigDirPath does not exist. Creating.")
                    innerConfigDirPath.mkdirs()
                }
                File(innerConfigDirPath, generator.fileName())
                    .writeText(text)
            }
        }

        // TODO: refactor crunch
         val intelliJRunDir = File(rootProjectDir.path, ".idea/runConfigurations")
        if (!intelliJRunDir.exists()) {
            project.logger.info("$intelliJRunDir does not exist. Creating.")
            intelliJRunDir.mkdirs()
        }
        File(configDirPath, "intellij").listFiles()?.forEach {
            it.copyTo(File(intelliJRunDir, it.name), true)
            project.logger.info("Copied '$it' into '$intelliJRunDir'")
        }
    }
}