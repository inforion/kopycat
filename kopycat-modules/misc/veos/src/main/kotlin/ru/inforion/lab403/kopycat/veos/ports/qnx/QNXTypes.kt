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
package ru.inforion.lab403.kopycat.veos.ports.qnx

import ru.inforion.lab403.common.extensions.*

//
//data class _thread_local_storage(
//    val __exitfunc: ULong,
//    val __arg: ULong,
//    val __errptr: ULong,
//    val __errval: ULong = 0u,
//    val __flags: ULong = 0u,
//    val __pid: ULong = 0u,
//    val __tid: ULong = 0u,
//    val __owner: ULong = 0u,
//    val __stackaddr: ULong = 0u,
//    val __reserved1: ULong = 0u,
//    val __numkeys: ULong = 0u,
//    val __keydata: ULong = 0u,
//    val __cleanup: ULong = 0u,
//    val __fpuemu_data: ULong = 0u,
//    val __reserved2_0: ULong = 0u,
//    val __reserved2_1: ULong = 0u
//) {
//    companion object {
//        const val sizeof = 0x40
//        fun fromByteArray(bytes: ByteArray) = _thread_local_storage(
//            bytes.getUInt32(0),
//            bytes.getUInt32(4),
//            bytes.getUInt32(8),
//            bytes.getUInt32(12),
//            bytes.getUInt32(16),
//            bytes.getUInt32(20),
//            bytes.getUInt32(24),
//            bytes.getUInt32(28),
//            bytes.getUInt32(32),
//            bytes.getUInt32(36),
//            bytes.getUInt32(40),
//            bytes.getUInt32(44),
//            bytes.getUInt32(48),
//            bytes.getUInt32(52),
//            bytes.getUInt32(56),
//            bytes.getUInt32(60),
//        )
//    }
//
//    val asByteArray
//        get() = ByteArray(sizeof).apply {
//            putUInt32(0, __exitfunc)
//            putUInt32(4, __arg)
//            putUInt32(8, __errptr)
//            putUInt32(12, __errval)
//            putUInt32(16, __flags)
//            putUInt32(20, __pid)
//            putUInt32(24, __tid)
//            putUInt32(28, __owner)
//            putUInt32(32, __stackaddr)
//            putUInt32(36, __reserved1)
//            putUInt32(40, __numkeys)
//            putUInt32(44, __keydata)
//            putUInt32(48, __cleanup)
//            putUInt32(52, __fpuemu_data)
//            putUInt32(56, __reserved2_0)
//            putUInt32(60, __reserved2_1)
//        }
//}
