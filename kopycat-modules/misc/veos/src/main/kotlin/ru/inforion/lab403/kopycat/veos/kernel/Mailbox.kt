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

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import java.util.*
import java.util.logging.Level



// MailReceiver - Task + pData
data class MailReceiver(val process: Process, val pData: Long)


class Mailbox(var name: String): ISerializable {
    companion object {
        @Transient val log = logger(Level.FINER)
    }

    val mails = LinkedList<Mail>()
    val semaphore = Semaphore()

    fun addMail(priority: Int, data: ByteArray, sender: Int, receiver: Int) {
        val mail = Mail(priority, data, sender, receiver)
        addMail(mail)
    }

    fun addMail(mail: Mail) {
        mail.status = Mail.Status.Sent
        mails.add(mail)
        mails.sortBy { it.priority }
    }

    fun getMail(priority: Int = 0): Mail? {
        return if (mails.size > 0) {
            val mail = mails.first { it.priority >= priority }
            mail.status = Mail.Status.Received
            mails.remove(mail)
            mail
        } else null
    }

    override fun toString(): String = "mBox `$name` [nMails=${mails.size}] "

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf("name" to name,
                "mails" to mails.map { mail -> mail.serialize(ctxt) }.toList()
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        name = (snapshot["name"] as String)
        (snapshot["mails"] as LinkedList<Mail>).forEach { mail -> mails.add(mail) }
    }
}