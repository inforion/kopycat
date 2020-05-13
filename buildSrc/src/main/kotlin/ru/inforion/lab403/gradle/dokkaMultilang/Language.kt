package ru.inforion.lab403.gradle.dokkaMultilang

import org.jetbrains.dokka.gradle.DokkaTask

class Language(val name: String) {
    lateinit var marker: String
    lateinit var task: DokkaTask

    override fun toString() = "Language(name=$name, marker=$marker, task=$task)"
}