package ru.inforion.lab403.kopycat.library.types

import ru.inforion.lab403.common.proposal.DynamicClassLoader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.zip.GZIPInputStream

class Resource(val path: String) {
    private fun openResourceStream(resource: String): InputStream {
        // WARNING: don't change -> with function not working
        val stream = DynamicClassLoader.getResourceAsStream(resource)
        if (stream == null) {
            val basepath = DynamicClassLoader.getResource("")
            throw FileNotFoundException("Can't open resource $resource within path $basepath")
        }
        return stream
    }

    fun exists(): Boolean {
        val stream = DynamicClassLoader.getResourceAsStream(path)
        return stream == null
    }

    fun inputStream(): InputStream {
        val stream = openResourceStream(path)
        return if (File(path).extension == "gz") GZIPInputStream(stream) else stream
    }

    fun readBytes(): ByteArray = inputStream().readBytes()
}