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
@file:Suppress("unused", "LocalVariableName", "PropertyName", "ObjectPropertyName", "MemberVisibilityCanBePrivate", "FunctionName",
    "UNUSED_VARIABLE"
)

package ru.inforion.lab403.kopycat.veos.api.impl

import gnu.getopt.Getopt
import gnu.getopt.LongOpt
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIVariable
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.datatypes.LongLong
import ru.inforion.lab403.kopycat.veos.api.datatypes.mode_t
import ru.inforion.lab403.kopycat.veos.api.datatypes.size_t
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.misc.toStdCErrno
import ru.inforion.lab403.kopycat.veos.api.pointers.*
import ru.inforion.lab403.kopycat.veos.exceptions.InvalidArgument
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONoSuchFileOrDirectory
import ru.inforion.lab403.kopycat.veos.filesystems.AccessFlags
import ru.inforion.lab403.kopycat.veos.filesystems.impl.FileSystem
import ru.inforion.lab403.kopycat.veos.kernel.System
import ru.inforion.lab403.kopycat.veos.ports.obstack._obstack_chunk
import ru.inforion.lab403.kopycat.veos.ports.obstack.obstack
import ru.inforion.lab403.kopycat.veos.ports.posix.*
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixReader.Companion.reader
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixWriter.Companion.writer
import ru.inforion.lab403.kopycat.veos.ports.rusage.rusage
import ru.inforion.lab403.kopycat.veos.ports.sysdep.ASystemDep.Companion.deps
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.util.*
import kotlin.experimental.or
import kotlin.random.asKotlinRandom


class PosixAPI(os: VEOS<*>) : API(os) {

    companion object {
        @Transient val log = logger(FINE)
    }

    init {
        type(ArgType.Pointer) { _, it -> StructPointer(os.sys, it) }
    }

    override fun setErrno(error: Exception?) {
        errno.allocated.value = error?.toStdCErrno(ra)?.id ?: PosixError.ESUCCESS.id
    }

    fun htons(value: Int) = if (os.abi.bigEndian) value else value.swap16()

    fun htons(value: ULong) = if (os.abi.bigEndian) value else value.swap16()

    fun htonl(value: ULong) = if (os.abi.bigEndian) value else value.swap32()

    enum class LC(val id: Int) {
        CTYPE(0),
        NUMERIC(1),
        MONETARY(2),
        TIME(3),
        COLLATE(4),
        MESSAGES(5),
        ALL(6)
    }

    var gid: ULong = 0u

    var openlogIdent = ""

    @DontAutoSerialize
    val noEntropyRandom = Random(0)

    val objstackDefaultAlignment = 4096 - 12 - 4 // TODO: 12 is sizeof (mhead) and 4 is EXTRA from GNU malloc.

    // TODO: refactor this
    var umaskValue = 0x12 // 022

    val mode_t.oct3: String get() = "${int / 64}${(int / 8) % 8}${int % 8}"

    // REVIEW: refactor and get rid of Context
    class jmp_buf(sys: System, address: ULong) : Pointer<Unit>(sys, address) {
        val ctx = sys.abi.createContext()

        fun restore() = ctx.restore(address)
        fun store() = ctx.store(address)
    }

    val argc = APIVariable.int(os, "_argc")
    val argv = APIVariable.pointer(os, "_argv")
    val errno = APIVariable.int(os, "errno")
    val lasterrno = APIVariable.pointer(os, "lasterrno")
    val stderr = APIVariable.pointer(os, "_Stderr")
    val stdout = APIVariable.pointer(os, "_Stdout")
    val optind = APIVariable.int(os, "optind")
    val optarg = APIVariable.pointer(os, "optarg")
    val optopt = APIVariable.int(os, "optopt")

    init {
        type(ArgType.Pointer) { _, it -> rusage(os.sys, it) }
        type(ArgType.Pointer) { _, it -> obstack(os.sys, it) }
        type(ArgType.Pointer) { _, it -> jmp_buf(os.sys, it) }

        ret<StructPointer> { APIResult.Value(it.address) }
    }

    override fun init(argc: ULong, argv: ULong, envp: ULong) {
        if (this.argc.linked && this.argv.linked) {
            this.argc.value = argc
            this.argv.value = argv
        }
    }

    /* --------------------- POSIX functions --------------------------------- */

    // REVIEW: Unknown
    val _xstream = nullsub("_xstream")

    // REVIEW: -- POSIX, BSD, SVID, SVr4, GNU Linux ---

    // REVIEW: #include <strings.h>
    // POSIX.1-2008
    // https://linux.die.net/man/3/stpcpy
    val stpcpy = object : APIFunction("stpcpy") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val dst = argv[0]
            val src = os.sys.readAsciiString(argv[1])
            os.sys.writeAsciiString(dst, src)
            return retval(dst + src.length)
        }
    }
    // 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/3/strcasecmp
    val strcasecmp = object : APIFunction("strcmp") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val str1 = os.sys.readAsciiString(argv[0]).lowercase()
            val str2 = os.sys.readAsciiString(argv[1]).lowercase()
            return retval(str1.compareTo(str2).ulong_z)
        }
    }
    // 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/3/strncasecmp
    val strncasecmp = object : APIFunction("strncasecmp") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val n = argv[2].int
            var str1 = os.sys.readAsciiString(argv[0])
            var str2 = os.sys.readAsciiString(argv[1])
            // TODO: make own implementation of toLowerCaseAsciiOnly
            str1 = str1[0..minOf(n, str1.length)].toLowerCaseAsciiOnly()
            str2 = str2[0..minOf(n, str2.length)].toLowerCaseAsciiOnly()
            return retval(str1.compareTo(str2).ulong_z)
        }
    }
    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/3/strdup
    val strdup = object : APIFunction("strdup") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val string = os.sys.readAsciiString(argv[0])
            val result = os.sys.allocate(string.length + 1)
            os.sys.writeAsciiString(result, string, true)
            return retval(result)
        }
    }
    @APIFunc
    fun __strdup(string: CharPointer) = strdup.exec("__strdup", string.address)

    // REVIEW: #include <fcntl.h>
    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/open
    val open = object : APIFunction("open") {
        val O_ACCMODE = 3

        infix fun ULong.check(b: Int) = (this and b.ulong_z).truth

        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val filenameStr = os.sys.readAsciiString(argv[0])
            val mode = argv[1]

            val deps = sys.deps

            val accmode = mode.int and O_ACCMODE
            val flags = AccessFlags(
                    accmode == O_RDWR || accmode == O_RDONLY,
                    accmode == O_RDWR || accmode == O_WRONLY,
                    mode check deps.O_APPEND,
                    mode check deps.O_CREAT,
                    mode check deps.O_TRUNC,
                    mode check deps.O_EXCL
            )

            val mask = (O_ACCMODE or deps.O_TRUNC or deps.O_APPEND or deps.O_CREAT or deps.O_EXCL or deps.O_LARGEFILE).ulong_z.inv()
            check(mode and mask == 0uL) { "[0x${ra.hex8}] Unknown combination of flags ($filenameStr): 0x${mode.hex8}" }

            val result = os.filesystem.open(filenameStr, flags)

            log.fine { "[0x${ra.hex8}] open(file='$filenameStr' mode='$flags') -> fd = $result in ${os.currentProcess}" }

            return retval(result.ulong_z)
        }
    }
    val open64 = open
    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/fcntl
    val fcntl = object : APIFunction("fcntl") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val fd = argv[0].int
            val cmd = argv[1].int
            val arg = argv[2].int

            log.fine { "[0x${ra.hex8}] fcntl(fd=$fd cmd=$cmd arg=$arg)" }

            val nonBlock = sys.deps.O_NONBLOCK
            val mask = O_WRONLY or O_RDWR or nonBlock

            when (cmd) {
                F_GETFD -> {
                    log.config { "[0x${ra.hex8}] F_GETFD is not implemented!" }
                    return retval(0uL)
                }
                F_SETFD -> log.config { "[0x${ra.hex8}] F_SETFD with $arg is not implemented!" }
                F_GETFL -> {
                    val file = os.filesystem.file(fd)
                    val result = when {
                        file.writable() && file.readable() -> O_RDWR
                        file.writable() -> O_WRONLY
                        file.readable() -> O_RDONLY
                        else -> TODO("Strange case")
                    }
                    return retval(result.ulong_z)
                }
                F_SETFL -> {
                    check(arg and mask.inv() == 0) { "Unimplemented flags: 0x${arg.hex8}" }

                    // TODO: use open.check()
                    if (arg and O_WRONLY != 0)
                        log.config { "[0x${ra.hex8}] F_SETFL with O_WRONLY not implemented!" }
                    if (arg and O_RDWR != 0)
                        log.config { "[0x${ra.hex8}] F_SETFL with O_RDWR not implemented!" }
                    if (arg and nonBlock != 0)
                        log.config { "[0x${ra.hex8}] F_SETFL with O_NONBLOCK not implemented!" }

                    os.ioSystem.setNonBlock(fd, arg and nonBlock != 0)
                }
                else -> throw IllegalArgumentException("[0x${ra.hex8}] Unknown fcntl cmd=0x${cmd.hex8} fd=$fd arg=0x${arg.hex8}")
            }

            return retval(0uL)
        }
    }
    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/fcntl64
    val fcntl64 = fcntl

    // REVIEW: #include <unistd.h>
    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/close
    val close = object : APIFunction("close") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val fd = argv[0].int
            log.fine { "[0x${ra.hex8}] close(fd=${argv[0]})" }
            val result = nothrow(-1) { os.ioSystem.close(fd); 0 }
            return retval(result.ulong_z)
        }
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/read
    val read = object : APIFunction("read") {
        // https://linux.die.net/man/2/read
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val fd = argv[0].int
            val buf = argv[1]
            val count = argv[2].int

            if (count == 0)
                return retval(0uL)

            os.block<ByteArray> {
                execute {
                    os.ioSystem.read(fd, count)
                }

                success {
                    log.fine { "[0x${ra.hex8}] read(fd=$fd buf=0x${buf.hex8} count=$count) -> read=${it.size} in ${os.currentProcess}" }
                    os.abi.writeBytes(buf, it); it.size.ulong_z
                }

                failure {
                    log.fine { "[0x${ra.hex8}] read(fd=$fd buf=0x${buf.hex8} count=$count) -> failed in ${os.currentProcess}" }
                    setErrno(it); 0u
                }
            }

            return void()
        }
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/write
    val write = object : APIFunction("write") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val fd = argv[0].int
            val buf = argv[1]
            val count = argv[2] // TODO: overflow?  // KC-1848 - непонятно

            log.fine { "[0x${ra.hex8}] write(fd=$fd buf=0x${buf.hex8} count=$count) in ${os.currentProcess}" }

            val data = os.abi.readBytes(buf, count.int)
            val result = nothrow(-1) { os.ioSystem.write(fd, data); data.size }
            return retval(result.ulong_z)
        }
    }

    // https://man7.org/linux/man-pages/man2/lseek.2.html
    @APIFunc
    fun lseek(fd: Int, offset: Int, whence: Int) = nothrow(-1) {
        log.fine { "[0x${ra.hex8}] lseek(fd=${fd} offset=0x${offset.hex8} whence=0x${whence.hex8})" }
        val seek = find<FileSystem.Seek> { it.id == whence } ?: throw InvalidArgument()
        sys.filesystem.seek(fd, offset.ulong_z, seek)
        sys.filesystem.tell(fd).int
    }

    @APIFunc
    fun lseek64(fd: Int, offset: LongLong, whence: Int) = nothrow(-1) {
        log.fine { "[0x${ra.hex8}] lseek64(fd=${fd} offset=0x${offset.ulong.hex} whence=0x${whence.hex8})" }
        val seek = find<FileSystem.Seek> { it.id == whence } ?: throw InvalidArgument()
        sys.filesystem.seek(fd, offset.ulong, seek)
        sys.filesystem.tell(fd).int
    }

    // SVr4, 4.3BSD, POSIX.1-2001.
    // https://linux.die.net/man/3/isatty
    val isatty = object : APIFunction("isatty") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val fd = argv[0].int
            log.fine { "[0x${ra.hex8}] isatty(fd=${fd})" }
            val result = nothrow(-1) { os.ioSystem.isTerm(fd); 0 }
            return retval(result.ulong_z)
        }
    }

    // SVr4, 4.3BSD, POSIX.1-2001.
    // https://linux.die.net/man/2/getpagesize
    val getpagesize = object : APIFunction("getpagesize") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: ULong): APIResult {
            log.fine { "[0x${ra.hex8}] getpagesize()" }
            // linux typical pagesize is 4096
            return retval(4096uL)
        }
    }

    // REVIEW: We can't use getopt on multiple argument sets
    // POSIX.2 and POSIX.1-2001
    // https://linux.die.net/man/3/getopt
    val getopt = object : APIFunction("getopt") {
        private fun getArg(pArgv: ULong, index: Int): String {
            val address = os.abi.readPointer(pArgv + os.abi.types.pointer.bytes * index)
            return os.sys.readAsciiString(address)
        }

        private fun getProgname(pArgv: ULong): String = getArg(pArgv, 0)
        private fun getArgs(pArgv: ULong, argc: Int) = List(argc) { getArg(pArgv, it) }.toTypedArray()

        @DontAutoSerialize
        private lateinit var g: Getopt
        private var initialized = false

        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            if (!initialized) {
                val argc = argv[0]
                val pArgv = argv[1]
                val pOptStr = argv[2]

                val optstr = os.sys.readAsciiString(pOptStr)
                val progname = getProgname(pArgv)
                val args = getArgs(pArgv, argc.int).drop(1).toTypedArray()

                initialized = true
                g = Getopt(progname, args, optstr)

                if (optind.linked)
                    optind.value = 1uL
            }

            val result = g.getopt()
            if (result == -1)
                return retval(-1uL)

            if (optind.linked)
                optind.value = g.optind.ulong_z + 1u

            if (optarg.linked) {
                if (optarg.value != 0uL)
                    os.sys.free(optarg.value)
                optarg.value = if (g.optarg != null) os.sys.allocateAsciiString(g.optarg) else 0uL
            }

            if (optopt.linked) {
                optopt.value = g.optopt.ulong_z
                TODO("Unchecked case -> results may be very unexpected")
            }

            return retval(result.ulong_z)
        }
    }
    // POSIX.1-2001, 4.3BSD, SVr4
    // https://linux.die.net/man/2/getpid
    val getpid = object : APIFunction("getpid") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val pid = os.currentProcess.id
            log.fine { "[0x${ra.hex8}] getpid() -> 0x${pid.hex8}" }
            return retval(pid.ulong_z)
        }
    }

    // POSIX.1-2001
    // https://linux.die.net/man/3/sleep
    val sleep = object : APIFunction("sleep") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val seconds = argv[0]
            TODO("Not implemented")
        }
    }

    // POSIX.1-2001, 4.3BSD
    // https://linux.die.net/man/2/getuid
    val getuid = object : APIFunction("getuid") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: ULong) = retval(0uL) // User: root
    }

    // POSIX.1-2001, 4.3BSD
    // https://linux.die.net/man/2/geteuid
    val geteuid = getuid

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/dup2
    val dup2 = object : APIFunction("dup2") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val oldfd = argv[0].int
            val newfd = argv[1].int
            log.fine { "[0x${ra.hex8}] dup2(oldfd=$oldfd newfd=$newfd)" }
            val result = nothrow(-1) { os.filesystem.dup2(oldfd, newfd) }
            return retval(result.ulong_z)
        }
    }

    // POSIX.1-2001
    // https://linux.die.net/man/3/getcwd
    val getcwd = object : APIFunction("getcwd") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val buf = argv[0]
            val size = argv[1].int

            val cwd = os.filesystem.cwd
            if (cwd.length + 1 > size)
                return retval(0uL)

            os.sys.writeAsciiString(buf, cwd)

            return retval(buf)
        }
    }

    // SVr4, 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/chdir
    val chdir = object : APIFunction("chdir") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            // TODO: WTF?

            val path = os.sys.readAsciiString(argv[0])

            log.config { "[0x${ra.hex8}] chdir(path='$path')"}

            println()
            return retval(-1uL)
        }
    }

    // POSIX.1-2001, 4.3BSD
    // https://linux.die.net/man/2/getgid
    val getgid = object : APIFunction("getgid") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: ULong) = retval(gid)
    }

    // POSIX.1-2001
    // https://linux.die.net/man/3/sysconf
    val sysconf = object : APIFunction("sysconf") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val id = argv[0].int

            check(id == _SC_IOV_MAX) { "[0x${ra.hex8}] Not _SC_IOV_MAX: $id" }

            return retval(IOV_MAX.ulong_z)
        }
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/fork
    val fork = object : APIFunction("fork") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val proc = os.copyProcess("forked")

            (os.currentProcess as PosixThread).childProcesses.add(proc as PosixThread)
            proc.parentProcess = os.currentProcess as PosixThread

            proc.context.setReturnValue(0uL)
            proc.context.programCounterValue = os.sys.returnerAddress

            return retval(proc.id.ulong_z)
        }
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/execve
    val execve = object : APIFunction("execve") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val filenameStr = os.sys.readAsciiString(argv[0])
            val _argv = os.sys.readArrayString(argv[1])

            os.currentProcess.saveState()

            // os.load restores state
            // TODO: that's why this is bad
            os.load(filenameStr, _argv)

            log.config { "[0x${ra.hex8}] execve() - env isn't implemented" }
            return redirect(os.abi.programCounterValue)
        }
    }

    // REVIEW: increment is intptr_t
    // REVIEW: return type is void*
    // 4.3BSD; SUSv1, marked LEGACY in SUSv2, removed in POSIX.1-2001
    // https://linux.die.net/man/2/sbrk
    @APIFunc
    fun sbrk(increment: Int): BytePointer {
        log.config { "[0x${ra.hex8}] sbrk(increment='$increment)" }
        if (increment != 0) {
            TODO("Not implemented")
        }
        return BytePointer(os.sys, os.currentProcess.allocator.breakAddress)
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/unlink
    @APIFunc
    fun unlink(path: CharPointer): Int {
        log.warning { "[0x${ra.hex8}] unlink(path='${path.string}') -> not implemented" }
        return 0
    }

    // REVIEW: #include <stdlib.h>
    // SVr4, POSIX.1-2001, 4.3BSD
    // https://linux.die.net/man/3/putenv
    val putenv = object : APIFunction("putenv") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val envData = os.sys.readAsciiString(argv[0]).split('=')
            return if (envData.size != 2) retval(-1uL) else {
                val varName = envData[0]
                val value = envData[1]
                log.fine { "[0x${ra.hex8}] putenv(name='$varName' value='$value')" }
                os.sys.allocateEnvironmentVariable(varName, value)
                retval(0uL)
            }
        }
    }

    // 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/3/mkstemp
    @APIFunc
    fun mkstemp(template: CharPointer): Int {
        log.config { "[0x${ra.hex8}] mkstemp(template='${template.string}')" }
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        val templateString = template.string
        if (!templateString.endsWith("XXXXXX"))
            return -1 // TODO: errno

        val rand = if (os.conf.useEntropy) random else noEntropyRandom
        val krand = rand.asKotlinRandom()
        val sequence = (0..5).map { letters.random(krand) }.joinToString("")
        template.string = templateString.replace("XXXXXX", sequence) // TODO: replace only last symbols
        val deps = sys.deps
        val result = open.exec("", template.address, (deps.O_CREAT or deps.O_TRUNC or O_RDWR).ulong_z)
        return (result as APIResult.Value).data.int
    }

    @APIFunc
    fun mkstemp64(template: CharPointer) = mkstemp(template)

    // 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/3/realpath
    @APIFunc
    fun realpath(path: CharPointer, resolvedPath: CharPointer): CharPointer {
        // TODO: as internal function
        // TODO: PATH_MAX = 256
        val buffer = if (resolvedPath.isNull) CharPointer.allocate(os.sys, 256) else resolvedPath
        val fullPath = os.filesystem.absolutePath(path.string)
        val rootPath = os.filesystem.absolutePath("/")
        val realPath = fullPath.removePrefix(rootPath) // TODO: as filesystem function
        buffer.string = realPath
        return buffer
    }

    // REVIEW: #include <sys/mman.h>
    // SVr4, 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/mmap
    val mmap = object : APIFunction("mmap") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int,
                ArgType.Int, ArgType.Int, ArgType.LongLong)

        override fun exec(name: String, vararg argv: ULong): APIResult {
            val start = argv[0]
            val length = argv[1]
            val prot = argv[2]
            val flags = argv[3]
            val fd = argv[4].int
            val offset = argv[5].int

            if (start != 0uL) TODO("start isn't zero: 0x${start.hex8}")

            // TODO: process prot
            log.config { "[0x${ra.hex8}] mmap(start=$start length=$length fd=$fd offset=$offset) -> parameter prot unused" }

            // TODO: process flags
            log.config { "[0x${ra.hex8}] mmap(start=$start length=$length fd=$fd offset=$offset) -> parameter flags unused" }

            val result = os.sys.mapFileToMemory(fd, length, offset)
            return retval(result)
        }
    }

    val mmap64 = mmap

    // SVr4, 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/munmap
    val munmap = object : APIFunction("munmap") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val start = argv[0]
            val length = argv[1].int

            // TODO: if something wrong then we don't know yet about it and should learn
            os.sys.unmapFileFromMemory(start, length)
            return retval(0u)
        }
    }

    // POSIX.1b. POSIX.1-2001
    // https://linux.die.net/man/2/madvise
    val madvise = object : APIFunction("madvise") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val addr = argv[0]
            val length = argv[1].int
            val advice = argv[2].int
            log.config { "[0x${ra.hex8}] madvise not implemented addr=0x${addr.hex8} length=${length} advice=$advice" }
            return retval(0uL)
        }
    }

    @APIFunc
    fun posix_fadvise(fd: Int, offset: Int, len: size_t, advice: Int): Int {
        log.config { "[0x${ra.hex8}] posix_fadvise(fd=$fd offset=$offset len=$len advice=$advice" }
        return 0
    }

    @APIFunc
    fun posix_fadvise64(fd: Int, offset: LongLong, len: size_t, advice: Int): Int {
        log.config { "[0x${ra.hex8}] posix_fadvise64(fd=$fd offset=$offset len=$len advice=$advice" }
        return 0
    }

    // REVIEW: #include <sys/socket.h>
    // 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/socket
    val socket = object : APIFunction("socket") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val domain = argv[0].int
            val type = argv[1].int
            val protocol = argv[2].int

            val sockStream = sys.deps.SOCK_STREAM
            require(domain == AF_INET && type == sockStream && (protocol == IPPROTO_IP || protocol == IPPROTO_TCP)) {
                "[0x${ra.hex8}] Unknown combination for socket(): domain=$domain, type=$type, protocol=$protocol"
            }
            val socket = nothrow(-1) { os.network.socket() }

            log.fine { "[0x${ra.hex8}] socket(domain=$domain type=$type protocol=$protocol) -> fd = $socket" }

            return retval(socket.ulong_z)
        }
    }
    val _socket = socket
    // SVr4, 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/bind
    val bind = object : APIFunction("bind") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val sockfd = argv[0].int
            val saPtr = argv[1]
            val addrlen = argv[2].int
            val sa = os.abi.reader.sockaddr(saPtr, addrlen)
            require(sa.sin_family == AF_INET) { "[0x${ra.hex8}] Only AF_INET currently supported!" }
            val address = InetSocketAddress(InetAddress.getByAddress(sa.sin_addr), htons(sa.sin_port))
            val result = nothrow(-1) { os.network.bind(sockfd, address); 0 }
            return retval(result.ulong_z)
        }
    }
    val _bind = bind
    // 4.4BSD, SVr4, POSIX.1-2001
    // https://linux.die.net/man/2/sendto
    val sendto = object : APIFunction("sendto") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult = TODO("use socketSystem")
    }
    val _sendto = sendto
    // 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/recvfrom
    val recvfrom = object : APIFunction("recvfrom") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult = TODO("use socketSystem")
    }
    val _recvfrom = recvfrom
    // 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/listen
    val listen = object : APIFunction("listen") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val s = argv[0].int
            val backlog = argv[1].int
            val result = nothrow(-1) { os.network.listen(s, backlog); 0 }
            return retval(result.ulong_z)
        }
    }
    val _listen = listen
    // POSIX.1-2001, SVr4, 4.4BSD
    // https://linux.die.net/man/2/accept
    val accept = object : APIFunction("accept") {
        // https://linux.die.net/man/3/accept
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val s = argv[0].int
            val addr = argv[1]
            val addrlen = argv[2].int

            require(addrlen >= sockaddr.sizeof) { "[0x${ra.hex8}] Wrong sockaddr size: $addrlen" }

            os.block<Pair<Int, InetSocketAddress>> {
                execute {
                    os.network.accept(s)
                }

                success { (fd, inet) ->
                    log.fine { "[0x${ra.hex8}] accept(fd=$s addr=0x${addr.hex8}) -> fd=$fd inet=$inet" }
                    os.abi.writer.sockaddr(addr, inet.address, inet.port)
                    fd.ulong_z
                }

                failure {
                    log.fine { "[0x${ra.hex8}] accept(fd=$s addr=0x${addr.hex8}) -> failed" }
                    setErrno(it); -1uL
                }
            }

            return void()
        }
    }
    val _accept = accept
    // SVr4, 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/getsockopt
    val getsockopt = object : APIFunction("getsockopt") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult = TODO("use socketSystem")
    }
    val _getsockopt = getsockopt
    // 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/recv
    val recv = object : APIFunction("recv") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val s = argv[0].int
            val buf = argv[1]
            val len = argv[2].int
            val flags = argv[3].int

            check(flags == 0) { "[0x${ra.hex8}] The program requires to use the flags value: $flags" }

            os.block<ByteArray> {
                execute {
                    os.network.recv(s, len)
                }

                success {
                    os.abi.writeBytes(buf, it)
                    it.size.ulong_z
                }

                failure {
                    setErrno(it); -1uL
                }
            }

            return void()
        }
    }
    val _recv = recv
    // 4.4BSD, SVr4, POSIX.1-2001
    // https://linux.die.net/man/2/send
    val send = object : APIFunction("send") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val s = argv[0].int
            val buf = argv[1]
            val len = argv[2].int
            val flags = argv[3].int

            check(flags == 0) { "[0x${ra.hex8}] The program requires to use the flags value: $flags" }

            val data = os.abi.readBytes(buf, len)

            val result = nothrow(-1) { os.network.send(s, data); len }

            return retval(result.ulong_z)
        }
    }
    // SVr4, 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/setsockopt
    val setsockopt = object : APIFunction("setsockopt") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val s = argv[0]
            val level = argv[1].int
            val optname = argv[2].int
            val optval = argv[3]
            val optlen = argv[4].int

            // TODO: now nothing to do
            val deps = sys.deps
            check(level == deps.SOL_SOCKET && optname == deps.SO_REUSEADDR || level == IPPROTO_TCP && optname == TCP_CORK) {
                "[0x${ra.hex8}] Unknown combination: level=$level, optname=$optname"
            }

            check(optlen == 4) { "[0x${ra.hex8}] Wrong optlen: $optlen (expected 4)" } // sizeof word

            os.abi.readInt(optval) // Check for access

            return retval(0uL)
        }
    }
    // POSIX.1-2001, 4.4BSD
    // https://linux.die.net/man/3/shutdown
    val shutdown = object : APIFunction("shutdown") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val s = argv[0].int
            val how = argv[1].int
            log.fine { "[0x${ra.hex8}] shutdown(s=$s how=$how) in ${os.currentProcess} " }
            val read = how == SHUT_RDWR || how == SHUT_RD
            val write = how == SHUT_RDWR || how == SHUT_WR
            val error = nothrow(-1) { os.network.shutdown(s, read, write); 0 }
            return retval(error.ulong_z)
        }
    }

    // REVIEW: #include <arpa/inet.h>
    // POSIX.1-2001
    // https://linux.die.net/man/3/htons
    val htons = object : APIFunction("htons") {
        override val args = arrayOf(ArgType.Short)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val value = argv[0]
            return retval(htons(value))
        }
    }
    // POSIX.1-2001
    // https://linux.die.net/man/3/htonl
    val htonl = object : APIFunction("htonl") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val value = argv[0]
            return retval(htonl(value))
        }
    }
    // POSIX.1-2001
    // https://linux.die.net/man/3/inet_ntop
    val inet_ntop = object : APIFunction("inet_ntop") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val af = argv[0].int
            val src = argv[1]
            val dst = argv[2]
            val cnt = argv[3].int

            check(af == AF_INET) { "[0x${ra.hex8}] Not AF_INET: $af" }

            val addr = os.abi.reader.sockaddr(src)

            if (addr.hostname.length + 1 < cnt)
                return retval(0uL)

            os.sys.writeAsciiString(dst, addr.hostname)

            return retval(dst)
        }
    }

    // REVIEW: #include <getopt.h>
    // GNU extension
    // https://linux.die.net/man/3/getopt_long
    val getopt_long = object : APIFunction("getopt_long") {
        override val args = arrayOf(
                ArgType.Int,        // argc
                ArgType.Pointer,    // argv
                ArgType.Pointer,    // optstring
                ArgType.Pointer,    // longopts
                ArgType.Pointer     // longindex
        )
        private fun getArg(pArgv: ULong, index: Int): String {
            val address = os.abi.readPointer(pArgv + os.abi.types.pointer.bytes * index)
            return os.sys.readAsciiString(address)
        }

        private fun getProgname(pArgv: ULong): String = getArg(pArgv, 0)
        private fun getArgs(pArgv: ULong, argc: Int) = List(argc) { getArg(pArgv, it) }.toTypedArray()

        @DontAutoSerialize
        private lateinit var g: Getopt
        private var initialized = false

        override fun exec(name: String, vararg argv: ULong): APIResult {
            val longopts = argv[3]
            val longindex = argv[4]

            val longoptsList = mutableListOf<LongOpt>()
            var ptr = longopts
            val stringBuffers = mutableMapOf<ULong, StringBuffer>()

            while (true) {
                val nameAddress = os.abi.readPointer(ptr)
                if (nameAddress == 0uL)
                    break
                val hasArg = os.abi.readInt(ptr + 4u)
                val flagAddress = os.abi.readPointer(ptr + 8u)
                val value = os.abi.readInt(ptr + 12u)

                val name = os.sys.readAsciiString(nameAddress)
                val flag = if (flagAddress == 0uL) null else StringBuffer().also { stringBuffers[flagAddress] = it }
                longoptsList.add(LongOpt(name, hasArg.int, flag, value.int))

                ptr += 16u
            }

            val array = longoptsList.toTypedArray()

            if (!initialized) {
                val argc = argv[0]
                val pArgv = argv[1]
                val pOptStr = argv[2]

                val optstr = os.sys.readAsciiString(pOptStr)
                val progname = getProgname(pArgv)
                val args = getArgs(pArgv, argc.int).drop(1).toTypedArray()

                initialized = true
                g = Getopt(progname, args, optstr, array)

                if (optind.linked)
                    optind.value = 1uL
            }

            val result = g.getopt()

            if (longindex != 0uL) {
                os.abi.writeLong(longindex, g.longind.ulong_z)
            }

            if (optind.linked)
                optind.value = g.optind.ulong_z + 1u

            if (optarg.linked)
                optarg.value = if (g.optarg != null) os.sys.allocateAsciiString(g.optarg) else 0uL

            return retval(result.ulong_z)
        }
    }
    // TODO: merge with getopt_long
    // GNU extension
    // https://linux.die.net/man/3/getopt_long_only
    val getopt_long_only = object : APIFunction("getopt_long_only") {
        override val args = arrayOf(
                ArgType.Int,        // argc
                ArgType.Pointer,    // argv
                ArgType.Pointer,    // optstring
                ArgType.Pointer,    // longopts
                ArgType.Pointer     // longindex
        )
        private fun getArg(pArgv: ULong, index: Int): String {
            val address = os.abi.readPointer(pArgv + os.abi.types.pointer.bytes * index)
            return os.sys.readAsciiString(address)
        }

        private fun getProgname(pArgv: ULong): String = getArg(pArgv, 0)
        private fun getArgs(pArgv: ULong, argc: Int) = List(argc) { getArg(pArgv, it) }.toTypedArray()

        @DontAutoSerialize
        private lateinit var g: Getopt
        private var initialized = false

        override fun exec(name: String, vararg argv: ULong): APIResult {
            val longopts = argv[3]
            val longindex = argv[4]

            val longoptsList = mutableListOf<LongOpt>()
            var ptr = longopts
            val stringBuffers = mutableMapOf<ULong, StringBuffer>()

            while (true) {
                val nameAddress = os.abi.readPointer(ptr)
                if (nameAddress == 0uL)
                    break
                val hasArg = os.abi.readInt(ptr + 4u)
                val flagAddress = os.abi.readPointer(ptr + 8u)
                val value = os.abi.readInt(ptr + 12u)

                val name = os.sys.readAsciiString(nameAddress)
                val flag = if (flagAddress == 0uL) null else StringBuffer().also { stringBuffers[flagAddress] = it }
                longoptsList.add(LongOpt(name, hasArg.int, flag, value.int))

                ptr += 16u
            }

            val array = longoptsList.toTypedArray()

            if (!initialized) {
                val argc = argv[0]
                val pArgv = argv[1]
                val pOptStr = argv[2]

                val optstr = os.sys.readAsciiString(pOptStr)
                val progname = getProgname(pArgv)
                val args = getArgs(pArgv, argc.int).drop(1).toTypedArray()

                initialized = true
                g = Getopt(progname, args, optstr, array, true)

                if (optind.linked)
                    optind.value = 1u
            }

            val result = g.getopt()


            if (longindex != 0uL)
                os.abi.writeInt(longindex, g.longind.ulong_z)

            if (optind.linked)
                optind.value = g.optind.ulong_z + 1u

            if (optarg.linked)
                optarg.value = if (g.optarg != null) os.sys.allocateAsciiString(g.optarg) else 0uL

            return retval(result.ulong_z)
        }
    }

    // REVIEW: #include <sys/resource.h>
    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/getrlimit
    val getrlimit = object : APIFunction("getrlimit") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val resource = argv[0].int
            val rlim = argv[1]

            log.config { "[0x${ra.hex8}] Not fair implementation of getrlimit -> values depends on arch" }
            val rlimitNofile = sys.deps.RLIMIT_NOFILE
            check(resource == rlimitNofile) { "[0x${ra.hex8}] TODO: Not implemented" }

            os.abi.writeBytes(rlim, rlimit(0x1000u, 0x1000u).asBytes)
            return retval(0u)
        }
    }

    val getrlimit64 = getrlimit

    // SVr4, 4.3BSD. POSIX.1-2001
    // https://linux.die.net/man/2/getrusage
    @APIFunc
    fun getrusage(who: Int, usage: rusage): Int {
        log.warning { "[0x${ra.hex8}] getrusage(who=$who rusage=$usage) - not implemented" }

        usage.ruUtime.tvSec = 0
        usage.ruUtime.tvUsec = 0
        usage.ruStime.tvSec = 0
        usage.ruStime.tvUsec = 0

        return 0
    }

    @APIFunc
    fun __getrusage_time64(who: Int, usage: rusage) = getrusage(who, usage)

    // REVIEW: #include <sys/stat.h>
    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/umask
    @APIFunc
    fun umask(mask: mode_t): mode_t = umaskValue.uint.also {
        log.fine { "[0x${ra.hex8}] umask(mask=${mask.oct3})" }
        umaskValue = mask.int and 0x1FF // 0777
    }
    // REVIEW: mode is mode_t
    // 4.4BSD, SVr4, POSIX.1-2001
    // https://linux.die.net/man/2/chmod
    @APIFunc
    fun chmod(path: CharPointer, mode: mode_t): Int {
        val pathString = path.string
        log.fine { "[0x${ra.hex8}] chmod(path='${pathString}' mode=${mode.oct3}) -> not implemented" }
        return nothrow(-1) {
            if (!os.filesystem.exists(pathString))
                throw IONoSuchFileOrDirectory(pathString)
            0 // TODO: get rid of fd
        }
    }

    // REVIEW: not implemented
    // REVIEW: uid_t, gid_t
    // 4.4BSD, SVr4, POSIX.1-2001
    // https://linux.die.net/man/2/chown
    @APIFunc
    fun chown() = 0.also { log.fine { "[0x${ra.hex8}] chown(...) -> not implemented" }}

    // REVIEW: #include <syslog.h>
    // SUSv2, POSIX.1-2001
    // https://linux.die.net/man/3/openlog
    val openlog = object : APIFunction("openlog") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val ident = os.sys.readAsciiString(argv[0])
            val option = argv[1]
            val facility = argv[2].int

            check(option and inv(0x03uL) == 0uL) { "[0x${ra.hex8}] Unknown option: $option" }
            log.config { "[0x${ra.hex8}] openlog() -> facility not implemented" }

            openlogIdent = ident
            return void()
        }
    }

    // REVIEW: #include <sys/select.h>
    // POSIX.1-2001, 4.4BSD
    // https://linux.die.net/man/2/select
    val select = object : APIFunction("select") {

        fun selectSingle(ptr: ULong, n: Int, block: (Int) -> Boolean): Int {
            var count = 0
            if (ptr != 0uL) {
                val fdSet = fd_set(os.abi.readBytes(ptr, fd_set.sizeof), n)
                for (i in 0 until n) {
                    val major = i / 32
                    val minor = i % 32
                    val value = fdSet.fds_bits[major]
                    if (value[minor].truth) {
                        if (block(i)) {
                            fdSet.fds_bits[major] = value.set(minor)
                            count++
                        } else {
                            fdSet.fds_bits[major] = value.clr(minor)
                        }
                    }
                }
                os.abi.writeBytes(ptr, fdSet.asBytes)
            }
            return count
        }

        // https://linux.die.net/man/2/select
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Pointer, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val n = argv[0].int
            val readfdsPtr = argv[1]
            val writefdsPtr = argv[2]
            val exceptfdsPtr = argv[3]
            val timeout = os.abi.reader.timeval(argv[4])

            if (n == 0) TODO("Sleep use")

            log.fine { "[0x${ra.hex8}] select(timeout=$timeout)" }

            val readCount = selectSingle(readfdsPtr, n) { fd -> os.ioSystem.readable(fd) }

            val writeCount = selectSingle(writefdsPtr, n) { fd -> os.ioSystem.writable(fd) }

            val exceptCount = selectSingle(exceptfdsPtr, n) { false } // TODO: catch exceptions?

            return retval(maxOf(readCount, writeCount, exceptCount).ulong_z)
        }
    }

    // REVIEW: #include <poll.h>
    // POSIX.1-2001
    // https://linux.die.net/man/2/poll
    val poll = object : APIFunction("poll") {
        val mask = POLLIN or POLLPRI or POLLOUT /*or POLLERR or POLLHUP or POLLNVAL*/

        private fun pollfd.isAnyEvents(mask: ULong) = events.ulong_z and mask != 0uL

        private fun pollfd.poll(): Boolean {
            check(!isAnyEvents(mask.inv())) { "[0x${ra.hex8}] Unimplemented flags: ${events.hex4}" }

            if (fd < 0) {
                // If this field is negative, then the corresponding
                // events field is ignored and the revents field returns zero.
                revents = 0
            } else if (os.ioSystem.isOpen(fd)) {
                var changed = false

                if (os.ioSystem.writable(fd)) {
                    revents = revents or POLLOUT.short
                    changed = true
                }

                if (os.ioSystem.readable(fd)) {
                    revents = revents or POLLIN.short
                    changed = true
                }

                if (changed) {
                    log.finer { "[0x${ra.hex8}] poll(fd=${fd}) -> revents = 0x${revents.hex4}" }
                    return true
                }
            } else {
                // https://man7.org/linux/man-pages/man2/poll.2.html
                // TODO: we must ignore if file descriptor isn't ok?
                log.fine { "[0x${ra.hex8}] file descriptor $fd isn't opened -> skip revents" }
            }

            return false
        }

        private fun List<pollfd>.poll(timeout: ULong = 0u): Int {
            if (timeout == 0uL)
                return count { it.poll() }

            var count: Int
            var remain = timeout
            do {
                val duration = remain.coerceAtMost(50u)
                Thread.sleep(duration.long)
                count = count { it.poll() }
                remain -= duration
            } while (count == 0 && remain != 0uL)

            return count
        }

        // https://linux.die.net/man/2/poll
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val fds = argv[0]
            val nfds = argv[1].int
            val timeout = argv[2]

            require(timeout != -1uL) { "[0x${ra.hex8}] timeout == -1 is not implemented!" }

            log.fine { "[0x${ra.hex8}] poll(nfds=$nfds timeout=$timeout)" }

            val structs = List(nfds) { os.abi.reader.pollfd(fds, it) }

            os.block<Int> {
                execute {
                    structs.poll(timeout)
                }

                success { count ->
                    structs.forEachIndexed { i, pollfd -> os.abi.writer.pollfd(fds, pollfd, i) }
                    count.ulong_z
                }

                failure {
                    // EFAULT, EINTR, EINVAL, ENOMEM <- currently not thrown
                    setErrno(it); -1uL
                }
            }

            return void()
        }
    }

    // REVIEW: #include <sys/uio.h>
    // 4.4BSD, POSIX.1-2001
    // https://linux.die.net/man/2/writev
    val writev = object : APIFunction("writev") {

        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val fd = argv[0].int
            val vector = argv[1]
            val count = argv[2].int

            val iovecs = List(count) { i -> iovec(os.abi.readBytes(vector + i * iovec.sizeof, iovec.sizeof)) }

            var total = 0uL

            iovecs.forEach {
                val result = write.exec(name, fd.ulong_z, it.iov_base, it.iov_len) as APIResult.Value
                if (result.data == -1uL)
                    return retval(-1uL)
                total += result.data
            }

            return retval(total)
        }
    }

    // REVIEW: #include <setjmp.h>
    // Unknown POSIX
    // https://linux.die.net/man/3/_setjmp
    @APIFunc
    fun _setjmp(env: jmp_buf): Int {
        env.store()
        return 0
    }
    // Unknown POSIX
    // https://linux.die.net/man/3/_longjmp
    @APIFunc
    fun __longjmp_chk(env: jmp_buf, value: Int) {
        env.restore()
        os.abi.setReturnValue(if (value == 0) 1uL else value.ulong_z) // TODO: return?
    }

    // REVIEW: #include <sys/wait.h>
    // REVIEW: return type is pid_t
    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/wait
    @APIFunc
    fun wait(status: IntPointer)/*: Int*/ {
        val current = (os.currentProcess as PosixThread)
        check(current.childProcesses.isNotEmpty()) {
            "No child processes"
        }
        os.block<PosixThread> {
            execute {
                current.exitedProcesses.take()
            }
            success {
                if (status.isNotNull) {
                    val statusValue = if (it.isSegfault)
                        SIGSEGV
                    else
                        it.context.returnValue.int shl 8
                    status.set(statusValue)
                }
                it.id.ulong_z
            }
            failure {
                -1uL
            }
        }
    }

    // REVIEW: #include <libintl.h>
    // REVIEW: unknown standard (GNU/Linux?)
    // https://linux.die.net/man/3/bindtextdomain
    val bindtextdomain = object : APIFunction("bindtextdomain") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val domainname = os.sys.readAsciiString(argv[0])
            val dirname = os.sys.readAsciiString(argv[1])

            return retval(os.sys.allocateAsciiString(dirname))
        }
    }
    // REVIEW: unknown standard (GNU/Linux?)
    // https://linux.die.net/man/3/textdomain
    val textdomain = object : APIFunction("textdomain") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val domainname = os.sys.readAsciiString(argv[0])

            return retval(os.sys.allocateAsciiString(domainname))
        }
    }
    // REVIEW: unknown standard (GNU/Linux?)
    // https://linux.die.net/man/3/dcgettext
    val dcgettext = object : APIFunction("dcgettext") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val msgid = os.sys.readAsciiString(argv[1])

            return retval(os.sys.allocateAsciiString(msgid))
        }
    }

    // REVIEW: #include <fnmatch.h>
    // POSIX.2
    // https://linux.die.net/man/3/fnmatch
    @APIFunc
    fun fnmatch(pattern: CharPointer, string: CharPointer, flags: Int): Int {
        log.config { "[0x${ra.hex8}] fnmatch(pattern='${pattern.string}' string='${string.string}')" }
        val matcher = FileSystems.getDefault().getPathMatcher("glob:${pattern.string}")
        return if (matcher.matches(Paths.get(string.string))) 0 else 1 // FNM_NOMATCH = 1
    }

    // REVIEW: #include <obstack.h>
    // https://code.woboq.org/userspace/glibc/malloc/obstack.c.html
    // Linux
    // https://refspecs.linuxfoundation.org/LSB_1.3.0/gLSB/gLSB/baselib--obstack-begin.html
    @APIFunc
    fun _obstack_begin(
            h: obstack,
            size: Int,
            alignment: Int,
            chunkfun: FunctionPointer,
            freefun: FunctionPointer
    ) = withCallback(h.address, size.ulong_z, alignment.ulong_z, chunkfun.address, freefun.address) {
        log.warning { "[0x${ra.hex8}] _obstack_begin(h=$h size=${size.hex8} alignment=${alignment.hex8} chunkfun=$chunkfun freefun=$freefun)" }
        val concreteAlignment = if (alignment != 0) alignment else 4 // TODO: way to determine alignment
        val concreteSize = if (size != 0) size else objstackDefaultAlignment

        h.chunkfun = chunkfun.address
        h.freefun = freefun.address
        h.chunkSize = concreteSize
        h.alignmentMask = concreteAlignment - 1
        h.useExtraArg = false

        check(!h.useExtraArg) { "Not implemented" }
        val chunk = _obstack_chunk(os.sys, it.interrupt(h.chunkfun, h.chunkSize.ulong_z))
        check(chunk.isNotNull) { "Not implemented" }
        h.chunk = chunk

        h.objectBase = BytePointer(os.sys, chunk.address) // TODO: alignment
        h.nextFree = h.objectBase.address
        chunk.limit = chunk.address + h.chunkSize
        h.chunkLimit = chunk.limit
        chunk.prev = _obstack_chunk(os.sys, 0uL)
        /* The initial chunk now contains no empty object.  */
        h.maybeEmptyObject = false
        h.allocFailed = false

        Unit
    }
    // Linux
    // https://refspecs.linuxfoundation.org/LSB_1.2.0/gLSB/baselib--obstack-newchunk.html
    @APIFunc
    fun _obstack_newchunk(h: obstack, length: Int) = withCallback(h.address, length.ulong_z) {
        log.warning { "[0x${ra.hex8}] _obstack_newchunk(h=$h length=${length.hex8})" }
        val oldChunk = h.chunk
        val objSize = h.nextFree - h.objectBase.address

        val chunkSize = h.chunkSize.ulong_z

        /* Compute size for new chunk.  */
        var newSize = (objSize + length) + (objSize ushr 3) + h.alignmentMask + 100uL
        if (newSize < chunkSize) newSize = chunkSize

        /* Allocate and initialize the new chunk.  */
        check(!h.useExtraArg) { "Not implemented" }
        val newChunk = _obstack_chunk(os.sys, it.interrupt(h.chunkfun, newSize))
        check(newChunk.isNotNull) { "Not implemented" }
        h.chunk = newChunk
        newChunk.prev = oldChunk
        h.chunkLimit = newChunk.address + newSize
        newChunk.limit = h.chunkLimit

        /* Compute an aligned object_base in the new chunk */
        val objectBase = BytePointer(os.sys, newChunk.address) // TODO: alignment // TODO: casts

        /* Move the existing object to the new chunk.
           Word at a time is fast and is safe if the object
           is sufficiently aligned.  */
        val already = if (h.alignmentMask + 1 < objstackDefaultAlignment) 0 else {
            TODO("Not implemented")
        }

        /* Copy remaining bytes one by one.  */
        objectBase.store(h.objectBase.load(objSize.int - already, already), already)

        /* If the object just copied was the only data in OLD_CHUNK,
           free that chunk and remove it from the chain.
           But not if that chunk might contain an empty object.  */
        if (!h.maybeEmptyObject && (h.objectBase.address == oldChunk.address)) {// TODO: alignment
            newChunk.prev = oldChunk.prev
            it.interrupt(h.freefun, oldChunk.address)
        }

        h.objectBase = objectBase
        h.nextFree = h.objectBase.address + objSize
        /* The new chunk certainly contains no empty object yet.  */
        h.maybeEmptyObject = false

        Unit
    }
    // Linux
    // https://refspecs.linuxfoundation.org/LSB_1.2.0/gLSB/baselib-obstack-free.html
    @APIFunc
    fun _obstack_free(h: obstack, obj: BytePointer) = withCallback(h.address, obj.address) {
        var lp = h.chunk /* below addr of any objects in this chunk */

        /* We use >= because there cannot be an object at the beginning of a chunk.
           But there can be an empty object at that address
           at the end of another chunk.  */
        while (lp.isNotNull && (lp.address >= obj.address || lp.limit < obj.address)) {
            val plp = lp.prev /* point to previous chunk if any */
            check(!h.useExtraArg) { "Not implemented" }
            it.interrupt(h.freefun, lp.address)
            lp = plp
            /* If we switch chunks, we can't tell whether the new current
               chunk contains an empty object, so assume that it may.  */
            h.maybeEmptyObject = true
        }
        when {
            lp.isNotNull -> {
                h.nextFree = obj.address
                h.objectBase = obj
                h.chunkLimit = lp.limit
                h.chunk = lp
            }
            obj.isNotNull -> {
                /* obj is not in any of the chunks! */
                // abort();
                throw IllegalArgumentException("Abort")
            }
        }

        Unit
    }


    // REVIEW --- No single standard ---

    // REVIEW: #include <sys/ioctl.h>
    // https://linux.die.net/man/2/ioctl
    val ioctl = object : APIFunction("ioctl") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Pointer)
        override fun exec(name: String, vararg argv: ULong): APIResult {
            val fd = argv[0].int
            val cmd = argv[1].int
            val argp = argv[2]

            val error = nothrow(-1) {
                // EBADF, EFAULT, EINVAL, ENOTTY, ENOTTY
                when (cmd) {
                    sys.deps.FIONREAD -> {
                        val available = os.ioSystem.available(fd)
                        log.fine { "[0x${ra.hex8}] ioctl(fd=$fd cmd=FIONREAD argp=0x${argp.hex8}) -> $available" }
                        os.abi.writeInt(argp, available.ulong_z)
                    }

                    else -> {
                        log.config { "[0x${ra.hex8}] ioctl not implemented cmd=0x${cmd.hex8} fd=${fd} arg=0x${argp.hex8}" }
                    }
                }
                return@nothrow 0
            }

            return retval(error.ulong_z)
        }
    }

    // REVIEW: #include <malloc.h>
    // https://linux.die.net/man/3/mallopt
    val mallopt = object : APIFunction("mallopt") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: ULong) = retval(1uL)
    }

    // REVIEW: #include <selinux/selinux.h>
    // https://man7.org/linux/man-pages/man3/freecon.3.html
    @APIFunc
    fun freecon(con: CharPointer) {
        con.free()
    }


    // TODO: Add aligment
    // https://man7.org/linux/man-pages/man3/posix_memalign.3.html
    @APIFunc
    fun posix_memalign(memptr: PointerPointer, aligment: size_t, size: size_t): Int {
        StdlibAPI.log.finest { "[0x${ra.hex8}] malloc(size=$size)" }
        val address = sys.allocate(size.int)
        return if (address != -1uL) {
            memptr.set(address)
            0
        } else 1
    }

    // https://man7.org/linux/man-pages/man2/gettimeofday.2.html
    @APIFunc
    fun gettimeofday(tvptr: StructPointer, tzptr: StructPointer): Int {
        if (tvptr.isNotNull) {
            val tv = timeval.from_millis(sys.time)
            tvptr.setBytes(0, tv.asBytes)
        }
        if (tzptr.isNotNull) {
            val tz = timezone(sys.timezone.rawOffset / 1000 / 60)
            tzptr.setBytes(0, tz.asBytes)
        }
        return 0
    }

    @APIFunc
    fun gettimeofday_time64(tvptr: StructPointer, tzptr: StructPointer): Int {
        if (tvptr.isNotNull) {
            val tv = timeval64.from_millis(sys.time)
            tvptr.setBytes(0, tv.asBytes)
        }
        if (tzptr.isNotNull) {
            val tz = timezone(sys.timezone.rawOffset / 1000 / 60)
            tzptr.setBytes(0, tz.asBytes)
        }
        return 0
    }

    @APIFunc
    fun __gettimeofday_time64(tv: StructPointer, tz: StructPointer) = gettimeofday_time64(tv, tz)

    @APIFunc
    fun signal(signum: Int) {
        // TODO: Realize sending signal
        log.config { "Send signal ${signum}" }
        return
    }
}