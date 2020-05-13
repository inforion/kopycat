package ru.inforion.lab403.kopycat.consoles.jep

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.Shell
import ru.inforion.lab403.common.proposal.splitWhitespaces
import java.lang.RuntimeException

class PythonShell(command: String) {
    companion object {
        private val log = logger()
    }

    // split to make possible get command like py -3.8
    val command = command.splitWhitespaces().toTypedArray()

    data class Version(val major: Int, val minor: Int, val micro: Int)

    fun version(): Version? {
        val shell = Shell(*command, "--version").execute()
        if (shell.status != 0) return null

        val output = when {
            shell.stdout.startsWith("Python") -> shell.stdout
            shell.stderr.startsWith("Python") -> shell.stderr
            else -> {
                log.severe { "Incredible, where is Python output?..." }
                return null
            }
        }

        val tokens = output.removePrefix("Python").trim().split(".")

        val version = Version(tokens[0].toInt(), tokens[1].toInt(), tokens[2].toInt())

        log.config { "Python $version" }

        return version
    }

    private val moduleNotFoundErrorSign = "ModuleNotFoundError:  No module named 'jep'"
    private val jepSign = "ImportError: Jep is not supported in standalone Python, it must be embedded in Java."

    fun isJepInstalled(): Boolean {
        val shell = Shell(*command, "-c", "import jep").execute()

        if (moduleNotFoundErrorSign in shell.stderr)
            return false

        if (jepSign !in shell.stderr)
            return false

        return true
    }

    // returns path without __init__.py
    fun jepPathPython2() = Shell(*command, "-c", "import pkgutil; print pkgutil.get_loader('jep').filename").execute()

    // return path with __init__.py ... uh-huh it's funny
    fun jepPathPython3() = Shell(*command, "-c", "import pkgutil; print(pkgutil.get_loader('jep').path)").execute()

    fun getJepFolderPath() : String {
        val version = version() ?: throw RuntimeException("Python not found!")

        if (!isJepInstalled()) throw RuntimeException("Jep not installed!")

        val shell = when (version.major) {
            2 -> jepPathPython2()
            3 -> jepPathPython3()
            else -> throw RuntimeException("Your Python version is ${version.major}... ¯\\_(ツ)_/¯")
        }

        return shell.stdout.removeSuffix("__init__.py")
    }
}