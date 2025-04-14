import org.jetbrains.dokka.gradle.DokkaTask
import ru.inforion.lab403.gradle.dokkaMultilang.Language

plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
    id("idea")
    id("ru.inforion.lab403.gradle.versionConfig")
    id("ru.inforion.lab403.gradle.dokkaMultilang")
    id("com.dorongold.task-tree").version("3.0.0") // taskTree to see task dependencies and call graph
    id("me.champeau.jmh").version("0.7.2")
}

base.archivesName.set("kopycat")
version = "0.10.00"
group = "ru.inforion.lab403"
description = "Hardware systems Kotlin emulator"

// ---------------------------------------------------------------------------------------------------------------------

val kotlinExtensionsVersion: String by project
val javalinVersion: String by project
val commonsTextVersion: String by project
val reflectionsVersion: String by project
val jnaVersion: String by project

dependencies {
    // Embedded Kotlin console
    implementation(kotlin("scripting-jsr223"))

    implementation("com.github.inforion.common:argparse:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:logging:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:reflection:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:scanner:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:utils:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:wsrpc:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:javalin:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:krest:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:gson-json:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:network:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:intervalmap:$kotlinExtensionsVersion")

    implementation("io.javalin:javalin:$javalinVersion")

    implementation("org.apache.commons:commons-text:$commonsTextVersion")

    // Module libraries and registry runtime-loader; needed when launching from IJ
    implementation("org.reflections:reflections:$reflectionsVersion")

    // TODO: update JLine and remove JNA dependency once IJ console is fixed
    implementation("com.github.inforion.common:jline:$kotlinExtensionsVersion")
    implementation("net.java.dev.jna:jna:$jnaVersion")
}

// ---------------------------------------------------------------------------------------------------------------------

val dokkaRU = tasks.register<DokkaTask>("dokkaRU") {
    outputDirectory.set(file("${projectDir}/doc/RU"))
    moduleName.set("kopycat")

    dokkaSourceSets {
        configureEach {
            includeNonPublic.set(false)
            skipDeprecated.set(false)
            skipEmptyPackages.set(true)
            platform.set(org.jetbrains.dokka.Platform.jvm)
            noStdlibLink.set(false)
        }
    }
}

val dokkaEN = tasks.register<DokkaTask>("dokkaEN") {
    outputDirectory.set(file("${projectDir}/doc/EN"))
    moduleName.set("kopycat")

    dokkaSourceSets {
        configureEach {
            includeNonPublic.set(false)
            skipDeprecated.set(false)
            skipEmptyPackages.set(true)
            platform.set(org.jetbrains.dokka.Platform.jvm)
            noStdlibLink.set(false)
        }
    }
}

jmh {
    zip64.set(true)
}

tasks {
    dokkaMultilang {
        targets += "java"
        excludes += "ru/inforion/lab403/kopycat/loader"

        val languages: NamedDomainObjectContainer<Language> by extensions

        languages {
            register("english") {
                marker = "EN"
                task = dokkaEN
            }
            register("russian") {
                marker = "RU"
                task = dokkaRU
            }
        }
    }

    val kotlinJvm: String by project

    compileJmhKotlin {
        kotlinOptions.jvmTarget = kotlinJvm
    }

    compileJmhJava {
        sourceCompatibility = kotlinJvm
        targetCompatibility = kotlinJvm
    }

    buildKopycatModule {
        independent = true
        require += listOf("independent:common:proposal")
    }
}
