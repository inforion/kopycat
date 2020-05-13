@file:Suppress("UnstableApiUsage")

package ru.inforion.lab403.gradle.kopycat

import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.inforion.lab403.gradle.common.kotlinPluginString

class KopycatPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(kotlinPluginString)

        val extensions = project.extensions.create(
                KopycatExtensions.extensionIdentifier,
                KopycatExtensions::class.java)

        val task = project.tasks.create(
                BuildKopycatModuleTask.taskIdentifier,
                BuildKopycatModuleTask::class.java)

        project.afterEvaluate { task.afterProjectEvaluate(extensions) }
    }
}