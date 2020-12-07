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
import ru.inforion.lab403.common.extensions.ceil
import ru.inforion.lab403.common.extensions.collect
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.veos.api.misc.BytePointer
import ru.inforion.lab403.kopycat.veos.api.misc.IntPointer
import ru.inforion.lab403.kopycat.veos.api.misc.StructPointer
import ru.inforion.lab403.kopycat.veos.kernel.System
import java.net.InetAddress

sealed class SysDep(
        val SOCK_STREAM: Int = 1,
        val RLIMIT_NOFILE: Int = 7,
        val FIONREAD: Int = 0x541B,
        val SOL_SOCKET: Int = 1,
        val SO_REUSEADDR: Int = 2,

        val O_CREAT: Int = 0x40, /*       00000100 */
        val O_EXCL: Int = 0x80, /*        00000200 */
        val O_TRUNC: Int = 0x200, /*      00001000 */
        val O_APPEND: Int = 0x400, /*     00002000 */
        val O_NONBLOCK: Int = 0x800, /*   00004000 */
        val O_LARGEFILE: Int = 0x8000, /* 00100000 */

        val SIGSET_NWORDS: Int = 64 / (8 * 4 /* sizeof(unsigned long) */)
) {

    abstract fun statStructToBytes(st: stat_struct): ByteArray

    open fun toSigaction(sys: System, address: Long = 0) = sigaction(sys, address)

    companion object {
        operator fun get(core: AGenericCore) = when (core) {
            is AARMCore -> ARM
            is MipsCore -> MIPS
            else -> throw NotImplementedError("Architecture: $core")
        }
    }

    object MIPS : SysDep(
            SOCK_STREAM = 2,
            RLIMIT_NOFILE = 5,
            FIONREAD = 0x467F,
            SOL_SOCKET = 0xFFFF,
            SO_REUSEADDR = 4,
            O_CREAT = 0x0100,
            O_EXCL = 0x0400,
            O_TRUNC = 0x0200,
            O_APPEND = 0x0008,
            O_NONBLOCK = 0x0080,
            O_LARGEFILE = 0x2000,
            SIGSET_NWORDS = 128 / (8 * 4)
    ) {
        override fun statStructToBytes(st: stat_struct) = ByteArray(0xA0).apply {
            putInt64(0x0, st.st_dev)
            putInt64(0x10, st.st_ino)
            putInt32(0x18, st.st_mode.asInt)
            putInt64(0x1C, st.st_nlink)
            putInt32(0x20, st.st_uid.asInt)
            putInt32(0x24, st.st_gid.asInt)
            putInt64(0x28, st.st_rdev)
            putInt64(0x38, st.st_size)
            putInt64(0x40, st.st_atime)
            putInt64(0x48, st.st_mtime)
            putInt64(0x50, st.st_ctime)
            putInt64(0x58, st.st_blksize)
            putInt64(0x60, st.st_blocks)
        }

        override fun toSigaction(sys: System, address: Long) = sigaction(sys, address,
                handlerOffset = 4,
                flagsOffset = 0,
                restorerOffset = 24,
                maskOffset = 8
        )
    }

    object ARM : SysDep(O_LARGEFILE = 0x20000 /* 0400000 */ ) {
        override fun statStructToBytes(st: stat_struct) = ByteArray(0x68).apply {
            putInt64(0x0, st.st_dev)
            putInt32(0xC, st.st_ino.asInt)
            putInt32(0x10, st.st_mode.asInt)
            putInt32(0x14, st.st_nlink.asInt)
            putInt32(0x18, st.st_uid.asInt)
            putInt32(0x1C, st.st_gid.asInt)
            putInt64(0x20, st.st_rdev)
            putInt32(0x30, st.st_size.asInt)
            putInt32(0x38, st.st_blksize.asInt)
            putInt32(0x40, st.st_blocks.asInt)
            putInt32(0x48, st.st_atime.asInt)
            putInt32(0x50, st.st_mtime.asInt)
            putInt32(0x58, st.st_ctime.asInt)
            putInt64(0x60, st.st_ino)
        }
    }
}
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

const val __S_IFDIR = 0x4000L	/* Directory.  */
const val __S_IFREG = 0x8000L	/* Regular file.  */

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


// TODO: BYTEORDER!!!
// TODO: Structures prototype
class time_struct(
        val tm_sec: Long = 0,
        val tm_min: Long = 0,
        val tm_hour: Long = 0,
        val tm_mday: Long = 0,
        val tm_mon: Long = 0,
        val tm_year: Long = 0,
        val tm_wday: Long = 0,
        val tm_yday: Long = 0,
        val tm_isdst: Long = 0
) {
    companion object {
        const val sizeof = 0x24

        fun fromBytes(bytes: ByteArray): time_struct {
            val tm_sec = bytes.getInt32(0)         /* seconds */
            val tm_min = bytes.getInt32(4)         /* minutes */
            val tm_hour = bytes.getInt32(8)        /* hours */
            val tm_mday = bytes.getInt32(12)        /* day of the month */
            val tm_mon = bytes.getInt32(16)         /* month */
            val tm_year = bytes.getInt32(20)        /* year */
            val tm_wday = bytes.getInt32(24)        /* day of the week */
            val tm_yday = bytes.getInt32(28)        /* day in the year */
            val tm_isdst = bytes.getInt32(32)       /* daylight saving time */
            return time_struct(tm_sec, tm_min, tm_hour, tm_mday, tm_mon, tm_year, tm_wday, tm_yday, tm_isdst)
        }
    }

    val asBytes get() = ByteArray(sizeof).apply {
        putInt32(0, tm_sec.asInt)
        putInt32(4, tm_min.asInt)
        putInt32(8, tm_hour.asInt)
        putInt32(12, tm_mday.asInt)
        putInt32(16, tm_mon.asInt)
        putInt32(20, tm_year.asInt)
        putInt32(24, tm_wday.asInt)
        putInt32(28, tm_yday.asInt)
        putInt32(32, tm_isdst.asInt)
    }
}

class timeval(var tv_sec: Int=0, var tv_usec: Int=0) {

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

class stat_struct(
        val st_dev: Long = 0,
        val st_ino: Long = 0,
        val st_mode: Long = 0,
        val st_nlink: Long = 0,
        val st_uid: Long = 0,
        val st_gid: Long = 0,
        val st_rdev: Long = 0,
        val st_size: Long = 0,
        val st_blksize: Long = 0,
        val st_blocks: Long = 0,
        val st_atime: Long = 0,
        val st_mtime: Long = 0,
        val st_ctime: Long = 0
)


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

    fun time_struct(ptr: Long) = time_struct.fromBytes(abi.readBytes(ptr, time_struct.sizeof))

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

class rusage(sys: System, address: Long) : IntPointer(sys, address) {
    companion object {
//        fun sizeOf(sys: System) = sys.sizeOf.word * 2
//        fun allocate(sys: System) = FILE(sys, sys.allocateClean(sizeOf(sys)))
    }

    // TODO: replace timeval with this class
    inner class timevalInner(val offset: Int): IntPointer(sys, address) {
        var tvSec by field(offset)
        var tvUsec by field(offset + 1)
    }

    var ruUtime = timevalInner(0)
    var ruStime = timevalInner(2)
    // type: long
    var ruMaxrss by field(4)
    var ruIxrss by field(5)
    var ruIdrss by field(6)
    var ruIsrss by field(7)
    var ruMinflt by field(8)
    var ruMajflt by field(9)
    var ruNswap by field(10)
    var ruInblock by field(11)
    var ruOublock by field(12)
    var ruMsgsnd by field(13)
    var ruMsgrcv by field(14)
    var ruNsignals by field(15)
    var ruNvcsw by field(16)
    var ruNivcsw by field(17)
}

class _obstack_chunk(sys: System, address: Long) : StructPointer(sys, address) {
    var limit by pointer(0x00)
    private var prevPointer by pointer(0x04)
    val contents get() = BytePointer(sys, 0x08)

    var prev: _obstack_chunk
        get() = _obstack_chunk(sys, prevPointer)
        set(value) { prevPointer = value.address }
}


class obstack(sys: System, address: Long) : StructPointer(sys, address) {
    // type: long
    /* 00 */ var chunkSize by int(0x00) /* preferred size to allocate chunks in */
    /* 04 */ private var chunkPointer by pointer(0x04) /* address of current struct obstack_chunk */
    /* 08 */ private var objectBasePointer by pointer(0x08) /* address of object we are building */
    /* 0C */ var nextFree by pointer(0x0C) /* where to add next char to current object */
    /* 10 */ var chunkLimit by pointer(0x10) /* address of char after current chunk */
    /* 14 */ var temp by pointer(0x14) /* Temporary for some macros.  */
    /* 18 */ var alignmentMask by int(0x18)  /* Mask of alignment for each object. */
    /* These prototypes vary based on 'use_extra_arg', and we use
       casts to the prototypeless function type in all assignments,
       but having prototypes here quiets -Wstrict-prototypes.  */
    /* 1C */ var chunkfun by pointer(0x1C)
    /* 20 */ var freefun by pointer(0x20)
    /* 24 */ var extraArg by pointer(0x24)    /* first arg for chunk alloc/dealloc funcs */
    /* 28 */ private var bitField by int(0x28)

    var useExtraArg by bit(::bitField, 0)       /*  chunk alloc/dealloc funcs take extra arg */
    var maybeEmptyObject by bit(::bitField, 1)  /*  There is a possibility that the current
                                                                chunk contains a zero-length object.  This
                                                                prevents freeing the chunk if we allocate
                                                                a bigger chunk to replace it. */
    var allocFailed by bit(::bitField, 2)       /*  No longer used, as we now call the failed
                                                                handler on error, but retained for binary
                                                                compatibility.  */

    var chunk: _obstack_chunk
        get() = _obstack_chunk(sys, chunkPointer)
        set(value) { chunkPointer = value.address }

    var objectBase: BytePointer
        get() = BytePointer(sys, objectBasePointer)
        set(value) { objectBasePointer = value.address }

}


class DIR(sys: System, address: Long = 0): StructPointer(sys, address) {
    companion object {
        const val sizeOf = 4
        fun allocate(sys: System) = DIR(sys, sys.allocateClean(sizeOf))
    }

    var fd by int(0)
}

class sigset_t(sys: System, address: Long = 0): IntPointer(sys, address) {
    companion object {
        fun allocate(sys: System) = DIR(sys, sys.allocateClean(
                SysDep[sys.fullABI.core].SIGSET_NWORDS * sys.sizeOf.int // TODO: sizeof(unsigned long)
        ))
    }

    val size = SysDep[sys.fullABI.core].SIGSET_NWORDS

    fun fill() = sys.fullABI.writeBytes(address, ByteArray(size * sys.sizeOf.int) { (-1).asByte })

}



class sigaction(
        sys: System,
        address: Long = 0,
        handlerOffset: Int = 0,
        flagsOffset: Int = 4,
        restorerOffset: Int = 8,
        maskOffset: Int = 12
): StructPointer(sys, address) {

    var handler by pointer(handlerOffset)
    var flags by int(flagsOffset) // unsigned long
    var restorer by pointer(restorerOffset)
    var mask by pointer(maskOffset)
}


