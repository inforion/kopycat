import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

@Suppress("unchecked_cast")
fun<T> getExtra(k: String) = if (gradle is ExtensionAware) {
    (gradle as ExtensionAware).extra[k]?.let { it as? T }
} else {
    null
}

buildKopycatModuleConfig {
    copyExternalDependencies = getExtra("copyExternalDependencies") ?: false
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

subprojects {
    if (childProjects.isEmpty()) {
        beforeEvaluate {
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
        }

        val kotlinJvm: String by project
        val kotlinExtensionsVersion: String by project
        val junitVersion: String by project

        afterEvaluate {
            tasks {
                compileKotlin {
                    kotlinOptions.jvmTarget = kotlinJvm
                    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.ExperimentalUnsignedTypes,kotlin.time.ExperimentalTime")
                }

                compileTestKotlin {
                    kotlinOptions.jvmTarget = kotlinJvm
                    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.ExperimentalUnsignedTypes,kotlin.time.ExperimentalTime")
                }

                compileJava {
                    sourceCompatibility = kotlinJvm
                    targetCompatibility = kotlinJvm
                }

                compileTestJava {
                    sourceCompatibility = kotlinJvm
                    targetCompatibility = kotlinJvm
                }
            }

            tasks.withType(Test::class.java).configureEach {
                useJUnitPlatform()
                ignoreFailures = true

                minHeapSize = "2g"
                maxHeapSize = "5g"

                @Suppress("unnecessary_not_null_assertion")
                jvmArgs!!.plusAssign(
                    arrayOf(
                        "-server",
                        "-XX:MaxDirectMemorySize=2g",
                        "-XX:+UseParallelGC",
                        "-XX:SurvivorRatio=6",
                        "-XX:-UseGCOverheadLimit"
                    )
                )

                testLogging {
                    events = setOf(PASSED, SKIPPED, FAILED) //, "standardOut", "standardError"

                    showExceptions = true
                    exceptionFormat = FULL
                    showCauses = true
                    showStackTraces = true

                    // showStandardStreams = false
                }
            }

            tasks.register<Jar>("sourcesJar") {
                dependsOn("classes")
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }

            extensions.findByType<PublishingExtension>()?.let {
                it.publications {
                    create<MavenPublication>(project.name) {
                        groupId = project.group.toString()
                        artifactId = project.name
                        version = project.version.toString()
                        from(components["kotlin"])
                        artifact(tasks["sourcesJar"])
                    }
                }
            }

            dependencies {
                implementation("com.github.inforion.common:logging:$kotlinExtensionsVersion")
                implementation("com.github.inforion.common:extensions:$kotlinExtensionsVersion")
                implementation("com.github.inforion.common:iobuffer:$kotlinExtensionsVersion")
                implementation("com.github.inforion.common:optional:$kotlinExtensionsVersion")

                implementation(kotlin("reflect"))

                // Fixes kts scripts in IntelliJ
                compileOnly(kotlin("script-runtime"))

                testImplementation(kotlin("test"))
                testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
            }
        }
    }
}
