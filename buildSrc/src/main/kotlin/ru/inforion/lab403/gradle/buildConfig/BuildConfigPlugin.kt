@file:Suppress("UnstableApiUsage")

package ru.inforion.lab403.gradle.buildConfig

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.inforion.lab403.gradle.common.kotlinPluginString


class BuildConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(kotlinPluginString))
            throw GradleException("Plugin $kotlinPluginString must be applied!")

        val task = project.tasks.create(BuildConfigTask.taskIdentifier, BuildConfigTask::class.java)
        project.afterEvaluate { task.afterProjectEvaluate() }
    }
}