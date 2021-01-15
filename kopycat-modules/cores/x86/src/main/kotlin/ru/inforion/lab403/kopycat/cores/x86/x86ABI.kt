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
package ru.inforion.lab403.kopycat.cores.x86

import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABIBase
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class x86ABI(core: x86Core, bigEndian: Boolean) : ABI<x86Core>(core, 32, bigEndian) {
    companion object {
        @Transient
        val log = logger(WARNING)
    }

    @DontAutoSerialize
    override val regArguments = listOf<Int>()

    override val minimumStackAlignment = 2.also { log.warning { "Determine stack alignment in x86" } }

    override val stackArgsOffset = 4L

    override val gprDatatype = Datatype.values().first { it.bits == (this.core.cpu.regs.msb + 1) }
    override fun register(index: Int) = x86Register.gpr(Datatype.DWORD, index)
    override val registerCount: Int get() = core.cpu.count()
    override val sizetDatatype get() = Datatype.DWORD

    override fun createContext() = x86Context(this)

    override val pc get() = x86Register.GPRDW.eip
    override val sp get() = x86Register.GPRDW.esp
    override val rv get() = x86Register.GPRDW.eax
    override val ra get() = throw IllegalAccessError("x86 has no return address register!")

    override val segmentSelector = SSR.SS.id

    override var returnAddressValue: Long
        get() = readPointer(stackPointerValue).also {
            log.severe { "Please, fix it" }
        }
        set(value) {
            push(value).also {
                log.severe { "Please, fix it" }
            }
        }

    override fun ret() {
//        super.ret()
        programCounterValue = pop(types.pointer)
//        programCounterValue = readPointer(stackPointerValue)
//        stackPointerValue += types.pointer.bytes
    }

    var argOffset = 0L

    override fun getArg(index: Int, type: Datatype, alignment: Int, instance: ABIBase) =
            instance.readStack(stackArgsOffset + argOffset, type).also {
                argOffset += maxOf(minimumStackAlignment, type.bytes)
            }

    override fun getArgs(args: Iterable<Datatype>, instance: ABIBase): Array<Long> {
        argOffset = 0
        return args.mapIndexed { i, it -> getArg(i, it, 0, instance) }.toTypedArray()
    }

}