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
package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype


 
abstract class AContext<T: AGenericCore>(val abi: ABI<T>): ABIBase(abi.bits, abi.bigEndian, abi.types) {

    private val registers = Array(abi.registerCount) { 0uL }

    override var stackPointerValue: ULong
        get() = readRegister(abi.sp.reg)
        set(value) = writeRegister(abi.sp.reg, value)
    override val returnValue: ULong
        get() = readRegister(abi.rv.reg)
    override var programCounterValue: ULong
        get() = readRegister(abi.pc.reg)
        set(value) = writeRegister(abi.pc.reg, value)
    override var returnAddressValue: ULong
        get() = readRegister(abi.ra.reg)
        set(value) = writeRegister(abi.ra.reg, value)

    override fun setReturnValue(value: ULong, type: Datatype, instance: ABIBase) =
            abi.setReturnValue(value, type, instance)

    override val gprDatatype get() = abi.gprDatatype

    override val sizetDatatype get() = abi.sizetDatatype

    override val regArguments get() = abi.regArguments
    override val minimumStackAlignment get() = abi.minimumStackAlignment

    override fun readRegister(index: Int) = registers[index]

    override fun writeRegister(index: Int, value: ULong) { registers[index] = value }

    override fun readStack(offset: ULong, type: Datatype) =
            abi.readStack(offset, type)

    override fun writeStack(offset: ULong, type: Datatype, value: ULong) =
            abi.writeStack(offset, type, value)


    override fun getArg(index: Int, type: Datatype, alignment: UInt, instance: ABIBase) =
            abi.getArg(index, type, alignment, instance)

    override fun getArgs(args: Iterable<Datatype>, instance: ABIBase) = abi.getArgs(args, instance)

    override fun setArg(index: Int, type: Datatype, value: ULong, push: Boolean, alignment: UInt, instance: ABIBase) =
            abi.setArg(index, type, value, push, alignment, instance)

    override fun setArgs(args: Iterable<Pair<Datatype, ULong>>, push: Boolean, instance: ABIBase) =
            abi.setArgs(args, push, instance)

    override fun setArgs(args: Array<ULong>, push: Boolean, instance: ABIBase) = abi.setArgs(args, push, instance)


    open fun save() = registers.indices.forEach { i -> registers[i] = abi.readRegister(i) }

    open fun load() = registers.forEachIndexed { i, it -> abi.writeRegister(i, it) }


    open fun store(address: ULong) = registers.forEachIndexed { i, it ->
        abi.writeMemory(address + (i * abi.gprDatatype.bytes).uint, it, abi.gprDatatype)
    }

    open fun restore(address: ULong) = registers.indices.forEach { i ->
        abi.readMemory(address + (i * abi.gprDatatype.bytes).uint, abi.gprDatatype)
    }

}