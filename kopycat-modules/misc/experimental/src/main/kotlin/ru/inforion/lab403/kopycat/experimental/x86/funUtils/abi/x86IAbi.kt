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
package ru.inforion.lab403.kopycat.experimental.x86.funUtils.abi

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.plus
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.FunArg
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.StackAllocation
import ru.inforion.lab403.kopycat.modules.cores.x86Core

interface x86IAbi {
    val x86: x86Core

    fun getStackAlignment(): Datatype;
    fun getStackAlignmentSize() = getStackAlignment().bytes;

    fun alignStack(addr: ULong): ULong {
        val alignment = getStackAlignmentSize().ulong_z
        val floored = (addr / alignment) * alignment
        return if (floored == addr) addr else floored + alignment
    }

    /**
     * Performs aligned stack allocation
     */
    fun allocOnStack(size: ULong): StackAllocation {
        require(size <= 0x1024uL) { "Large stack memory allocation (> 1KiB) causes bugs" }

        val rsp = x86.cpu.regs.rsp
        val oldRsp = rsp.value
        val alignedSize = alignStack(size)
        rsp.value -= alignedSize
        val newRsp = rsp.value
        return StackAllocation(
            oldRsp,
            newRsp,
            alignedSize,
            newRsp
        )
    }

    fun clearStackAllocation(allocation: StackAllocation) {
        val rsp = x86.cpu.regs.rsp
        require(allocation.newSP == rsp.value) {
            "Allocation RSP (0x${allocation.newSP.hex}) " +
                    "does not equal to Core SP (0x${rsp.value.hex})"
        }

        rsp.value += allocation.size
    }

    /**
     * The function result must be used
     */
    fun putOnStack(value: String): StackAllocation {
        val byteArray = value.toByteArray() + 0x00.byte
        return putOnStack(byteArray)
    };

    fun putOnStack(value: ByteArray) =
        allocOnStack(value.size.ulong_z).also { allocated ->
            x86.store(
                allocated.address,
                value,
                SSR.SS.id
            )
        }

    fun putOnStack(value: ULong, size: Int) =
        allocOnStack(size.ulong_z).also { allocated ->
            x86.write(
                allocated.address,
                SSR.SS.id,
                size,
                value
            );
        }

    fun pushStack(value: ULong) = x86utils.push(x86, value, getStackAlignment(), Prefixes(x86))

    // TODO: wasn't tested
    fun popStack(): ULong = x86utils.pop(x86, getStackAlignment(), Prefixes(x86))

    /**
     * Gets i-th element from the top of the stack
     */
    fun getTopStack(i: Int): ULong {
        val alignment = getStackAlignment()
        val sp = x86.cpu.regs.gpr(x86GPR.RSP, alignment)
        return x86.read(sp.value + alignment.bytes * i, SSR.SS.id, alignment.bytes)
    }

    /**
     * Sets i-th element from the top of the stack
     */
    fun setTopStack(i: Int, value: ULong) {
        val alignment = getStackAlignment()
        val sp = x86.cpu.regs.gpr(x86GPR.RSP, alignment)
        x86.write(sp.value + alignment.bytes * i, SSR.SS.id, alignment.bytes, value)
    }

    fun allocArgsOnStack(argsAmount: Int): StackAllocation

    fun allocAndPutArgs(args: List<FunArg>): List<StackAllocation> {
        val (allocasData, preparedArgs) = args.map {
            when (it) {
                is FunArg.String -> putOnStack(it.value)
                    .let { alloca -> alloca to alloca.address }

                is FunArg.ByteArray -> putOnStack(it.value)
                    .let { alloca -> alloca to alloca.address }

                is FunArg.Pointer -> null to it.value
                is FunArg.Number -> null to it.value
            }
        }.unzip()
        val argsAlloca = allocArgsOnStack(args.size)
        val resultAllocs = allocasData.filterNotNull() + listOf(argsAlloca)

        preparedArgs.forEachIndexed { i, value -> setArgument(i, value) }
        return resultAllocs
    }

    fun allocAndPutArgs(vararg args: FunArg) = allocAndPutArgs(args.toList())

    fun saveState(): List<ULong> = (0..16).map { i ->
        x86.cpu.regs.gpr(x86GPR.byIndex(i), Datatype.QWORD).value
    }.toList()

    fun restoreState(state: List<ULong>) = state.forEachIndexed { i, it ->
        x86.cpu.regs.gpr(x86GPR.byIndex(i), Datatype.QWORD).value = it
    }

    fun getRegisterArgsAmount(): Int;

    fun argRegister(i: Int): ARegistersBankNG<x86Core>.Register?;

    fun setArgument(i: Int, value: ULong) {
        argRegister(i)?.also {
            it.value = value
        } ?: setTopStack(getRegisterArgsAmount() - i, value)
    }

    fun getArgument(i: Int): ULong =
        argRegister(i)?.value ?: getTopStack(getRegisterArgsAmount() - i)

    fun getResult(): ULong;
    fun setResult(value: ULong);
}