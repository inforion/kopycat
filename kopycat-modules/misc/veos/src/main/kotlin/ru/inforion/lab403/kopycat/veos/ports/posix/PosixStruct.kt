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
@file:Suppress("unused", "UNUSED_VARIABLE", "ObjectPropertyName", "ObjectPropertyName")

package ru.inforion.lab403.kopycat.veos.ports.posix

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.veos.api.pointers.IntPointer
import ru.inforion.lab403.kopycat.veos.kernel.System
import java.net.InetAddress


const val O_RDONLY = 0x0000
const val O_WRONLY = 0x0001
const val O_RDWR = 0x0002

const val nullptr: Long = 0
const val AF_INET = 2
const val IPPROTO_IP = 0
const val IPPROTO_TCP = 6

const val F_GETFD = 1
const val F_SETFD = 2
const val F_GETFL = 3
const val F_SETFL = 4

const val TCP_CORK = 3

const val LOG_PID = 0x01	/* log the pid with each message */
const val LOG_CONS = 0x02	/* log on the console if errors in sending */

const val __S_IFIFO  = 0x001000L  /* FIFO.  */
const val __S_IFDIR  = 0x004000L  /* Directory.  */
const val __S_IFCHR  = 0x002000L  /* Character device.  */
const val __S_IFBLK  = 0x006000L  /* Block device.  */
const val __S_IFREG  = 0x008000L  /* Regular file.  */
const val __S_IFLNK  = 0x00A000L  /* Symbolic link.  */
const val __S_IFSOCK = 0x00C000L  /* Socket.  */

const val S_IRWXU = 0x1C0			/* RWX mask for owner */
const val S_IRUSR = 0x100			/* R for owner */
const val S_IWUSR = 0x080			/* W for owner */
const val S_IXUSR = 0x040			/* X for owner */

const val S_IRWXG = 0x038			/* RWX mask for group */
const val S_IRGRP = 0x020			/* R for group */
const val S_IWGRP = 0x010			/* W for group */
const val S_IXGRP = 0x008			/* X for group */

const val S_IRWXO = 0x007			/* RWX mask for other */
const val S_IROTH = 0x004			/* R for other */
const val S_IWOTH = 0x002			/* W for other */
const val S_IXOTH = 0x001			/* X for other */

const val POLLIN     = 0x0001L    /* Можно считывать данные */
const val POLLPRI    = 0x0002L    /* Есть срочные данные */
const val POLLOUT    = 0x0004L    /* Запись не будет блокирована */
const val POLLERR    = 0x0008L    /* Произошла ошибка */
const val POLLHUP    = 0x0010L    /* "Положили трубку" */
const val POLLNVAL   = 0x0020L    /* Неверный запрос: fd не открыт */

const val _SC_IOV_MAX = 0x3C

const val IOV_MAX = 1024

const val SHUT_RD = 0
const val SHUT_WR = 1
const val SHUT_RDWR = 2

const val SIGSEGV = 11

class timeval constructor(var tv_sec: Int = 0, var tv_usec: Int = 0) {

    companion object {
        const val sizeof = 0x8

        fun fromBytes(bytes: ByteArray): timeval {
            val tv_sec = bytes.getInt32(0).asInt         /* seconds */
            val tv_usec = bytes.getInt32(4).asInt        /* microseconds */
            return timeval(tv_sec, tv_usec)
        }
    }

    val asBytes get() = ByteArray(sizeof).apply {
        putInt32(0, tv_sec.asInt)
        putInt32(4, tv_usec.asInt)
    }
}




/*
struct sched_param {
    int32_t  sched_priority;
    int32_t  sched_curpriority;
    union {
        int32_t  reserved[8];
        struct {
            int32_t  __ss_low_priority;
            int32_t  __ss_max_repl;
            struct timespec     __ss_repl_period;
            struct timespec     __ss_init_budget;
        }           __ss;
    }           __ss_un;
}
*/

class sched_param(
        val sched_priority: Long = 0,
        val sched_curpriority: Long = 0,
        val reserved: Long = 0,
        val __ss_low_priority: Long = 0,
        val __ss_max_repl: Long = 0,
        val __ss_repl_period: Long = 0,
        val __ss_init_budget: Long = 0
) {

    companion object {
        const val sizeof = 0x34

        fun fromBytes(bytes: ByteArray): sched_param {
            val st_dev = bytes.getInt32(0)
            return sched_param()
        }
    }

    val asBytes get() = ByteArray(sizeof)
}


class pthread_attr_t(
        val __detachstate: Long = 0,
        val __schedpolicy: Long = 0,
        val __schedparam: sched_param,
        val __inheritsched: Long = 0,
        val __scope: Long = 0,
        val __guardsize: Long = 0,
        val __stackaddr_set: Long = 0,
        val __stackaddr: Long = 0,
        val __stacksize: Long = 0
) {

    companion object {
        const val sizeof = 0x4C

        fun fromBytes(bytes: ByteArray) = pthread_attr_t(__schedparam = sched_param())
    }

    val asBytes get() = ByteArray(sizeof)
}

class pthread {
    companion object {
        const val sizeof = 0x4C

        fun fromBytes(bytes: ByteArray) = pthread_attr_t(__schedparam=sched_param())
    }

    val asBytes get() = ByteArray(sizeof)
}

class sockaddr(
        val sin_family: Int,
        val sin_port: Int,
        val sin_addr: ByteArray
) {
    companion object {
        const val sizeof = 0x8 // TODO: minimal size
    }

    constructor(bytes: ByteArray) : this(
            bytes.getInt16(0).asInt,
            bytes.getInt16(2).asInt,
            bytes.getArray(4, 4)
    )

    val asBytes get() = ByteArray(4 + sin_addr.size).apply {
        putInt16(0, sin_family.asInt)
        putInt16(2, sin_port.asInt)
        putArray(4, sin_addr)
    }

    val hostname = "${sin_addr[0].asUInt}.${sin_addr[1].asUInt}.${sin_addr[2].asUInt}.${sin_addr[3].asUInt}"
}

class SOCKSEL(var se_inflags: Short, var se_outflags: Short, val se_fd: Int, val se_1reserved: Int, var se_user: Int, val se_2reserved: Int) {
    companion object {
        const val sizeof = 0x14

        fun fromBytes(bytes: ByteArray): SOCKSEL {
            val se_inflags = bytes.getInt16(0).asShort
            val se_outflags = bytes.getInt16(2).asShort
            val se_fd = bytes.getInt32(4).asInt
            val se_1reserved = bytes.getInt32(8).asInt
            val se_user = bytes.getInt32(12).asInt
            val se_2reserved = bytes.getInt32(16).asInt
            return SOCKSEL(se_inflags, se_outflags, se_fd, se_1reserved, se_user, se_2reserved)
        }
    }

    val asBytes get() = ByteArray(sizeof).apply {
        putInt16(0, se_inflags.asInt)
        putInt16(2, se_outflags.asInt)
        putInt32(4, se_fd.asInt)
        putInt32(8, se_1reserved.asInt)
        putInt32(12, se_user.asInt)
        putInt32(16, se_2reserved.asInt)
    }
}

class rlimit(val rlim_cur: Long, val rlim_max: Long) {
    companion object {
        const val sizeof = 0x8
    }

    constructor(bytes: ByteArray) : this(bytes.getInt32(0), bytes.getInt32(4))

    val asBytes get() = ByteArray(sizeof).apply {
        putInt32(0, rlim_cur.asInt)
        putInt32(4, rlim_max.asInt)
    }
};

// TODO: it's only 32-bit
class fd_set(val fds_bits: Array<Long>) {
    companion object {
        const val sizeof = 0x1024 * 4
    }

    constructor(bytes: ByteArray, n: Int) : this(
            collect(n.ceil(32)) { bytes.getInt32(it * 4) }.toTypedArray()
    )

    val asBytes: ByteArray
        get() {
            val bytes = ByteArray(fds_bits.size * 4)
            fds_bits.forEachIndexed { i, it ->
                bytes.putInt32(i * 4, it.asInt)
            }
            return bytes
        }
}

class pollfd(val fd: Int, val events: Short, var revents: Short) {
    companion object {
        const val sizeof = 8
    }

    constructor(bytes: ByteArray) : this(
            bytes.getInt32(0).asInt,
            bytes.getInt16(4).asShort,
            bytes.getInt16(6).asShort
    )

    val asBytes get() = ByteArray(sizeof).apply {
        putInt32(0, fd)
        putInt16(4, events.asUInt)
        putInt16(6, revents.asUInt)
    }
}

class iovec(val iov_base: Long, val iov_len: Long) {
    companion object {
        const val sizeof = 8
    }

    constructor(bytes: ByteArray) : this(
            bytes.getInt32(0),
            bytes.getInt32(4)
    )

    val asBytes get() = ByteArray(sizeof).apply {
        putInt32(0, iov_base.asInt)
        putInt32(4, iov_len.asInt)
    }
}


inline class PosixReader<T: AGenericCore>(val abi: ABI<T>) {
    companion object {
        inline val <T : AGenericCore> ABI<T>.reader get() = PosixReader(this)
    }

    fun sockaddr(ptr: Long, len: Int = sockaddr.sizeof) = sockaddr(abi.readBytes(ptr, len))

    fun pollfd(ptr: Long, index: Int = 0) = pollfd(abi.readBytes(ptr + index * pollfd.sizeof, pollfd.sizeof))

    fun timeval(ptr: Long) = timeval.fromBytes(abi.readBytes(ptr, timeval.sizeof))
}

inline class PosixWriter<T: AGenericCore>(val abi: ABI<T>) {
    companion object {
        inline val <T : AGenericCore> ABI<T>.writer get() = PosixWriter(this)
    }

    fun sockaddr(ptr: Long, address: InetAddress, port: Int) {
        val data = sockaddr(AF_INET, port, address.address)
        abi.writeBytes(ptr, data.asBytes)
    }

    fun pollfd(ptr: Long, struct: pollfd, index: Int = 0) =
            abi.writeBytes(ptr + index * pollfd.sizeof, struct.asBytes)
}
