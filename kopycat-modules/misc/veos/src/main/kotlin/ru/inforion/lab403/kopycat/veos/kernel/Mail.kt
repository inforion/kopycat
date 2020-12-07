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
package ru.inforion.lab403.kopycat.veos.kernel

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ISerializable



class Mail(var priority: Int, var data: ByteArray, var sender: Int, var receiver: Int): ISerializable {
    enum class Status { New, Sent, Received, ToDelete }
    var status = Status.New
    override fun toString(): String = "Mail [${data.toString()}] : $status"
    companion object {
        operator fun invoke(ctxt: GenericSerializer, snapshot: Map<String, Any>): Mail {
            val priority = (snapshot["priority"] as String).hexAsUInt
            val data = (snapshot["data"] as String).unhexlify()
            val sender = (snapshot["sender"] as String).hexAsUInt
            val receiver = (snapshot["receiver"] as String).hexAsUInt
            val mail = Mail(priority, data, sender, receiver)
            mail.status = find<Status> { it.ordinal == (snapshot["status"] as String).hexAsUInt }!!
            return mail
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf("priority" to priority.hex,
                "data" to data.hexlify(),
                "sender" to sender.hex,
                "receiver" to receiver.hex,
                "status" to status.ordinal.hex
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        priority = (snapshot["priority"] as String).hexAsUInt
        data = (snapshot["data"] as String).unhexlify()
        sender = (snapshot["sender"] as String).hexAsUInt
        receiver = (snapshot["receiver"] as String).hexAsUInt
        status = find<Status> { it.ordinal == (snapshot["status"] as String).hexAsUInt }!!
    }
}

