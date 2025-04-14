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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.modules.elanSC520.PIT.READBACK_TYPE.*
import ru.inforion.lab403.kopycat.serializer.loadEnum
import ru.inforion.lab403.kopycat.serializer.loadValue

@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
class PIT(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient val log = logger(FINE)

        const val INTERRUPT_COUNT = 3

        private val CTR_SEL_RANGE = 7..6
        private val CTR_CMD_RANGE = 5..4
    }

    inner class Ports : ModulePorts(this) {
        val irq = Port("irq")
        val io = Port("io")
    }

    override val ports = Ports()

    // 1.1892 MHz - frequency of one tick (triggering every 840 ns)
    val internalClockFrequency = 1_189_200.Hz
    val internalClockPeriod = (1.0 / internalClockFrequency).to_ns()

    enum class READBACK_TYPE { Count, Status, StatusThenCount, None }

    inner class PITxCNT_STA(port: Port, val id: Int) : Register(port, 0x0040uL + id.uint, BYTE, "PIT${id}CNT_STA") {
        // Fields for read
        var OUTPUT by bit(7)
        var NULL_CNT by bit(6)
        var RW by field(5..4)
        var CTR_MODE_STA by field(3..1)
        var BCD by bit(0)

        var CHx_CNT by field(7..0)

        var READBACK_LATCHED = 0uL

        // Latched data that counter repeatedly work out
        var LATCHED = 0uL
        // Current count of timer
        var COUNT = 0uL
        // Count latch mode (see datasheet)
        var CTR_RW_LATCH = 0uL
        // Meaning only for CTR_RW_LATCH == 3
        var no = 0

        // Readback count or status activated or status then count
        var READBACK = Status

        private var outputValue = false

        var output // real output value
            get() = outputValue
            set(value) {
                if (value && !outputValue) // positive edge
                    ports.irq.request(id)
                outputValue = value
            }

        var mode = 0
            set(value) {
                field = value
                outputValue = when (value) {
                    in 0..1 -> false
                    in 2..5 -> true
                    else -> throw GeneralException("Unknown mode $value")
                }
            }

        override fun stringify() = "${super.stringify()} [LATCHED=$LATCHED ENABLED=${timer.enabled} OUTPUT=$OUTPUT NULL_CNT=$NULL_CNT RW=$RW CTR_MODE_STA=$CTR_MODE_STA BCD=$BCD]"

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val tmp = value and 0xFFFF_FFFFuL // TODO: idk, why, but there was an int-cast
            when (CTR_RW_LATCH.int) {
                1 -> LATCHED = insert(tmp, 7..0)
                2 -> LATCHED = insert(tmp, 15..8)
                3 -> if (no == 0) {
                    LATCHED = LATCHED.insert(tmp, 7..0)
                    no += 1
                } else if (no == 1) {
                    LATCHED = LATCHED.insert(tmp, 15..8)
                    no += 1
                }
            }
            COUNT = LATCHED
            // Timer enabled for emulator core if at least one LATCHED value configured
            timer.enabled = PIT_CNT_STA.any { it.LATCHED != 0uL }
            log.write(FINE)
        }

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            when (READBACK) {
                Count -> when (CTR_RW_LATCH.int) {
                    1 -> CHx_CNT = READBACK_LATCHED[7..0]
                    2 -> CHx_CNT = READBACK_LATCHED[15..8]
                    3 -> if (no == 0) {
                        CHx_CNT = READBACK_LATCHED[7..0]
                        no += 1
                    } else if (no == 1) {
                        CHx_CNT = READBACK_LATCHED[15..7]
                        no += 1
                    }
                }
                Status, StatusThenCount -> {
                    OUTPUT = 0  // Output always low ...
                    NULL_CNT = 0  // Counter is available for reading (data LATCHED at once)
                    RW = CTR_RW_LATCH  // Reflects the last bit setting that was programmed
                    CTR_MODE_STA = PITMODECTL.CTR_MODE
                    BCD = PITMODECTL.BCD  // Reflects the last BCD setting for counters

                    // if all requested first give status then count
                    if (READBACK == StatusThenCount)
                        READBACK = Count
                }
                None -> throw GeneralException("Nothing latched for READBACK! Maybe it status by default...")
            }
            return super.read(ea, ss, size)
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return super.serialize(ctxt) + mapOf(
                    "LATCHED" to LATCHED,
                    "COUNT" to COUNT,
                    "CTR_RW_LATCH" to CTR_RW_LATCH,
                    "no" to no,
                    "READBACK_LATCHED" to READBACK_LATCHED,
                    "READBACK" to READBACK)
        }

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super.deserialize(ctxt, snapshot)
            LATCHED = loadValue(snapshot, "LATCHED") { 0u }
            COUNT = loadValue(snapshot, "COUNT") { 0u }
            CTR_RW_LATCH = loadValue(snapshot, "CTR_RW_LATCH") { 0u }
            no = loadValue(snapshot, "no") { 0 }
            READBACK = loadEnum(snapshot, "READBACK", Status)
            READBACK_LATCHED = loadValue(snapshot, "READBACK_LATCHED") { 0u }
        }
    }

    private val PIT_CNT_STA = Array(INTERRUPT_COUNT) { PITxCNT_STA(ports.io, it) }

    private val PITMODECTL = object : Register(ports.io, 0x0043u, BYTE, name = "PITMODECTL") {
        val CTR_SEL by field(CTR_SEL_RANGE)
        val CTR_RW_LATCH by field(CTR_CMD_RANGE)
        val CTR_MODE by field(3..1)
        val BCD by bit(0)

        override fun stringify(): String = "${super.stringify()} [CTR_SEL=$CTR_SEL CTR_RW_LATCH=$CTR_RW_LATCH CTR_MODE=$CTR_MODE BCD=$BCD]"

        override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong) =
                value[CTR_SEL_RANGE] != 3uL && value[CTR_CMD_RANGE] != 0uL

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            val PITCNT = PIT_CNT_STA[CTR_SEL.int]
            PITCNT.CTR_RW_LATCH = CTR_RW_LATCH
            PITCNT.no = 0
            PITCNT.mode = CTR_MODE.int
        }
    }

    private val PITCNTLAT = object : Register(ports.io, 0x0043u, BYTE, name = "PITCNTLAT") {
        val CTR_SEL by field(CTR_SEL_RANGE)
        val CTR_CMD by field(CTR_CMD_RANGE)

        override fun stringify(): String = "${super.stringify()} [CTR_SEL=$CTR_SEL CTR_CMD=$CTR_CMD]"

        // When this address (Port 0043h) is written with bits 7–6 != 11b and bits 5–4 = 00b, the PITCNTLAT register is addressed
        override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong) =
                value[CTR_SEL_RANGE] != 3uL && value[CTR_CMD_RANGE] == 0uL

        // Reads of this register (PITCNTLAT) return an undefined value
        override fun read(ea: ULong, ss: Int, size: Int): ULong = 0u

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            PIT_CNT_STA[CTR_SEL.int].READBACK = Count
            PIT_CNT_STA[CTR_SEL.int].READBACK_LATCHED = PIT_CNT_STA[CTR_SEL.int].COUNT
        }
    }

    private val PITRDBACK = object : Register(ports.io, 0x0043u, BYTE, "PITRDBACK") {
        val CTR_SEL by field(7..6)
        val LCNT by bit(5)
        val LSTAT by bit(4)
        val CNT2 by bit(3)
        val CNT1 by bit(2)
        val CNT0 by bit(1)

        override fun stringify(): String = "${super.stringify()} [CTR_SEL=$CTR_SEL LCNT=$LCNT LSTAT=$LSTAT CNT2=$CNT2 CNT1=$CNT1 CNT0=$CNT0]"

        // When this address (Port 0043h) is written with bits 7–6 = 11b, the PITRDBACK register is addressed
        override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong) = value[CTR_SEL_RANGE] == 3uL

        // Reads of this register (PITRDBACK) return an undefined value
        override fun read(ea: ULong, ss: Int, size: Int): ULong = 0u

        private fun prepareReadbackIfReq(pit: PITxCNT_STA, cnt: Int) {
            if (cnt == 1) when {
                LCNT == 0 && LSTAT == 1 -> {
                    pit.READBACK = Count
                    pit.READBACK_LATCHED = pit.COUNT
                }

                LCNT == 1 && LSTAT == 0 ->
                    pit.READBACK = Status

                LCNT == 0 && LSTAT == 0 -> {
                    pit.READBACK = StatusThenCount
                    pit.READBACK_LATCHED = pit.COUNT
                }

                else -> {
                    log.warning { "Something weird for LCNT=$LCNT and LSTAT=$LSTAT for PITRDBACK!" }
                    None
                }
            }
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            prepareReadbackIfReq(PIT_CNT_STA[2], CNT2)
            prepareReadbackIfReq(PIT_CNT_STA[1], CNT1)
            prepareReadbackIfReq(PIT_CNT_STA[0], CNT0)
        }
    }

    private val UNK_REG = object : Register(ports.io, 0x004Eu, BYTE, "UNKNOWNREGISTER") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong = TODO("$name: Read from ${address.hex2}")

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.severe { "$name: Write to ${address.hex2} = ${value.hex8}" }
        }
    }

    private val UNK_REG2 = object : Register(ports.io, 0x004Fu, BYTE, "UNKNOWNREGISTER2") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong = TODO("$name: Read from ${address.hex2}")

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.severe { "$name: Write to ${address.hex2} = ${value.hex8}" }
        }
    }

    private val timer = object : SystemClock.PeriodicalTimer("PIT Counter") {

        override fun trigger() {
            super.trigger()
            PIT_CNT_STA.filter { it.LATCHED != 0uL }.forEach { it ->
                when (it.mode) {
                    // MODE 0: INTERRUPT ON TERMINAL COUNT
                    // Gate = pause
                    0 -> {
                        it.COUNT -= 1u
                        if (it.COUNT == 0uL)
                            it.output = true
                    }
                    // MODE 1: HARDWARE RETRIGGERABLE ONE-SHOT
                    // Gate = reload
                    1 -> {
                        it.COUNT -= 1u
                        if (it.COUNT == 0uL)
                            it.output = true
                    }
                    // MODE 2: RATE GENERATOR
                    2 -> {
                        if (it.COUNT == 1uL) {
                            it.output = false
                            it.COUNT = it.LATCHED
                        }
                        else
                            it.COUNT -= 1u

                        if (it.COUNT == 1uL)
                            it.output = true
                    }
                    // MODE 3: SQUARE WAVE MODE
                    3 -> {
                        if (it.COUNT <= 1uL)
                            it.COUNT = it.LATCHED
                        else
                            it.COUNT -= 2u

                        if (it.COUNT <= 1uL)
                            it.output = !it.output
                    }
                    // MODE 4: SOFTWARE TRIGGERED MODE
                    // Gate = pause
                    4 -> {
                        if (it.COUNT == 0uL)
                            it.output = false

                        it.COUNT -= 1u

                        if (it.COUNT == 0uL)
                            it.output = true
                    }
                    // MODE 5: HARDWARE TRIGGERED STROBE (RETRIGGERABLE)
                    // Gate = reload
                    5 -> {
                        if (it.COUNT == 0uL)
                            it.output = false

                        it.COUNT -= 1u

                        if (it.COUNT == 0uL)
                            it.output = true
                    }


//                    2, 3 -> PIT_CNT_STA // TODO: that's nothing common with UserManual
//                        .filter { it.LATCHED != 0uL }
//                        .forEach {
//                            it.COUNT -= 1u
//                            if (it.COUNT == 0uL) {
//                                ports.irq.request(it.id)
//                                it.COUNT = it.LATCHED
//                                log.finest { "%s counter reached latched value at %,d ns".format(name, core.clock.time(Time.ns)) }
//                            }
//                        }

                    else -> TODO("Timer mode not implemented yet or not supported ${PITMODECTL.CTR_MODE}")

                }
            }
//            log.finest { "$name COUNT0=${PIT_CNT_STA[0].COUNT} COUNT1=${PIT_CNT_STA[1].COUNT} COUNT2=${PIT_CNT_STA[2].COUNT} triggered at %,d ns".format(dev.timer.time(Time.ns)) }
//            when (PITMODECTL.CTR_MODE.int) {
//            // Counting data from LATCHED value to 0 for each channel where LATCHED != 0 (just for performance)
//            // When zero reached trigger interrupt and reload COUNT value
//                2, 3 -> PIT_CNT_STA // TODO: that's nothing common with UserManual
//                        .filter { it.LATCHED != 0uL }
//                        .forEach {
//                            it.COUNT -= 1u
//                            if (it.COUNT == 0uL) {
//                                ports.irq.request(it.id)
//                                it.COUNT = it.LATCHED
//                                log.finest { "%s counter reached latched value at %,d ns".format(name, core.clock.time(Time.ns)) }
//                            }
//                        }
//                4 -> {
//
//                }
//                else -> TODO("Timer mode not implemented yet or not supported ${PITMODECTL.CTR_MODE}")
//            }
        }
    }

    override fun initialize(): Boolean {
        // FIXME: PIT AMD Elan - external clock source not supported
        // In hardware timer is working continually without stop but for performance sake timer enabled
        // for emulator core only if LATCHED != 0
        if (!super.initialize()) return false
        core.clock.connect(timer, internalClockPeriod, Time.ns, false)
        return true
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "timer" to timer.serialize(ctxt))
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        timer.deserialize(ctxt, snapshot["timer"] as Map<String, Any>)
    }
}