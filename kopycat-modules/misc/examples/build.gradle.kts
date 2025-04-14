plugins {
    id("ru.inforion.lab403.gradle.kopycat")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

tasks {
    buildKopycatModule {
        library = "misc"
        require += listOf("cores:x86", "cores:arm", "mcu:stm32f0xx", "mcu:virtarm")
    }
}
