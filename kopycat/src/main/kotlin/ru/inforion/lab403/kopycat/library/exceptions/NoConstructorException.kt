package ru.inforion.lab403.kopycat.library.exceptions

class NoConstructorException(name: String,
                             plugin: String,
                             path: String?,
                             private val constructors: String
) : ModuleInstantiateException(name, plugin, path) {
    override fun toString(): String {
        return "No correct constructor for $name (class: $plugin, $path)." +
                "\nUse one of the following constructors:" +
                "\n$constructors"
    }
}