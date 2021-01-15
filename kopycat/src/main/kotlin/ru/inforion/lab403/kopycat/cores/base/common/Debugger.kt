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
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.common.Breakpoint.Access.*
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.enums.Status.NOT_EXECUTED
import ru.inforion.lab403.kopycat.cores.base.exceptions.BreakpointException
import ru.inforion.lab403.kopycat.cores.base.extensions.*
import ru.inforion.lab403.kopycat.gdbstub.GDB_BPT
import ru.inforion.lab403.kopycat.interfaces.IDebugger
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.modules.BUS32
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

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
        val dbgAreaSize: Long = BUS32
): Module(parent, name), IDebugger {

    inner class Ports : ModulePorts(this) {
        val breakpoint = Slave("breakpoint", dbgAreaSize)
        val reader = Master("reader", dbgAreaSize)
        val trace = Master("trace", TRACER_BUS_SIZE)
    }

    final override val ports = Ports()

    private val defaultByte = 0xCCL

    val breakpoints = BreakpointController()

    private inline fun checkHit(ea: Long, access: Breakpoint.Access, block: (bpt: Breakpoint) -> Unit): Boolean {
        if (isRunning) {
            val bpt = breakpoints.lookup(ea) ?: return false
            if (bpt.check(access)) {
                bpt.onBreak?.invoke(ea)
                block(bpt)  // may raise exception do not rearrange!
            }
        }
        return false
    }

    private val BRK_SPACE = object : Area(ports.breakpoint, 0, dbgAreaSize - 1, "BRK_SPACE", ACCESS.R_W, true) {
        override fun fetch(ea: Long, ss: Int, size: Int): Long = throw IllegalStateException("not implemented")
        override fun read(ea: Long, ss: Int, size: Int): Long = throw IllegalStateException("not implemented")
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = throw IllegalStateException("not implemented")

        // For fetch we should stop core before instruction execution but for read and write
        // instruction must be executed and then core stops. Reason of this is to make possible
        // pass breakpoint IDA Pro disassembler.
        override fun beforeFetch(from: MasterPort, ea: Long) = checkHit(ea, EXEC) { throw BreakpointException(it) }
        override fun beforeRead(from: MasterPort, ea: Long) = checkHit(ea, READ) { isRunning = false }
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long) = checkHit(ea, WRITE) { isRunning = false }
    }

    private val lock = ReentrantLock()
    private val stopped = lock.newCondition()
    private fun signalStop() = lock.withLock { stopped.signal() }

    /**
     * {RU}Ожидание перехода в останов{RU}
     */
    fun waitUntilStop() = synchronized(lock) {
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

    /**
     * {RU}
     * Чтение значения регистра
     *
     * @param index индекс регистра в банке регистров общего назначения
     * @return значение регистра
     * {RU}
     */
    override fun regRead(index: Int) = core.cpu.reg(index)

    /**
     * {RU}
     * Запись значения регистра
     *
     * @param index индекс регистра в банке регистров общего назначения
     * @param value значение для записи в регистр
     * {RU}
     */
    override fun regWrite(index: Int, value: Long) = core.cpu.reg(index, value)

    /**
     * {RU}
     * Причина останова
     *
     * @return код причины останова (GDB_SIGNAL.SIGTRAP)
     * {RU}
     */
    final override fun exception() = core.cpu.exception

    /**
     * {RU}
     * Действие перед выполнением инструкции процессора
     *
     * @param value значение для проверки
     *
     * @return успешность выполнения (true/false)
     * {RU}
     */
    private fun preExecute(value: Int = 0) = ports.trace.preExecute(value)

    /**
     * {RU}
     * Действие после выполнения инструкции процессора
     *
     * @param value значение для проверки
     *
     * @return успешность выполнения (true/false)
     * {RU}
     */
    private fun postExecute(value: Int = 0) = ports.trace.postExecute(value)

    /**
     * {RU}Действия выполняемые отладчиком перед командой run (перед запуском процесса эмуляции){RU}
     */
    private fun start() = ports.trace.start(0)

    /**
     * {RU}Действия выполняемые отладчиком после команды run (после остановки процесса эмуляции){RU}
     */
    private fun stop() = ports.trace.stop(0)

    private var steps = 0
    private var startTime: Long = 0

    private inline fun stopAndSignalIf(predicate: () -> Boolean): Boolean {
        val result = predicate()
        if (result) {
            isRunning = false
            signalStop()
        }
        return result
    }

    /**
     * {RU}
     * Выполнение программного кода
     *
     * Именно этот метод вызывается при нажатии кнопки F9 в IDA Pro
     * {RU}
     */
    final override fun cont(): Status {
        var status: Status = NOT_EXECUTED
        isRunning = true
        steps = 0

        startTime = System.currentTimeMillis()

        if (ports.trace.isTracerOk()) {
            log.finer { "Running with tracer..." }
            // if tracer has errors show it in catch block
            try {
                start()
                loop@ while (isRunning) {
                    status = core.enter()
                    if (stopAndSignalIf { !status.resume }) break

                    status = core.decode()
                    if (stopAndSignalIf { !status.resume }) break

                    when (preExecute()) {
                        TRACER_STATUS_SUCCESS -> Unit  // Nothing to do
                        TRACER_STATUS_STOP -> {
                            stopAndSignalIf { true }
                            // pipeline breaks here
                            // it should carefully checked for with restore/reset methods
                            // required for VEOS to stop when Application exited
                            break
                        }
                        TRACER_STATUS_SKIP -> continue@loop
                    }

                    status = core.execute()

                    steps += 1

                    stopAndSignalIf { !status.resume || postExecute(status.ordinal) == TRACER_STATUS_STOP }
                }
                stop()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        } else while (isRunning) {
            status = core.step()
            steps += 1
            stopAndSignalIf { !status.resume }
        }

        val deltaTimeMilliSec = System.currentTimeMillis() - startTime
        if (deltaTimeMilliSec > 500) {
            val kips = steps / deltaTimeMilliSec
            log.info { "Emulation running for %,d ms [%,d KIPS]".format(deltaTimeMilliSec, kips) }
        }

        return status
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
     *   он прибавит еще одну инструкции. При этом метод [cont] пропустить инкремент переменной внутреннего
     *   количества шагов [steps].
     *
     * @return успешность выполнения шага (true/false)
     * {RU}
     */
    final override fun step(): Boolean {
        if (ports.trace.isTracerOk()) {
            if (!core.enter().resume)
                return false

            if (!core.decode().resume)
                return false

            return when (val code = preExecute()) {
                TRACER_STATUS_SUCCESS -> {
                    val status = core.execute()

                    if (postExecute(status.ordinal) == TRACER_STATUS_STOP)
                        return false

                    status.resume
                }
                TRACER_STATUS_STOP -> false
                TRACER_STATUS_SKIP -> true

                else -> error("Tracer return wrong status code=$code")
            }
        } else {
            return core.step().resume
        }
    }

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
    final override fun dbgLoad(address: Long, size: Int) = ports.reader.load(address, size) { defaultByte }

    /**
     * {RU}
     * Запись в память массива байт
     *
     * @param address адрес блока памяти
     * @param data массив байт для записи в память
     * {RU}
     */
    final override fun dbgStore(address: Long, data: ByteArray) = ports.reader.store(address, data)

    /**
     * {RU}
     * Установка точки останова (Breakpoint)
     *
     * @param bpType тип точки останова (один из вариантов GDB_BPT.: HARDWARE/READ/WRITE/ACCESS/SOFTWARE)
     * @param address адрес установки точки останова
     * @param comment необязательный комментарий
     *
     * @return результат установки точки останова (true/false)
     * {RU}
     */
    override fun bptSet(bpType: GDB_BPT, address: Long, comment: String?): Boolean {
        log.fine { "Setup breakpoint at address=0x%08X".format(address) }
        return when (bpType) {
            GDB_BPT.HARDWARE -> breakpoints.add(address, EXEC)
            GDB_BPT.READ -> breakpoints.add(address, READ)
            GDB_BPT.WRITE -> breakpoints.add(address, WRITE)
            GDB_BPT.ACCESS -> breakpoints.add(address, RW)
            GDB_BPT.SOFTWARE -> breakpoints.add(address, EXEC)
        }
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
    override fun bptClr(address: Long): Boolean {
        log.fine { "Clear breakpoint at address=%08X".format(address) }
        return breakpoints.remove(address)
    }

    /**
     * {RU}
     * Настройка парсера аргументов командной строки.
     * Для использования команд в консоли эмулятора.
     *
     * @param parent родительский парсер, к которому будут добавлены новые аргументы
     * @param useParent необходимость использования родительского парсера
     *
     * @return парсер аргументов
     * {RU}
     */
    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent)?.apply {
                subparser("run", help = "run target")
                subparser("halt", help = "halt target")
                subparser("reset", help = "reset target")
                subparser("step", help = "step target")
                subparser("status", help = "read target status")
                subparser("bp", help = "set a breakpoint").apply {
                    variable<String>("-a", "--address", required = false, help = "Address to set breakpoint")
                    flag("-c", "--clear", help = "If set breakpoint will be clear")
                }
            }

    /**
     * {RU}
     * Обработка аргументов командной строки.
     * Для использования команд в консоли эмулятора.
     *
     * @param context контекст интерактивной консоли
     *
     * @return результат обработки команд (true/false)
     * {RU}
     */
    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        context.result = when (context.command()) {
            "run" -> {
                if (!isRunning) {
                    thread { cont() }
                    "Target run"
                } else {
                    "Target already running..."
                }
            }
            "halt" -> {
                halt()
                "Target halted at %08X".format(core.cpu.pc)
            }
            "reset" -> {
                halt()
                core.reset()
                "Target reset"
            }
            "step" -> {
                step()
                "Step to instruction ${core.cpu.pc.hex8}"
            }
            "status" -> {
                "${core.name} is running $isRunning at %,d".format(core.clock.time())
            }
            "bp" -> {
                val address = context.getString("address")
                val isClear = context.getBoolean("clear")
                val bpAddr: Long = address?.hexAsULong ?: core.cpu.pc
                if (!isClear) {
                    breakpoints.add(bpAddr, EXEC)
                    "Breakpoint set to ${bpAddr.hex8}"
                } else {
                    breakpoints.remove(bpAddr)
                    "Breakpoint cleared from ${bpAddr.hex8}"
                }
            }
            else -> return false
        }

        context.pop()

        return true
    }

    /**
     * {RU}
     * Имя команды для текущего класса в интерактивной консоли эмулятора.
     * Для использования команд в консоли эмулятора.
     *
     * @return строковое имя команды
     * {RU}
     */
    override fun command(): String = name
}