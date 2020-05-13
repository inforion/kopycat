package ru.inforion.lab403.common.proposal

import java.io.File

fun createTimeFile(prefix: String? = null, suffix: String? = null, directory: File? = null): File {
    val realPrefix = prefix ?: System.currentTimeMillis().toString()
    return File.createTempFile(realPrefix, suffix, directory).also { it.deleteOnExit() }
}