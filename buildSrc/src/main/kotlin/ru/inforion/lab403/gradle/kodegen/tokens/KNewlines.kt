package ru.inforion.lab403.gradle.kodegen.tokens

import ru.inforion.lab403.gradle.kodegen.abstracts.FinalToken

class KNewlines(val count: Int) : FinalToken() {
    override fun render(builder: StringBuilder, indent: String) = builder.apply {
        repeat(count - 1) { append("\n") }
    }
}