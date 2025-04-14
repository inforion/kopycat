plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

val kotlinExtensionsVersion: String by project

dependencies {
    testImplementation("com.github.inforion.common:swarm:$kotlinExtensionsVersion")
}

tasks {
    buildKopycatModule {
        library = "mcu"
        require += listOf("cores:arm", "mcu:cortexm0")
    }
}
