package ru.inforion.lab403.kopycat.cores.base.abstracts

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.Time
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.CoreInfo
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.enums.Status.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.*
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.serializer.loadEnum
import kotlin.collections.HashMap

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
 * {RU}
 **/
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
    abstract fun abi(heap: LongRange, stack: LongRange): ABI<R>

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
    private fun explain(exception: Throwable, comment: String) =
            "$this $exception pc=0x${cpu.pc.hex} time=${clock.time(Time.ns)} ns -> $comment"

    /**
     * {EN}
     * Function called when user trying to execute something when processor has unhandled exceptions
     * We can't continue execution until work out exception mess (e.g. just reset it by moving CPU PC)
     *
     * @return [Status] - returns [UNBEARABLE_EXCEPTION] always
     * {EN}
     */
    private fun processUnhandledException(): Status {
        log.severe { explain(cpu.exception!!,
                "unhandled exception left in core! If debugger support you may reset exception by setting PC") }
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
        when (exception) {
            is BreakpointException -> {
                log.config { explain(exception, "stop at breakpoint hit") }
                cpu.exception = null
                return BREAKPOINT_EXCEPTION
            }

            is HardwareException -> {
                // Если исключение обработано, возвращается null
                // If exception is handled then null is returned
                cpu.exception = cop.handleException(cpu.exception)

                if (cpu.exception == null) {
                    log.finest { explain(exception, "handled by coprocessor") }
                    return INTERNAL_EXCEPTION
                }

                log.warning { explain(exception, "coprocessor can't handle exception ... pass to debugger") }
            }

            is DecoderException -> log.warning { explain(exception, "instruction decoding error...") }

            is UnsupportedInstruction -> log.warning { explain(exception, "unsupported instruction found...") }

            else -> log.warning { explain(exception, "unknown exception passed to debugger...") }
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
        log.severe { explain(throwable, "Core halted!") }
        throwable.printStackTrace()
        cpu.exception = exception
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
    private inline fun monitor(block: () -> Unit) = if (cpu.exception != null) {
        processUnhandledException()
    } else try {
        block()
        CORE_EXECUTED
    } catch (exception: GeneralException) {
        processGeneralException(exception)
    } catch (exception: Throwable) {
        processJavaException(exception)
    }

    fun doProcessInterrupts() {
        cop.processInterrupts()
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
     * Execute one instruction and process each coprocessor and peripheral device.
     * Try to handle exception if possible.
     *
     * @return [Status] - return execution status
     * {EN}
     */
    fun step() = monitor {
        doProcessInterrupts()
        doDecodeInstruction()
        doExecuteInstruction()
    }

    override fun reset() {
        super.reset()
        stage = Stage.INTERRUPTS_PROCESSED
        info.reset()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> =
            super.serialize(ctxt) + ctxt.storeValues("stage" to stage)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        stage = loadEnum(snapshot, "stage")
    }

    @Suppress("UNCHECKED_CAST")
    override fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        val snapshotValues = snapshot["components"] as Map<String, Any>
        snapshotValues.forEach { (cName, cData) -> components[cName]?.restore(ctxt, cData as HashMap<String, Any>) }
        stage = loadEnum(snapshot, "stage")
    }

    /**
     * {RU}
     * Настройка парсера аргументов командной строки.
     * Для использования команд в консоли эмулятора.
     *
     * @param parent родительский парсер, к которому будут добавлены новые аргументы
     * @param useParent необходимость использования родительского парсера
     * @return парсер аргументов
     * {RU}
     */
    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent).apply {
                clock.configure(this)
            }

    /**
     * {RU}
     * Обработка аргументов командной строки.
     * Для использования команд в консоли эмулятора.
     *
     * @param context контекст интерактивной консоли
     * @return результат обработки команд (true/false)
     * {RU}
     */
    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        if (context.command() == clock.command()) {
            return clock.process(context)
        }

        return false
    }

    /**
     * {RU}
     * Имя команды для текущего класса в интерактивной консоли эмулятора.
     * Для использования команд в консоли эмулятора.
     *
     * @return строковое имя команды
     * {RU}
     */
    final override fun command(): String = "core"

    final override fun fetch(ea: Long, ss: Int, size: Int): Long = cpu.ports.mem.fetch(ea, ss, size)
    final override fun read(ea: Long, ss: Int, size: Int): Long = cpu.ports.mem.read(ea, ss, size)
    final override fun write(ea: Long, ss: Int, size: Int, value: Long): Unit = cpu.ports.mem.write(ea, ss, size, value)

    fun exception() = cpu.exception

    fun reg(index: Int): Long = cpu.reg(index)

    fun reg(index: Int, value: Long): Unit = cpu.reg(index, value)

    var pc: Long
        get() = cpu.pc
        set(value) {
            cpu.pc = value
        }
}