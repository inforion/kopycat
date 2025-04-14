plugins {
    id("ru.inforion.lab403.gradle.kopycat")
    id("maven-publish")
}

version = "0.1.0"
group = "ru.inforion.lab403"

val kotlinExtensionsVersion: String by project

dependencies {
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    compileOnly(kotlin("compiler-embeddable"))
    implementation("com.github.inforion.common:utils:$kotlinExtensionsVersion")
}

tasks {
    buildKopycatModule {
        independent = true
    }
}
