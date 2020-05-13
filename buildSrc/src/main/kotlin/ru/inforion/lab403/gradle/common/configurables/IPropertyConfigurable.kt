package ru.inforion.lab403.gradle.common.configurables

interface IPropertyConfigurable : IConfigurable {
    // It's a Groovy implicit inheritance (Groovy will see it)
    // here we extract the closure from arguments, etc
    fun propertyMissing(name: String, value: Any?): Any?
}