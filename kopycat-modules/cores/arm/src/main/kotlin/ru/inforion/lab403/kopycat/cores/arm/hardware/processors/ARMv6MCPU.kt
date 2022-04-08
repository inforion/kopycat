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
@file:Suppress("FunctionName")

package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.enums.Mode
import ru.inforion.lab403.kopycat.cores.arm.enums.StackPointer
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.Thumb16Decoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.Thumb32Decoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet
import ru.inforion.lab403.kopycat.modules.cores.ARMv6MCore
import java.util.logging.Level
import kotlin.collections.set
import ru.inforion.lab403.kopycat.interfaces.*


class ARMv6MCPU(core: ARMv6MCore, name: String) : AARMCPU(core, name) {
    companion object {
        @Transient val log = logger(Level.FINER)
    }

    override fun CurrentMode(): Mode = CurrentMode

    override fun CurrentModeIsPrivileged(): Boolean = (CurrentMode == Mode.Handler || !spr.control.npriv)

    override fun StackPointerSelect() =
            if (arm.cpu.spr.control.spsel) when (CurrentMode) {
                Mode.Thread -> StackPointer.Process
                Mode.Handler -> StackPointer.Main
            } else StackPointer.Main

    override fun ALUWritePC(address: ULong) = BranchWritePC(address)

    override fun LoadWritePC(address: ULong) = BXWritePC(address)

    override fun BranchWritePC(address: ULong, refill: Boolean) = BranchTo(address clr 0, refill)

    override fun BXWritePC(address: ULong, refill: Boolean) {
        if (CurrentMode == Mode.Handler && address[31..28] == 0xFuL) {
            ExceptionReturn(address)
        } else {
            sregs.epsr.t = address[0] == 1uL
            BranchTo(address clr 0, refill)
        }
    }

    fun BLXWritePC(address: ULong) {
        sregs.epsr.t = address[0] == 1uL
        pc = address clr 0
    }

    override fun CurrentInstrSet(): InstructionSet = InstructionSet.THUMB

    override fun SelectInstrSet(target: InstructionSet) = Unit

    var CurrentMode: Mode = Mode.Thread

    fun ExceptionReturn(excReturn: ULong) {
        if (CurrentMode != Mode.Handler) throw GeneralException("ExceptionReturn() function call in Thread mode")
        if (excReturn[27..4] != 0xFF_FFFFuL)
            throw Unpredictable

        val framePtr: ULong

        when (excReturn[3..0]) {
            0b0001uL -> {
                framePtr = regs.sp.main
                CurrentMode = Mode.Handler
                spr.control.spsel = false
            }
            0b1001uL -> {
                framePtr = regs.sp.main
                CurrentMode = Mode.Thread
                spr.control.spsel = false
            }
            0b1101uL -> {
                framePtr = regs.sp.process
                CurrentMode = Mode.Thread
                spr.control.spsel = true
            }
            else -> throw Unpredictable
        }

        PopStack(framePtr, excReturn)

        if (CurrentMode == Mode.Handler) {
            if (sregs.ipsr.exceptionNumber == 0uL) throw Unpredictable
        } else {
            if (sregs.ipsr.exceptionNumber != 0uL) throw Unpredictable
        }
    }

    fun PopStack(framePtr: ULong, excReturn: ULong) {
        arm.cpu.regs.r0.value = core.inl(framePtr)
        arm.cpu.regs.r1.value = core.inl(framePtr + 0x4u)
        arm.cpu.regs.r2.value = core.inl(framePtr + 0x8u)
        arm.cpu.regs.r3.value = core.inl(framePtr + 0xCu)
        arm.cpu.regs.r12.value = core.inl(framePtr + 0x10u)
        arm.cpu.regs.lr.value = core.inl(framePtr + 0x14u)
        val pc = core.inl(framePtr + 0x18u) clr 0
        val psr = core.inl(framePtr + 0x1Cu)

//        if(pc[0] == 1L) throw Unpredictable
        BranchTo(pc, true)

        when (excReturn[3..0]) {
            0b0001uL, 0b1001uL, 0b1101uL -> regs.sp.value = (regs.sp.value + 0x20u).insert(psr[9], 2)
            else -> throw Unpredictable
        }

        arm.cpu.sregs.apsr.value = insert(psr[31..28], 31..28)

        val forceThread = (CurrentMode == Mode.Thread && spr.control.npriv)
        sregs.ipsr.exceptionNumber = if (forceThread) 0u else psr[5..0]
        sregs.epsr.t = psr[24] == 1uL
    }

    val VTOR = 0x800_0000u

    private val thumb16 = Thumb16Decoder(core)
    private val thumb32 = Thumb32Decoder(core)

    override fun flags() = sregs.apsr.value

    override fun reset() {
        super.reset()
        CurrentMode = Mode.Thread
        pipelineRefillRequired = false
    }

    private fun fetch(where: ULong) = core.fetch(where, 0, 4)

    private fun swapByte(data: ULong): ULong {
        val high = data and 0xFFFF_0000u
        val low = data and 0xFFFFu
        return (high ushr 16) or (low shl 16)
    }

    private val cache = dictionary<ULong, Pair<AARMInstruction, UInt>>(1024 * 1024)

    private var offset: UInt = 0u

    override fun decode() {
        var data = fetch(pc clr 0)  // we must allays call fetch to check breakpoints
        val e = cache[pc]
        if (e == null) {
            val type = data[15..11]

            if (type != 0b11101uL && type != 0b11110uL && type != 0b11111uL) {
                data = data[15..0]
                insn = thumb16.decode(data)
                offset = 2u
            } else {
                data = swapByte(data)
                insn = thumb32.decode(data)
                offset = 0u
            }

            insn.ea = pc

            cache[pc] = Pair(insn, offset)
        } else {
            insn = e.first
            offset = e.second
        }

//        log.finer { ("[${pc.hex8}] ${insn.opcode.hex8} $insn") }
    }

    override fun execute(): Int {
        pc += offset + insn.size

        try {
            insn.execute()
        } catch (error: Throwable) {
            pc = insn.ea
            throw error
        }

        // PC points at the address for fetched instruction when executing restore normal code flow if no jump occurred
        if (!pipelineRefillRequired) pc -= offset
        // PC has been changed during instruction execution-> nothing to fix up
        else pipelineRefillRequired = false

        return 1  // TODO: get from insn.execute()
    }
}