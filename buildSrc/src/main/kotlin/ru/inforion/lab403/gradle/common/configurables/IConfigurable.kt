package ru.inforion.lab403.gradle.common.configurables

import groovy.lang.Closure

interface IConfigurable {

//    val properties = HashMap<String, Any?>()

    fun configure(closure: Closure<*>) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure.call()
    }

    // It's a Groovy implicit inheritance (Groovy will see it)
//    fun methodMissing(name: String, value: Any?): Any? { // here we extract the closure from arguments, etc
//        TODO("methodMissing called with name '$name' and args = $args")
//    }

    // It's a Groovy implicit inheritance (Groovy will see it)
//    fun propertyMissing(name: String, value: Any?): Any? { // here we extract the closure from arguments, etc
//        return properties.set(name, value)
//    }
}