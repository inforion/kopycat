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
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIVariable
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.pointers.*
import ru.inforion.lab403.kopycat.veos.ports.cstdlib.CTypeB
import ru.inforion.lab403.kopycat.veos.ports.locale.lconv
import java.nio.ByteBuffer


/**
 *
 * Implementation of locale.h of C standard library
 */
class LocaleAPI(os: VEOS<*>) : API(os) {
    companion object {
        @Transient val log = logger(CONFIG)
    }

    init {
        ret<lconv> { APIResult.Value(it.address) }
    }

    val ctype_b = APIVariable.int(os, "__ctype_b")

    @DontAutoSerialize
    private val ctype_b_table by lazy {
        val table = CTypeB.data
        val size = table.size * os.sys.sizeOf.short // sizeof(uint16_t)
        val buffer = ByteBuffer.allocate(size).order(os.currentMemory.endian)
        table.forEach { buffer.putShort(it.asShort) }
        os.sys.allocateArray(buffer.array(), os.systemData)
    }

    override fun init(argc: Long, argv: Long, envp: Long) {
        // Allocate __ctype_b table
        if (ctype_b.linked) ctype_b.value = ctype_b_table
    }

    // http://www.cplusplus.com/reference/clocale/setlocale
    val setlocale = object : APIFunction("setlocale") {
        override val args = arrayOf(ArgType.Int, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val category = first<PosixAPI.LC> { it.id == argv[0].asInt }
            val locale = argv[1]
            if (locale != 0L) {
                val localeString = os.sys.readAsciiString(locale)
                check(localeString == "C" || localeString.isEmpty()) { "[0x${ra.hex8}] setlocale $category = $localeString failed" }
                log.fine { "[0x${ra.hex8}] setlocale($category=$localeString) in ${os.currentProcess}" }
            }
            val result = os.sys.allocateAsciiString("C") // TODO: constant
            return retval(result)
        }
    }

    @DontAutoSerialize
    private val lconv_c by lazy {
        lconv.allocate(sys).apply {
            val CHAR_MAX = 127.asByte
            val empty = sys.allocateAsciiString("")
            val numempty = sys.allocateAsciiString(byteArrayOf(CHAR_MAX, 0x00).convertToString())

            decimal_point = sys.allocateAsciiString(".")
            thousands_sep = sys.allocateAsciiString(" ")

            grouping = numempty
            int_curr_symbol = empty
            currency_symbol = empty
            mon_decimal_point = empty
            mon_thousands_sep = empty
            mon_grouping = numempty
            positive_sign = empty
            negative_sign = empty

            int_frac_digits = CHAR_MAX
            frac_digits = CHAR_MAX
            p_cs_precedes = CHAR_MAX
            p_sep_by_space = CHAR_MAX
            n_cs_precedes = CHAR_MAX
            n_sep_by_space = CHAR_MAX
            p_sign_posn = CHAR_MAX
            n_sign_posn = CHAR_MAX
            int_p_cs_precedes = CHAR_MAX
            int_p_sep_by_space = CHAR_MAX
            int_n_cs_precedes = CHAR_MAX
            int_n_sep_by_space = CHAR_MAX
            int_p_sign_posn = CHAR_MAX
            int_n_sign_posn = CHAR_MAX
        }
    }

    @APIFunc
    fun localeconv(): lconv {
        log.fine { "[0x${ra.hex8}] localeconv() in ${os.currentProcess}" }
        return lconv_c
    }

    val nl_langinfo_buffer by lazy { sys.allocate(512) }

    // https://git.sprintf.io/emscripten-ports/emscripten/commit/ee4b6ebf353585fb0eb0809a4b677f5694a9ba79?style=unified
    @APIFunc
    fun nl_langinfo(item: Int): CharPointer {
        val string = when (item) {
            0xE -> "ANSI_X3.4-1968"
            0x20028 -> "%a %b %e %H:%M:%S %Y"
            0x20029 -> "%m/%d/%y"
            0x2002A -> "%H:%M:%S"
            0x2002B -> "%I:%M:%S %p"
            0x20026 -> "AM"
            0x20027 -> "PM"
            0x20007 -> "Sunday"
            0x20008 -> "Monday"
            0x20009 -> "Tuesday"
            0x2000A -> "Wednesday"
            0x2000B -> "Thursday"
            0x2000C -> "Friday"
            0x2000D -> "Saturday"
            0x20000 -> "Sun"
            0x20001 -> "Mon"
            0x20002 -> "Tue"
            0x20003 -> "Wed"
            0x20004 -> "Thu"
            0x20005 -> "Fri"
            0x20006 -> "Sat"
            0x2001A -> "January"
            0x2001B -> "February"
            0x2001C -> "March"
            0x2001D -> "April"
            0x2001E -> "May"
            0x2001F -> "June"
            0x20020 -> "July"
            0x20021 -> "August"
            0x20022 -> "September"
            0x20023 -> "October"
            0x20024 -> "November"
            0x20025 -> "December"
            0x2000E -> "Jan"
            0x2000F -> "Feb"
            0x20010 -> "Mar"
            0x20011 -> "Apr"
            0x20012 -> "May"
            0x20013 -> "Jun"
            0x20014 -> "Jul"
            0x20015 -> "Aug"
            0x20016 -> "Sep"
            0x20017 -> "Oct"
            0x20018 -> "Nov"
            0x20019 -> "Dec"
            0x10000 -> "."
            0x10001 -> ""
            0x50000 -> "^[yY]"
            0x50001 -> "^[nN]"
            0x4000F -> "-"
            else -> ""
        }

        log.fine { "[0x${ra.hex8}] nl_langinfo(item=$item) -> $string in ${os.currentProcess}" }

        sys.writeAsciiString(nl_langinfo_buffer, string)
        return CharPointer(sys, nl_langinfo_buffer)
    }

    @APIFunc
    fun __ctype_b_loc(): VoidPointer {
        log.fine { "[0x${ra.hex8}] __ctype_b_loc() -> 0x$ctype_b_table in ${os.currentProcess}" }
        if (!ctype_b.linked) {
            ctype_b.allocated.value = ctype_b_table
        }
        return VoidPointer(sys, ctype_b.address!!)
    }
}