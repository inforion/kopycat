/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
@file:Suppress("PropertyName")

package ru.inforion.lab403.kopycat.modules.elanSC520

import org.joda.time.DateTime
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.field
import kotlin.reflect.KProperty

/**
 * Real-time clock
 */
class RTC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val io = Port("io")
    }

    override val ports = Ports()

    private val RTC_IDX_DATA = object : ByteAccessRegister(ports.io, 0x70u, WORD, "RTC_IDX_DATA") {
        var CMOSIDX by field(6..0)
        var CMOSDATA by field(15..8)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            val byte = when (val index = CMOSIDX.int) {
                0 -> RTCCURSEC
                1 -> TODO("RTCALMSEC not implemented!")
                2 -> RTCCURMIN
                3 -> TODO("RTCALMMIN not implemented!")
                4 -> RTCCURHR
                5 -> TODO("RTCALMHR not implemented!")
                6 -> RTCCURDOW
                7 -> RTCCURDOM
                8 -> RTCCURMON
                9 -> RTCCURYR
                10 -> RTCCTLA
                11 -> RTCCTLB
                12 -> RTCCTLC
                13 -> RTCCTLD
                else -> RTCCMOS[index].uint_z
            }
            CMOSDATA = byte.ulong_z
            log.warning { "RTC read $CMOSIDX -> $byte" }
            return super.read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            val off = offset(ea).int
            if (off == 0 && size > 1 || off == 1) {
                val byte = CMOSDATA.uint
                log.warning { "RTC write $CMOSIDX -> $byte" }
                when (val index = CMOSIDX.int) {
                    0 -> RTCCURSEC = byte
                    1 -> if (byte != 0u) TODO("RTCALMSEC not implemented!")
                    2 -> RTCCURMIN = byte
                    3 -> if (byte != 0u) TODO("RTCALMMIN not implemented!")
                    4 -> RTCCURHR = byte
                    5 -> if (byte != 0u) TODO("RTCALMHR not implemented!")
                    6 -> RTCCURDOW = byte
                    7 -> RTCCURDOM = byte
                    8 -> RTCCURMON = byte
                    9 -> RTCCURYR = byte
                    10 -> RTCCTLA = byte
                    11 -> RTCCTLB = byte
                    12 -> RTCCTLC = byte
                    13 -> RTCCTLD = byte
                    else -> RTCCMOS[index] = byte.byte
                }
            }
        }
    }

    private var date = DateTime(2000, 1, 1, 0, 0, 0)

    class DateRegister(val how: (date: DateTime) -> DateTime.Property) {
        operator fun getValue(thisRef: RTC, property: KProperty<*>) = how(thisRef.date).get().uint
        operator fun setValue(thisRef: RTC, property: KProperty<*>, value: UInt) {
            thisRef.date = how(thisRef.date).setCopy(value.int)
            val year = thisRef.date.yearOfEra()
            if (year.get() < 2000)
                thisRef.date = year.setCopy(2000)
        }
    }

    class ControlRegister(val index: Char) {
        private var field: UInt = 0u

        operator fun getValue(thisRef: RTC, property: KProperty<*>): UInt {
            log.severe { "Read control reg $index -> ${field.hex}" }
            return field
        }

        operator fun setValue(thisRef: RTC, property: KProperty<*>, value: UInt) {
            log.severe { "Written control reg $index -> ${value.hex}" }
            field = value
        }
    }

    private var RTCCURYR by DateRegister { it.yearOfCentury() }
    private var RTCCURMON by DateRegister { it.monthOfYear() }
    private var RTCCURDOM by DateRegister { it.dayOfMonth() }
    private var RTCCURDOW by DateRegister { it.dayOfWeek() }
    private var RTCCURHR by DateRegister { it.hourOfDay() }
    private var RTCCURMIN by DateRegister { it.minuteOfHour() }
    private var RTCCURSEC by DateRegister { it.secondOfMinute() }
    private var RTCCTLA by ControlRegister('A')
    private var RTCCTLB by ControlRegister('B')
    private var RTCCTLC by ControlRegister('C')
    private var RTCCTLD by ControlRegister('D')
    private val RTCCMOS = ByteArray(114)

    private val secondsTimer = object : SystemClock.PeriodicalTimer("RTC Timer") {
        override fun trigger() {
            super.trigger()
            date = date.plusSeconds(1)
        }
    }

    override fun initialize(): Boolean {
        if (!super.initialize()) return false
        core.clock.connect(secondsTimer, 1, Time.s, true)
        return true
    }
}