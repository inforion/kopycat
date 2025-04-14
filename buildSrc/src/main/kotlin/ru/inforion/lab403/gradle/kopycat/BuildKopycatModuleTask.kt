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

import org.gradle.api.artifacts.*
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.artifacts.dsl.FileSystemPublishArtifact
import org.gradle.api.internal.file.AbstractFileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.jvm.tasks.Jar
import ru.inforion.lab403.gradle.buildConfig.BuildConfigTask
import ru.inforion.lab403.gradle.common.implementation
import ru.inforion.lab403.gradle.common.jarTask
import java.io.File

open class BuildKopycatModuleTask : Copy() {
    companion object {
        const val TASK_ID = "buildKopycatModule"
        private const val KOPYCAT_MODULES_SUBPROJECT = ":kopycat-modules"
        private const val INDEPENDENT_LIBRARY_NAME = "independent"
        private const val DEFAULT_PRODUCTION_DIR_PATH = "production"
    }

    override fun getGroup() = "kopycat"

    /**
     * {RU}Если задана в true, то версия у jar-файла модуля будет удалена{RU}
     */
    @Input var removeJarVersion = true

    /**
     * {RU}
     * Список зависимостей от модулей Kopycat. Зависимости могут быть указаны в виде подпути относительно проекта
     * :kopycat-modules, например ```require += cores:x86```. Независимые от ядра kopycat модули начинаются с
     * `independent:` и ищутся относительно корня проекта kopycat.
     * {RU}
     */
    @Input var require = mutableListOf<String>()

    /**
     * {RU}
     * `true` если модуль независимый. Зависимые модули имеют неявную зависимость от ядра kopycat.
     * Для избежания циклических зависимостей, само ядро kopycat - независимое. Независимые модули
     * попадают в библиотеку `independent`.
     * {RU}
     */
    @Input var independent: Boolean = false

    /**
     * {RU}
     * Реестр для установки модулей
     * (используется как под-путь для [BuildKopycatModuleConfigExtension.productionDirPath])
     * {RU}
     */
    @Input var registry = "modules"

    /**
     * {RU}
     * Библиотека в которую необходимо положить модуль после компиляции и сборки.
     * Итоговый путь, по которому будет установлен модуль:
     * [BuildKopycatModuleConfigExtension.productionDirPath]/[registry]/[library]
     * {RU}
     */
    @Input @Optional var library: String? = null

    private val buildKopycatModuleConfigExtension by lazy {
        project
            .rootProject
            .extensions
            .findByName(BuildKopycatModuleConfigExtension.EXT_ID) as? BuildKopycatModuleConfigExtension
    }

    /** {RU}[BuildKopycatModuleConfigExtension.copyExternalDependencies] (если задан) или `false`{RU} */
    private val copyExternalDependencies by lazy {
        buildKopycatModuleConfigExtension?.copyExternalDependencies ?: false
    }

    /** {RU}[BuildKopycatModuleConfigExtension.productionDirPath] (если задан) или путь по умолчанию{RU} */
    private val productionDirPath by lazy {
        buildKopycatModuleConfigExtension?.productionDirPath ?: File(
            project.rootProject.rootDir,
            DEFAULT_PRODUCTION_DIR_PATH,
        )
    }

    /** {RU}
     * Путь [productionDirPath]/[registry]/[library] или
     * [productionDirPath]/[INDEPENDENT_LIBRARY_NAME] (если [independent])
     * {RU} */
    private val libraryDirPath by lazy {
        if (independent) {
            File(productionDirPath, INDEPENDENT_LIBRARY_NAME)
        } else {
            library?.let {
                File(File(productionDirPath, registry), it)
            } ?: throw IllegalArgumentException("Expected library != null")
        }
    }

    /** {RU}Путь до выходного jar-файла{RU} */
    private val outputJarPath by lazy { File(libraryDirPath, project.jarTask.makeJarName()) }

    /** {RU}Файлы внешних зависимостей{RU} */
    @Internal var externalDependencies: SetProperty<File> = project.objects.setProperty(File::class.java)

    /** {RU}Если [removeJarVersion] = `true`, то удаляет версию у jar-файла{RU} */
    private fun Jar.makeJarName() = if (removeJarVersion) {
        "${archiveBaseName.get()}.${archiveExtension.get()}"
    } else {
        archiveFileName.get()
    }

    private fun setupJarTask() {
        val jar = project.jarTask
        jar.archiveFileName.set(jar.makeJarName())

        dependsOn(jar)
        from(jar)
        into(libraryDirPath)

        logger.info("${project.name}: copy ${jar.archiveFileName.get()} to $libraryDirPath")

        doFirst {
            if (!libraryDirPath.exists()) {
                logger.lifecycle("Creating directory: '$libraryDirPath'")
                libraryDirPath.mkdirs()
            }
        }
    }

    private fun addDependencies() = with (project.dependencies) {
        if (independent) {
            require
        } else {
            // Add kopycat dependency
            listOf("$INDEPENDENT_LIBRARY_NAME:kopycat") + require
        }.map { dependency ->
            if (dependency.startsWith("$INDEPENDENT_LIBRARY_NAME:")) {
                dependency.substring(INDEPENDENT_LIBRARY_NAME.length)
            } else {
                "$KOPYCAT_MODULES_SUBPROJECT:$dependency"
            }
        }.forEach { projectName ->
            logger.info("${project.name}: depending on $projectName")
            dependsOn("$projectName:$TASK_ID") // Depend on module build task
            add(implementation, project.project(projectName))
        }
    }

    private val nonTestSourceSets get() = project
        .extensions
        .getByType(JavaPluginExtension::class.java)
        .sourceSets
        .filter { it.name != "test" }

    private fun isSubpath(parent: File, child: File) = !parent.toURI().relativize(child.toURI()).isAbsolute

    private fun populateExternalDependencies(runtimeClasspath: FileCollection): FileCollection =
        if (copyExternalDependencies) {
            val external = runtimeClasspath.filter { !isSubpath(project.rootProject.rootDir, it) }
            externalDependencies.addAll(external)

            val externalLibrary = File(productionDirPath, CopyExternalDeps.EXTERNAL_LIBRARY_NAME)
            (runtimeClasspath as AbstractFileCollection).lazyMap { f ->
                if (!isSubpath(project.rootProject.rootDir, f)) {
                    File(externalLibrary, f.name)
                } else {
                    f
                }
            }
        } else {
            runtimeClasspath
        }

    private fun modifyOutputAndClasspath() {
        if (copyExternalDependencies && project.rootProject.tasks.findByName(CopyExternalDeps.TASK_ID) == null) {
            CopyExternalDeps.createTask(project.rootProject, productionDirPath).also {
                finalizedBy(it)
            }
        }

        project.gradle.taskGraph.whenReady { graph ->
            if (!graph.allTasks.any { it is BuildConfigTask }) {
                nonTestSourceSets.forEach { sourceSet ->
                    populateExternalDependencies(sourceSet.runtimeClasspath)
                }
                return@whenReady
            }

            val jarTaskArtifact = project.jarTask.outputs.files.files.first()

            project.configurations
                .stream()
                .forEach { conf ->
                    conf.artifacts.removeIf {
                        it.file == jarTaskArtifact
                    }
                }

            val artifact = FileSystemPublishArtifact({ outputJarPath }, project.version.toString())
            project.artifacts.add(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME, artifact)
            project.artifacts.add(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME, artifact)
            project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, artifact)

            nonTestSourceSets.forEach { sourceSet ->
                val classDirs = sourceSet.output.files.toSet()
                sourceSet.runtimeClasspath = populateExternalDependencies(
                    sourceSet.runtimeClasspath.filter { it !in classDirs }
                ) + project.files(outputJarPath)
            }
        }
    }

    fun afterProjectEvaluate() {
        setupJarTask()
        addDependencies()
        modifyOutputAndClasspath()
    }
}
