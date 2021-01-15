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
@file:Suppress("FunctionName", "PropertyName", "unused")

package ru.inforion.lab403.kopycat.veos.api.impl

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.*
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.datatypes.LongLong
import ru.inforion.lab403.kopycat.veos.api.datatypes.VaList
import ru.inforion.lab403.kopycat.veos.api.format.charArrayPointer
import ru.inforion.lab403.kopycat.veos.api.format.vsprintfMain
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.misc.*
import ru.inforion.lab403.kopycat.veos.api.pointers.CharPointer
import ru.inforion.lab403.kopycat.veos.api.pointers.IntPointer
import ru.inforion.lab403.kopycat.veos.exceptions.InvalidArgument
import ru.inforion.lab403.kopycat.veos.filesystems.impl.FileSystem
import ru.inforion.lab403.kopycat.veos.ports.stdc.FILE
import ru.inforion.lab403.kopycat.veos.ports.windows.ProcessorFeature


class WindowsAPI(os: VEOS<*>) : API(os) {

    companion object {
        @Transient val log = logger(INFO)
    }

    val argc = APIVariable.int(os, "_argc")
    val argv = APIVariable.pointer(os, "_argv")
    val envp = APIVariable.pointer(os, "_envp")
    val _OptionsStorage = APIVariable.longLong(os, "_OptionsStorage")


    fun vsprintf_internal(fmt: String?, args: Iterator<Long>): String {
        if (fmt == null) throw InvalidArgument()
        val buffer = CharArray(10000) // TODO: get rid of
        val count = vsprintfMain(os, buffer.charArrayPointer, fmt, args)
        return buffer.slice(0 until count).joinToString("")
    }

    init {
        type(ArgType.Pointer) { _, address -> FILE(sys, address) }

        ret<FILE> { APIResult.Value(it.address) }
    }

    override fun init(argc: Long, argv: Long, envp: Long) {
        this.argc.allocated.value = argc
        this.argv.allocated.value = argv
        this.envp.allocated.value = envp
        stdin.allocated.value = FILE.new(sys, FileSystem.STDIN_INDEX).address
        stdout.allocated.value = FILE.new(sys, FileSystem.STDOUT_INDEX).address
        stderr.allocated.value = FILE.new(sys, FileSystem.STDERR_INDEX).address
    }

    val stdin = APIVariable.pointer(os, "stdin")
    val stdout = APIVariable.pointer(os, "stdout")
    val stderr = APIVariable.pointer(os, "stderr")
    val stdinFile get() = FILE(sys, stdin.value)
    val stdoutFile get() = FILE(sys, stdout.value)
    val stderrFile get() = FILE(sys, stderr.value)

    abstract class __cdecl(name: String) : APIFunction(name)

    abstract inner class __stdcall(name: String) : __cdecl(name) {
        fun typeOf(arg: ArgType) = when(arg) {
            ArgType.Char -> os.abi.types.char
            ArgType.Short -> os.abi.types.short
            ArgType.Int -> os.abi.types.int
            ArgType.Long -> os.abi.types.long
            ArgType.LongLong -> os.abi.types.longLong
            ArgType.Pointer -> os.abi.types.pointer
        }

        fun retStack() {
            val pointerType = os.abi.types.pointer
            val ra = os.abi.pop(pointerType)

            args.forEach { os.abi.pop(typeOf(it)) } // TODO: alignment // TODO: use ABI

            os.abi.push(ra, pointerType)
        }

        override fun retval(value: Long): APIResult.Value {
            retStack()
            return super.retval(value)
        }

        override fun void(): APIResult.Void {
            retStack()
            return super.void()
        }
    }

    val GetSystemTimeAsFileTime = object : __stdcall("GetSystemTimeAsFileTime") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            // TODO: as struct
            os.abi.writeInt(argv[0], 0)
            os.abi.writeInt(argv[0] + 4, 0)
            return void()
        }
    }

    val GetCurrentThreadId = object : __stdcall("GetCurrentThreadId") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.warning { "GetCurrentThreadId() is not implemented properly" }
            return retval(os.currentProcess.id.asULong)
        }
    }

    val GetCurrentProcessId = object : __stdcall("GetCurrentProcessId") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            return retval(os.currentProcess.id.asULong)
        }
    }

    val QueryPerformanceCounter = object : __stdcall("QueryPerformanceCounter") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.warning { "QueryPerformanceCounter() is not implemented properly" }
            os.abi.writeLongLong(argv[0], System.currentTimeMillis())
            return retval(1L)
        }
    }

    val IsProcessorFeaturePresent = object : __stdcall("IsProcessorFeaturePresent") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val result = when (argv[0].asInt) {
                ProcessorFeature.XMMI64_INSTRUCTIONS_AVAILABLE.value -> false
                else -> TODO("Not implemented")
            }

            return retval(result.asLong)
        }
    }

    val _initterm_e = object : __cdecl("_initterm_e") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.warning { "_initterm() is not implemented properly" }
            return void()
        }
    }

    val _initterm = _initterm_e

    val __p___argc = object : __cdecl("__p___argc") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            return retval(argc.address!!)
        }
    }

    val __p___argv = object : __cdecl("__p___argv") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            return retval(this@WindowsAPI.argv.address!!)
        }
    }
    val _get_initial_narrow_environment = object : __cdecl("_get_initial_narrow_environment") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            return retval(envp.address!!)
        }
    }

    val __acrt_iob_func = object : __cdecl("__acrt_iob_func") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val result = when (argv[0]) {
                0L -> stdinFile.address
                1L -> stdoutFile.address
                2L -> stderrFile.address
                else -> 0L
            }
            return retval(result)
        }
    }
    val __local_stdio_printf_options = object : __cdecl("__local_stdio_printf_options") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            return retval(_OptionsStorage.allocated.address!!) // Unused
        }
    }


    @APIFunc
    fun __stdio_common_vfprintf(
            _Options: LongLong, // unsigned
            _Stream: FILE,
            _Format: CharPointer,
            _Locale: IntPointer, // _locale_t
            _ArgList: VaList): Int {

        return nothrow(-1) {
            val string = vsprintf_internal(_Format.string, _ArgList) // InvalidArgument -> EINVAL
            _Stream.write(string)
            string.length
        }
    }


    val GetModuleHandleW = object : __stdcall("GetModuleHandleW") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.warning { "GetModuleHandleW() is not implemented properly" }
            if (argv[0] == 0L)
                return retval(0)

            val lpModuleName = os.sys.readWideString(argv[0])
            return retval(0)
        }
    }

    val exit = object : __cdecl("GetModuleHandleW") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            return terminate(argv[0].asInt)
        }
    }

}