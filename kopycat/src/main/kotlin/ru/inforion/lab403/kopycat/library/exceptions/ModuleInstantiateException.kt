package ru.inforion.lab403.kopycat.library.exceptions

open class ModuleInstantiateException(val name: String, val plugin: String, val path: String?) : Exception()