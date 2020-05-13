package ru.inforion.lab403.gradle.kodegen.interfaces

import ru.inforion.lab403.gradle.kodegen.annotations.CodegenTokenMarker

@CodegenTokenMarker
interface ICommonToken {
    fun render(builder: StringBuilder, indent: String): StringBuilder
}