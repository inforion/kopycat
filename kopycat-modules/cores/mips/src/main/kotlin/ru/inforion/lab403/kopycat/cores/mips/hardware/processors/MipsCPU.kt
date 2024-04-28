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
package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.mips.enums.GPR
import ru.inforion.lab403.kopycat.cores.mips.enums.InstructionSet
import ru.inforion.lab403.kopycat.cores.mips.enums.InstructionSet.*
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.HWRBank
import ru.inforion.lab403.kopycat.cores.mips.hardware.systemdc.Mips16SystemDecoder
import ru.inforion.lab403.kopycat.cores.mips.hardware.systemdc.Mips32SystemDecoder
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class MipsCPU(val mips: MipsCore, name: String) : ACPU<MipsCPU, MipsCore, AMipsInstruction, GPR>(mips, name) {

    companion object {
        const val INVALID_DELAY_JUMP_ADDRESS = ULONG_MAX
    }
    enum class Mode { R32, R64 }

    val mode: Mode get() = when {
        // p 260 PRA
        mips.Config0Preset[14] == 1uL -> Mode.R64
        else -> Mode.R32
    }

    val BIT_DEPTH = when (mode) {
        Mode.R32 -> Datatype.DWORD
        Mode.R64 -> Datatype.QWORD
    }

    inner class BranchController: ICoreUnit {
        override val name: String = "Branch Controller"

        private var delayedJumpInsnRemain = 0

        var delayedJumpAddress: ULong = INVALID_DELAY_JUMP_ADDRESS
            private set

        var hasDelayedJump = false
            private set

        var hasChangeIsa = NONE
            private set

        val isDelaySlot get() = hasDelayedJump && delayedJumpInsnRemain == 0

        override fun reset() {
            super.reset()
            delayedJumpInsnRemain = 0
            delayedJumpAddress = INVALID_DELAY_JUMP_ADDRESS
            hasChangeIsa = NONE
        }

        fun validate() {
            if (hasDelayedJump) throw GeneralException("Branch found in the delay slot: ${pc.hex8}")
        }

        fun setIp(ea: ULong) {
            pc = ea clr 0
            delayedJumpInsnRemain = 0
            delayedJumpAddress = INVALID_DELAY_JUMP_ADDRESS
            hasDelayedJump = false
            val change = hasChangeIsa
            if (change != NONE)
                iset = change
            hasChangeIsa = NONE
        }

        fun schedule(ea: ULong, delay: Int = 1, changeIsa: InstructionSet = NONE) {
            delayedJumpAddress = if (ea == INVALID_DELAY_JUMP_ADDRESS) ea else ea like BIT_DEPTH
            delayedJumpInsnRemain = delay
            hasDelayedJump = true
            hasChangeIsa = changeIsa
        }

        fun nop(delay: Int = 1) = schedule(INVALID_DELAY_JUMP_ADDRESS, delay)
        fun jump(ea: ULong) = schedule(ea, delay = 0)

        fun processIp(size: UInt): ULong {
            if (hasDelayedJump) {
                if (delayedJumpInsnRemain == 0) {
                    if (delayedJumpAddress == INVALID_DELAY_JUMP_ADDRESS) {
                        hasDelayedJump = false
                        pc += size
                    } else {
                        setIp(delayedJumpAddress)
                    }
                    return pc
                }
                delayedJumpInsnRemain -= 1
            }
            pc += size
            return pc
        }

        override fun stringify() =
                "Is waiting for jump? = $hasDelayedJump\n" +
                "Instructions remain  = $delayedJumpInsnRemain\n" +
                "Delayed jump address = 0x${delayedJumpAddress.hex8}"

        override fun serialize(ctxt: GenericSerializer) = mapOf(
                "delayedJumpAddress" to delayedJumpAddress.hex8,
                "delayedJumpInsnRemain" to delayedJumpInsnRemain.hex8,
                "hasDelayedJump" to hasDelayedJump.toString())

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            delayedJumpAddress = (snapshot["delayedJumpAddress"] as String).ulongByHex
            delayedJumpInsnRemain = (snapshot["delayedJumpInsnRemain"] as String).intByHex
            hasDelayedJump = (snapshot["hasDelayedJump"] as String).bool
        }
    }

    override fun reg(index: Int): ULong = regs.read(index)
    override fun reg(index: Int, value: ULong) = regs.write(index, value)
    override fun count() = regs.count
    override fun flags(): ULong = 0u

    var bigEndianCPU
        get() = mips.cop.regs.Config0.BE.int
        set(value) {
            mips.cop.regs.Config0.BE = value > 0
        }

    val mips32decoder = Mips32SystemDecoder(mips)
    val mips16decoder = Mips16SystemDecoder(mips)
    val branchCntrl = BranchController()

    val regs = GPRBank(this, mips.abi)

    val hwrs = HWRBank(mips)        // COP2

    val sgprs = Array(mips.countOfShadowGPR) { GPRBank(this) }

    var hi: ULong = 0u
        get() = if (this.mode == Mode.R32) field mask 32 else field mask 64
        set(value) {
            field = if (this.mode == Mode.R32) value mask 32 else value mask 64
        }
    var lo: ULong = 0u
        get() = if (this.mode == Mode.R32) field mask 32 else field mask 64
        set(value) {
            field = if (this.mode == Mode.R32) value mask 32 else value mask 64
        }

    var status: ULong = 0u
        private set

    override var pc: ULong = 0u
    var llbit: Int = 0

    var iset = MIPS32

    override fun reset() {
        branchCntrl.reset()
        regs.reset()
        hi = 0u
        lo = 0u
        status = 0u
        pc = when (mode) {
            Mode.R32 -> 0xBFC00000u
            Mode.R64 -> 0xFFFF_FFFF_BFC0_0000uL
        }
    }

    private fun fetch(pc: ULong): ULong = core.fetch(pc, 0 ,4)

    override fun decode() {
        val data = fetch(pc)
        insn = when (iset) {
            MIPS32 -> mips32decoder.decode(data, pc)
            MIPS16 -> mips16decoder.decode(data, pc)
            NONE -> error("Instruction set can't be NONE")
        }
        insn.ea = pc
    }

    override fun execute(): Int {
        insn.execute()
        branchCntrl.processIp(insn.size.uint)
        return 1  // TODO: get from insn.execute()
    }

    override fun stringify() = buildString {
        appendLine("MIPS CPU:")
        appendLine(branchCntrl.stringify())
        appendLine("pc = 0x${pc.hex8} status = 0x${status.hex16} hi:lo = 0x${hi.hex8}:${lo.hex16}")
        append(regs.stringify())
    }

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + mapOf(
        "hi" to hi.hex8,
        "lo" to lo.hex8,
        "status" to status.hex8,
        "pc" to pc.hex8,
        "llbit" to llbit.toString(),
        "regs" to regs.serialize(ctxt),
        "branchCntrl" to branchCntrl.serialize(ctxt),
        "hwrs" to hwrs.serialize(ctxt),
    )

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        hi = (snapshot["hi"] as String).ulongByHex
        lo = (snapshot["lo"] as String).ulongByHex
        status = (snapshot["status"] as String).ulongByHex
        pc = (snapshot["pc"] as String).ulongByHex
        llbit = (snapshot["llbit"] as String).intByDec
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, String>)
        branchCntrl.deserialize(ctxt, snapshot["branchCntrl"] as Map<String, String>)
        if ("hwrs" in snapshot) {
            hwrs.deserialize(ctxt, snapshot["hwrs"] as Map<String, String>)
        }
    }
}