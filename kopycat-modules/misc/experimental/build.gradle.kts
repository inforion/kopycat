plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":kopycat-modules:cores:x86"))
    compileOnly(project(":kopycat-modules:cores:arm"))
    compileOnly(project(":kopycat-modules:cores:mips"))
}

tasks {
    buildKopycatModule {
        library = "misc"
    }
}
