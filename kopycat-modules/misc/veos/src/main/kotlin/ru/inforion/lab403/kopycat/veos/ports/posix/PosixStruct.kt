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
@file:Suppress("unused", "UNUSED_VARIABLE", "ObjectPropertyName", "ObjectPropertyName", "INLINE_CLASS_DEPRECATED")

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

const val nullptr: ULong = 0u
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

const val __S_IFIFO  = 0x001000uL  /* FIFO.  */
const val __S_IFDIR  = 0x004000uL  /* Directory.  */
const val __S_IFCHR  = 0x002000uL  /* Character device.  */
const val __S_IFBLK  = 0x006000uL  /* Block device.  */
const val __S_IFREG  = 0x008000uL  /* Regular file.  */
const val __S_IFLNK  = 0x00A000uL  /* Symbolic link.  */
const val __S_IFSOCK = 0x00C000uL  /* Socket.  */

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

const val POLLIN     = 0x0001uL    /* Можно считывать данные */
const val POLLPRI    = 0x0002uL    /* Есть срочные данные */
const val POLLOUT    = 0x0004uL    /* Запись не будет блокирована */
const val POLLERR    = 0x0008uL    /* Произошла ошибка */
const val POLLHUP    = 0x0010uL    /* "Положили трубку" */
const val POLLNVAL   = 0x0020uL    /* Неверный запрос: fd не открыт */

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
            val tv_sec = bytes.getUInt32(0).int         /* seconds */
            val tv_usec = bytes.getUInt32(4).int        /* microseconds */
            return timeval(tv_sec, tv_usec)
        }

        fun from_millis(msec: Long) = timeval((msec / 1000).int, (msec % 1000).int)
    }

    val asBytes get() = ByteArray(sizeof).apply {
        putUInt32(0, tv_sec.ulong_z)
        putUInt32(4, tv_usec.ulong_z)
    }
}

class timeval64 constructor(var tv_sec: Long = 0, var tv_usec: Long = 0) {

    companion object {
        const val sizeof = 16

        fun fromBytes(bytes: ByteArray): timeval64 {
            val tv_sec = bytes.getUInt64(0).long         /* seconds */
            val tv_usec = bytes.getUInt32(8).long        /* microseconds */
            return timeval64(tv_sec, tv_usec)
        }

        fun from_millis(msec: Long) = timeval64((msec / 1000), (msec % 1000))
    }

    val asBytes get() = ByteArray(sizeof).apply {
        putUInt64(0, tv_sec.ulong)
        putUInt64(8, tv_usec.ulong)
    }
}

class timezone constructor(
    var tz_minuteswest: Int = 0, /* minutes west of Greenwich */
    var tz_dsttime: Int = 0 /* type of DST correction */
) {

    companion object {
        const val sizeof = 0x8

        fun fromBytes(bytes: ByteArray): timezone {
            val tz_minuteswest = bytes.getUInt32(0).int
            val tz_dsttime = bytes.getUInt32(4).int
            return timezone(tz_minuteswest, tz_dsttime)
        }
    }

    val asBytes get() = ByteArray(sizeof).apply {
        putUInt32(0, tz_minuteswest.ulong_z)
        putUInt32(4, tz_dsttime.ulong_z)
    }
}

class sched_param(
    val sched_priority: ULong = 0u,
    val sched_curpriority: ULong = 0u,
    val reserved: ULong = 0u,
    val __ss_low_priority: ULong = 0u,
    val __ss_max_repl: ULong = 0u,
    val __ss_repl_period: ULong = 0u,
    val __ss_init_budget: ULong = 0u
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
    val __detachstate: ULong = 0u,
    val __schedpolicy: ULong = 0u,
    val __schedparam: sched_param,
    val __inheritsched: ULong = 0u,
    val __scope: ULong = 0u,
    val __guardsize: ULong = 0u,
    val __stackaddr_set: ULong = 0u,
    val __stackaddr: ULong = 0u,
    val __stacksize: ULong = 0u
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
            bytes.getUInt16(0).int,
            bytes.getUInt16(2).int,
            bytes.getArray(4, 4)
    )

    val asBytes get() = ByteArray(4 + sin_addr.size).apply {
        putUInt16(0, sin_family.ulong_z)
        putUInt16(2, sin_port.ulong_z)
        putArray(4, sin_addr)
    }

    val hostname = "${sin_addr[0]}.${sin_addr[1]}.${sin_addr[2]}.${sin_addr[3]}"
}

class SOCKSEL(var se_inflags: Short, var se_outflags: Short, val se_fd: Int, val se_1reserved: Int, var se_user: Int, val se_2reserved: Int) {
    companion object {
        const val sizeof = 0x14

        fun fromBytes(bytes: ByteArray): SOCKSEL {
            val se_inflags = bytes.getUInt16(0).short
            val se_outflags = bytes.getUInt16(2).short
            val se_fd = bytes.getUInt32(4).int
            val se_1reserved = bytes.getUInt32(8).int
            val se_user = bytes.getUInt32(12).int
            val se_2reserved = bytes.getUInt32(16).int
            return SOCKSEL(se_inflags, se_outflags, se_fd, se_1reserved, se_user, se_2reserved)
        }
    }

    val asBytes get() = ByteArray(sizeof).apply {
        putUInt16(0, se_inflags.ulong_z)
        putUInt16(2, se_outflags.ulong_z)
        putUInt32(4, se_fd.ulong_z)
        putUInt32(8, se_1reserved.ulong_z)
        putUInt32(12, se_user.ulong_z)
        putUInt32(16, se_2reserved.ulong_z)
    }
}

class rlimit(val rlim_cur: ULong, val rlim_max: ULong) {
    companion object {
        const val sizeof = 0x8
    }

    constructor(bytes: ByteArray) : this(bytes.getUInt32(0), bytes.getUInt32(4))

    val asBytes get() = ByteArray(sizeof).apply {
        putUInt32(0, rlim_cur)
        putUInt32(4, rlim_max)
    }
};

// TODO: it's only 32-bit
class fd_set(val fds_bits: Array<ULong>) {
    companion object {
        const val sizeof = 0x1024 * 4
    }

    constructor(bytes: ByteArray, n: Int) : this(
            List(n ceil 32) { bytes.getUInt32(it * 4) }.toTypedArray()
    )

    val asBytes: ByteArray
        get() {
            val bytes = ByteArray(fds_bits.size * 4)
            fds_bits.forEachIndexed { i, it ->
                bytes.putUInt32(i * 4, it)
            }
            return bytes
        }
}

class pollfd(val fd: Int, val events: Short, var revents: Short) {
    companion object {
        const val sizeof = 8
    }

    constructor(bytes: ByteArray) : this(
            bytes.getUInt32(0).int,
            bytes.getUInt16(4).short,
            bytes.getUInt16(6).short
    )

    val asBytes get() = ByteArray(sizeof).apply {
        putUInt32(0, fd.ulong_z)
        putUInt16(4, events.ulong_z)
        putUInt16(6, revents.ulong_z)
    }
}

class iovec(val iov_base: ULong, val iov_len: ULong) {
    companion object {
        const val sizeof = 8
    }

    constructor(bytes: ByteArray) : this(
            bytes.getUInt32(0),
            bytes.getUInt32(4)
    )

    val asBytes get() = ByteArray(sizeof).apply {
        putUInt32(0, iov_base)
        putUInt32(4, iov_len)
    }
}


inline class PosixReader<T: AGenericCore>(val abi: ABI<T>) {
    companion object {
        inline val <T : AGenericCore> ABI<T>.reader get() = PosixReader(this)
    }

    fun sockaddr(ptr: ULong, len: Int = sockaddr.sizeof) = sockaddr(abi.readBytes(ptr, len))

    fun pollfd(ptr: ULong, index: Int = 0) = pollfd(abi.readBytes(ptr + index * pollfd.sizeof, pollfd.sizeof))

    fun timeval(ptr: ULong) = timeval.fromBytes(abi.readBytes(ptr, timeval.sizeof))
}

inline class PosixWriter<T: AGenericCore>(val abi: ABI<T>) {
    companion object {
        inline val <T : AGenericCore> ABI<T>.writer get() = PosixWriter(this)
    }

    fun sockaddr(ptr: ULong, address: InetAddress, port: Int) {
        val data = sockaddr(AF_INET, port, address.address)
        abi.writeBytes(ptr, data.asBytes)
    }

    fun pollfd(ptr: ULong, struct: pollfd, index: Int = 0) =
            abi.writeBytes(ptr + index * pollfd.sizeof, struct.asBytes)
}
