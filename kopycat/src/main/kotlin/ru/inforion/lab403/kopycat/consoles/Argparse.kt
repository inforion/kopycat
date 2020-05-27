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
package ru.inforion.lab403.kopycat.consoles

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.argparser
import ru.inforion.lab403.common.extensions.parse
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.interfaces.IInteractive

class Argparse(val kopycat: Kopycat) : AConsole("Argparse") {
    private lateinit var parser: ArgumentParser

    override fun onInitialize(): Boolean = reconfigure()

    override fun onReconfigure(): Boolean {
        parser = argparser("kopycat").apply { kopycat.configure(this) }
        return true
    }

    override fun onEval(statement: String): Boolean {
        val args = statement.split(" ")
        val options = parser.parse(args)
        if (options != null) {
            val context = IInteractive.Context(options)
            return kopycat.process(context)
        }
        return false
    }

    override fun onExecute(statement: String): Result {
        val args = statement.split(" ")
        val options = parser.parse(args)
        if (options != null) {
            val context = IInteractive.Context(options)
            val isOk = kopycat.process(context)
            return Result(if (isOk) 0 else -1, context.result)
        }
        return Result(-1, null)
    }

    override val working get() = kopycat.working
}