plugins {
    id("org.jetbrains.kotlin.jvm").version("1.9.23")
    id("org.gradle.java-gradle-plugin")
}

repositories {
    mavenLocal()

    project.properties["mavenInternalRepositoryUrl"].let { localUrl ->
        if (localUrl == null) {
            project.logger.error("Using mavenCentral repository")
            mavenCentral()
        } else {
            project.logger.error("Using internal URL repository: $localUrl")
            maven {
                url = uri(localUrl as String)
                credentials {
                    username = project.properties["mavenUsername"] as String?
                    password = project.properties["mavenPassword"] as String?
                }
            }
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")

    implementation("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.github.oshi:oshi-core:3.13.0") // Outdated

    implementation("org.reflections:reflections:0.10.2")
    implementation("org.apache.commons:commons-text:1.11.0")

    implementation("com.github.inforion.common:extensions:0.5.0")
    implementation("com.github.inforion.common:gson-json:0.5.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

gradlePlugin {
    plugins {
        register("gradle-build-config-plugin") {
            id = "ru.inforion.lab403.gradle.buildConfig"
            implementationClass = "ru.inforion.lab403.gradle.buildConfig.BuildConfigPlugin"
        }

        register("gradle-version-config-plugin") {
            id = "ru.inforion.lab403.gradle.versionConfig"
            implementationClass = "ru.inforion.lab403.gradle.versionConfig.VersionConfigPlugin"
        }

        register("gradle-dokka-mutltilang-plugin") {
            id = "ru.inforion.lab403.gradle.dokkaMultilang"
            implementationClass = "ru.inforion.lab403.gradle.dokkaMultilang.DokkaMultilangPlugin"
        }

        register("gradle-kopycat-plugin") {
            id = "ru.inforion.lab403.gradle.kopycat"
            implementationClass = "ru.inforion.lab403.gradle.kopycat.KopycatPlugin"
        }


    }
}



kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
