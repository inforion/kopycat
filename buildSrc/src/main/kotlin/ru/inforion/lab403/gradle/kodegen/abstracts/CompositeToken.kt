package ru.inforion.lab403.gradle.kodegen.abstracts

import ru.inforion.lab403.gradle.kodegen.Config
import ru.inforion.lab403.gradle.kodegen.interfaces.ICommonToken
import ru.inforion.lab403.gradle.kodegen.tokens.KComment
import ru.inforion.lab403.gradle.kodegen.tokens.KFile
import ru.inforion.lab403.gradle.kodegen.tokens.KNewlines
import ru.inforion.lab403.gradle.kodegen.tokens.KText
import java.io.File

abstract class CompositeToken constructor(
        val config: Config,
        val increaseIndent: Boolean = true
) : ICommonToken {

    private val nextIndent = "%${config.indentSize}s".format("")

    val children = arrayListOf<ICommonToken>()

    protected fun <T: CompositeToken> addToken(token: T, init: T.() -> Unit) = token.also {
        it.init()
        children.add(it)
    }

    protected fun <T: FinalToken> addToken(token: T) = token.also { children.add(it) }

    override fun render(builder: StringBuilder, indent: String) = builder.apply {
        children.forEach {
            val newIdent = if (increaseIndent) "${indent}${nextIndent}" else indent
            it.render(this, newIdent)
            append("\n")
        }
    }

//    operator fun <T: ICommonToken>T.unaryPlus() {
//        this@CompositeToken.children.add(this)
//    }

    override fun toString() = buildString { render(this, "") }

    fun newlines(count: Int) = addToken(KNewlines(count))
    fun comment(comment: String) = addToken(KComment(comment))

    fun text(text: String) = addToken(KText(text))
    fun file(file: File) = addToken(KFile(file))

    operator fun File.unaryPlus() = file(this)
    operator fun String.unaryPlus() = text(this)
    operator fun String.unaryMinus() = comment(this)
}