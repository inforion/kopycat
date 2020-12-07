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
package ru.inforion.lab403.kopycat.veos.api.impl

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIResult
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction


class QNXAPI(os: VEOS<*>) : API(os) {

    val MsgSendv = object : APIFunction("MsgSendv") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val coid = argv[0]
            val siov = argv[1]
            val sparts = argv[2]
            val riov = argv[3]
            val rparts = argv[4]
            log.fine { "<$name> coid=${coid.hex8} siov=${siov.hex8}" }

            TODO("Not implemented")
        }
    }

    val atomic_sub_value = object : APIFunction("atomic_sub_value") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val loc = argv[0]
            val decr = argv[1]
            val prev = os.abi.readInt(loc)
            os.abi.writeInt(loc, prev-decr)
            return retval(prev)
        }
    }

    val atomic_add = object : APIFunction("atomic_add") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val loc = argv[0]
            val incr = argv[1]
            val prev = os.abi.readInt(loc)
            os.abi.writeInt(loc, prev+incr)
            return void()
        }
    }

    val SyncMutexLock_r = object : APIFunction("SyncMutexLock_r") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val sync = argv[0]
            return void()
        }
    }
    val _mux_smp_cmpxchg = nullsub("_mux_smp_cmpxchg")

    val _mux_smp_xchg = nullsub("_mux_smp_xchg")

    val TimerCreate = object : APIFunction("TimerCreate") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val clockId = argv[0]
            val event = argv[1]
            return retval(0)
        }
    }

    val SignalProcmask = object : APIFunction("SignalProcmask") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val pid = argv[0]
            val tid = argv[1]
            val how = argv[2]
            val set = argv[3]
            val oldset = argv[4]
            return retval(0)
        }
    }

    val ChannelCreate = object : APIFunction("ChannelCreate") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            TODO("Not implemented")
        }
    }

    val ConnectAttach = object : APIFunction("ConnectAttach") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val nd = argv[0]
            val pid = argv[1]
            val chid = argv[2]
            val index = argv[3]
            val flags = argv[4]
            log.finer { "Connect to QNX channel nd=${nd.hex8} pid=${pid.hex8} chid=${chid.hex8} index=${index.hex8} flags=${flags.hex8}" }
            return retval(0)
        }
    }

    val SchedCtl = object : APIFunction("SchedCtl") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val cmd = argv[0]
            val data = argv[1]
            val length = argv[2]

            log.finer { "Control the adaptive partitioning scheduler" }
            return retval(0)
        }
    }


}