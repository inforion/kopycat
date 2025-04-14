plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

val kotlinExtensionsVersion: String by project

dependencies {
    testImplementation(files("../tests/unicorn_d4b92485.jar"))
    testImplementation("com.github.inforion.common:math:$kotlinExtensionsVersion")
}

tasks {
    buildKopycatModule {
        library = "cores"
    }
}
