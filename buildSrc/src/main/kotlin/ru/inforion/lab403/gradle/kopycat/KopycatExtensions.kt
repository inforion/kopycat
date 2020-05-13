package ru.inforion.lab403.gradle.kopycat

open class KopycatExtensions {
    companion object {
        const val extensionIdentifier = "kopycat"
    }

    var useDevelopmentCore = true

    var addKopycatDependency = true
    var addKotlinStdlibDependency = true
    var addExtensionsDependency = true
    var addLoggingDependency = true

    var kopycatHome: String? = null
}