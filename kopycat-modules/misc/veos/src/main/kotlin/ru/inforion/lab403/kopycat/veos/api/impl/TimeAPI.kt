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
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.datatypes.Sizet
import ru.inforion.lab403.kopycat.veos.api.datatypes.size_t
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.pointers.CharPointer
import ru.inforion.lab403.kopycat.veos.api.pointers.LongPointer
import ru.inforion.lab403.kopycat.veos.ports.time.timespec
import ru.inforion.lab403.kopycat.veos.ports.time.tm
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of time.h of C standard library
 */
class TimeAPI(os: VEOS<*>) : API(os) {
    companion object {
        @Transient val log = logger(CONFIG)
    }

    init {
        type(ArgType.Pointer) { _, it -> timespec(os.sys, it) }
        type(ArgType.Pointer) { _, it -> tm(os.sys, it) }

        ret<tm> { APIResult.Value(it.address) }
    }

    // http://www.cplusplus.com/reference/ctime/strftime/
    @APIFunc
    fun strftime(s: CharPointer, max: size_t, fmt: CharPointer, tm_s: tm): size_t {
        var format = fmt.string

        if ("'" in format)
            throw NotImplementedError("[0x${ra.hex8}] Now we have to escape the '-symbols")

        format = "'$format'"

        val calendar = Calendar.getInstance().apply {
            set(tm_s.tm_year.asInt, tm_s.tm_mon.asInt, tm_s.tm_mday.asInt)
        }

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
        if (max < result.length + 1)
            return Sizet(0U)

        s.string = result

        return Sizet(result.length.ulong)
    }

    @DontAutoSerialize
    private val tm_glob_s by lazy { tm.allocate(sys) }

    private fun tm.fromCalendar(calendar: Calendar) = apply {
        tm_sec = calendar[Calendar.SECOND]
        tm_min = calendar[Calendar.MINUTE]
        tm_hour = calendar[Calendar.HOUR]
        tm_mday = calendar[Calendar.DAY_OF_MONTH]
        tm_mon = calendar[Calendar.MONTH]
        tm_year = calendar[Calendar.YEAR] - 1900  // we need since 1900 see doc
        tm_wday = calendar[Calendar.DAY_OF_WEEK] - 1
        tm_yday = calendar[Calendar.DAY_OF_YEAR] - 1
        tm_isdst = -1
    }

    private fun tm.toCalendar() = Calendar.getInstance().apply {
        set(tm_year + 1900, tm_mon, tm_mday, tm_hour, tm_min, tm_sec)
    }

    private fun time_intern(timep: LongPointer, tm_s: tm, tz: String?): tm {
        log.finer { "[0x${ra.hex8}] time_intern_r(time=${timep.get} tm=0x$tm_s tz=$tz) in ${os.currentProcess}" }
        val zone = if (tz != null) TimeZone.getTimeZone(tz) else TimeZone.getDefault()
        val calendar = Calendar.getInstance(zone).apply { timeInMillis = timep.get * 1000 }
        return tm_s.fromCalendar(calendar)
    }

    @APIFunc
    fun localtime_r(timep: LongPointer, tm_s: tm): tm {
        log.fine { "[0x${ra.hex8}] localtime_r(time=${timep.get} tm=0x$tm_s) in ${os.currentProcess}" }
        return time_intern(timep, tm_s, null)
    }

    @APIFunc
    fun localtime(timep: LongPointer): tm {
        log.fine { "[0x${ra.hex8}] localtime(time=${timep.get}) in ${os.currentProcess}" }
        return time_intern(timep, tm_glob_s, null)
    }

    @APIFunc
    fun gmtime_r(timep: LongPointer, tm_s: tm): tm {
        log.fine { "[0x${ra.hex8}] gmtime_r(time=${timep.get} tm=0x$tm_s) in ${os.currentProcess}" }
        return time_intern(timep, tm_s, "UTC")
    }

    @APIFunc
    fun gmtime(timep: LongPointer): tm {
        log.fine { "[0x${ra.hex8}] gmtime(time=${timep.get}) in ${os.currentProcess}" }
        return time_intern(timep, tm_glob_s, "UTC")
    }

    @APIFunc
    fun time(t: LongPointer): Long {
        val time = os.sys.time / 1_000
        log.fine { "[0x${ra.hex8}] time(t=0x$t) -> $time in ${os.currentProcess}" }
        if (t.isNotNull) t.set(time)
        return time
    }

    @APIFunc
    fun clock_gettime(clk_id: Int, tp: timespec): Int {
        tp.tv_sec = os.sys.time / 1_000
        log.fine { "[0x${ra.hex8}] clock_gettime(clk_id=$clk_id tp=0x$tp) -> tv_sec=${tp.tv_sec} in ${os.currentProcess}" }
        return 0
    }

    // https://linux.die.net/man/3/mktime
    @APIFunc
    fun mktime(tm_s: tm): Long {
        log.fine { "[0x${ra.hex8}] mktime(tm=0x$tm_s) -> in ${os.currentProcess}" }
        return tm_s.toCalendar().timeInMillis / 1000
    }

    // https://linux.die.net/man/3/tzset
    @APIFunc
    fun strptime(s: CharPointer, format: CharPointer, tm_s: tm): CharPointer {
        log.config { "[0x${ra.hex8}] strptime(s=0x$s format=${format.string} tm=0x$tm_s) not implemented" }
        return s
    }

    // https://linux.die.net/man/3/tzset
    @APIFunc
    fun tzset(): Int {
        log.config { "[0x${ra.hex8}] tzset() not implemented" }
        return 0
    }
}