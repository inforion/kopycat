package ru.inforion.lab403.gradle.common.configurables

interface IMethodConfigurable : IConfigurable {
    // It's a Groovy implicit inheritance (Groovy will see it)
    fun methodMissing(name: String, value: Any?): Any?
}