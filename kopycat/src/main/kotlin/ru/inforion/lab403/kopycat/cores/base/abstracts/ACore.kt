/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logStackTrace
import ru.inforion.lab403.kopycat.annotations.ExperimentalWarning
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.debug.CoreInfo
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.enums.Status.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.*
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.serializer.loadEnum
import ru.inforion.lab403.kopycat.serializer.storeValues

/**
 * {RU}
 * Абстрактный класс ядра эмулятора.
 *
 * @param parent родительский модуль, в который встраивается Ядро
 * @param name произвольное имя объекта Ядра
 * @property frequency базовая частота ядра в Гц (примечание: 10.MHz = 10_000_000.Hz)
 * @property clock системные "часы" - служат для синхронизации всех устройств
 * @property cpu центральный процессор
 * @property cop сопроцессор
 * @property mmu блок управления памятью (memory management unit, MMU)
 * @property fpu блок работы с вещественными числами
 *
 * @property frequency частота ядра в Hz (note: 10.MHz = 10_000_000.Hz)
 * @property ipc значение Instruction Per Cycles (характеристика ядра микропроцессора/микроконтроллера)
 * see https://en.wikipedia.org/wiki/Instructions_per_second and
 *
 * Processor        MIPS        Frequency   IPC     Year
 * Intel i486DX2	25.6 MIPS   66 MHz      0.388   1992
 * PowerPC 750	    525 MIPS    233 MHz     2.3	    1997
 * ARM Cortex-M0	45 MIPS     50 MHz      0.9     2009
 * ARM Cortex-M3	125 MIPS    100 MHz     1.25	2004
 * ARM Cortex-A8    2,000 MIPS  1.0 GHz     2.0     2005
 * MIPS32 24K	    604 MIPS    400 MHz     1.51	2006
 * MIPS 64 20Kc     1,1370 MIPS 600 MHz     2.3     2007
 * {RU}
 **/
@Suppress("INAPPLICABLE_JVM_NAME")
abstract class ACore<
        R: ACore<R, U, P>,  // Recursive generic resolution
        U: ACPU<U, R, *, *>,
        P: ACOP<P, R>>(
        parent: Module,
        name: String,
        val frequency: Long,
        val ipc: Double
) : Module(parent, name), IFetchReadWrite {

    enum class Stage { INTERRUPTS_PROCESSED, INSTRUCTION_DECODED, INSTRUCTION_EXECUTED, CLOCK_UPDATED }

    /**
     * {EN}
     * System clock timer (consider it as PLL/XTAL/Quartz)
     * {EN}
     */
    val clock = SystemClock(this, frequency,"SystemClock")

    /**
     * {RU}
     * Выполняет инструкции и является контейнером для регистров.
     * {RU}
     *
     * {EN}
     * Core CPU (Central Processing Unit)
     * Basic instructor processor and register container
     * {EN}
     */
    abstract val cpu: U

    /**
     * {RU}
     * Сопроцессор ядра (Coprocessor, COP)
     * Обрабатывает прерывания и исключения.
     * {RU}
     *
     * {EN}
     * Core COP (Coprocessor)
     * Basic interrupt and exception processor
     * {EN}
     */
    abstract val cop: P

    /**
     * {RU}
     * Опциональный MMU (Memory Management Unit).
     * Выполняет трансляцию виртуальных (логических) адресов в физические.
     * {RU}
     *
     * {EN}
     * Optional MMU (Memory Management Unit)
     * Virtual to physical translation unit
     * {EN}
     */
    abstract val mmu: AddressTranslator?

    /**
     * {RU}Опциональный FPU (Floating Point Unit){RU}
     *
     * {EN}Optional FPU (Floating Point Unit){EN}
     */
    abstract val fpu: AFPU<*>?

    /**
     * {RU}
     * Формирование двоичного интерфейса приложения (ABI).
     * Требуется для реализации операционных систем.
     * @return ABI
     * @throw NotImplementedError
     * {RU}
     *
     * {EN}Optional (throw NotImplementedError if absent) for operating system supports{EN}
     */
    open fun abi(): ABI<R> = throw NotImplementedError("Operating system not supported")

    val info = CoreInfo(this)

    var stage = Stage.INTERRUPTS_PROCESSED
        private set

    private fun unbearableExceptionAndTrace(): Status {
        info.dump()
        return UNBEARABLE_EXCEPTION
    }

    /**
     * {EN}Make pretty text view with maximum information about occurred exception{EN}
     */
    private fun Throwable?.explain(comment: String) =
            "${this@ACore} $this pc=0x${cpu.pc.hex} time=${clock.time(Time.ns)} ns -> $comment"

    private inline fun processBlock(block: () -> Unit): Status {
        cpu.exception = null
        block()
        return CORE_EXECUTED
    }

    /**
     * {EN}
     * Function called when user trying to execute something when processor has unhandled exceptions
     * We can't continue execution until work out exception mess (e.g. just reset it by moving CPU PC)
     *
     * @return [Status] - returns [UNBEARABLE_EXCEPTION] always
     * {EN}
     */
    private fun processUnhandledException(): Status {
        val exception = cpu.exception
            ?: throw IllegalStateException("CPU was deserialized from faulty snapshot -> this should not happen")
        log.severe { exception.explain("CPU in faulty state! If debugger support you may reset exception by setting PC") }
        return UNBEARABLE_EXCEPTION
    }

    /**
     * {EN}
     * Function processing emulator exception
     * if [BreakpointException] - stops and reset
     * if [HardwareException] try to handle it with coprocessor
     * otherwise suggest user to solve it
     *
     * @return [Status] - may returns [BREAKPOINT_EXCEPTION], [INTERNAL_EXCEPTION] or [UNBEARABLE_EXCEPTION]
     * {EN}
     */
    private fun processGeneralException(exception: GeneralException): Status {
        if (exception !is BreakpointException)
            info.trace(true)

        cpu.exception = exception
        cpu.fault = true

        when (exception) {
            is BreakpointException -> {
                log.config { exception.explain("stop at breakpoint hit") }
                cpu.fault = false
                return BREAKPOINT_EXCEPTION
            }

            is HardwareException -> {
                // Если исключение обработано, возвращается null
                // If exception is handled then null is returned
                val exc = cop.handleException(exception)

                if (exc == null) {
                    log.finest { exc.explain("handled by coprocessor") }
                    cpu.fault = false
                    return INTERNAL_EXCEPTION
                }

                log.warning { exception.explain("coprocessor can't handle exception ... pass to debugger") }
            }

            is DecoderException -> log.warning { exception.explain("instruction decoding error: ${exception.data.hex}") }

            is UnsupportedInstructionException -> log.warning { exception.explain("unsupported instruction found...") }

            else -> log.warning { exception.explain("unknown exception passed to debugger...") }
        }

        return unbearableExceptionAndTrace()
    }

    /**
     * {EN}
     * Function processing common Java exception not a [GeneralException]
     *
     * @return [Status] - returns [UNBEARABLE_EXCEPTION] always
     * {EN}
     */
    private fun processJavaException(throwable: Throwable): Status {
        val exception = GeneralException(throwable.toString())
        log.severe { "${throwable.explain("Core halted!")}\n${throwable.stackTraceToString()}" }
        cpu.exception = exception
        cpu.fault = true
        return unbearableExceptionAndTrace()
    }

    /**
     * {EN}
     * Do one or multiple core conveyor steps  under monitoring
     * exception and if any happens try to handle it.
     *
     * @return [Status] - return monitor status
     * {EN}
     */
    private inline fun monitor(block: () -> Unit) = if (cpu.fault) {
        processUnhandledException()
    } else try {
        processBlock(block)
    } catch (exception: GeneralException) {
        processGeneralException(exception)
    } catch (exception: Throwable) {
        processJavaException(exception)
    }

    fun doProcessInterrupts() {
        runCatching {
            cop.processInterrupts()
        }.onFailure {
            // This code prevents cyclic hardware exception handling
            // If COP for some architecture throws some kind of hardware exception,
            // you should rewrite this code
            it.logStackTrace(log)
            error("COP shouldn't throw any exception during processInterrupts")
        }
        stage = Stage.INTERRUPTS_PROCESSED
    }

    fun doDecodeInstruction() {
        cpu.decode()
        stage = Stage.INSTRUCTION_DECODED
    }

    fun doExecuteInstruction() {
        info.saveProgramCounter()

        val cycles = cpu.execute()  // return microcode operations count

        stage = Stage.INSTRUCTION_EXECUTED

        clock.update(cycles)

        stage = Stage.CLOCK_UPDATED

        info.trace(false)
    }

    /**
     * {EN}
     * Just enter into execution step (process possible interrupts)
     * Beware this function should be first in core execution step
     *
     * @return [Status] - return enter execution status
     * {EN}
     */
    internal fun enter() = monitor { doProcessInterrupts() }

    /**
     * {EN}
     * Just decode instruction
     * Beware this function should be called after core [enter] into execution step
     *
     * @return [Status] - return decode execution status
     * {EN}
     */
    internal fun decode() = monitor { doDecodeInstruction() }

    /**
     * {EN}
     * WARNING: Be careful! This function no longer completely execute core conveyor! Use [step] instead!
     *
     * Just execute already decoded instruction
     * Beware this function must be called after core [decode] instruction
     *
     * @return [Status] - return execution status
     * {EN}
     */
    internal fun execute() = monitor { doExecuteInstruction() }

    /**
     * {EN}
     * Should be executed after [Debugger] call [ATracer.postExecute] method
     * {EN}
     */
    internal fun epilog() = info.epilog()

    /**
     * {RU}
     * Обработка прерываний, декодирование и выполнение инструкции
     *
     * Выполнить одну инструкцию, обработать события сопроцессора и периферийных устройств.
     * Обработать исключения, если это возможно.
     *
     * @return [Status] - возвращает статус выполнения
     * {RU}
     *
     * {EN}
     * Executes one instruction and process each coprocessor and peripheral device.
     * Tries to handle exception if possible.
     *
     * @return [Status] - step execution status
     * {EN}
     */
    fun step() = monitor {
        doProcessInterrupts()
        doDecodeInstruction()
        doExecuteInstruction()
        epilog()
    }

    override fun reset() {
        super.reset()
        stage = Stage.INTERRUPTS_PROCESSED
        info.reset()
    }

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
        "stage" to stage,
        "clock" to clock.serialize(ctxt),
        "info" to info.serialize(ctxt)
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        val clockSnapshot = snapshot["clock"]
        val infoSnapshot = snapshot["info"]

        stage = loadEnum(snapshot, "stage")
        if (clockSnapshot != null) clock.deserialize(ctxt, clockSnapshot.cast())
        if (infoSnapshot != null) info.deserialize(ctxt, infoSnapshot.cast())
    }

    override fun stringify() = buildString {
        appendLine("${this@ACore::class.simpleName}:")
        appendLine(cpu.stringify())
        appendLine(cop.stringify())
        mmu?.let { appendLine(it.stringify()) }
        fpu?.let { append(it.stringify()) }
    }

    final override fun fetch(ea: ULong, ss: Int, size: Int): ULong = cpu.ports.mem.fetch(ea, ss, size)
    final override fun read(ea: ULong, ss: Int, size: Int): ULong = cpu.ports.mem.read(ea, ss, size)
    final override fun write(ea: ULong, ss: Int, size: Int, value: ULong): Unit = cpu.ports.mem.write(ea, ss, size, value)

    /**
     * {EN}
     * Last CPU exception. This exception may be processed by COP or may be not in either case it will not be null.
     * Exception reset before each successful CPU step.
     * {EN}
     */
    fun exception() = cpu.exception

    /**
     * {RU}Задать значение регистра общего назначения по его индексу{RU}
     */
    @JvmName("reg")
    fun reg(index: Int): ULong = cpu.reg(index)

    /**
     * {RU}Получить общее количество регистров общего назначения{RU}
     */
    @JvmName("reg")
    fun reg(index: Int, value: ULong): Unit = cpu.reg(index, value)

    /**
     * {RU}Прочитать все регистры CPU{RU}
     */
    fun registers() = cpu.registers()

    /**
     * {RU}Прочитать значения флагов CPU{RU}
     */
    @ExperimentalWarning
    @JvmName("flags")
    fun flags() = cpu.flags()

    @get:JvmName("getPc")
    var pc: ULong
        get() = cpu.pc
        set(value) {
            cpu.pc = value
        }
}