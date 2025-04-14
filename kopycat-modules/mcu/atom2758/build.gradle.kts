plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

tasks {
    buildKopycatModule {
        library = "mcu"
        require += listOf("cores:x86", "misc:experimental")
    }
}
