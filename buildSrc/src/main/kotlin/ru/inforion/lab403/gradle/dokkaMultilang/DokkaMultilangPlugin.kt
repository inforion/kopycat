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