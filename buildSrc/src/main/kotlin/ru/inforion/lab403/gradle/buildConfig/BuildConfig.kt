package ru.inforion.lab403.gradle.buildConfig

//import ru.inforion.lab403.common.logging
import ru.inforion.lab403.gradle.common.className
import ru.inforion.lab403.gradle.common.kotlinFileExtension
import ru.inforion.lab403.gradle.common.packageName
import ru.inforion.lab403.gradle.kodegen.Kodegen
import java.io.File

internal object BuildConfig {
//    val log = logger(Level.INFO)

    internal fun generateBuildConfig(configObject: String, properties: Map<String, Any>) = Kodegen {
        pkg(configObject.packageName()) {
            obj(configObject.className()) {
                properties.forEach { (name, value) ->
//                    log.debug("Setup $name: ${value.javaClass.simpleName} = $value")
                    constval(name, value)
                }
            }
        }
    }

    private fun createDirIfNotExists(dir: File) {
        if (!dir.exists()) {
//            log.lifecycle("Creating directory: '$dir'")
            dir.mkdirs()
        }
    }

    fun makeConfigSources(baseDir: File, configObject: String, properties: Map<String, Any>): Boolean {
        val newSourceFile = File(baseDir, configObject.replace(".", "/") + kotlinFileExtension)

        val output = generateBuildConfig(configObject, properties).toString()

        if (newSourceFile.exists() && newSourceFile.readText() == output) {
//            log.info("Nothing changed in '$configObject'")
            return false
        }

        createDirIfNotExists(newSourceFile.parentFile)

//        log.lifecycle("Writing config to '${newSourceFile.path}'\n$output")
        newSourceFile.writeText(output)

        return true
    }
}