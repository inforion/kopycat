package ru.inforion.lab403.common.proposal

import java.io.File

operator fun String.times(n: Int) = repeat(n)

fun String.toFile() = File(this)

fun String.splitTrim(regex: Regex) = split(regex).map { it.trim() }

fun String.splitTrim(vararg delimiters: String, ignoreCase: Boolean = false) =
        split(*delimiters, ignoreCase = ignoreCase).map { it.trim() }

fun String.splitWhitespaces() = splitTrim(Regex("\\s+"))

fun String.substringBetween(start: String, end: String): String {
    val startIndex = indexOf(start)
    require(startIndex >= 0)
    val endIndex = indexOf(end)
    require(endIndex >= 0)
    return substring(startIndex + 1, endIndex)
}