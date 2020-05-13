package ru.inforion.lab403.kopycat.library.exceptions

class AmbiguousConstructorException(name: String,
                                    plugin: String,
                                    path: String?,
                                    private val constructors: String
) : ModuleInstantiateException(name, plugin, path) {
    override fun toString(): String {
        return "Ambiguous constructor definition for $name (class: $plugin, $path)." +
                "\n$constructors" +
                "\nBelow are the available options. " +
                "\nPlease specify one of these using the type definition."
    }
}