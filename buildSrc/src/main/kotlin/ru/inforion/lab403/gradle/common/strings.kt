package ru.inforion.lab403.gradle.common

import java.util.*

private val undefined = UUID.randomUUID().toString()

fun String.classpathToPath() = replace(".", "/")
fun String.className() = substringAfterLast(".")
fun String.packageName() = substringBeforeLast(".")
fun String.addClasspath(string: String) = "$this.$string"

fun String.Companion.undefined() = undefined
fun String.isUndefined() = this == undefined
fun String.nullIfUndefined() = if (isUndefined()) null else this
