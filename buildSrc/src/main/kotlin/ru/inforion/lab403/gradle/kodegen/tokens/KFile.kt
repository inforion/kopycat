package ru.inforion.lab403.gradle.kodegen.tokens

import ru.inforion.lab403.gradle.kodegen.abstracts.FinalToken
import java.io.File

class KFile(val file: File) : FinalToken() {
    override fun render(builder: StringBuilder, indent: String) = builder.apply {
        append(indent)
        append(file.readText())
    }
}