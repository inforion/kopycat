plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
}

version = "1.1.4"
group = "ru.inforion.lab403"

tasks {
    buildKopycatModule {
        independent = true
    }
}
