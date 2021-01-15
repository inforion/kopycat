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
package ru.inforion.lab403.kopycat.interactive.rest

import ru.inforion.lab403.common.extensions.Krest
import ru.inforion.lab403.kopycat.serializer.MetaInfo


class KopycatClient(host: String, port: Int, retries: Int = 10) {
    private val client = Krest(host, port, "kopycat", retries = retries)

    fun bus(designator: String, name: String, size: String): String =
            client.post("bus", mapOf("name" to name, "size" to size), "designator" to designator)

    fun port(designator: String, name: String, type: String, size: String): String =
            client.post("port", mapOf("name" to name, "type" to type, "size" to size), "designator" to designator)

    fun connect(designator: String, src: String, dst: String, offset: Int = 0): Unit =
            client.post("connect", arrayOf(src, dst, offset), "designator" to designator)

    fun step() = client.post<Boolean>("step")
    fun start() = client.post<Unit>("start")
    fun halt() = client.post<Unit>("halt")
    fun isRunning() = client.get<Boolean>("isRunning")

    fun memLoad(address: Long, size: Int, ss: Int): String =
            client.post("memLoad", mapOf("address" to address, "size" to size, "ss" to ss))

    fun memStore(address: Long, data: String, ss: Int): Unit =
            client.post("memStore", mapOf("address" to address, "data" to data, "ss" to ss))

    fun regRead(index: Int) = client.post<Long>("regRead", mapOf("index" to index))

    fun regWrite(index: Int, value: Long) =
            client.post<Unit>("regWrite", mapOf("index" to index, "value" to value))

    fun pcRead() = client.post<Long>("pcRead")
    fun pcWrite(value: Long) = client.post<Unit>("pcWrite", mapOf("value" to value))

    fun save(name: String, comment: String? = null) = client.post<Boolean>(
            "save", null, "name" to name, "comment" to comment)

    fun load(name: String) = client.post<Boolean>("load", null, "name" to name)
    fun reset() = client.post<Unit>("reset")
    fun close() = client.post<Unit>("close")
    fun exit() = client.post<Unit>("exit")

    fun open(top: String, gdbPort: Int?, gdbBinaryProto: Boolean = false, traceable: Boolean = false ) =
            client.post<Unit>("open", mapOf(
                    "top" to top,
                    "gdbPort" to gdbPort,
                    "gdbBinaryProto" to gdbBinaryProto,
                    "traceable" to traceable)
            )

    fun getSnapshotMetaInfo(path: String) = client.get<MetaInfo>("getSnapshotMetaInfo", "path" to path)
}

