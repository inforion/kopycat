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
package ru.inforion.lab403.kopycat.veos.ports.qnx

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.getInt32
import ru.inforion.lab403.common.extensions.putInt32


data class _thread_local_storage(
        val __exitfunc: Long,
        val __arg: Long,
        val __errptr: Long,
        val __errval: Long = 0,
        val __flags: Long = 0,
        val __pid: Long = 0,
        val __tid: Long = 0,
        val __owner: Long = 0,
        val __stackaddr: Long = 0,
        val __reserved1: Long = 0,
        val __numkeys: Long = 0,
        val __keydata: Long = 0,
        val __cleanup: Long = 0,
        val __fpuemu_data: Long = 0,
        val __reserved2_0: Long = 0,
        val __reserved2_1: Long = 0
) {
    companion object {
        const val sizeof = 0x40
        fun fromByteArray(bytes: ByteArray): _thread_local_storage {
            val __exitfunc = bytes.getInt32(0)
            val __arg = bytes.getInt32(4)
            val __errptr = bytes.getInt32(8)
            val __errval = bytes.getInt32(12)
            val __flags = bytes.getInt32(16)
            val __pid = bytes.getInt32(20)
            val __tid = bytes.getInt32(24)
            val __owner = bytes.getInt32(28)
            val __stackaddr = bytes.getInt32(32)
            val __reserved1 = bytes.getInt32(36)
            val __numkeys = bytes.getInt32(40)
            val __keydata = bytes.getInt32(44)
            val __cleanup = bytes.getInt32(48)
            val __fpuemu_data = bytes.getInt32(52)
            val __reserved2_0 = bytes.getInt32(56)
            val __reserved2_1 = bytes.getInt32(60)
            return _thread_local_storage(__exitfunc, __arg, __errptr, __errval, __flags, __pid, __tid,
                    __owner, __stackaddr, __reserved1, __numkeys, __keydata, __cleanup, __fpuemu_data, __reserved2_0, __reserved2_1)
        }
    }

    val asByteArray
        get() = ByteArray(sizeof).apply {
            putInt32(0, __exitfunc.asInt)
            putInt32(4, __arg.asInt)
            putInt32(8, __errptr.asInt)
            putInt32(12, __errval.asInt)
            putInt32(16, __flags.asInt)
            putInt32(20, __pid.asInt)
            putInt32(24, __tid.asInt)
            putInt32(28, __owner.asInt)
            putInt32(32, __stackaddr.asInt)
            putInt32(36, __reserved1.asInt)
            putInt32(40, __numkeys.asInt)
            putInt32(44, __keydata.asInt)
            putInt32(48, __cleanup.asInt)
            putInt32(52, __fpuemu_data.asInt)
            putInt32(56, __reserved2_0.asInt)
            putInt32(60, __reserved2_1.asInt)
        }
}
