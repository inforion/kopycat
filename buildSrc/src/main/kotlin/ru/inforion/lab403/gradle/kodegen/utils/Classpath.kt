package ru.inforion.lab403.gradle.kodegen.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.inforion.lab403.gradle.common.addClasspath
import ru.inforion.lab403.gradle.common.className
import ru.inforion.lab403.gradle.common.classpathToPath
import ru.inforion.lab403.gradle.common.packageName
import java.io.File

data class Classpath(val pkg: String, val name: String) {

    companion object {
        inline fun <reified T: Any>from() = Classpath(T::class.java.canonicalName)
    }

    constructor(classpath: String) : this(classpath.packageName(), classpath.className())

    @JsonIgnore
    val full = pkg.addClasspath(name)

    override fun toString() = full

    fun toURI(suffix: String = "") = full.classpathToPath() + suffix
    fun toFile(baseDir: File, extension: String) = File(baseDir, toURI(extension))
}