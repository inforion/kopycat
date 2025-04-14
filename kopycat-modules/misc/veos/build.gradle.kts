import groovy.lang.Closure
import ru.inforion.lab403.gradle.buildConfig.BuildConfigData

plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
    id("ru.inforion.lab403.gradle.buildConfig")
}

group = "ru.inforion.lab403.kopycat"
version = "0.1"

val kotlinExtensionsVersion: String by project
val javaGetoptVersion: String by project

dependencies {
    implementation("com.github.inforion.common:concurrent:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:utils:$kotlinExtensionsVersion")
    implementation("com.github.inforion.common:kryo:$kotlinExtensionsVersion")

    implementation("gnu.getopt:java-getopt:$javaGetoptVersion")

    testImplementation("com.github.inforion.common:swarm:$kotlinExtensionsVersion")
}

tasks {
    buildKopycatModule {
        library = "misc"
        require += listOf("independent:common:elfloader", "cores:arm", "cores:mips", "cores:x86", "cores:ppc")
    }
    createKopycatConfig {
        kcFullTopClass = "ru.inforion.lab403.kopycat.modules.veos.ARMApplication"
        kcLibraryDirectory = "devices"

        @Suppress("unchecked_cast")
        addConfig(delegateClosureOf<BuildConfigData> {
            name = "arm"
            description = "ARM VEOS application"
            kcConstructorArguments = hashMapOf()
            kcArguments = hashMapOf(
                "-w" to defaults.tempDir(),
            )
            withKotlinConsole = false
            withDefaultArguments = false
        } as Closure<BuildConfigData>)

        @Suppress("unchecked_cast")
        addConfig(delegateClosureOf<BuildConfigData> {
            name = "mips"
            fullTopClass = "ru.inforion.lab403.kopycat.modules.veos.MIPSApplication"
            description = "MIPS VEOS application"
            kcConstructorArguments = hashMapOf()
            kcArguments = hashMapOf(
                "-w" to defaults.tempDir(),
            )
            withKotlinConsole = false
            withDefaultArguments = false
        } as Closure<BuildConfigData>)
    }
}
