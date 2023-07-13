/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.atom2758.e1000.protocols

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.modules.atom2758.e1000.Protocol

internal class Tcp(buffer: List<Byte>) : Protocol() {
    class Flags(override var data: ULong) : IValuable {
        var NS by bit(8)
        var CWR by bit(7)
        var ECE by bit(6)
        var URG by bit(5)
        var ACK by bit(4)
        var PSH by bit(3)
        var RST by bit(2)
        var SYN by bit(1)
        var FIN by bit(0)

        override fun toString() = arrayOf(
            "NS" to NS,
            "CWR" to CWR,
            "ECE" to ECE,
            "URG" to URG,
            "ACK" to ACK,
            "PSH" to PSH,
            "RST" to RST,
            "SYN" to SYN,
            "FIN" to FIN,
        ).filter { it.second.truth }.joinToString(separator = " ") { it.first }
    }

    override fun headerSize() = headerLength shl 2
    override fun fullSize() = throw RuntimeException("Full packet size is already known at this point")

    private val sport = buffer.subList(0, 2).map { it.ubyte }.toUByteArray() //.be()
    private val dport = buffer.subList(2, 4).map { it.ubyte }.toUByteArray() //.be()
    private val headerLength = buffer[12].int_z[7..4]
    val flags = Flags(buffer.subList(12, 14).be())

    val hasData = buffer.size > headerSize()

    override fun toString() = "TCP: ${sport.be()} -> ${dport.be()}, flags: $flags"
}
