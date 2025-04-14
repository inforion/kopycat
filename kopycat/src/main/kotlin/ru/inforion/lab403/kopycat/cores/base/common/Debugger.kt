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
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.*
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.common.Breakpoint.Access.*
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.enums.Status.CORE_EXECUTED
import ru.inforion.lab403.kopycat.cores.base.enums.Status.NOT_EXECUTED
import ru.inforion.lab403.kopycat.cores.base.exceptions.BreakpointException
import ru.inforion.lab403.kopycat.cores.base.extensions.*
import ru.inforion.lab403.kopycat.interfaces.IDebugger
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.settings
import java.math.BigInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.milliseconds

/**
 * {RU}
 * Стандартный модуль отладчик
 *
 *
 * @param parent родительский модуль, в который встраивается Отладчик
 * @param name произвольное имя объекта Отладчика
 * @property ports отображение (HashMap) доступных портов Отладчика
 * @property breakpoints менеджер точек останова (BreakpointController)
 * @property BRK_SPACE область перехвата обращений дебаггера
 * @property lock мьютекс для захвата управления над процессом выполения
 * @property stopped состояние остановки (Condition)
 * @property isRunning флаг активности выполнения
 * {RU}
 **/
open class Debugger(
    parent: Module,
    name: String,
    val dbgAreaSize: ULong = BUS32
): Module(parent, name), IDebugger {

    companion object {
        @Transient val log = logger(FINE)
    }

    inner class Ports : ModulePorts(this) {
        val breakpoint = Port("breakpoint")
        val reader = Port("reader")
        val trace = Port("trace")
    }

    final override val ports = Ports()

    val breakpoints = BreakpointController()

    private inline fun checkHit(ea: ULong, size: Int, access: Breakpoint.Access, block: (bpt: Breakpoint) -> Unit) {
        if (isRunning) {
            val bpt = breakpoints.lookup(ea, size, access) ?: return
            if (access != EXEC || ea >= bpt.range.first) {
                bpt.onBreak?.invoke(ea)
                block(bpt)  // may raise exception do not rearrange!
            }
        }
    }

    private val BRK_SPACE = object : Area(ports.breakpoint, 0u, dbgAreaSize - 1u, "BRK_SPACE", ACCESS.R_W, true) {
        override fun fetch(ea: ULong, ss: Int, size: Int): ULong = throw IllegalStateException("not implemented")
        override fun read(ea: ULong, ss: Int, size: Int): ULong = throw IllegalStateException("not implemented")
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = throw IllegalStateException("not implemented")

        // For fetch we should stop core before instruction execution but for read and write
        // instruction must be executed and then core stops. Reason of this is to make possible
        // pass breakpoint IDA Pro disassembler.
        override fun beforeFetch(from: Port, ea: ULong, size: Int): Boolean {
            checkHit(ea, size, EXEC) { throw BreakpointException(it) }
            return false
        }

        override fun beforeRead(from: Port, ea: ULong, size: Int): Boolean {
            checkHit(ea, size, READ) { bpt ->
                log.fine { bpt }
                isRunning = false
            }
            return false
        }

        override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong): Boolean {
            checkHit(ea, size, WRITE) { bpt ->
                log.fine { bpt }
                isRunning = false
            }
            return false
        }
    }

    private val lock = ReentrantLock()
    private val stopped = lock.newCondition()
    private fun signalStop() = lock.withLock { stopped.signal() }

    /**
     * {RU}Ожидание перехода в останов{RU}
     */
    private fun waitUntilStop() = synchronized(lock) {
        while (isRunning) stopped.await()
    }

    // GDB Debugger implementation
    // Реализация GDB-отладчика

    override var isRunning = false

    /**
     * {RU}Прерывание работы и ожидание перехода в останов{RU}
     */
    final override fun halt() {
        isRunning = false
        waitUntilStop()
    }

    override fun ident() = "debugger"

    /**
     * {RU}
     * Прочитать значения всех регистров
     *
     * @return список значений регистров
     * {RU}
     */
    override fun registers() = List(core.cpu.count()) { regRead(it) }  // method regRead may be overridden


    override fun sizes() = List(core.cpu.count()) { regSize(it) }  // method regSize may be overridden

    /**
     * {RU}
     * Чтение значения регистра
     *
     * @param index индекс регистра в банке регистров общего назначения
     * @return значение регистра
     * {RU}
     */
    override fun regRead(index: Int) = core.cpu.reg(index).bigint

    /**
     * {RU}
     * Запись значения регистра
     *
     * @param index индекс регистра в банке регистров общего назначения
     * @param value значение для записи в регистр
     * {RU}
     */
    override fun regWrite(index: Int, value: BigInteger) = core.cpu.reg(index, value.ulong)

    override fun regSize(index: Int) = Datatype.DWORD

    /**
     * {RU}
     * Причина останова
     *
     * @return код причины останова (GDB_SIGNAL.SIGTRAP)
     * {RU}
     */
    final override fun exception() = core.cpu.exception

    private inline fun stopAndSignalIf(predicate: () -> Boolean): Boolean {
        val result = predicate()
        if (result) {
            isRunning = false
            signalStop()
        }
        return result
    }

    private open inner class Cycler {
        protected var steps: Long = 0
        protected var startTime: Long = currentTimeMillis
        protected var status: Status = NOT_EXECUTED

        open fun cont(): Status {
            while (isRunning) {
                status = core.step()
                steps += 1
                stopAndSignalIf { !status.resume }
            }
            return status
        }

        open fun step() = core.step().resume

        fun epilog() {
            val deltaTimeMilliSec = (currentTimeMillis - startTime).milliseconds
            if (deltaTimeMilliSec > settings.printEmulatorRateThreshold) {
                val kips = steps / deltaTimeMilliSec.inWholeMilliseconds
                log.fine { "Emulation executed %,d steps for %s [%,d KIPS]".format(steps, deltaTimeMilliSec, kips) }
            }
        }
    }

    private inner class TraceableCycler : Cycler() {
        private fun preExecute(value: Int = 0) = ports.trace.preExecute(value)

        private fun postExecute(value: Int = 0) = ports.trace.postExecute(value)

        private fun onStart() = ports.trace.start(0)

        private fun onStop() = ports.trace.stop(0)

        override fun cont(): Status {
            onStart()
            while (isRunning) {
                status = core.enter()
                if (stopAndSignalIf { !status.resume }) break
                if (status != CORE_EXECUTED) continue

                status = core.decode()
                if (stopAndSignalIf { !status.resume }) break
                if (status != CORE_EXECUTED) continue

                when (preExecute()) {
                    TRACER_STATUS_SUCCESS -> Unit  // Nothing to do
                    TRACER_STATUS_STOP -> {
                        stopAndSignalIf { true }
                        // pipeline breaks here
                        // it should carefully checked for with restore/reset methods
                        // required for VEOS to stop when Application exited
                        break
                    }
                    TRACER_STATUS_SKIP -> continue
                }

                status = core.execute()

                steps += 1

                stopAndSignalIf { !status.resume || postExecute(status.ordinal) == TRACER_STATUS_STOP }

                core.epilog()
            }
            onStop()

            return status
        }

        override fun step(): Boolean {
            if (!core.enter().resume)
                return false

            status = core.decode()
            if (!status.resume)
                return false
            if (status != CORE_EXECUTED) {
                core.enter()
                return true
            }

            return when (val code = preExecute()) {
                TRACER_STATUS_SUCCESS -> {
                    val status = core.execute()

                    val response = postExecute(status.ordinal)

                    core.epilog()

                    if (response == TRACER_STATUS_STOP) false else status.resume
                }
                TRACER_STATUS_STOP -> false
                TRACER_STATUS_SKIP -> true

                else -> error("Tracer return wrong status code=$code")
            }
        }
    }

    private fun cycler() = if (ports.trace.isTracerOk()) TraceableCycler() else Cycler()

    /**
     * {RU}
     * Выполнение программного кода
     *
     * Именно этот метод вызывается при нажатии кнопки F9 в IDA Pro
     * {RU}
     */
    final override fun cont(): Status {
        val cycler = cycler()
        try {
            isRunning = true
            return cycler.cont()
        } finally {
            isRunning = false
            cycler.epilog()
        }
    }

    /**
     * {RU}
     * Выполнение одного шага программного кода (Step)
     *
     * Именно этот метод вызывается при нажатии кнопки F7 в IDA Pro
     *
     * ВНИМАНИЕ: данные метод немного отличается от [cont] в поведении - в случае [TRACER_STATUS_SKIP] будет
     *   возвращено значение true и вызывающий данный метод код не может никак отличить этот статус
     *   от [TRACER_STATUS_SUCCESS]. Поэтому, если в вызывающем коде есть счетчик выполненных инструкций, то
     *   он прибавит еще одну инструкции.
     *
     * @return успешность выполнения шага (true/false)
     * {RU}
     */
    final override fun step() = cycler().step()

    /**
     * {RU}
     * Чтение памяти
     *
     * @param address адрес блока памяти
     * @param size количество байт для вычитывания
     *
     * @return массив байт из памяти (ByteArray)
     * {RU}
     */
    override fun dbgLoad(address: ULong, size: Int) = ports.reader.load(address, size)

    /**
     * {RU}
     * Запись в память массива байт
     *
     * @param address адрес блока памяти
     * @param data массив байт для записи в память
     * {RU}
     */
    override fun dbgStore(address: ULong, data: ByteArray) = ports.reader.store(address, data)

    /**
     * {RU}
     * Установка точки останова (Breakpoint)
     *
     * @param access тип точки останова
     * @param range интервал адресов установки точки останова
     * @param comment необязательный комментарий
     *
     * @return результат установки точки останова (true/false)
     * {RU}
     */
    final override fun bptSet(access: Breakpoint.Access, range: ULongRange, comment: String?): Boolean {
        log.finer { "Setup breakpoint at address=0x${range.hex8}" }
        return breakpoints.add(range, access, comment)
    }

    /**
     * {RU}
     * Удаление точки останова (Breakpoint)
     *
     * @param address адрес установки точки останова
     *
     * @return результат установки точки останова (true/false)
     * {RU}
     */
    final override fun bptClr(address: ULong): Boolean {
        log.finer { "Clear breakpoint at address=%08X".format(address.long) }
        return breakpoints.remove(address)
    }
}