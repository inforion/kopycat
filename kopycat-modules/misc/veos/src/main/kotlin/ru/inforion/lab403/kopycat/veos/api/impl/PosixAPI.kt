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
@file:Suppress("unused", "LocalVariableName", "PropertyName", "ObjectPropertyName", "MemberVisibilityCanBePrivate", "FunctionName")

package ru.inforion.lab403.kopycat.veos.api.impl

import gnu.getopt.Getopt
import gnu.getopt.LongOpt
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.extensions.collect
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.*
import ru.inforion.lab403.kopycat.veos.api.misc.*
import ru.inforion.lab403.kopycat.veos.api.misc.BytePointer
import ru.inforion.lab403.kopycat.veos.api.misc.CharPointer
import ru.inforion.lab403.kopycat.veos.api.misc.FunctionPointer
import ru.inforion.lab403.kopycat.veos.api.misc.toStdCErrno
import ru.inforion.lab403.kopycat.veos.exceptions.io.*
import ru.inforion.lab403.kopycat.veos.filesystems.AccessFlags
import ru.inforion.lab403.kopycat.veos.kernel.System
import ru.inforion.lab403.kopycat.veos.ports.cstdlib.CTypeB
import ru.inforion.lab403.kopycat.veos.ports.posix.*
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixReader.Companion.reader
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixWriter.Companion.writer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.or
import kotlin.random.asKotlinRandom


class PosixAPI(os: VEOS<*>) : API(os) {

    companion object {
        @Transient val log = logger(FINE)
    }

    override fun setErrno(error: Exception?) {
        errno.allocated.value = error?.toStdCErrno(ra)?.id ?: PosixError.ESUCCESS.id
    }

    fun htons(value: Int) = if (os.abi.bigEndian) value else value.swap16()

    fun htons(value: Long) = if (os.abi.bigEndian) value else value.swap16()

    fun htonl(value: Long) = if (os.abi.bigEndian) value else value.swap32()

    val argc = APIVariable.int(os, "_argc")
    val argv = APIVariable.pointer(os, "_argv")
    val errno = APIVariable.int(os, "errno")
    val lasterrno = APIVariable.pointer(os, "lasterrno")
    val stderr = APIVariable.pointer(os, "_Stderr")
    val stdout = APIVariable.pointer(os, "_Stdout")
    val optind = APIVariable.int(os, "optind")
    val optarg = APIVariable.pointer(os, "optarg")
    val optopt = APIVariable.int(os, "optopt")
    val ctype_b = APIVariable.int(os, "__ctype_b")

    init {
        type(ArgType.Pointer) { _, it -> rusage(os.sys, it) }
        type(ArgType.Pointer) { _, it -> obstack(os.sys, it) }
        type(ArgType.Pointer) { _, it -> DIR(os.sys, it) }
        type(ArgType.Pointer) { _, it -> jmp_buf(os.sys, it) }
        type(ArgType.Pointer) { _, it -> sigset_t(os.sys, it) }
        type(ArgType.Pointer) { _, it -> SysDep[os.abi.core].toSigaction(os.sys, it) }

        ret<DIR> { APIResult.Value(it.address) }
    }

    override fun init(argc: Long, argv: Long, envp: Long) {
        if (this.argc.linked && this.argv.linked) {
            this.argc.value = argc
            this.argv.value = argv
        }

        // Allocate __ctype_b table
        if (ctype_b.linked) {
            val table = CTypeB.data
            val size = table.size * os.sys.sizeOf.short // sizeof(uint16_t)
            val buffer = ByteBuffer.allocate(size).order(os.currentMemory.endian)
            table.forEach {
                buffer.putShort(it.asShort)
            }
            ctype_b.value = os.sys.allocateArray(buffer.array(), os.systemData)
        }
    }

    /* --------------------- POSIX functions --------------------------------- */

    val _init_libc = nullsub("_init_libc")
    val _preinit_array = nullsub("_preinit_array")
    val _init = nullsub("_init")
    val _init_array = nullsub("_init_array")
    val _fini_array = nullsub("_fini_array")
    val __aeabi_atexit = nullsub("__aeabi_atexit")

    val _xstream = nullsub("_xstream")

    val sigemptyset = nullsub("sigemptyset")
    val sigaddset = nullsub("sigaddset")

    val __cxa_guard_acquire = object : APIFunction("__cxa_guard_acquire") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            // Returns 1 if the caller needs to run the initializer and then either
            // call __cxa_guard_release() or __cxa_guard_abort().  If zero is returned,
            // then the initializer has already been run.
            val p_guard_object = argv[0]
            var guard_value = os.abi.readInt(p_guard_object)
            return if (guard_value != 1L) {
                guard_value = 1L
                os.abi.writeInt(p_guard_object, guard_value)
                retval(guard_value)
            } else retval(0)
        }
    }

    val __cxa_guard_release = object : APIFunction("__cxa_guard_release") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            // Sets the first byte of the guard_object to a non-zero value.
            // Releases any locks acquired by __cxa_guard_acquire().
            val p_guard_object = argv[0]
            os.abi.writeChar(p_guard_object, 0)
            return void()
        }
    }

    val __get_errno_ptr = object : APIFunction("__get_errno_ptr") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.finest { "[0x${ra.hex8}] get_errno_ptr()" }
            val p_errno = errno.allocated.address!!
            return retval(p_errno)
        }
    }
    val __errno_location = __get_errno_ptr

    val __stackavail = object : APIFunction("__stackavail") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.severe { "[0x${ra.hex8}] __stackavail returns always 0x1000 ... should be fixed" }
            return retval(0x1000)
        }
    }

    val strcasecmp = object : APIFunction("strcmp") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val str1 = os.sys.readAsciiString(argv[0]).toLowerCase()
            val str2 = os.sys.readAsciiString(argv[1]).toLowerCase()
            return retval(str1.compareTo(str2).asLong)
        }
    }

    val strncasecmp = object : APIFunction("strncasecmp") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val n = argv[2].asInt
            var str1 = os.sys.readAsciiString(argv[0])
            var str2 = os.sys.readAsciiString(argv[1])
            // TODO: make own implementation of toLowerCaseAsciiOnly
            str1 = str1[0..minOf(n, str1.length)].toLowerCaseAsciiOnly()
            str2 = str2[0..minOf(n, str2.length)].toLowerCaseAsciiOnly()
            return retval(str1.compareTo(str2).asLong)
        }
    }

    val stpcpy = object : APIFunction("stpcpy") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val dst = argv[0]
            val src = os.sys.readAsciiString(argv[1])
            os.sys.writeAsciiString(dst, src)
            return retval(dst + src.length)
        }
    }

    val strdup = object : APIFunction("strdup") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val string = os.sys.readAsciiString(argv[0])
            val result = os.sys.allocate(string.length + 1)
            os.sys.writeAsciiString(result, string, true)
            return retval(result)
        }
    }

    @APIFunc fun __strdup(string: CharPointer) = strdup.exec("__strdup", string.address)

    val open = object : APIFunction("open") {
        val O_ACCMODE = 3

        infix fun Long.check(b: Int) = (this and b.asULong).toBool()

        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val filenameStr = os.sys.readAsciiString(argv[0])
            val mode = argv[1]

            val sysDep = SysDep[os.abi.core]

            val accmode = mode.asInt and O_ACCMODE
            val flags = AccessFlags(
                    accmode == O_RDWR || accmode == O_RDONLY,
                    accmode == O_RDWR || accmode == O_WRONLY,
                    mode check sysDep.O_APPEND,
                    mode check sysDep.O_CREAT,
                    mode check sysDep.O_TRUNC,
                    mode check sysDep.O_EXCL
            )

            val mask = (O_ACCMODE or sysDep.O_TRUNC or sysDep.O_APPEND or sysDep.O_CREAT or sysDep.O_EXCL or sysDep.O_LARGEFILE).asULong.inv()
            check(mode and mask == 0L) { "[0x${ra.hex8}] Unknown combination of flags ($filenameStr): 0x${mode.hex8}" }

            val result = os.filesystem.open(filenameStr, flags)

            log.fine { "[0x${ra.hex8}] open(file='$filenameStr' mode='$flags') -> fd = $result in ${os.currentProcess}" }

            return retval(result.asLong)
        }
    }

    val open64 = open

    val close = object : APIFunction("close") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val fd = argv[0].asInt
            log.fine { "[0x${ra.hex8}] close(fd=${argv[0]})" }
            val result = nothrow(-1) { os.ioSystem.close(fd); 0 }
            return retval(result.asLong)
        }
    }

    val read = object : APIFunction("read") {
        // https://linux.die.net/man/2/read
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val fd = argv[0].asInt
            val buf = argv[1]
            val count = argv[2].asInt

            if (count == 0)
                return retval(0)

            os.block<ByteArray> {
                execute {
                    os.ioSystem.read(fd, count)
                }

                success {
                    log.fine { "[0x${ra.hex8}] read(fd=$fd buf=0x${buf.hex8} count=$count) -> read=${it.size} in ${os.currentProcess}" }
                    os.abi.writeBytes(buf, it); it.size.asULong
                }

                failure {
                    log.fine { "[0x${ra.hex8}] read(fd=$fd buf=0x${buf.hex8} count=$count) -> failed in ${os.currentProcess}" }
                    setErrno(it); 0
                }
            }

            return void()
        }
    }

    val write = object : APIFunction("write") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val fd = argv[0].asInt
            val buf = argv[1]
            val count = argv[2] // TODO: overflow?  // KC-1848 - непонятно

            log.fine { "[0x${ra.hex8}] write(fd=$fd buf=0x${buf.hex8} count=$count) in ${os.currentProcess}" }

            val data = os.abi.readBytes(buf, count.asInt)
            val result = nothrow(-1) { os.ioSystem.write(fd, data); data.size }
            return retval(result.asLong)
        }
    }

    val fgetc = object : APIFunction("fgetc") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val stream = argv[0].asInt

            log.fine { "[0x${ra.hex8}] fgetc(stream=$stream) in ${os.currentProcess}" }

            os.block<ByteArray> {
                execute {
                    os.filesystem.read(stream, 1)
                }

                success {
                    it.single().asULong
                }

                failure {
                    setErrno(it); -1
                }
            }

            return void()
        }
    }

    val tzset = object : APIFunction("tzset") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long) = retval(0)
    }

    // TODO: check parameters on every call?   KC-1848 - описать подробнее, если можно в одно предложение то лучше в одно, а не в 5 туду:)
    // TODO: multiple calls?
    // TODO: non-default argc, argv arguments?
    val getopt = object : APIFunction("getopt") {
        private fun getArg(pArgv: Long, index: Int): String {
            val address = os.abi.readPointer(pArgv + os.abi.types.pointer.bytes * index)
            return os.sys.readAsciiString(address)
        }

        private fun getProgname(pArgv: Long): String = getArg(pArgv, 0)
        private fun getArgs(pArgv: Long, argc: Int) = collect(argc) { getArg(pArgv, it) }.toTypedArray()

        @DontAutoSerialize
        private lateinit var g: Getopt
        private var initialized = false

        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            if (!initialized) {
                val argc = argv[0]
                val pArgv = argv[1]
                val pOptStr = argv[2]

                val optstr = os.sys.readAsciiString(pOptStr)
                val progname = getProgname(pArgv)
                val args = getArgs(pArgv, argc.asInt).drop(1).toTypedArray()

                initialized = true
                g = Getopt(progname, args, optstr)

                if (optind.linked)
                    optind.value = 1
            }

            val result = g.getopt()
            if (result == -1)
                return retval(-1)

            if (optind.linked)
                optind.value = g.optind.asULong + 1

            if (optarg.linked) {
                if (optarg.value != 0L)
                    os.sys.free(optarg.value)
                optarg.value = if (g.optarg != null) os.sys.allocateAsciiString(g.optarg) else 0L
            }

            if (optopt.linked) {
                optopt.value = g.optopt.asULong
                TODO("Unchecked case -> results may be very unexpected")
            }

            return retval(result.asULong)
        }
    }

    val getpid = object : APIFunction("getpid") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            val pid = os.currentProcess.id
            log.fine { "[0x${ra.hex8}] getpid() -> 0x${pid.hex8}" }
            return retval(pid.asLong)
        }
    }

    val putenv = object : APIFunction("putenv") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val envData = os.sys.readAsciiString(argv[0]).split('=')
            return if (envData.size != 2) retval(-1) else {
                val varName = envData[0]
                val value = envData[1]
                log.fine { "[0x${ra.hex8}] putenv(name='$varName' value='$value')" }
                os.sys.allocateEnvironmentVariable(varName, value)
                retval(0)
            }
        }
    }

    val pthread_once = object : APIFunction("pthread_once") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val once_control = argv[0]
            val init_routine = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_once @ ${init_routine.hex8}" }
            TODO("Not implemented")
        }
    }

    val pthread_attr_init = object : APIFunction("pthread_attr_init") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            log.fine { "[0x${ra.hex8}] pthread_attr_init(attr=0x${attr.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_attr_destroy = object : APIFunction("pthread_attr_destroy") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            log.fine { "[0x${ra.hex8}] pthread_attr_destroy(attr=0x${attr.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_attr_getschedparam = object : APIFunction("pthread_attr_getschedparam") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val param = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_getschedparam(attr=0x${attr.hex8} param=0x${param.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_attr_setschedparam = object : APIFunction("pthread_attr_setschedparam") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val param = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_setschedparam(attr=0x${attr.hex8} param=0x${param.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_attr_setinheritsched = object : APIFunction("pthread_attr_setinheritsched") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val inheritsched = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_setinheritsched(attr=0x${attr.hex8} inherited=0x${inheritsched.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_attr_setschedpolicy = object : APIFunction("pthread_attr_setschedpolicy") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val policy = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_setschedparam(attr=0x${attr.hex8} policy=0x${policy.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_attr_setstacksize = object : APIFunction("pthread_attr_setstacksize") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val stacksize = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_setschedparam(attr=0x${attr.hex8} stacksize=0x${stacksize.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_create = object : APIFunction("pthread_create") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val thread = argv[0]
            val attr = argv[1]
            val start_routine = argv[2]
            val arg = argv[3]

            var stackSize = os.conf.stackSize
            var priority: Int = 0 // TODO: right priority
            if (attr != 0L) {
                val th_attr = pthread_attr_t.fromBytes(os.abi.readBytes(attr, pthread_attr_t.sizeof))
                stackSize = th_attr.__stacksize
                priority = th_attr.__schedparam.sched_priority.asInt
            }

            val currentThread = os.createThread() as PosixThread
            val currentProcess = os.currentProcess as PosixThread

            currentProcess.childProcesses.add(currentThread)
            currentThread.parentProcess = currentProcess

            val stack = os.currentMemory.allocateBySize("stack_${currentThread.id}", stackSize)
                    ?: throw GeneralException("Can't allocate stack for thread")
            // TODO: Here is old method of stack allocation
            //  Maybe, reimplement them?
            //        val size = taskStackSize + _thread_local_storage.sizeof
            //        val align = (1 + size) % 3
            //        val taskStack = aligned(sys.malloc(size) + size, abi.types.word.bytes)

            // TODO: allocate by alignment
            currentThread.initContext(start_routine, currentThread.aligned(stack.last), os.sys.threadExitAddress)

            os.abi.writeInt(thread, currentThread.id.toULong())

            currentThread.context.setArg(0, os.abi.types.pointer, arg, true)

            log.fine { "[0x${ra.hex8}] pthread_create(start_routine=0x${start_routine.hex8})" }

            return retval(0)
        }
    }

    val pthread_join = object : APIFunction("pthread_join") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val thread = argv[0]
            val retval = argv[1]

            os.findProcessById(thread.asInt) ?: return retval(PosixError.ESRCH.id)

            val current = os.currentProcess as PosixThread

            os.block<PosixThread> {
                execute {
                    val exitedProcesses = mutableListOf<PosixThread>()
                    var availableThread: PosixThread
                    while (true) {
                        availableThread = current.exitedProcesses.take()
                        if (availableThread.id == thread.asInt)
                            break
                        exitedProcesses.add(availableThread)
                    }
                    current.exitedProcesses.addAll(exitedProcesses)
                    availableThread
                }
                success {
                    if (retval != 0L)
                        os.abi.writeInt(retval, it.context.returnValue)
                    it.id.asULong
                }
                failure {
                    -1 // TODO: Is failure possible here?
                }
            }

            log.fine { "[0x${ra.hex8}] pthread_join(thread=0x${thread.hex8})" }

            return void()
        }
    }

    val pthread_self = object : APIFunction("pthread_self") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.fine { "[0x${ra.hex8}] pthread_self()" }
            TODO("Not implemented")
        }
    }

    val pthread_setname_np = object : APIFunction("pthread_setname_np") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val thread = argv[0]
            val _name = os.sys.readAsciiString(argv[1])
            log.fine { "[0x${ra.hex8}] pthread_setname_np(thread${thread.hex8} name='$_name')" }
            TODO("Not implemented")
        }
    }

    val pthread_key_create = object : APIFunction("pthread_key_create") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val key = argv[0]
            val destr_function = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_key_create(key=0x${key.hex8} destr_function=0x${destr_function.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_mutex_init = object : APIFunction("pthread_mutex_init") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val mutex = argv[0]
            val mutexattr = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_mutex_init(mutex=0x${mutex.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_mutex_lock = object : APIFunction("pthread_mutex_lock") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val mutex = argv[0]
            log.fine { "[0x${ra.hex8}] pthread_mutex_lock(mutex=0x${mutex.hex8})" }
            TODO("Not implemented")
        }
    }

    val pthread_mutex_unlock = object : APIFunction("pthread_mutex_unlock") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val mutex = argv[0]
            log.fine { "[0x${ra.hex8}] pthread_mutex_unlock(mutex=0x${mutex.hex8})" }
            TODO("Not implemented")
        }
    }

    val __tls = object : APIFunction("__tls") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long) = TODO("retval(os.currentProcess.localStorageAddress)")
    }

    // https://linux.die.net/man/2/time
    val time = object : APIFunction("time") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val time_t = argv[0]
            val time = os.sys.time
            if (time_t != 0L)
                os.abi.writeInt(time_t, time)
            log.fine { "[0x${ra.hex8}] time($time_t) -> $time" }
            return retval(time)
        }
    }

    fun calendarToTimeStuct(calendar: Calendar) = time_struct(
            calendar.get(Calendar.SECOND).asULong,
            calendar.get(Calendar.MINUTE).asULong,
            calendar.get(Calendar.HOUR).asULong,
            calendar.get(Calendar.DAY_OF_MONTH).asULong,
            calendar.get(Calendar.MONTH).asULong,
            calendar.get(Calendar.YEAR).asULong,
            (calendar.get(Calendar.DAY_OF_WEEK) - 1).asULong,
            (calendar.get(Calendar.DAY_OF_YEAR) - 1).asULong,
            -1 // Not available
    )

    val localtime = object : APIFunction("localtime") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val timeStamp = os.abi.readInt(argv[0])

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeStamp * 1000L

            val tm = calendarToTimeStuct(calendar)

            val timePtr = os.sys.allocate(time_struct.sizeof)
            os.abi.writeBytes(timePtr, tm.asBytes)

            return retval(timePtr)
        }
    }

    val gmtime = object : APIFunction("localtime") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val timeStamp = os.abi.readInt(argv[0])

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = timeStamp * 1000L

            val tm = calendarToTimeStuct(calendar)

            val timePtr = os.sys.allocate(time_struct.sizeof)
            os.abi.writeBytes(timePtr, tm.asBytes)

            return retval(timePtr)
        }
    }

    val mktime = object : APIFunction("mktime") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val p_tm = argv[0]

            val tm = os.abi.reader.time_struct(p_tm)

            val calendar = Calendar.getInstance()
            calendar.set(tm.tm_year.asInt, tm.tm_mon.asInt, tm.tm_mday.asInt)

            return retval(calendar.timeInMillis)
        }
    }

    val strptime = object : APIFunction("strptime") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val ptr = argv[0]
            log.config { "[0x${ra.hex8}] strptime() not implemented" }
            return retval(ptr)
        }
    }

    val strftime = object : APIFunction("strftime") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val ptr = argv[0]
            val maxsize = argv[1].asInt
            var format = os.sys.readAsciiString(argv[2])
            val timeptr = argv[3]

            val tm = os.abi.reader.time_struct(timeptr)

            if ("'" in format)
                throw NotImplementedError("[0x${ra.hex8}] Now we have to escape the '-symbols")

            format = "'$format'"

            val calendar = Calendar.getInstance()
            calendar.set(tm.tm_year.asInt, tm.tm_mon.asInt, tm.tm_mday.asInt)

            format = format.replace("%a", "'EEE'")  // %a	Abbreviated weekday name *	Thu
            format = format.replace("%A", "'EEEE'") // %A	Full weekday name *	Thursday
            format = format.replace("%b", "'MMM'")  // %b	Abbreviated month name *	Aug
            format = format.replace("%B", "'MMMM'") // %B	Full month name *	August
            format = format.replace("%c", calendar.time.toString()) // %c	Date and time representation *	Thu Aug 23 14:55:02 2001
            format = format.replace("%C", "'yy'")   // %C	Year divided by 100 and truncated to integer (00-99)	20
            format = format.replace("%d", "'dd'")   // %d	Day of the month, zero-padded (01-31)	23
            //%D	Short MM/DD/YY date, equivalent to %m/%d/%y	08/23/01
            //%e	Day of the month, space-padded ( 1-31)	23
            //%F	Short YYYY-MM-DD date, equivalent to %Y-%m-%d	2001-08-23
            //%g	Week-based year, last two digits (00-99)	01
            //%G	Week-based year	2001
            //%h	Abbreviated month name * (same as %b)	Aug
            format = format.replace("%H", "'HH'")   // %H	Hour in 24h format (00-23)	14
            //%I	Hour in 12h format (01-12)	02
            //%j	Day of the year (001-366)	235
            format = format.replace("%m", "'MM'")   // %m	Month as a decimal number (01-12)	08
            format = format.replace("%M", "'mm'")   // %M	Minute (00-59)	55
            //%n	New-line character ('\n')
            //%p	AM or PM designation	PM
            //%r	12-hour clock time *	02:55:02 pm
            //%R	24-hour HH:MM time, equivalent to %H:%M	14:55
            format = format.replace("%S", "'ss'")   // %S	Second (00-61)	02
            //%t	Horizontal-tab character ('\t')
            //%T	ISO 8601 time format (HH:MM:SS), equivalent to %H:%M:%S	14:55:02
            //%u	ISO 8601 weekday as number with Monday as 1 (1-7)	4
            //%U	Week number with the first Sunday as the first day of week one (00-53)	33
            //%V	ISO 8601 week number (01-53)	34
            //%w	Weekday as a decimal number with Sunday as 0 (0-6)	4
            //%W	Week number with the first Monday as the first day of week one (00-53)	34
            //%x	Date representation *	08/23/01
            //%X	Time representation *	14:55:02
            //%y	Year, last two digits (00-99)	01
            format = format.replace("%Y", "'yyyy'") // %Y	Year	2001
            //%z	ISO 8601 offset from UTC in timezone (1 minute=1, 1 hour=100)
            //If timezone cannot be determined, no characters	+100
            //%Z	Timezone name or abbreviation *
            //If timezone cannot be determined, no characters	CDT
            format = format.replace("%%", "%") //%% A % sign	%


            format = format.replace("''", "") // Get rid of ''-sequences

            val result = SimpleDateFormat(format).format(calendar.time)
            if (maxsize < result.length + 1)
                return retval(0)

            os.abi.writeBytes(ptr, result.toByteArray())
            return retval(result.length.asULong)
        }
    }

    val stat = object : APIFunction("stat") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val path = os.sys.readAsciiString(argv[0])
            val buf = argv[1]

            log.config { "[0x${ra.hex8}] stat(path='$path' buf=0x${buf.hex8}) -> not fair implementation of stat" }

            if (!os.filesystem.exists(path)) {
                log.warning { "File '$path' doesn't exist" }
                // TODO: move to exception catcher and make one point to handle errno
                errno.allocated.value = PosixError.ENOENT.id
                return retval(-1)
            }

            val size: Long
            val type: Long
            if (!os.filesystem.isDirectory(path)) {
                size = nothrow(-1L) { os.filesystem.fileSize(path) }
                type = __S_IFREG
                if (size == -1L) {
                    log.warning { "Can't get size for file '$path'" }
                    return retval(-1)
                }
            } else {
                size = 0
                type = __S_IFDIR
            }

            val stat = stat_struct(st_mode = type, st_size = size)
            os.abi.writeBytes(buf, SysDep[os.abi.core].statStructToBytes(stat))
            return retval(0)
        }
    }

    val stat64 = stat

    val __xstat64 = object : APIFunction("lstat") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val ver = argv[0]
            val path = argv[1]
            val buf = argv[2]
            require(ver == 3L) { "Unknown version of __xstat64: $ver" }
            return stat64.exec(name, path, buf)
        }
    }

    val lstat = object : APIFunction("lstat") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val path = argv[0]
            val buf = argv[1]

            log.warning { "lstat(path=${os.sys.readAsciiString(path)} buf=${buf.hex8}) -> will not work with symbolic link" }
            return stat.exec(name, path, buf)
        }
    }

    val lstat64 = lstat

    @APIFunc fun __lxstat64(ver: Int, path: CharPointer, statBuf: BytePointer): APIResult {
        require(ver == 3) { "Unknown version of __lxstat64: $ver" }
        return lstat64.exec("__lxstat64", path.address, statBuf.address)
    }

    val madvise = object : APIFunction("madvise") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val addr = argv[0].asLong
            val length = argv[1].asInt
            val advice = argv[2].asInt
            log.config { "[0x${ra.hex8}] madvise not implemented addr=0x${addr.hex8} length=${length} advice=$advice" }
            return retval(0)
        }
    }

    val ioctl = object : APIFunction("ioctl") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val fd = argv[0].asInt
            val cmd = argv[1].asInt
            val argp = argv[2]

            val error = nothrow(-1) {
                // EBADF, EFAULT, EINVAL, ENOTTY, ENOTTY
                when (cmd) {
                    SysDep[os.abi.core].FIONREAD -> {
                        val available = os.ioSystem.available(fd)
                        log.fine { "[0x${ra.hex8}] ioctl(fd=$fd cmd=FIONREAD argp=0x${argp.hex8}) -> $available" }
                        os.abi.writeInt(argp, available.asULong)
                    }

                    else -> {
                        log.config { "[0x${ra.hex8}] ioctl not implemented cmd=0x${cmd.hex8} fd=${fd} arg=0x${argp.hex8}" }
                    }
                }
                return@nothrow 0
            }

            return retval(error.asLong)
        }
    }

    val fcntl = object : APIFunction("fcntl") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val fd = argv[0].asInt
            val cmd = argv[1].asInt
            val arg = argv[2].asInt

            log.fine { "[0x${ra.hex8}] fcntl(fd=$fd cmd=$cmd arg=$arg)" }

            val nonBlock = SysDep[os.abi.core].O_NONBLOCK
            val mask = O_WRONLY or O_RDWR or nonBlock

            when (cmd) {
                F_GETFD -> {
                    log.config { "[0x${ra.hex8}] F_GETFD is not implemented!" }
                    return retval(0L)
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
                    return retval(result.asULong)
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

            return retval(0)
        }
    }

    val fcntl64 = fcntl

    val socket = object : APIFunction("socket") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val domain = argv[0].asInt
            val type = argv[1].asInt
            val protocol = argv[2].asInt

            val sockStream = SysDep[os.abi.core].SOCK_STREAM
            require(domain == AF_INET && type == sockStream && (protocol == IPPROTO_IP || protocol == IPPROTO_TCP)) {
                "[0x${ra.hex8}] Unknown combination for socket(): domain=$domain, type=$type, protocol=$protocol"
            }
            val socket = nothrow(-1) { os.network.socket() }

            log.fine { "[0x${ra.hex8}] socket(domain=$domain type=$type protocol=$protocol) -> fd = $socket" }

            return retval(socket.asULong)
        }
    }

    val _socket = socket

    val bind = object : APIFunction("bind") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val sockfd = argv[0].asInt
            val saPtr = argv[1]
            val addrlen = argv[2].asInt
            val sa = os.abi.reader.sockaddr(saPtr, addrlen)
            require(sa.sin_family == AF_INET) { "[0x${ra.hex8}] Only AF_INET currently supported!" }
            val address = InetSocketAddress(InetAddress.getByAddress(sa.sin_addr), htons(sa.sin_port))
            val result = nothrow(-1) { os.network.bind(sockfd, address); 0 }
            return retval(result.asLong)
        }
    }

    val _bind = bind

    val sendto = object : APIFunction("sendto") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult = TODO("use socketSystem")
    }

    val _sendto = sendto

    val recvfrom = object : APIFunction("recvfrom") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult = TODO("use socketSystem")
    }

    val _recvfrom = recvfrom

    val listen = object : APIFunction("listen") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val s = argv[0].asInt
            val backlog = argv[1].asInt
            val result = nothrow(-1) { os.network.listen(s, backlog); 0 }
            return retval(result.asLong)
        }
    }

    val _listen = listen

    val accept = object : APIFunction("accept") {
        // https://linux.die.net/man/3/accept
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val s = argv[0].asInt
            val addr = argv[1]
            val addrlen = argv[2]

            require(addrlen >= sockaddr.sizeof) { "[0x${ra.hex8}] Wrong sockaddr size: $addrlen" }

            os.block<Pair<Int, InetSocketAddress>> {
                execute {
                    os.network.accept(s)
                }

                success { (fd, inet) ->
                    log.fine { "[0x${ra.hex8}] accept(fd=$s addr=0x${addr.hex8}) -> fd=$fd inet=$inet" }
                    os.abi.writer.sockaddr(addr, inet.address, inet.port)
                    fd.asULong
                }

                failure {
                    log.fine { "[0x${ra.hex8}] accept(fd=$s addr=0x${addr.hex8}) -> failed" }
                    setErrno(it); -1
                }
            }

            return void()
        }
    }

    val _accept = accept

    val getsockopt = object : APIFunction("getsockopt") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult = TODO("use socketSystem")
    }

    val _getsockopt = getsockopt

    val recv = object : APIFunction("recv") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val s = argv[0].asInt
            val buf = argv[1]
            val len = argv[2].asInt
            val flags = argv[3].asInt

            check(flags == 0) { "[0x${ra.hex8}] The program requires to use the flags value: $flags" }

            os.block<ByteArray> {
                execute {
                    os.network.recv(s, len)
                }

                success {
                    os.abi.writeBytes(buf, it)
                    it.size.asULong
                }

                failure {
                    setErrno(it); -1
                }
            }

            return void()
        }
    }

    val _recv = recv

    val send = object : APIFunction("send") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val s = argv[0].asInt
            val buf = argv[1]
            val len = argv[2].asInt
            val flags = argv[3].asInt

            check(flags == 0) { "[0x${ra.hex8}] The program requires to use the flags value: $flags" }

            val data = os.abi.readBytes(buf, len)

            val result = nothrow(-1) { os.network.send(s, data); len }

            return retval(result.asULong)
        }
    }

    val htons = object : APIFunction("htons") {
        override val args = arrayOf(ArgType.Short)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val value = argv[0]
            return retval(htons(value))
        }
    }

    val htonl = object : APIFunction("htonl") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val value = argv[0]
            return retval(htonl(value))
        }
    }

    val __uClibc_main = object : APIFunction("__uClibc_main") {
        override val args = arrayOf(
                ArgType.Pointer, ArgType.Int, ArgType.Pointer, ArgType.Pointer,
                ArgType.Pointer, ArgType.Pointer, ArgType.Pointer
        )

        override fun exec(name: String, vararg argv: Long): APIResult {
            val main = argv[0]
            val argc = argv[1]
            val _argv = argv[2]

            /* The environment begins right after argv.  */
            var __environ = os.abi.readInt(_argv + (argc + 1) * 4)

            /* If the first thing after argv is the arguments
             * then the environment is empty. */
            if (__environ == os.abi.readInt(_argv)) {
                /* Make __environ point to the NULL at argv[argc] */
                __environ = os.abi.readInt(_argv + argc * 4)
            }

            // TODO: Init/fini

            os.abi.returnAddressValue = os.sys.processExitAddress

            os.abi.setArgs(arrayOf(argc, _argv, __environ), true)

            return redirect(main)
        }
    }

    // TODO: merge with __uClibc_main
    val __libc_start_main = object : APIFunction("__libc_start_main") {
        override val args = arrayOf(
                ArgType.Pointer,    // int (*main)(int, char**, char**)
                ArgType.Int,       // int argc
                ArgType.Pointer,    // char** ubp_av
                ArgType.Pointer,    // void (*init)(void)
                ArgType.Pointer,    // void (*fini)(void)
                ArgType.Pointer,    // void (*rtld_fini)(void)
                ArgType.Pointer     // void* stack_end
        )

        override fun exec(name: String, vararg argv: Long): APIResult {
            val main = argv[0]
            val argc = argv[1]
            val _argv = argv[2]

            /* The environment begins right after argv.  */
            var __environ = os.abi.readInt(_argv + (argc + 1) * 4)

            /* If the first thing after argv is the arguments
             * then the environment is empty. */
            if (__environ == os.abi.readInt(_argv)) {
                /* Make __environ point to the NULL at argv[argc] */
                __environ = os.abi.readInt(_argv + argc * 4)
            }

            // TODO: Init/fini

            os.abi.returnAddressValue = os.sys.processExitAddress

            os.abi.setArgs(arrayOf(argc, _argv, __environ), true)

            return redirect(main)
        }
    }

    val sleep = object : APIFunction("sleep") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val seconds = argv[0]
            TODO("Not implemented")
        }
    }

    val mallopt = object : APIFunction("mallopt") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long) = retval(1)
    }

    val getuid = object : APIFunction("getuid") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long) = retval(0) // User: root
    }

    val geteuid = getuid

    val dup2 = object : APIFunction("dup2") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val oldfd = argv[0].asInt
            val newfd = argv[1].asInt
            log.fine { "[0x${ra.hex8}] dup2(oldfd=$oldfd newfd=$newfd)" }
            val result = nothrow(-1) { os.filesystem.dup2(oldfd, newfd) }
            return retval(result.asULong)
        }
    }

    enum class LC(val id: Int) {
        CTYPE(0),
        NUMERIC(1),
        MONETARY(2),
        TIME(3),
        COLLATE(4),
        MESSAGES(5),
        ALL(6)
    }

    val setlocale = object : APIFunction("setlocale") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val category = first<LC> { it.id == argv[0].asInt }
            val locale = argv[1]
            if (locale != 0L) {
                val localeString = os.sys.readAsciiString(locale)
                check(localeString == "C" || localeString.isEmpty()) { "[0x${ra.hex8}] setlocale $category = $localeString failed" }
                log.fine { "[0x${ra.hex8}] setlocale($category=$localeString)" }
            }
            val result = os.sys.allocateAsciiString("C") // TODO: constant
            return retval(result)
        }
    }

    val getcwd = object : APIFunction("getcwd") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val buf = argv[0]
            val size = argv[1]

            val cwd = os.filesystem.cwd
            if (cwd.length + 1 > size)
                return retval(0)

            os.sys.writeAsciiString(buf, cwd)

            return retval(buf)
        }
    }

    val chdir = object : APIFunction("chdir") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            // TODO: WTF?

            val path = os.sys.readAsciiString(argv[0])

            log.config { "[0x${ra.hex8}] chdir(path='$path')"}

            println()
            return retval(-1)
        }
    }

    val mmap = object : APIFunction("mmap") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int,
                ArgType.Int, ArgType.Int, ArgType.LongLong)

        override fun exec(name: String, vararg argv: Long): APIResult {
            val start = argv[0]
            val length = argv[1]
            val prot = argv[2]
            val flags = argv[3]
            val fd = argv[4].asInt
            val offset = argv[5].asInt

            if (start != 0L) TODO("start isn't zero: 0x${start.hex8}")

            // TODO: process prot
            log.config { "[0x${ra.hex8}] mmap(start=$start length=$length fd=$fd offset=$offset) -> parameter prot unused" }

            // TODO: process flags
            log.config { "[0x${ra.hex8}] mmap(start=$start length=$length fd=$fd offset=$offset) -> parameter flags unused" }

            val result = os.sys.mapFileToMemory(fd, length, offset)
            return retval(result)
        }
    }

    val mmap64 = mmap

    val munmap = object : APIFunction("munmap") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val start = argv[0]
            val length = argv[1].asInt

            // TODO: if something wrong then we don't know yet about it and should learn
            os.sys.unmapFileFromMemory(start, length)
            return retval(0)
        }
    }

    val getrlimit = object : APIFunction("getrlimit") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val resource = argv[0].asInt
            val rlim = argv[1]

            log.config { "[0x${ra.hex8}] Not fair implementation of getrlimit -> values depends on arch" }
            val rlimitNofile = SysDep[os.abi.core].RLIMIT_NOFILE
            check(resource == rlimitNofile) { "[0x${ra.hex8}] TODO: Not implemented" }

            os.abi.writeBytes(rlim, rlimit(0x1000, 0x1000).asBytes)
            return retval(0)
        }
    }

    val getrlimit64 = getrlimit

    val setsockopt = object : APIFunction("setsockopt") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val s = argv[0]
            val level = argv[1].asInt
            val optname = argv[2].asInt
            val optval = argv[3]
            val optlen = argv[4].asInt

            // TODO: now nothing to do
            val sysDep = SysDep[os.abi.core]
            check(level == sysDep.SOL_SOCKET && optname == sysDep.SO_REUSEADDR || level == IPPROTO_TCP && optname == TCP_CORK) {
                "[0x${ra.hex8}] Unknown combination: level=$level, optname=$optname"
            }

            check(optlen == 4) { "[0x${ra.hex8}] Wrong optlen: $optlen (expected 4)" } // sizeof word

            os.abi.readInt(optval) // Check for access

            return retval(0)
        }
    }

    var gid = 0L

    val getgid = object : APIFunction("getgid") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long) = retval(gid)
    }

    var openlogIdent = ""

    val openlog = object : APIFunction("openlog") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val ident = os.sys.readAsciiString(argv[0])
            val option = argv[1]
            val facility = argv[2].asInt

            check(option and 0x03L.inv() == 0L) { "[0x${ra.hex8}] Unknown option: $option" }
            log.config { "[0x${ra.hex8}] openlog() -> facility not implemented" }

            openlogIdent = ident
            return void()
        }
    }

    val select = object : APIFunction("select") {

        fun selectSingle(ptr: Long, n: Int, block: (Int) -> Boolean): Int {
            var count = 0
            if (ptr != 0L) {
                val fdSet = fd_set(os.abi.readBytes(ptr, fd_set.sizeof), n)
                for (i in 0 until n) {
                    val major = i / 32
                    val minor = i % 32
                    val value = fdSet.fds_bits[major]
                    if (value[minor].toBool()) {
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
        override fun exec(name: String, vararg argv: Long): APIResult {
            val n = argv[0].asInt
            val readfdsPtr = argv[1]
            val writefdsPtr = argv[2]
            val exceptfdsPtr = argv[3]
            val timeout = os.abi.reader.timeval(argv[4])

            if (n == 0) TODO("Sleep use")

            log.fine { "[0x${ra.hex8}] select(timeout=$timeout)" }

            val readCount = selectSingle(readfdsPtr, n) { fd -> os.ioSystem.readable(fd) }

            val writeCount = selectSingle(writefdsPtr, n) { fd -> os.ioSystem.writable(fd) }

            val exceptCount = selectSingle(exceptfdsPtr, n) { false } // TODO: catch exceptions?

            return retval(maxOf(readCount, writeCount, exceptCount).asULong)
        }
    }

    val poll = object : APIFunction("poll") {
        val mask = POLLIN or POLLPRI or POLLOUT /*or POLLERR or POLLHUP or POLLNVAL*/

        private fun pollfd.isAnyEvents(mask: Long) = events.asULong and mask != 0L

        private fun pollfd.poll(): Boolean {
            check(!isAnyEvents(mask.inv())) { "[0x${ra.hex8}] Unimplemented flags: ${events.hex4}" }

            if (fd < 0) {
                // If this field is negative, then the corresponding
                // events field is ignored and the revents field returns zero.
                revents = 0
            } else if (os.ioSystem.isOpen(fd)) {
                var changed = false

                if (os.ioSystem.writable(fd)) {
                    revents = revents or POLLOUT.asShort
                    changed = true
                }

                if (os.ioSystem.readable(fd)) {
                    revents = revents or POLLIN.asShort
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

        private fun List<pollfd>.poll(timeout: Long = 0): Int {
            if (timeout == 0L)
                return count { it.poll() }

            var count: Int
            var remain = timeout
            do {
                val duration = minOf(50, remain)
                Thread.sleep(duration)
                count = count { it.poll() }
                remain -= duration
            } while (count == 0 && remain > 0)

            return count
        }

        // https://linux.die.net/man/2/poll
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val fds = argv[0]
            val nfds = argv[1].asInt
            val timeout = argv[2]

            require(timeout != -1L) { "[0x${ra.hex8}] timeout == -1 is not implemented!" }

            log.fine { "[0x${ra.hex8}] poll(nfds=$nfds timeout=$timeout)" }

            val structs = List(nfds) { os.abi.reader.pollfd(fds, it) }

            os.block<Int> {
                execute {
                    structs.poll(timeout)
                }

                success { count ->
                    structs.forEachIndexed { i, pollfd -> os.abi.writer.pollfd(fds, pollfd, i) }
                    count.asULong
                }

                failure {
                    // EFAULT, EINTR, EINVAL, ENOMEM <- currently not thrown
                    setErrno(it); -1
                }
            }

            return void()
        }
    }

    val inet_ntop = object : APIFunction("inet_ntop") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val af = argv[0].asInt
            val src = argv[1]
            val dst = argv[2]
            val cnt = argv[3]

            check(af == AF_INET) { "[0x${ra.hex8}] Not AF_INET: $af" }

            val addr = os.abi.reader.sockaddr(src)

            if (addr.hostname.length + 1 < cnt)
                return retval(0)

            os.sys.writeAsciiString(dst, addr.hostname)

            return retval(dst)
        }
    }

    val sysconf = object : APIFunction("sysconf") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val id = argv[0].asInt

            check(id == _SC_IOV_MAX) { "[0x${ra.hex8}] Not _SC_IOV_MAX: $id" }

            return retval(IOV_MAX.asULong)
        }
    }

    val writev = object : APIFunction("writev") {

        override val args = arrayOf(ArgType.Int, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val fd = argv[0].asInt
            val vector = argv[1]
            val count = argv[2].asInt

            val iovecs = collect(count) { i -> iovec(os.abi.readBytes(vector + i * iovec.sizeof, iovec.sizeof)) }

            var total = 0L

            iovecs.forEach {
                val result = write.exec(name, fd.asULong, it.iov_base, it.iov_len) as APIResult.Value
                if (result.data == -1L)
                    return retval(-1)
                total += result.data
            }

            return retval(total)
        }
    }

    // https://linux.die.net/man/3/shutdown
    val shutdown = object : APIFunction("shutdown") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val s = argv[0].asInt
            val how = argv[1].asInt
            log.fine { "[0x${ra.hex8}] shutdown(s=$s how=$how) in ${os.currentProcess} " }
            val read = how == SHUT_RDWR || how == SHUT_RD
            val write = how == SHUT_RDWR || how == SHUT_WR
            val error = nothrow(-1) { os.network.shutdown(s, read, write); 0 }
            return retval(error.asLong)
        }
    }

    val dlopen = object : APIFunction("dlopen") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val (filename, flag) = argv
            val filenameString = os.sys.readAsciiString(filename)
            if (filenameString[0] == '/') {
                if (!os.filesystem.exists(filenameString))
                    return retval(0) // TODO: error processing

                val fullPath = os.filesystem.absolutePath(filenameString)
                os.loader.loadLibrary(fullPath)
                val module = fullPath.toFile().name
                val result = os.loader.moduleAddress(module)
                return retval(result)
            } else TODO("I have no LD_LIBRARY_PATH and etc...")
        }
    }

    val dlsym = object : APIFunction("dlsym") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val (handle, symbol) = argv
            val symbolString = os.sys.readAsciiString(symbol)

            val module = os.loader.moduleName(handle)

            log.fine { "[0x${ra.hex8}] dlsym($module, $symbol)" }

            val result = os.loader.findSymbol(module, symbolString)?.address ?: 0L
            check (result != 0L) { "Symbol $symbolString not found in $module" }
            return retval(result)
        }
    }

    val dlerror = object : APIFunction("dlerror") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.config { "[0x${ra.hex8}] dlerror() is not implemented!" }
            return retval(0L)
        }
    }

    val dlclose = object : APIFunction("dlclose") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.config { "[0x${ra.hex8}] dlclose() is not implemented!" }
            return retval(0L)
        }
    }

    val fork = object : APIFunction("fork") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            val proc = os.copyProcess("forked")

            (os.currentProcess as PosixThread).childProcesses.add(proc as PosixThread)
            proc.parentProcess = os.currentProcess as PosixThread

            proc.context.setReturnValue(0)
            proc.context.programCounterValue = os.sys.returnerAddress

            return retval(proc.id.toULong())
        }
    }

    val execve = object : APIFunction("execve") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
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

    val bindtextdomain = object : APIFunction("bindtextdomain") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val domainname = os.sys.readAsciiString(argv[0])
            val dirname = os.sys.readAsciiString(argv[1])

            return retval(os.sys.allocateAsciiString(dirname))
        }
    }

    val textdomain = object : APIFunction("textdomain") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val domainname = os.sys.readAsciiString(argv[0])

            return retval(os.sys.allocateAsciiString(domainname))
        }
    }

    val dcgettext = object : APIFunction("dcgettext") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val msgid = os.sys.readAsciiString(argv[1])

            return retval(os.sys.allocateAsciiString(msgid))
        }
    }

    val getopt_long = object : APIFunction("getopt_long") {
        override val args = arrayOf(
                ArgType.Int,        // argc
                ArgType.Pointer,    // argv
                ArgType.Pointer,    // optstring
                ArgType.Pointer,    // longopts
                ArgType.Pointer     // longindex
        )
        private fun getArg(pArgv: Long, index: Int): String {
            val address = os.abi.readPointer(pArgv + os.abi.types.pointer.bytes * index)
            return os.sys.readAsciiString(address)
        }

        private fun getProgname(pArgv: Long): String = getArg(pArgv, 0)
        private fun getArgs(pArgv: Long, argc: Int) = collect(argc) { getArg(pArgv, it) }.toTypedArray()

        @DontAutoSerialize
        private lateinit var g: Getopt
        private var initialized = false

        override fun exec(name: String, vararg argv: Long): APIResult {
            val longopts = argv[3]
            val longindex = argv[4]

            val longoptsList = mutableListOf<LongOpt>()
            var ptr = longopts
            val stringBuffers = mutableMapOf<Long, StringBuffer>()

            while (true) {
                val nameAddress = os.abi.readPointer(ptr)
                if (nameAddress == 0L)
                    break
                val hasArg = os.abi.readInt(ptr + 4)
                val flagAddress = os.abi.readPointer(ptr + 8)
                val value = os.abi.readInt(ptr + 12)

                val name = os.sys.readAsciiString(nameAddress)
                val flag = if (flagAddress == 0L) null else StringBuffer().also { stringBuffers[flagAddress] = it }
                longoptsList.add(LongOpt(name, hasArg.asInt, flag, value.asInt))

                ptr += 16
            }

            val array = longoptsList.toTypedArray()

            if (!initialized) {
                val argc = argv[0]
                val pArgv = argv[1]
                val pOptStr = argv[2]

                val optstr = os.sys.readAsciiString(pOptStr)
                val progname = getProgname(pArgv)
                val args = getArgs(pArgv, argc.asInt).drop(1).toTypedArray()

                initialized = true
                g = Getopt(progname, args, optstr, array)

                if (optind.linked)
                    optind.value = 1
            }

            val result = g.getopt()


            if (longindex != 0L) TODO("This case was not tested and results may be unexpected")

            if (optind.linked)
                optind.value = g.optind.asULong + 1

            if (optarg.linked)
                optarg.value = if (g.optarg != null) os.sys.allocateAsciiString(g.optarg) else 0L

            return retval(result.asULong)
        }
    }

    // TODO: merge with getopt_long
    val getopt_long_only = object : APIFunction("getopt_long_only") {
        override val args = arrayOf(
                ArgType.Int,        // argc
                ArgType.Pointer,    // argv
                ArgType.Pointer,    // optstring
                ArgType.Pointer,    // longopts
                ArgType.Pointer     // longindex
        )
        private fun getArg(pArgv: Long, index: Int): String {
            val address = os.abi.readPointer(pArgv + os.abi.types.pointer.bytes * index)
            return os.sys.readAsciiString(address)
        }

        private fun getProgname(pArgv: Long): String = getArg(pArgv, 0)
        private fun getArgs(pArgv: Long, argc: Int) = collect(argc) { getArg(pArgv, it) }.toTypedArray()

        @DontAutoSerialize
        private lateinit var g: Getopt
        private var initialized = false

        override fun exec(name: String, vararg argv: Long): APIResult {
            val longopts = argv[3]
            val longindex = argv[4]

            val longoptsList = mutableListOf<LongOpt>()
            var ptr = longopts
            val stringBuffers = mutableMapOf<Long, StringBuffer>()

            while (true) {
                val nameAddress = os.abi.readPointer(ptr)
                if (nameAddress == 0L)
                    break
                val hasArg = os.abi.readInt(ptr + 4)
                val flagAddress = os.abi.readPointer(ptr + 8)
                val value = os.abi.readInt(ptr + 12)

                val name = os.sys.readAsciiString(nameAddress)
                val flag = if (flagAddress == 0L) null else StringBuffer().also { stringBuffers[flagAddress] = it }
                longoptsList.add(LongOpt(name, hasArg.asInt, flag, value.asInt))

                ptr += 16
            }

            val array = longoptsList.toTypedArray()

            if (!initialized) {
                val argc = argv[0]
                val pArgv = argv[1]
                val pOptStr = argv[2]

                val optstr = os.sys.readAsciiString(pOptStr)
                val progname = getProgname(pArgv)
                val args = getArgs(pArgv, argc.asInt).drop(1).toTypedArray()

                initialized = true
                g = Getopt(progname, args, optstr, array, true)

                if (optind.linked)
                    optind.value = 1
            }

            val result = g.getopt()


            if (longindex != 0L)
                os.abi.writeInt(longindex, g.longind.asULong)

            if (optind.linked)
                optind.value = g.optind.asULong + 1

            if (optarg.linked)
                optarg.value = if (g.optarg != null) os.sys.allocateAsciiString(g.optarg) else 0L

            return retval(result.asULong)
        }
    }


    // TODO: increment is intptr_t
    // TODO: return type is void*
    @APIFunc fun sbrk(increment: Int): BytePointer {
        log.config { "[0x${ra.hex8}] sbrk(increment='$increment)" }
        if (increment != 0) {
            TODO("Not implemented")
        }
        return BytePointer(os.sys, os.currentProcess.allocator.breakAddress)
    }

    @APIFunc fun fnmatch(pattern: CharPointer, string: CharPointer, flags: Int): Int {
        log.config { "[0x${ra.hex8}] fnmatch(pattern='${pattern.string}' string='${string.string}')" }
        val matcher = FileSystems.getDefault().getPathMatcher("glob:${pattern.string}")
        return if (matcher.matches(Paths.get(string.string))) 0 else 1 // FNM_NOMATCH = 1
    }

    @DontAutoSerialize
    val noEntropyRandom = Random(0)

    @APIFunc fun mkstemp(template: CharPointer): Int {
        log.config { "[0x${ra.hex8}] mkstemp(template='${template.string}')" }
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        val templateString = template.string
        if (!templateString.endsWith("XXXXXX"))
            return -1 // TODO: errno

        val rand = if (os.conf.useEntropy) random else noEntropyRandom
        val krand = rand.asKotlinRandom()
        val sequence = (0..5).map { letters.random(krand) }.joinToString("")
        template.string = templateString.replace("XXXXXX", sequence) // TODO: replace only last symbols
        val sysDep = SysDep[os.abi.core]
        val result = open.exec("", template.address, (sysDep.O_CREAT or sysDep.O_TRUNC or O_RDWR).asULong)
        return (result as APIResult.Value).data.asInt
    }

    @APIFunc fun mkstemp64(template: CharPointer) = mkstemp(template)

    @APIFunc fun getrusage(who: Int, usage: rusage): Int {
        log.warning { "[0x${ra.hex8}] getrusage(who=$who rusage=$usage) - not implemented" }

        usage.ruUtime.tvSec = 0
        usage.ruUtime.tvUsec = 0
        usage.ruStime.tvSec = 0
        usage.ruStime.tvUsec = 0

        return 0
    }

    val objstackDefaultAlignment = 4096 - 12 - 4 // TODO: 12 is sizeof (mhead) and 4 is EXTRA from GNU malloc.

    // https://code.woboq.org/userspace/glibc/malloc/obstack.c.html
    @APIFunc fun _obstack_begin(
            h: obstack,
            size: Int,
            alignment: Int,
            chunkfun: FunctionPointer,
            freefun: FunctionPointer
    ) = withCallback(h.address, size.asULong, alignment.asULong, chunkfun.address, freefun.address) {
        log.warning { "[0x${ra.hex8}] _obstack_begin(h=$h size=${size.hex8} alignment=${alignment.hex8} chunkfun=$chunkfun freefun=$freefun)" }
        val concreteAlignment = if (alignment != 0) alignment else 4 // TODO: way to determine alignment
        val concreteSize = if (size != 0) size else objstackDefaultAlignment

        h.chunkfun = chunkfun.address
        h.freefun = freefun.address
        h.chunkSize = concreteSize
        h.alignmentMask = concreteAlignment - 1
        h.useExtraArg = false

        check (!h.useExtraArg) { "Not implemented" }
        val chunk = _obstack_chunk(os.sys, it.interrupt(h.chunkfun, h.chunkSize.asULong))
        check (chunk.isNotNull) { "Not implemented" }
        h.chunk = chunk

        h.objectBase = BytePointer(os.sys, chunk.address) // TODO: alignment
        h.nextFree = h.objectBase.address
        chunk.limit = chunk.address + h.chunkSize
        h.chunkLimit = chunk.limit
        chunk.prev = _obstack_chunk(os.sys, 0)
        /* The initial chunk now contains no empty object.  */
        h.maybeEmptyObject = false
        h.allocFailed = false
        Unit
    }

    @APIFunc fun _obstack_newchunk(h: obstack, length: Int) = withCallback(h.address, length.asULong) {
        log.warning { "[0x${ra.hex8}] _obstack_newchunk(h=$h length=${length.hex8})" }
        val oldChunk = h.chunk
        val objSize = h.nextFree - h.objectBase.address

        /* Compute size for new chunk.  */
        var newSize = (objSize + length) + (objSize shr 3) + h.alignmentMask + 100
        if (newSize < h.chunkSize)
            newSize = h.chunkSize.asULong

        /* Allocate and initialize the new chunk.  */
        check (!h.useExtraArg) { "Not implemented" }
        val newChunk = _obstack_chunk(os.sys, it.interrupt(h.chunkfun, newSize))
        check (newChunk.isNotNull) { "Not implemented" }
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
        objectBase.store(h.objectBase.load(objSize.asInt - already, already), already)

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

    @APIFunc fun _obstack_free(h: obstack, obj: BytePointer) = withCallback(h.address, obj.address) {
        var lp = h.chunk /* below addr of any objects in this chunk */

        /* We use >= because there cannot be an object at the beginning of a chunk.
           But there can be an empty object at that address
           at the end of another chunk.  */
        while (lp.isNotNull && (lp.address >= obj.address || lp.limit < obj.address)) {
            val plp = lp.prev /* point to previous chunk if any */
            check (!h.useExtraArg) { "Not implemented" }
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

    @APIFunc fun realpath(path: CharPointer, resolvedPath: CharPointer): CharPointer {
        // TODO: as internal function
        // TODO: PATH_MAX = 256
        val buffer = if (resolvedPath.isNull) CharPointer(os.sys, os.sys.allocate(256)) else resolvedPath
        val fullPath = os.filesystem.absolutePath(path.string)
        val rootPath = os.filesystem.absolutePath("/")
        val realPath = fullPath.removePrefix(rootPath) // TODO: as filesystem function
        buffer.string = realPath
        return buffer
    }

    @APIFunc fun opendir(name: CharPointer): DIR {
        log.fine { "[0x${ra.hex8}] opendir(name='${name.string}')" }
        return nothrow(DIR(os.sys)) {
            val fileDescriptor = os.filesystem.openDir(name.string)
            DIR.allocate(os.sys).apply {
                fd = fileDescriptor
            }
        }
    }

    val closedir = object : APIFunction("closedir") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val desc = argv[0].asInt
            val res = os.filesystem.closeDir(desc)
            log.fine { "[0x${ra.hex8}] closedir('${desc}') -> res = $res in ${os.currentProcess}" }
            return retval(res.asLong)
        }
    }

    @APIFunc fun unlink(path: CharPointer): Int {
        log.warning { "[0x${ra.hex8}] unlink(path='${path.string}') -> not implemented" }
        return 0
    }

    // TODO: refactor this
    var umaskValue = 0x12 // 022

    val mode_t.oct3: String get() = "${int / 64}${(int / 8) % 8}${int % 8}"

    @APIFunc fun umask(mask: mode_t): mode_t = umaskValue.uint.also {
        log.fine { "[0x${ra.hex8}] umask(mask=${mask.oct3})" }
        umaskValue = mask.int and 0x1FF // 0777
    }

    // TODO: mode is mode_t
    @APIFunc fun chmod(path: CharPointer, mode: mode_t): Int {
        val pathString = path.string
        log.fine { "[0x${ra.hex8}] chmod(path='${pathString}' mode=${mode.oct3}) -> not implemented" }
        return nothrow(-1) {
            if (!os.filesystem.exists(pathString))
                throw IONoSuchFileOrDirectory(pathString)
            0 // TODO: get rid of fd
        }
    }

    // TODO: refactor and get rid of Context
    class jmp_buf(sys: System, address: Long) : IntPointer(sys, address) {
        val ctx = sys.fullABI.createContext()

        fun restore() = ctx.restore(address)
        fun store() = ctx.store(address)
    }

    @APIFunc fun _setjmp(env: jmp_buf): Int {
        env.store()
        return 0
    }

    @APIFunc fun __longjmp_chk(env: jmp_buf, value: Int) {
        env.restore()
        os.abi.setReturnValue(if (value == 0) 1L else value.asULong) // TODO: return?
    }


    @APIFunc fun sigfillset(set: sigset_t): Int {
        set.fill()
        return 0
    }

    @APIFunc fun sigaction(signum: Int, act: sigaction, oldact: sigaction): Int {
//        log.severe { "High probability of fail: sigset_t offsets" }
        log.severe { "[0x${ra.hex8}] sigaction(signum=${signum} act=$act oldact=$oldact) - not implemented" }
        if (oldact.isNotNull) {
            oldact.handler = 0L
            oldact.mask = 0L
        }
        return 0
    }

    // TODO: return type is pid_t
    @APIFunc fun wait(status: IntPointer)/*: Int*/ {
        val current = (os.currentProcess as PosixThread)
        check (current.childProcesses.isNotEmpty()) {
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
                        it.context.returnValue.asInt shl 8
                    status.set(statusValue)
                }
                it.id.asULong
            }
            failure {
                -1
            }
        }
    }
}


