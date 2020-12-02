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
package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.enums.GPR
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.HWRBank
import ru.inforion.lab403.kopycat.cores.mips.hardware.systemdc.MipsSystemDecoder
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class MipsCPU(val mips: MipsCore, name: String) : ACPU<MipsCPU, MipsCore, AMipsInstruction, GPR>(mips, name) {

    inner class BranchController: ICoreUnit {
        override val name: String = "Branch Controller"

        private var delayedJumpInsnRemain = 0

        var delayedJumpAddress = WRONGL
            private set

        var hasDelayedJump = false
            private set

        val isDelaySlot get() = hasDelayedJump && delayedJumpInsnRemain == 0

        override fun reset() {
            super.reset()
            delayedJumpInsnRemain = 0
            delayedJumpAddress = WRONGL
        }

        fun validate() {
            if (hasDelayedJump) throw GeneralException("Branch found in the delay slot: ${pc.hex8}")
        }

        fun setIp(ea: Long) {
            pc = ea
            delayedJumpInsnRemain = 0
            delayedJumpAddress = WRONGL
            hasDelayedJump = false
        }

        fun schedule(ea: Long, delay: Int = 1) {
            delayedJumpAddress = ea
            delayedJumpInsnRemain = delay
            hasDelayedJump = true
        }

        fun nop(delay: Int = 1) = schedule(-1, delay)
        fun jump(ea: Long) = schedule(ea, delay = 0)

        fun processIp(size: Int): Long {
            if (hasDelayedJump) {
                if (delayedJumpInsnRemain == 0) {
                    if (delayedJumpAddress == WRONGL) {
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
            delayedJumpAddress = (snapshot["delayedJumpAddress"] as String).hexAsULong
            delayedJumpInsnRemain = (snapshot["delayedJumpInsnRemain"] as String).hexAsUInt
            hasDelayedJump = (snapshot["hasDelayedJump"] as String).toBoolean()
        }
    }

    override fun reg(index: Int): Long = regs.read(index)
    override fun reg(index: Int, value: Long) = regs.write(index, value)
    override fun count() = regs.count
    override fun flags(): Long = 0

    val bigEndianCPU = 0

    val decoder = MipsSystemDecoder(mips)
    val branchCntrl = BranchController()

    val regs = GPRBank()

    val hwrs = HWRBank(mips)

    val sgprs = Array(mips.countOfShadowGPR) { GPRBank() }

    var hi: Long = 0
        get() = field and 0xFFFFFFFF
        set(value) {
            field = value and 0xFFFFFFFF
        }
    var lo: Long = 0
        get() = field and 0xFFFFFFFF
        set(value) {
            field = value and 0xFFFFFFFF
        }

    var status: Long = 0
        private set
    override var pc: Long = 0
    var llbit: Int = 0

    override fun reset() {
        branchCntrl.reset()
        regs.reset()
        hi = 0
        lo = 0
        status = 0
        pc = 0xBFC00000
    }

    private fun fetch(pc: Long): Long = core.fetch(pc, 0 ,4)

    override fun decode() {
        val data = fetch(pc)
        insn = decoder.decode(data, pc)
        insn.ea = pc
    }

    override fun execute(): Int {
        insn.execute()
        branchCntrl.processIp(insn.size)
        return 1  // TODO: get from insn.execute()
    }

    override fun stringify() = buildString {
        appendLine("MIPS CPU:")
        appendLine(branchCntrl.stringify())
        appendLine("pc = 0x${pc.hex8} status = 0x${status.hex8} hi:lo = 0x${hi.hex8}:${lo.hex8}")
        append(regs.stringify())
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "hi" to hi.hex8,
                "lo" to lo.hex8,
                "status" to status.hex8,
                "pc" to pc.hex8,
                "llbit" to llbit.toString(),
                "regs" to regs.serialize(ctxt),
                "branchCntrl" to branchCntrl.serialize(ctxt))
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        hi = (snapshot["hi"] as String).hexAsULong
        lo = (snapshot["lo"] as String).hexAsULong
        status = (snapshot["status"] as String).hexAsULong
        pc = (snapshot["pc"] as String).hexAsULong
        llbit = (snapshot["llbit"] as String).toInt()
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, String>)
        branchCntrl.deserialize(ctxt, snapshot["branchCntrl"] as Map<String, String>)
    }
}