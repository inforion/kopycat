/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.consoles.kotlin.completers

import org.apache.commons.text.StringEscapeUtils
import org.jline.reader.Candidate
import org.jline.reader.ParsedLine
import ru.inforion.lab403.common.extensions.toFile
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.consoles.kotlin.ICustomArgumentCompleter
import ru.inforion.lab403.kopycat.cores.base.classResourcePath
import ru.inforion.lab403.kopycat.library.types.Resource
import kotlin.io.path.div

internal class KopycatRunScriptCompleter : ICustomArgumentCompleter {
    override fun complete(
        line: ParsedLine?,
        kopycat: Kopycat,
    ): Iterable<Candidate> {
        val scriptsInFS = (
            Common.completeRelativeFileList("script file", Kopycat.scriptDir.toFile(), recursive = true) {
                it.extension == "kts"
            } ?: emptySequence()
        ).toList()

        // Is not empty only when loaded as a jar file
        val scriptsInJar = Resource(
            kopycat.top::class.classResourcePath / Kopycat.scriptResourceDir
        ).jarFileListing().filter { jarScriptName ->
            jarScriptName.endsWith(".kts") && scriptsInFS.firstOrNull { it.displ() == jarScriptName } == null
        }.map {
            Candidate(
                "\"${StringEscapeUtils.escapeXSI(it)}\")",
                it,
                "script resource",
                null,
                null,
                null,
                true,
            )
        }

        return scriptsInFS.asIterable() + scriptsInJar.asIterable()
    }
}
