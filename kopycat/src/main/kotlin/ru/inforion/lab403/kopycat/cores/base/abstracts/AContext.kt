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

import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype


 
abstract class AContext<T: AGenericCore>(val abi: ABI<T>): ABIBase(abi.bits, abi.bigEndian, abi.types) {

    private val registers = Array(abi.registerCount) { 0L }

    override var stackPointerValue: Long
        get() = readRegister(abi.sp.reg)
        set(value) = writeRegister(abi.sp.reg, value)
    override val returnValue: Long
        get() = readRegister(abi.rv.reg)
    override var programCounterValue: Long
        get() = readRegister(abi.pc.reg)
        set(value) = writeRegister(abi.pc.reg, value)
    override var returnAddressValue: Long
        get() = readRegister(abi.ra.reg)
        set(value) = writeRegister(abi.ra.reg, value)

    override fun setReturnValue(value: Long, type: Datatype, instance: ABIBase) =
            abi.setReturnValue(value, type, instance)

    override val gprDatatype get() = abi.gprDatatype

    override val sizetDatatype get() = abi.sizetDatatype

    override val regArguments get() = abi.regArguments
    override val minimumStackAlignment get() = abi.minimumStackAlignment

    override fun readRegister(index: Int) = registers[index]

    override fun writeRegister(index: Int, value: Long) { registers[index] = value }

    override fun readStack(offset: Long, type: Datatype) =
            abi.readStack(offset, type)

    override fun writeStack(offset: Long, type: Datatype, value: Long) =
            abi.writeStack(offset, type, value)


    override fun getArg(index: Int, type: Datatype, alignment: Int, instance: ABIBase) =
            abi.getArg(index, type, alignment, instance)

    override fun getArgs(args: Iterable<Datatype>, instance: ABIBase) = abi.getArgs(args, instance)

    override fun setArg(index: Int, type: Datatype, value: Long, push: Boolean, alignment: Int, instance: ABIBase) =
            abi.setArg(index, type, value, push, alignment, instance)

    override fun setArgs(args: Iterable<Pair<Datatype, Long>>, push: Boolean, instance: ABIBase) =
            abi.setArgs(args, push, instance)

    override fun setArgs(args: Array<Long>, push: Boolean, instance: ABIBase) = abi.setArgs(args, push, instance)


    open fun save() = registers.indices.forEach { i -> registers[i] = abi.readRegister(i) }

    open fun load() = registers.forEachIndexed { i, it -> abi.writeRegister(i, it) }


    open fun store(address: Long) = registers.forEachIndexed { i, it ->
        abi.writeMemory(address + i * abi.gprDatatype.bytes, it, abi.gprDatatype)
    }

    open fun restore(address: Long) = registers.indices.forEach { i ->
        abi.readMemory(address + i * abi.gprDatatype.bytes, abi.gprDatatype)
    }

}