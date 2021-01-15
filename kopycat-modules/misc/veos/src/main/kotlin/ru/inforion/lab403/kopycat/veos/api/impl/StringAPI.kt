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
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.pointers.CharPointer
import ru.inforion.lab403.kopycat.veos.api.datatypes.size_t
import ru.inforion.lab403.kopycat.veos.ports.cstdlib.errlistEnternal
import ru.inforion.lab403.kopycat.veos.ports.posix.nullptr

/**
 *
 * Implementation of string.h of C standard library
 */
class StringAPI(os: VEOS<*>) : API(os) {
    /**
        Copying:
        [memcpy]    - Copy block of memory
        [memmove]   - Move block of memory
        [strcpy]    - Copy string
        [strncpy]   - Copy characters from string

        Concatenation:
        [strcat]    - Concatenate strings
        [strncat]   - Append characters from string

        Comparison:
        [memcmp]    - Compare two blocks of memory
        [strcmp]    - Compare two strings
        [strcoll]   - Compare two strings using locale
        [strncmp]   - Compare characters of two strings
        TODO: strxfrm - Transform string using locale

        Searching:
        [memchr]    - Locate character in block of memory
        [strchr]    - Locate first occurrence of character in string
        [strcspn]   - Get span until character in string
        [strpbrk]   - Locate characters in string
        [strrchr]   - Locate last occurrence of character in string
        TODO: strspn - Get span of character set in string
        [strstr]    - Locate substring
        TODO: strtok - Split string into tokens

        Other:
        [memset]    - Fill block of memory
        [strerror]  - Get pointer to error message string
        [strlen]    - Get string length

        REVIEW: --- POSIX extension of Stdio functions ---
        [strnlen]   - REVIEW: description
     */

    val strerrorBuffer by lazy { sys.allocate(1024, os.systemData) }

    // --- Copying ---

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/memcpy/
    val memcpy = object : APIFunction("memcpy") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val buf = os.abi.readBytes(argv[1], argv[2].asInt)
            os.abi.writeBytes(argv[0], buf)
            return retval(argv[0])
        }
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/memmove/
    val memmove = memcpy

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strcpy/
    val strcpy = object : APIFunction("strcpy") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val dst = argv[0]
            val src = os.sys.readAsciiString(argv[1])
            os.sys.writeAsciiString(dst, src)
            return retval(dst)
        }
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strncpy/
    val strncpy = object : APIFunction("strncpy") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val dst = argv[0]
            val src = os.sys.readAsciiString(argv[1])
            val n = argv[2].asInt
            os.sys.writeAsciiString(dst, src[0..minOf(n, src.length)])
            return retval(dst)
        }
    }

    // --- Concatenation ---

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strcat/
    val strcat = object : APIFunction("strcat") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val deststr = os.sys.readAsciiString(argv[0])
            val srcstr = os.sys.readAsciiString(argv[1])
            os.sys.writeAsciiString(argv[0], deststr + srcstr)
            return retval(argv[0])
        }
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strncat/
    val strncat = object : APIFunction("strncat") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val deststr = os.sys.readAsciiString(argv[0])
            val srcstr = os.sys.readAsciiString(argv[1])
            val num = argv[2].asInt
            val res = deststr + srcstr[0..minOf(srcstr.length, num)]
            os.sys.writeAsciiString(argv[0], res)
            return retval(argv[0])
        }
    }

    // --- Comparison ---

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/memcmp/
    val memcmp = object : APIFunction("memcmp") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val num = argv[2].asInt
            val data1 = os.abi.readBytes(argv[0], num)
            val data2 = os.abi.readBytes(argv[1], num)
            var res = 0

            for(i in 0 until num) {
                res = data1[i] - data2[i]
                if (res != 0)
                    break
            }

            return retval(res.asLong)
        }
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strcmp/
    val strcmp = object : APIFunction("strcmp") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val str1 = os.sys.readAsciiString(argv[0])
            val str2 = os.sys.readAsciiString(argv[1])
            return retval(str1.compareTo(str2).asLong)
        }
    }

    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstring/strcoll/
    @APIFunc
    fun strcoll(str1: CharPointer, str2: CharPointer) = strcmp.exec("strcoll", str1.address, str2.address)

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strncmp/
    val strncmp = object : APIFunction("strncmp") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val n = argv[2].asInt
            var str1 = os.sys.readAsciiString(argv[0])
            var str2 = os.sys.readAsciiString(argv[1])
            str1 = str1[0..minOf(n, str1.length)]
            str2 = str2[0..minOf(n, str2.length)]
            return retval(str1.compareTo(str2).asLong)
        }
    }

    // TODO: strxfrm
    // http://www.cplusplus.com/reference/cstring/strxfrm/

    // --- Searching ---

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/memchr/
    val memchr = object : APIFunction("memchr") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val buf = os.abi.readBytes(argv[0], argv[2].asInt)
            val value = argv[1].asByte
            val idx = buf.indexOfFirst { it == value }
            val res = if (idx != -1) argv[0] + idx else 0
            return retval(res)
        }
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strchr/
    val strchr = object : APIFunction("strchr") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val s = os.sys.readAsciiString(argv[0])
            val ch = argv[1].toChar()
            val index = s.indexOfFirst { it == ch }
            return if (index == -1) retval(0) else retval(argv[0] + index)
        }
    }

    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstring/strcspn/
    @APIFunc
    fun strcspn(str1: CharPointer, str2: CharPointer): size_t {
        val start = strpbrk(str1, str2)
        return if (start.isNull) size_t(str1.string.length.ulong) else size_t((start.address - str1.address).ulong)
    }

    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstring/strpbrk/
    @APIFunc
    fun strpbrk(str1: CharPointer, str2: CharPointer): CharPointer {
        val string = str1.string
        val pattern = str2.string

        val ind = string.withIndex().find { it.value in pattern }?.index ?: return CharPointer.nullPtr(os.sys)

        return CharPointer(os.sys, str1.address + ind)
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strrchr/
    val strrchr = object : APIFunction("strrchr") {
        override val args = arrayOf(ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val s = os.sys.readAsciiString(argv[0])
            val ch = argv[1].toChar()
            val index = s.lastIndexOf(ch)
            return if (index == -1) retval(0) else retval(argv[0] + index)
        }
    }

    // TODO: strspn
    // http://www.cplusplus.com/reference/cstring/strspn/

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strstr/
    val strstr = object : APIFunction("strstr") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val haystack = os.sys.readAsciiString(argv[0])
            val needle = os.sys.readAsciiString(argv[1])
            val start = haystack.indexOf(needle)
            return if (start == -1) retval(nullptr) else retval(argv[0] + start)
        }
    }

    // TODO: strtok
    // http://www.cplusplus.com/reference/cstring/strtok/

    // --- Other ---

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/memset/
    val memset = object : APIFunction("memset") {
        override val args = arrayOf(ArgType.Int, ArgType.Int, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val buf = ByteArray(argv[2].asInt) { argv[1].asByte }
            os.abi.writeBytes(argv[0], buf)
            return retval(argv[0])
        }
    }

    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstring/strerror/
    @APIFunc
    fun strerror(errnum: Int): CharPointer {
        val result = errlistEnternal[errnum] ?: "Unknown error $errnum"
        sys.writeAsciiString(strerrorBuffer, result)
        return CharPointer(sys, strerrorBuffer)
    }

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strlen/
    val strlen = object : APIFunction("strlen") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long) = retval(os.sys.readAsciiString(argv[0]).length.asLong)
    }

    // REVIEW: --- POSIX extension of Stdio functions ---

    // TODO: convert to new API
    // http://www.cplusplus.com/reference/cstring/strnlen/
    val strnlen = object : APIFunction("strnlen") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val maxLen = argv[1].asInt
            val res = minOf(os.sys.readAsciiString(argv[0]).length, maxLen)
            return retval(res.asLong)
        }
    }
}