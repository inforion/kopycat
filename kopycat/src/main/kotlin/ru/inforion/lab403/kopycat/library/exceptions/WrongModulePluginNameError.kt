package ru.inforion.lab403.kopycat.library.exceptions

class WrongModulePluginNameError(pluginName: String) : Exception("Wrong module plugin name for this builder $pluginName")