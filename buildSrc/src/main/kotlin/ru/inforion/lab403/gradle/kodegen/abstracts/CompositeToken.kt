/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
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