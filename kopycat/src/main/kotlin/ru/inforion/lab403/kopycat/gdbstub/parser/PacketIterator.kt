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
package ru.inforion.lab403.kopycat.gdbstub.parser

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.gdbstub.*
import java.io.InputStream

internal class PacketIterator(val stream: InputStream) : Iterator<Packet> {
    companion object {
        private val oneByteMessages = setOf(interruptChar, acknowledgeChar, rejectChar)

        fun InputStream.toPacketIterator() = PacketIterator(this)

        fun InputStream.packetSequence() = toPacketIterator().asSequence()
    }

    private var packet: Packet? = null

    private fun nextOrNull(): Packet? {
        val header = stream.readWhileOrNull { it !in oneByteMessages && it != packetStartChar } ?: return null
        return when (val first = header[0].int_z) {
            in oneByteMessages -> Packet.service(first.char)
            else -> {
                val body = stream.readWhileOrNull(false) { it != packetCrcChar } ?: return null
                val crc = stream.readNBytes(2).string
                Packet.message(body.string, crc.uintByHex.int)
            }
        }
    }

    override fun hasNext() = nextOrNull().also { packet = it } != null

    override fun next(): Packet {
        val next = packet
        return next?.also { packet = null } ?: nextOrNull() ?: throw NoSuchElementException()
    }
}
