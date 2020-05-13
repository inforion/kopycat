package ru.inforion.lab403.gradle.kodegen.tokens

import ru.inforion.lab403.gradle.kodegen.abstracts.FinalToken

class KComment(val comment: String) : FinalToken() {
    override fun render(builder: StringBuilder, indent: String) = builder.apply {
        append("$indent// $comment")
    }
}