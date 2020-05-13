package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException

/**
 * {RU}
 * Абстрактный класс CPU.
 *
 *
 * @param name произвольное имя объекта Процессора
 * @property core ядро, к которому привязан Процессор
 * @property pc счётчик команд
 * @property insn последняя успешно выполненная инструкция
 * @property exception последнее исключение
 * {RU}
 */
abstract class ACPU<
        U: ACPU<U, R, I, E>,  // Recursive generic resolution
        R: ACore<R, U, *>,
        I: AInstruction<R>,
        E: Enum<E>>(core: R, name: String) : Module(core, name) {

    inner class Ports : ModulePorts(this) {
        val mem = Master("mem")
    }

    override val ports = Ports()

    /**
     * {RU}Получить значение регистра общего назначения по его индексу{RU}
     */
    abstract fun reg(index: Int): Long

    /**
     * {RU}Задать значение регистра общего назначения по его индексу{RU}
     */
    abstract fun reg(index: Int, value: Long)

    /**
     * {RU}Получить общее количество регистров общего назначения{RU}
     */
    abstract fun count(): Int

    /**
     * {RU}Счётчик команд{RU}
     *
     * {EN}
     * Program counter in form that most likely suits for current processor core.
     * Logging system in different part of Kopycat system uses this very property.
     * So in x86 it has a messy way - read will return physical address, but write is offset within cs segment.
     * In MIPS pc field fully correspond internal core program counter (virtual address)
     *
     * NOTE: Try not to use it for hardware description (especially when CPU has it own program counter register)
     * {EN}
     */
    abstract var pc: Long

    abstract fun decode()

    abstract fun execute(): Int

    /**
     * {RU}Последняя успешно выполненная инструкция{RU}
     *
     * {EN}Last successful executed instruction{EN}
     */
    lateinit var insn: I

    var callOccurred: Boolean = false

    var exception: GeneralException? = null

    /**
     * {EN}
     * If true then processor in halted state, don't execute instructions and wait for interrupt.
     *
     * SHOULD BE USED in instruction like Hlt, Wait For Interrupt, CP15WFI
     * {EN}
     */
    var halted = false

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        exception = null
    }
}