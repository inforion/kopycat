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
package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.kopycat.annotations.ExperimentalWarning
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues

/**
 * {RU}
 * Абстрактный класс CPU.
 * {RU}
 */
abstract class ACPU<
        U: ACPU<U, R, I, E>,  // Recursive generic resolution
        R: ACore<R, U, *>,
        I: AInstruction<R>,
        E: Enum<E>>(core: R, name: String, val busSize: Long = BUS32) : Module(core, name) {

    inner class Ports : ModulePorts(this) {
        val mem = Master("mem", busSize)
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
     * {RU}Прочитать все регистры CPU{RU}
     */
    fun registers() = List(count()) { reg(it) }

    /**
     * {RU}Метод возвращает текущее значение регистра флагов{RU}
     */
    @ExperimentalWarning
    open fun flags(): Long = throw NotImplementedError("The flags are not implemented")

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

    val hasInstruction get() = ::insn.isInitialized

    /**
     * {RU}Последняя успешно выполненная инструкция{RU}
     *
     * {EN}Last successful executed instruction{EN}
     */
    lateinit var insn: I

    var callOccurred = false
        internal set

    /**
     * {EN}
     * If true then processor in halted state, don't execute instructions and wait for interrupt.
     *
     * SHOULD BE USED in instruction like Hlt, Wait For Interrupt, CP15WFI
     * {EN}
     */
    var halted = false
        internal set

    /**
     * {EN}
     * Last exception of CPU. This exception may be processed by COP or may be not in either case it will not be null.
     * Exception reset before each successful CPU step.
     * {EN}
     */
    var exception: GeneralException? = null
        internal set

    /**
     * {EN}
     * If this variable is true then CPU got exception that wasn't processed by COP and CPU won't run anymore
     * {EN}
     */
    var fault = false
        internal set

    fun resetFault() {
        log.warning { "Hardware exception was cleared!" }
        exception = null
        fault = false
    }

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
            "halted" to halted,
            "faulty" to fault,
            "callOccurred" to callOccurred)

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        halted = loadValue(snapshot, "halted") { false }
        fault = loadValue(snapshot, "faulty")  { false }
        callOccurred = loadValue(snapshot, "callOccurred") { false }
        exception = null
    }
}