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
package ru.inforion.lab403.kopycat.library.builders.text

import ru.inforion.lab403.kopycat.cores.base.APort
import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError

class ConnectionConfig(items: Collection<String>): ArrayList<Any>(items) {

    /**
     * Required for jackson mapper deserialization
     */
    @Suppress("unused")
    constructor() : this(emptyList())

    fun create(module: Module) {
        val conn1 = moduleTextParser.getConnector(this[0] as String, module)
        val conn2 = moduleTextParser.getConnector(this[1] as String, module)

        val offset = if (size == 3) moduleTextParser.getOffset(this[2]) else 0

        when {
            conn1 is APort && conn2 is Bus -> conn1.connect(conn2, offset)
            conn1 is APort && conn2 is APort -> module.buses.connect(conn1, conn2, offset)
            else -> ConnectionError.raise {
                "Can't connect($conn1, $conn2, $offset)\n" +
                        "Use connection: \n" +
                        "1. Port, Bus, [Offset|def=0]\n" +
                        "2. Port, Port, [Offset|def=0]"
            }
        }
    }
}