package ru.inforion.lab403.gradle.buildConfig

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.inforion.lab403.gradle.common.*
import java.io.File


open class BuildConfigTask : DefaultTask() {

    companion object {
        const val taskIdentifier = "buildConfig"
    }

    @Input var generatedPath = "generated"
    @Input lateinit var configObject: String

    private val baseDir get() = File(temporaryDir, generatedPath)

    fun afterProjectEvaluate() {
        buildConfig()
        project.addKotlinSourceDir(baseDir)

        project.compileKotlinTask.dependsOn(this)
        project.cleanTaskOrNull?.delete(baseDir)
    }

    @TaskAction
    fun buildConfig() {
        if (BuildConfig.makeConfigSources(baseDir, configObject, extraProperties)) {
            project.compileKotlinTask.rebuildRequired()
        }
    }
}