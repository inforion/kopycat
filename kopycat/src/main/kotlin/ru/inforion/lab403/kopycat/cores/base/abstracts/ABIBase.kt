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

import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import java.io.Serializable


 
abstract class ABIBase(val bits: Int, val bigEndian: Boolean, val types: Types = Types.default): IAutoSerializable {

    class Types constructor(
            val char: Datatype,
            val short: Datatype,
            val int: Datatype,
            val long: Datatype,
            val longLong: Datatype,
            val pointer: Datatype
    ): IAutoSerializable {
        companion object {
            val default = Types(BYTE, WORD, DWORD, DWORD, QWORD, DWORD)
        }
    }

    private inline val ArgType.asCType get() = when (this) {
        ArgType.Char -> types.char
        ArgType.Short -> types.short
        ArgType.Int -> types.int
        ArgType.Long -> types.long
        ArgType.LongLong -> types.longLong
        ArgType.Pointer -> types.pointer
    }

    abstract var stackPointerValue: Long
    abstract val returnValue: Long
    abstract var programCounterValue: Long
    abstract var returnAddressValue: Long

    abstract fun setReturnValue(value: Long, type: Datatype = gprDatatype, instance: ABIBase = this)
    open fun setReturnValue(value: Long, type: ArgType, instance: ABIBase = this) =
            setReturnValue(value, type.asCType, instance)

    abstract val regArguments: List<Int>
    abstract val gprDatatype: Datatype
    abstract val sizetDatatype: Datatype
    abstract val minimumStackAlignment: Int
    open val stackArgsOffset: Long = 0

    open fun ret() {
        programCounterValue = returnAddressValue
    }

    abstract fun readRegister(index: Int): Long
    abstract fun writeRegister(index: Int, value: Long)

    abstract fun readStack(offset: Long, type: Datatype): Long
    abstract fun writeStack(offset: Long, type: Datatype, value: Long)


    open fun pop(type: Datatype = types.int, alignment: Int = type.bytes) =
            readStack(0, type).also { stackPointerValue += alignment }

    open fun push(value: Long, type: Datatype = types.int, alignment: Int = type.bytes) {
        stackPointerValue -= alignment
        writeStack(0, type, value)
    }

    open fun getAlignment(args: Iterable<Datatype>) = (args.map { it.bytes } + minimumStackAlignment).maxOf { it }

    open fun getArg(index: Int, type: Datatype, alignment: Int = type.bytes, instance: ABIBase = this) =
            if (index < regArguments.size) {
                if (type.bits > bits)
                    throw NotImplementedError("Override this function for ${type.bits}-bit arguments")
                instance.readRegister(regArguments[index]) like type
            }
            else
                instance.readStack(stackArgsOffset + (index - regArguments.size) * alignment, type)

    open fun getArgs(args: Iterable<Datatype>, instance: ABIBase = this): Array<Long> {
        val alignment = getAlignment(args)
        return args.mapIndexed { i, it -> getArg(i, it, alignment, instance) }.toTypedArray()
    }

    open fun getCArgs(args: Array<ArgType>) = getArgs(args.map { it.asCType})

    open fun setArg(index: Int, type: Datatype, value: Long, push: Boolean, alignment: Int = type.bytes, instance: ABIBase = this) {
        when {
            index < regArguments.size -> {
                if (type.bits > bits)
                    throw NotImplementedError("Override this function for ${type.bits}-bit arguments")
                instance.writeRegister(regArguments[index], value like type)
            }
            push -> instance.push(value, type)
            else -> instance.writeStack(stackArgsOffset + (index - regArguments.size) * alignment, type, value)
        }
    }

    open fun setArgs(args: Iterable<Pair<Datatype, Long>>, push: Boolean, instance: ABIBase = this) {
        val alignment = getAlignment(args.map { it.first })
        args.forEachIndexed { i, it -> setArg(i, it.first, it.second, push, alignment, instance) }
    }

    open fun setArgs(args: Array<Long>, push: Boolean, instance: ABIBase = this) =
            setArgs(args.map { types.int to it }, push, instance)

    open fun setCArgs(args: Iterable<Pair<ArgType, Long>>, push: Boolean) =
            setArgs(args.map { (k, v) -> k.asCType to v }, push)

}