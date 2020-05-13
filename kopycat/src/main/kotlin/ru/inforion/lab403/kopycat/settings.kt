package ru.inforion.lab403.kopycat

/**
 * {EN}Settings for build configuration of Kopycat core{EN}
 */
object settings {
    /**
     * {EN}When set to true enables some restriction on emulator connected with usage of host system resources{EN}
     */
    const val hasConstraints = false

    /**
     * {EN}
     * Maximum possible guest RAM size after which exception will be generated during instantiate
     * NOTE: Only make sense when [hasConstraints] is true
     * {EN}
     */
    const val maxPossibleRamSize = 0x1000_0000

    /**
     * {EN}
     * Maximum possible modules in whole guest system
     * NOTE: Only make sense when [hasConstraints] is true
     * {EN}
     */
    const val maxPossibleModules = 0x80

    /**
     * {EN}Enable or not REST API compilation{EN}
     */
    const val enableRestApi = true

    // the following variables used as extensions when plugins loading

    /**
     * {EN}Jar-file plugin extension{EN}
     */
    const val jarFileExt = "jar"

    /**
     * {EN}Zip-file plugin extension{EN}
     */
    const val zipFileExt = "zip"

    /**
     * {EN}Class-file plugin extension{EN}
     */
    const val classFileExt = "class"

    /**
     * {EN}Json-file plugin extension{EN}
     */
    const val jsonFileExt = "json"

    /**
     * {EN}How module parameters (arguments) section name for JSON {EN}
     */
    const val jsonParamsSectionName = "params."

    /**
     * {EN}Name of file for plugins and modules to define which of class export as loadable modules{EN}
     */
    const val exportFilename = "export.txt"

    /**
     * {EN}Where in classpath placed embedded into Kopycat core modules{EN}
     */
    const val internalModulesClasspath = "ru.inforion.lab403.kopycat.modules"

    const val libraryPathSeparator = ":"

    const val librariesSeparator = ","

    /**
     * {EN}Available types in json for variables, parameters and arguments{EN}
     */
    val availableTypes = listOf("String", "char", "int", "long", "float", "double", "boolean", "File", "Resource")
}