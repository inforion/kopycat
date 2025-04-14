plugins {
    id("ru.inforion.lab403.gradle.kopycat")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

tasks {
    buildKopycatModule {
        library = "mcu"
        require += "cores:ppc"
    }
}
