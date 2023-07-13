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

import ru.inforion.lab403.kopycat.modules.atom2758.e1000.Protocol

internal class Udp(buffer: List<Byte>) : Protocol() {
    override fun headerSize() = 8
    override fun fullSize() = throw RuntimeException("Full packet size is already known at this point")

    // private val sport = buffer.subList(0, 2).map { it.ubyte }.toUByteArray() //.be()
    // private val dport = buffer.subList(2, 4).map { it.ubyte }.toUByteArray() //.be()
    val length = buffer.subList(4, 6).be()
    val checksum = buffer.subList(6, 8).be()

    override fun toString() = "UDP"
}
