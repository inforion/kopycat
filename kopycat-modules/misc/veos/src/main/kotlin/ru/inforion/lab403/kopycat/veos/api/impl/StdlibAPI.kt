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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.pointers.BytePointer
import ru.inforion.lab403.kopycat.veos.api.pointers.FunctionPointer
import ru.inforion.lab403.kopycat.veos.api.pointers.VoidPointer
import ru.inforion.lab403.kopycat.veos.api.datatypes.size_t
import ru.inforion.lab403.kopycat.veos.ports.posix.nullptr

/**
 *
 * Implementation of stdlib.h of C standard library
 */
class StdlibAPI(os: VEOS<*>) : API(os) {
    /**
        String conversion:
        TODO: atof - Convert string to double
        [atoi] - Convert string to integer
        [atol] - Convert string to long integer
        TODO: atoll  - Convert string to long long integer
        TODO: strtod - Convert string to double
        TODO: strtof  - Convert string to float
        [strtol] - Convert string to long integer
        TODO: strtold  - Convert string to long double
        TODO: strtoll  - Convert string to long long integer
        [strtoul] - Convert string to unsigned long integer
        TODO: strtoull  - Convert string to unsigned long long integer

        Pseudo-random sequence generation:
        TODO: rand - Generate random number
        TODO: srand - Initialize random number generator

        Dynamic memory management:
        [calloc] - Allocate and zero-initialize array
        [free] - Deallocate memory block
        [malloc] - Allocate memory block
        [realloc] - Reallocate memory block

        Environment:
        [abort] - Abort current process
        [atexit] - Set function to be executed on exit
        TODO: at_quick_exit  - Set function to be executed on quick exit
        [exit] - Terminate calling process
        [getenv] - Get environment string
        TODO: quick_exit  - Terminate calling process quick
        TODO: system - Execute system command
        TODO: _Exit  - Terminate calling process

        Searching and sorting:
        TODO: bsearch - Binary search in array
        [qsort] - Sort elements of array

        Integer arithmetics:
        TODO: abs - Absolute value
        TODO: div - Integral division
        TODO: labs - Absolute value
        TODO: ldiv - Integral division
        TODO: llabs  - Absolute value
        TODO: lldiv  - Integral division

        Multibyte characters:
        TODO: mblen - Get length of multibyte character
        TODO: mbtowc - Convert multibyte sequence to wide character
        TODO: wctomb - Convert wide character to multibyte sequence

        Multibyte strings:
        TODO: mbstowcs - Convert multibyte string to wide-character string
        TODO: wcstombs - Convert wide-character string to multibyte string

        Macro constants:
        TODO: EXIT_FAILURE - Failure termination code
        TODO: EXIT_SUCCESS - Success termination code
        TODO: MB_CUR_MAX - Maximum size of multibyte characters
        TODO: NULL - Null pointer
        TODO: RAND_MAX - Maximum value returned by rand

        Types:
        TODO: div_t - Structure returned by div
        TODO: ldiv_t - Structure returned by ldiv
        TODO: lldiv_t  - Structure returned by lldiv
        TODO: size_t - Unsigned integral type
     */

    companion object {
        @Transient val log = logger(CONFIG)
    }

    // --- String conversion ---
    // http://www.cplusplus.com/reference/cstdlib/atof/
    // TODO: atof

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstdlib/atoi/
    val atoi = object : APIFunction("atoi") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val str = os.sys.readAsciiString(argv[0])
            val res = if (str != "") str.toInt().asLong else 0L
            return retval(res)
        }
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstdlib/atol/
    val atol = atoi

    // TODO: atoll
    // http://www.cplusplus.com/reference/cstdlib/atoll/
    // TODO: strtod
    // http://www.cplusplus.com/reference/cstdlib/atod/
    // TODO: strtof
    // http://www.cplusplus.com/reference/cstdlib/strtof/

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstdlib/strtol/
    val strtol = object : APIFunction("strtol") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val str = os.sys.readAsciiString(argv[0])
            val endptr = argv[1]
            val base = argv[2].asInt

            val items = str.trim().splitWhitespaces()
            val value = items[0].toLong(base)

            if (endptr != nullptr) {
                val feedback = argv[0] + str.indexOf(items[0]) + items[0].length
                os.abi.writePointer(endptr, feedback)
            }
            return retval(value)
        }
    }

    // TODO: strtold
    // http://www.cplusplus.com/reference/cstdlib/strtold/
    // TODO: strtoll
    // http://www.cplusplus.com/reference/cstdlib/strtoll/

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstdlib/strtoul/
    val strtoul = object : APIFunction("strtoul") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val str = os.sys.readAsciiString(argv[0])
            val endptr = argv[1]

            log.config { "[0x${ra.hex8}] strtoul(str='$str' endptr=${endptr.hex8} base=${argv[2]})" }

            val item = str.trim().splitWhitespaces().first()

            val base = if (argv[2] == 0L) {
                when {
                    item.startsWith("0x") -> 16
                    item.startsWith("0") && item.length != 1 -> 8
                    else -> 10
                }
            }
            else argv[2].asInt

            val pattern = when {
                base == 16 && item.startsWith("0x") -> item.substring(2)
                base == 8 && item.startsWith("0") && item.length != 1 -> item.substring(1)
                else -> item
            }

            val value = runCatching {
                pattern.toULong(base)
            }.getOrDefault(0)

            if (endptr != nullptr) {
                val feedback = argv[0] + str.indexOf(item) + item.length
                os.abi.writePointer(endptr, feedback)
            }
            return retval(value)
        }
    }

    // TODO: strtoull
    // http://www.cplusplus.com/reference/cstdlib/strtoull/

    // --- Pseudo-random sequence generation ---
    // TODO: rand
    // http://www.cplusplus.com/reference/cstdlib/rand/
    // TODO: srand
    // http://www.cplusplus.com/reference/cstdlib/srand/

    // --- Dynamic memory management ---

    // TODO: not tested
    // http://www.cplusplus.com/reference/cstdlib/calloc/
    @APIFunc
    fun calloc(num: size_t, size: size_t): VoidPointer {
        log.finest { "[0x${ra.hex8}] calloc(num=$num size=$size)" }
        val address = sys.allocateClean((num * size).toInt())
        return VoidPointer(sys, address)
    }

    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdlib/free/
    @APIFunc
    fun free(ptr: VoidPointer) {
        log.finest { "[0x${ra.hex8}] free(ptr=0x$ptr)" }

        // REVIEW: Different exception
        if (ptr.isNotNull && !sys.free(ptr.address))
            throw GeneralException("[0x${ra.hex8}] free() of unknown address: 0x${ptr.address.hex8}")
    }

    // REVIEW: check size
    // REVIEW: exception operating
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdlib/malloc/
    @APIFunc
    fun malloc(size: size_t): VoidPointer {
        log.finest { "[0x${ra.hex8}] malloc(size=$size)" }
        val address = sys.allocate(size.toInt())
        return VoidPointer(sys, address)
    }

    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdlib/realloc/
    @APIFunc
    fun realloc(ptr: VoidPointer, size: size_t): VoidPointer {
        log.finest { "[0x${ra.hex8}] realloc(ptr=$ptr size=$size)" }

        if (size.equals(0)) {
            sys.free(ptr.address)
            return VoidPointer.nullPtr(sys)
        }

        val address = sys.allocate(size.toInt())

        if (ptr.isNotNull) {
            val oldSize = sys.allocatedBlockSize(ptr.address) // REVIEW: rename function
            var oldData = sys.abi.readBytes(ptr.address, oldSize)
            sys.free(ptr.address)

            if (oldSize > size.toInt())
                oldData = oldData.copyOfRange(0, size.toInt())

            sys.abi.writeBytes(address, oldData)
        }

        return VoidPointer(sys, address)
    }

    // --- Environment ---

    // TODO: convert to new API
    // REVIEW: wrong implementation: should use signals
    // http://www.cplusplus.com/reference/cstdlib/abort/
    val abort = object : APIFunction("abort") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long) = terminate(0)
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstdlib/atexit/
    val atexit = nullsub("atexit")

    // TODO: at_quick_exit
    // http://www.cplusplus.com/reference/cstdlib/at_quick_exit/

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstdlib/exit/
    val exit = object : APIFunction("exit") {
        override val args = arrayOf(ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.fine { "[0x${ra.hex8}] exit(code=${argv[0]})" }
            return terminate(argv[0].asInt)
        }
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstdlib/getenv/
    val getenv = object : APIFunction("getenv") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val varName = os.sys.readAsciiString(argv[0])
            log.fine { "[0x${ra.hex8}] getenv(name='$varName')" }
            val res = os.sys.getEnvironmentVariable(varName)
            return retval(res)
        }
    }

    // TODO: quick_exit
    // http://www.cplusplus.com/reference/cstdlib/quick_exit/
    // TODO: system
    // http://www.cplusplus.com/reference/cstdlib/system/
    // TODO: _Exit
    // http://www.cplusplus.com/reference/cstdlib/_Exit/

    // --- Searching and sorting ---
    // TODO: bsearch
    // http://www.cplusplus.com/reference/cstdlib/bsearch/

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstdlib/qsort/
    @APIFunc
    fun qsort(ptr: BytePointer, count: Int, size: Int, comp: FunctionPointer) = /* void */
            withCallback(ptr.address, count.asULong, size.asULong, comp.address) {
                log.config { "[0x${ra.hex8}] qsort(ptr=$ptr count=${count.hex8} size=${size.hex8} comp=$comp)" }
                val addresses = List(count) { ptr.address + it * size }
                val sortedAddress = addresses.sortedWith { o1, o2 -> it.interrupt(comp.address, o1, o2).asInt }
                val sortedItems = sortedAddress.map { os.abi.readBytes(it, size) }
                (addresses zip sortedItems).forEach { (address, item) -> os.abi.writeBytes(address, item) }
            }

    // --- Integer arithmetics ---
    // TODO: abs
    // http://www.cplusplus.com/reference/cstdlib/abs/
    // TODO: div
    // http://www.cplusplus.com/reference/cstdlib/div/
    // TODO: labs
    // http://www.cplusplus.com/reference/cstdlib/labs/
    // TODO: ldiv
    // http://www.cplusplus.com/reference/cstdlib/ldiv/
    // TODO: llabs
    // http://www.cplusplus.com/reference/cstdlib/llabs/
    // TODO: lldiv
    // http://www.cplusplus.com/reference/cstdlib/lldiv/

    // --- Multibyte characters ---
    // TODO: mblen
    // http://www.cplusplus.com/reference/cstdlib/mblen/
    // TODO: mbtowc
    // http://www.cplusplus.com/reference/cstdlib/mbtowc/
    // TODO: wctomb
    // http://www.cplusplus.com/reference/cstdlib/wctomb/

    // --- Multibyte strings ---
    // TODO: mbstowcs
    // http://www.cplusplus.com/reference/cstdlib/mbstowcs/
    // TODO: wcstombs
    // http://www.cplusplus.com/reference/cstdlib/wcstombs/

    // --- Macro constants ---
    // TODO: EXIT_FAILURE
    // http://www.cplusplus.com/reference/cstdlib/EXIT_FAILURE/
    // TODO: EXIT_SUCCESS
    // http://www.cplusplus.com/reference/cstdlib/EXIT_SUCCESS/
    // TODO: MB_CUR_MAX
    // http://www.cplusplus.com/reference/cstdlib/MB_CUR_MAX/
    // TODO: NULL
    // http://www.cplusplus.com/reference/cstdlib/NULL/
    // TODO: RAND_MAX
    // http://www.cplusplus.com/reference/cstdlib/RAND_MAX/

    // --- Types ---
    // TODO: div_t
    // http://www.cplusplus.com/reference/cstdlib/div_t/
    // TODO: ldiv_t
    // http://www.cplusplus.com/reference/cstdlib/ldiv_t/
    // TODO: lldiv_t
    // http://www.cplusplus.com/reference/cstdlib/lldiv_t/
    // TODO: size_t
    // http://www.cplusplus.com/reference/cstddef/size_t/
}

