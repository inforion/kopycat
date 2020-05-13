package ru.inforion.lab403.kopycat.library.exceptions


class ParsingConstructorException(val param: String,
                                  name: String,
                                  plugin: String,
                                  path: String?,
                                  private val types: Iterable<String>
) : ModuleInstantiateException(name, plugin, path) {
    override fun toString(): String {
        return "Incorrect parameter definition \"$param\" for $name (class: $plugin, $path)." +
                "\nUse only parameter name (for example 'data') or parameter name and type (for example 'data:String')" +
                "\nAvailable types is: ${types.joinToString(separator = ", ")}"
    }
}