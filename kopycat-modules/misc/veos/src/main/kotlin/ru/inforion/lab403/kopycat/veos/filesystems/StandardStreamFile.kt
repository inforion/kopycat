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
package ru.inforion.lab403.kopycat.veos.filesystems

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.PrintStream


class StandardStreamFile() : StreamFile(), Externalizable {
    lateinit var stdio: STDIO

    constructor(stdio: STDIO) : this() {
        this.stdio = stdio
        stream = stdio.toStream()
    }

    companion object {
        val stdin get() = StandardStreamFile(STDIO.IN)
        val stdout get() = StandardStreamFile(STDIO.OUT)
        val stderr get() = StandardStreamFile(STDIO.ERR)
    }

    enum class STDIO {
        IN, OUT, ERR;

        fun toStream(): PrintStream = when (this) {
            IN -> System.out // TODO: input can't be an output
            OUT -> System.out
            ERR -> System.err
        }
    }

    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(stdio)
    }

    override fun readExternal(`in`: ObjectInput) {
        stdio = `in`.readObject() as STDIO
        stream = stdio.toStream()
    }

    override fun serialize(ctxt: GenericSerializer) = mapOf("stdio" to stdio.name)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        stdio = STDIO.values().first { it.name == snapshot["stdio"] as String }
        stream = stdio.toStream()
    }
}

