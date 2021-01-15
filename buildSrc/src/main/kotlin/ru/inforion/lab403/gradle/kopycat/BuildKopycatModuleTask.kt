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
@file:Suppress("UnstableApiUsage")

package ru.inforion.lab403.gradle.kopycat

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import ru.inforion.lab403.gradle.common.*
import java.io.File


open class BuildKopycatModuleTask : Copy() {

    companion object {
        const val taskIdentifier = "buildKopycatModule"
        const val kopycatHomeEnvVariable = "KOPYCATE_HOME"
        const val kopycatModulesSubproject = ":kopycat-modules"
        const val defaultProductionDirPath = "production"
    }

    /**
     * {RU}Если задана в true, то версия у jar-файла модуля будет удалена{RU}
     */
    @Input var removeJarVersion = true

    /**
     * {RU}
     * Ищет зависимости по модулям Kopycat по следущей логике:
     * Если переменная ```kopycat.useDevelopmentCore = true```, то:
     * зависимости могут быть указаны в виде под пути к проекту относительно проекта :kopycat-modules,
     * например ```require += cores:x86```, или в виде под пути относительно пути реестра модулей (production/library)
     * к jar-файлу, например ```require += cores/x86.jar```.
     *
     * Если же переменная ```kopycat.useDevelopmentCore = false```, то:
     * все зависимости разрешаются как jar-файлы, то зависимость, указанная в виде ```require += cores:x86```
     * будет преобразована в cores/x86.jar относительно пути production/library. Зависимости ```require += cores/x86.jar```
     * также можно указывать.
     * {RU}
     */
    @Input var require = mutableListOf<String>()

    /**
     * {RU}
     * Базовый путь к директории с модулями. Место куда инсталируются полученные jar-файлы,
     * а также используется для зависимостей, если ```kopycat.useDevelopmentCore = false``` см. [require]
     * {RU}
     */
    @Input var outputRootDirPath: String = File(project.rootProject.rootDir, defaultProductionDirPath).path

    /**
     * {RU}Реестр для установки модулей (используется как под-путь для [outputRootDirPath]){RU}
     */
    @Input var registry = "modules"

    /**
     * {RU}
     * Библиотека в которую необходимо положить модуль после компиляции и сборки (под-путь для [registry])
     * Итоговый путь, по которому будет установлен модуль: [outputRootDirPath]/[registry]/[library]
     * {RU}
     */
    @Input lateinit var library: String

    /**
     * {RU}
     * Получить домашнюю директорию kopycat с помощью команды 'kopycat' из shell.
     * Для этого команда kopycat должна находится в переменной PATH (версия не ниже 0.3.2)
     * {RU}
     */
    private fun getKopycatFromShell(): String? {
        val kopycatShellCmd = Shell("kopycat", "--home").execute()
        if (kopycatShellCmd.status != 0) {
            logger.error(kopycatShellCmd.stderr)
            return null
        }
        return kopycatShellCmd.stdout
    }

    /**
     * {RU}
     * Сформировать зависимость ядра kopycat в виде подпроекта Gradle (работает только, если kopycat
     * предоставлен в виде полных исходных кодов).
     * {RU}
     */
    private fun getKopycatAsProjectDependency(): Project? {
        val kopycat = project.rootProject.subprojects.find { it.name == "kopycat" } ?: return null
        return project.project(":${kopycat.name}")
    }

    /**
     * {RU}
     * Сформировать зависимость ядра kopycat в виде jar-файла.
     * Поиск jar-файла осуществляется в следующем порядке:
     * - kopycat { kopycatHome = ... } в Gradle
     * - KOPYCAT_HOME - переменная среды
     * - kopycat --home - получение домашней директории с помощью команды kopycat
     * {RU}
     */
    private fun getKopycatAsJarDependency(extensions: KopycatExtensions): Any {
        val home = extensions.kopycatHome ?: System.getProperty(kopycatHomeEnvVariable) ?: getKopycatFromShell()
        checkNotNull(home) { "Shell kopycat execution failed, '$kopycatHomeEnvVariable' not set and 'kopycatHome' not set in Kopycat Gradle config..." }
        val kopycatLibDir = File(home, "lib")
        val kopycatJarFiles = kopycatLibDir.list { _, name -> name.startsWith("kopycat") && name.endsWith(jarFileExtension) }
        checkNotNull(kopycatJarFiles) { "Something weird happen when reading kopycat home directory '$home'" }
        check(kopycatJarFiles.isNotEmpty()) { "kopycat jar file not found in kopycat home directory '$home'" }
        val file = kopycatJarFiles.first()
        return project.files("${kopycatLibDir.absolutePath}/$file")
    }

    /**
     * {RU}
     * Сформировать зависимость ядра kopycat в виде проекта Gradle или jar-файла
     * {RU}
     */
    private fun getKopycatDependency(extensions: KopycatExtensions) = if (extensions.useDevelopmentCore)
        getKopycatAsProjectDependency() ?: getKopycatAsJarDependency(extensions)
    else
        getKopycatAsJarDependency(extensions)

    /**
     * {RU}Сформировать список зависимостей для данного модуля, порядок разрешения см. в [require]{RU}
     */
    private fun getModuleDependency(extensions: KopycatExtensions): List<Any> {
        val registry = File(outputRootDirPath, registry).absolutePath
        return if (extensions.useDevelopmentCore) require.map<String, Any> {
            if (it.endsWith(jarFileExtension))
                project.files("$registry/$it")
            else
                project.project(":$kopycatModulesSubproject:$it")
        } else require.map {
            val module = if (it.endsWith(jarFileExtension)) it else it.replace(":", "/") + jarFileExtension
            project.files("$registry/$module")
        }
    }

    private fun addDependencyOnOtherProjectTask(dependency: Project) {
        val me = this@BuildKopycatModuleTask
        if (dependency.plugins.hasPlugin(KopycatPlugin::class.java)) {
            val other = dependency.tasks.getByName(taskIdentifier)
            logger.info("Make $me depends on $other")
            me.dependsOn(other)
        }
    }

    private fun DependencyHandler.addImplementationAndTest(dependency: Any) {
        logger.debug("Adding dependency $dependency")
        add(implementation, dependency)
        // add(testImplementation, dependency)
    }

    /**
     * {RU}Основная функция конфигурации таска плагина{RU}
     */
    fun afterProjectEvaluate(extensions: KopycatExtensions) {
        val jar = project.jarTask

        if (removeJarVersion) {
            logger.info("Version string removed from module '${project.name}' jar filename")
            jar.archiveName = "${jar.baseName}.${jar.extension}"
        }

        val output = File(outputRootDirPath, "$registry/$library")

        if (!output.exists()) {
            logger.lifecycle("Creating directory: '$output'")
            output.mkdirs()
        }

        with(project.repositories) {
            add(mavenCentral())
        }

        with(project.dependencies) {
            if (extensions.addKopycatDependency) {
                val kopycatDependency = getKopycatDependency(extensions)
                addImplementationAndTest(kopycatDependency)
            }

            getModuleDependency(extensions).forEach { addImplementationAndTest(it) }
        }

        project.configurations.getByName(implementation).dependencies
                .filterIsInstance<DefaultProjectDependency>()
                .map { it.dependencyProject }
                .forEach { dependency ->
                    // for those projects that already evaluated
                    addDependencyOnOtherProjectTask(dependency)

                    // for those projects that will be evaluated
                    dependency.afterEvaluate { addDependencyOnOtherProjectTask(it) }
                }

        dependsOn(jar)
        from(jar)
        into(output)
    }
}