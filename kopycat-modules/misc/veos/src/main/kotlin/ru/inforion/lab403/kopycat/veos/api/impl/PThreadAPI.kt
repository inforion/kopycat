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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixError
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixThread
import ru.inforion.lab403.kopycat.veos.ports.posix.pthread_attr_t


/**
 *
 * Implementation of pthread.h of POSIX threading library
 */
class PThreadAPI(os: VEOS<*>) : API(os) {

    //https://linux.die.net/man/3/pthread_once
    val pthread_once = object : APIFunction("pthread_once") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val once_control = argv[0]
            val init_routine = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_once @ ${init_routine.hex8}" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_attr_init
    val pthread_attr_init = object : APIFunction("pthread_attr_init") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            log.fine { "[0x${ra.hex8}] pthread_attr_init(attr=0x${attr.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_attr_destroy
    val pthread_attr_destroy = object : APIFunction("pthread_attr_destroy") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            log.fine { "[0x${ra.hex8}] pthread_attr_destroy(attr=0x${attr.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_attr_getschedparam
    val pthread_attr_getschedparam = object : APIFunction("pthread_attr_getschedparam") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val param = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_getschedparam(attr=0x${attr.hex8} param=0x${param.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_attr_setschedparam
    val pthread_attr_setschedparam = object : APIFunction("pthread_attr_setschedparam") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val param = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_setschedparam(attr=0x${attr.hex8} param=0x${param.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_attr_setinheritsched
    val pthread_attr_setinheritsched = object : APIFunction("pthread_attr_setinheritsched") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val inheritsched = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_setinheritsched(attr=0x${attr.hex8} inherited=0x${inheritsched.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_attr_setschedpolicy
    val pthread_attr_setschedpolicy = object : APIFunction("pthread_attr_setschedpolicy") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val policy = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_setschedparam(attr=0x${attr.hex8} policy=0x${policy.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_attr_setstacksize
    val pthread_attr_setstacksize = object : APIFunction("pthread_attr_setstacksize") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val attr = argv[0]
            val stacksize = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_attr_setschedparam(attr=0x${attr.hex8} stacksize=0x${stacksize.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_create
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

    // https://linux.die.net/man/3/pthread_join
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

    // https://linux.die.net/man/3/pthread_self
    val pthread_self = object : APIFunction("pthread_self") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.fine { "[0x${ra.hex8}] pthread_self()" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_setname_np
    val pthread_setname_np = object : APIFunction("pthread_setname_np") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val thread = argv[0]
            val _name = os.sys.readAsciiString(argv[1])
            log.fine { "[0x${ra.hex8}] pthread_setname_np(thread${thread.hex8} name='$_name')" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_key_create
    val pthread_key_create = object : APIFunction("pthread_key_create") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val key = argv[0]
            val destr_function = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_key_create(key=0x${key.hex8} destr_function=0x${destr_function.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_mutex_init
    val pthread_mutex_init = object : APIFunction("pthread_mutex_init") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val mutex = argv[0]
            val mutexattr = argv[1]
            log.fine { "[0x${ra.hex8}] pthread_mutex_init(mutex=0x${mutex.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_mutex_lock
    val pthread_mutex_lock = object : APIFunction("pthread_mutex_lock") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val mutex = argv[0]
            log.fine { "[0x${ra.hex8}] pthread_mutex_lock(mutex=0x${mutex.hex8})" }
            TODO("Not implemented")
        }
    }

    // https://linux.die.net/man/3/pthread_mutex_unlock
    val pthread_mutex_unlock = object : APIFunction("pthread_mutex_unlock") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val mutex = argv[0]
            log.fine { "[0x${ra.hex8}] pthread_mutex_unlock(mutex=0x${mutex.hex8})" }
            TODO("Not implemented")
        }
    }
}