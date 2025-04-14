plugins {
    id("ru.inforion.lab403.gradle.kopycat")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

val jodaTimeVersion: String by project

dependencies {
    implementation("joda-time:joda-time:$jodaTimeVersion")
}

tasks {
    buildKopycatModule {
        library = "mcu"
        require += "cores:x86"
    }
}
