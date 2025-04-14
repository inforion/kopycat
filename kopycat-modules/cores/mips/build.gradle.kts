plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

val kotlinExtensionsVersion: String by project

dependencies {
    testImplementation("com.github.inforion.common:utils:$kotlinExtensionsVersion")
}

tasks {
    buildKopycatModule {
        library = "cores"
    }
}
