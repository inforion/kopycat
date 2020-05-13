package ru.inforion.lab403.kopycat.consoles.jep

import ru.inforion.lab403.common.extensions.walk
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.DynamicClassLoader
import ru.inforion.lab403.common.proposal.toFile
import java.io.File
import java.lang.RuntimeException

object JepLoader {
    private val log = logger()

    private fun findFileInPath(folder: File, description: String, depth: Int = 0, predicate: (File) -> Boolean): File {
        val files = walk(folder, depth = depth).filter { it.isFile }.filter(predicate)

        if (files.isEmpty())
            throw RuntimeException("Can't find Jep $description file!")

        if (files.size > 1) {
            val str = files.joinToString("\n") { "- '${it.absolutePath}'" }
            throw RuntimeException("Found more than one $description file:\n${str}")
        }

        val jepSharedLibrary = files.first()

        log.config { "Jep $description file: ${jepSharedLibrary.absolutePath}" }

        return jepSharedLibrary
    }

    private fun getJepJarFile(folder: File) = findFileInPath(folder, "jar") {
        it.name.startsWith("jep-") && it.extension == "jar"
    }

    private fun getJepSharedLibrary(folder: File) = findFileInPath(folder, "shared library") {
        it.name.startsWith("jep.") && it.extension in setOf("dll", "so", "dylib")
    }

    private fun setDynamicLibraryPath(jepSharedFile: File) {
        val jepSharedPath = jepSharedFile.absolutePath

        val jepMainInterpreter = DynamicClassLoader.loadClass("jep.MainInterpreter")

        // get method didn't work here...
        val jepMethodSetJepLibraryPath = jepMainInterpreter.methods.first { it.name == "setJepLibraryPath" }

        jepMethodSetJepLibraryPath.invoke(jepMainInterpreter, jepSharedPath)
    }

    private var isJepLoaded = false

    fun load(python: String) {
        if (!isJepLoaded) {
            log.config { "Loading Jep using Python command '$python' to overwrite use '--python' option" }

            val folder = PythonShell(python).getJepFolderPath().toFile()

            val jar = getJepJarFile(folder)
            val shared = getJepSharedLibrary(folder)

            // Jep folder required to be loaded due to python jep library *.so into java.library.path
            // DynamicClassLoader.addLibraryPath(folder)

            // Now loading the jar file into class path
            DynamicClassLoader.loadIntoClasspath(jar)

            // set right path to jep shared library for main interpreter
            setDynamicLibraryPath(shared)
        }
    }
}