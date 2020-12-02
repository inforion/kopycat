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
package ru.inforion.lab403.kopycat.cores.base.common

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.Time
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.choices
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.logging.Level

/**
 * {EN}
 *
 * System timer calculate device time and watch the hardware timers triggered
 *
 * @param frequency Timer frequency (according to quartz generator, i.e. 50 MHz)
 * @param name System timer name (used for serialization/deserialization)
 * {EN}
 */
class SystemClock constructor(val core: AGenericCore, val frequency: Long, override val name: String) : ICoreUnit {

    companion object {
        @Transient val log = logger(Level.WARNING)
    }

    /**
     * {RU}Общее количество тысячных частей циклов{RU}
     */
    private var totalCycles: Double = 0.0

    /**
     * {RU}Общее количество вызовов update системного времени{RU}
     */
    var totalTicks: Long = 0
        private set

    private val triggered = ArrayList<ATriggerable>(32)

    /**
     * {RU}
     * @param scaledCycles количество циклов, которое необходимо добавить к счетчику
     * {RU}
     *
     * {EN}
     * Update internal [totalCycles] count in system timer and check for timers to trigger and launch it
     *
     * @param scaledCycles cycles count passed in timescale of [SystemClock] (equals to cycles / ipc) not a processor
     * {EN}
     */
    private fun updateAndTrigger(scaledCycles: Double) {
        totalCycles += scaledCycles
        totalTicks += 1

        // ATTENTION: don't use for (k in indices) ... -> race conditions in core can occurred

        triggered.clear()

        for (k in timers.indices) {
            val timer = timers[k]
            if (timer.enabled && timer.isTriggered(totalCycles))
                triggered.add(timer)
        }

        for (k in triggered.indices)
            triggered[k].trigger()
    }

    /**
     * {EN}
     * Scale cycles to timescale of [SystemClock] and execute [updateAndTrigger]
     * Then checks is [AGenericCore] in halted state and this update timers and wait until processor will be unhalted.
     * Unhalt of processor can occurs when interrupt is given.
     *
     * @param cycles cycles count passed in timescale of [AGenericCore]
     * {EN}
     */
    fun update(cycles: Int) {
        updateAndTrigger(cycles / core.ipc)
        while (core.cpu.halted) {
            // for performance sake
            var cyclesLeft = Double.MAX_VALUE
            for (k in timers.indices) {
                val timer = timers[k]
                if (timer.enabled) {
                    val newLeftCycles = timer.cyclesLeft(totalCycles)
                    if (newLeftCycles < cyclesLeft)
                        cyclesLeft = newLeftCycles
                }
            }
            updateAndTrigger(cyclesLeft)
        }
    }

    private val timers = ArrayList<ATriggerable>()

    abstract class ATriggerable : ICoreUnit {
        private lateinit var clock: SystemClock

        // trigger counter
        protected var triggered: Long = 0

        // timer period in CPU clock cycles
        protected var period: Double = 0.0
            private set

        // reference value from which timer check period
        protected var reference: Double = 0.0

        fun cyclesLeft(cycles: Double): Double {
            val left = period - cycles + reference
            return if (left < 0) 0.0 else left
        }

        var enabled = false
            set(value) {
                if (value) {
                    if (!field) {
                        // log.finest { "Enable timer: %s at %,d us".format(name, dev.timer.timestamp) }
                        reference = clock.totalCycles
                        triggered = 0
                        clock.timers.add(this)
                    }
                } else {
                    if (field) {
                        // log.finest { "Disable timer: %s at %,d us".format(name, dev.timer.timestamp) }
                        reference = 0.0
                        clock.timers.remove(this)
                    }
                }
                field = value
            }

        val isClockConnected get() = ::clock.isInitialized

        fun connect(clock: SystemClock, period: Long, unit: Time? = null, enabled: Boolean = true) {
            // ATTENTION: Don't change this (timers may not work)
            val conf = if (unit == null) period else period * clock.frequency / unit.divider
            if (!isClockConnected) {
                this.clock = clock
            } else if (this.clock != clock)
                throw IllegalArgumentException("Wrong clock signal reconnected to timer")
            this.period = conf.toDouble()
            this.enabled = enabled
        }

        fun passed(cycles: Double) = cycles - reference
        protected fun willTrigger(cycles: Double) = passed(cycles) >= period

        abstract fun isTriggered(cycles: Double): Boolean
        abstract fun trigger()

        override fun reset() {
            super.reset()
            enabled = false
            period = 0.0
            reference = 0.0
            triggered = 0
        }

        override fun serialize(ctxt: GenericSerializer) = storeValues(
                "name" to name,
                "enabled" to enabled,
                "period" to period,
                "reference" to reference,
                "triggered" to triggered,
                "clock" to clock.serialize(ctxt))

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = with(ctxt) {
            // don't shuffle it, order is important!
            period = loadValue(snapshot, "period")
            enabled = loadValue(snapshot, "enabled")
            reference = loadValue(snapshot, "reference")
            triggered = loadValue(snapshot, "triggered")
            @Suppress("UNCHECKED_CAST")
            clock.deserialize(ctxt, snapshot["clock"] as Map<String, Any>)
        }
    }

    open class OneshotTimer(final override val name: String) : SystemClock.ATriggerable() {
        final override fun isTriggered(cycles: Double): Boolean = willTrigger(cycles)

        override fun trigger() {
            enabled = false
        }
    }

    open class PeriodicalTimer(final override val name: String) : SystemClock.ATriggerable() {
        final override fun isTriggered(cycles: Double): Boolean {
            //  Ref    Cycles   Delta    Period    isTrigger              viaAlgo
            //   0      10       10       16          -                  ---------
            //   0      20       20       16          +         20 - (20 % 16) = 20 - 4 = 16
            //   16     30       14       16          -                  ---------
            //   16     40       24       16          +         40 - (40 % 16) = 40 - 8 = 32
            //   32     50       18       16          +         50 - (50 % 16) = 20 - 4 = 48
            //   48     60       12       16          -                  ---------
            if (willTrigger(cycles)) {
                reference = if (period != 0.0) cycles - (cycles % period) else 0.0
                return true
            }
            return false
        }

        override fun trigger() {
            triggered += 1
        }
    }

    fun connect(timer: ATriggerable, period: Long, unit: Time, run: Boolean = true) =
            timer.connect(this, period, unit, run)

    fun connect(timer: ATriggerable, period: Long, run: Boolean = true) =
            timer.connect(this, period, null, run)

    fun connect(timer: ATriggerable) =
            timer.connect(this, 0, null, false)

    fun add(timer: ATriggerable): Boolean {
        // log.finest { "%s enabled period = %d ref = %08X".format(timer.name, timer.period.asLong, cycles.asLong) }
        if (timer in timers) {
            log.warning { "Timer %d already enabled and trying to wind up it -> ignore".format(timer) }
            return false
        }
        timers.add(timer)
        return true
    }

    /**
     * {EN}
     * Equals to time in us (deprecated, use time)
     * {EN}
     */
    @Deprecated("Use `time` instead", replaceWith = ReplaceWith("time"))
    var timestamp: Long
        get() = time(Time.us)
        set(value) = time(value, Time.us)

    fun time(unit: Time = Time.us): Long = (totalCycles * unit.divider / frequency).asLong

    fun timeToCycles(value: Long, unit: Time = Time.us) = value.toDouble() * frequency / unit.divider

    fun time(value: Long, unit: Time = Time.us) {
        totalCycles = timeToCycles(value, unit)
    }

    override fun reset() {
        super.reset()
        totalCycles = 0.0
    }

    override fun serialize(ctxt: GenericSerializer) = storeValues("totalCycles" to totalCycles)

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        // TODO: Remove backward compat. quirk ASAP
        val key = if ("totalCycles" !in snapshot) "ticks" else "totalCycles"
        totalCycles = loadValue(snapshot, key)
    }

    override fun command(): String? = "timer"

    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent)?.apply {
                choices("type", listOf("count", "timestamp"), help = "What to get?")
            }

    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        context.result = when (context.command()) {
            "timestamp" -> "timestamp = %08X (%,d)".format(time(), time())
            else -> return false
        }

        context.pop()

        return true
    }
}