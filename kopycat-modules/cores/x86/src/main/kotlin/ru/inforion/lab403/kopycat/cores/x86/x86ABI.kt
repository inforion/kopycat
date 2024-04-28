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
package ru.inforion.lab403.kopycat.cores.x86

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABIBase
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
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

    override val stackArgsOffset = 4uL

    override val gprDatatype = Datatype.values().first { it.bits == (this.core.cpu.regs.msb + 1) }
    override fun register(index: Int) = core.cpu.regs.gpr(x86GPR.byIndex(index), Datatype.DWORD).toOperand()
    override val registerCount: Int get() = core.cpu.count()
    override val sizetDatatype get() = Datatype.DWORD

    override fun createContext() = x86Context(this)

    override val pc get() = core.cpu.regs.eip.toOperand()
    override val sp get() = core.cpu.regs.esp.toOperand()
    override val rv get() = core.cpu.regs.eax.toOperand()
    override val ra get() = throw IllegalAccessError("x86 has no return address register!")

    override val segmentSelector = SSR.SS.id

    override var returnAddressValue: ULong
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

    var argOffset = 0uL

    override fun getArg(index: Int, type: Datatype, alignment: UInt, instance: ABIBase): ULong =
            instance.readStack(stackArgsOffset + argOffset, type).also {
                argOffset += maxOf(minimumStackAlignment, type.bytes).uint
            }

    override fun getArgs(args: Iterable<Datatype>, instance: ABIBase): Array<ULong> {
        argOffset = 0u
        return args.mapIndexed { i, it -> getArg(i, it, 0u, instance) }.toTypedArray()
    }

}