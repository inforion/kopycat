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
package ru.inforion.lab403.kopycat.cores.mips.instructions

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.mips.enums.COND
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ACOP0
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.MipsCPU
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.io.Serializable
import kotlin.reflect.KProperty

// WARNING: Other classes made without delegate due to delegated property can't be made inline!

class DoubleRegister(val index: Int) : Serializable {
    operator fun getValue(insn: AMipsInstruction, property: KProperty<*>): ULong {
        val op1 = insn[index - 1] as MipsRegister
        if (insn.core.fpuDtype == Datatype.DWORD) {
            val op2 = op1.desc.next.toOperand()
            return op1.value(insn.core).insert(op2.value(insn.core), 63..32)
        }
        return op1.value(insn.core)
    }

    operator fun setValue(insn: AMipsInstruction, property: KProperty<*>, value: ULong) {
        val op1 = insn[index - 1] as MipsRegister

        if (insn.core.fpuDtype == Datatype.DWORD) {
            val op2 = op1.desc.next.toOperand()
            op1.value(insn.core, value[31..0])
            op2.value(insn.core, value[63..32])
        } else {
            op1.value(insn.core, value)
        }
    }
}


abstract class CcFsFtInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val fs: MipsRegister,
        val ft: MipsRegister,
        val condition: MipsImmediate,
        val cc: MipsImmediate,
) : AMipsInstruction(core, data, type, fs, ft, condition) {
    inline var vfs: ULong
        get() = fs.value(core)
        set(value) = fs.value(core, value)
    inline var vft: ULong
        get() = ft.value(core)
        set(value) = ft.value(core, value)

    var dfs : ULong by DoubleRegister(1)
    var dft : ULong by DoubleRegister(2)

    inline var vcc: Boolean
        get() = core.fpu.cntrls.fccr.fccn(cc.value.int)
        set(value) = core.fpu.cntrls.fccr.fccn(cc.value.int, value)

    val cond by lazy { COND.values().first { it.ordinal == (op3 as MipsImmediate).value.int } }
}

abstract class CcOffsetInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val cc: MipsImmediate,
        val off: MipsNear
) : AMipsInstruction(core, data, type, cc, off) {
    inline val address: ULong get() = when {
        core.is32bit -> (core.cpu.pc + size) + off.offset
        else -> core.cpu.pc + size.uint + off.usext(core)
    }
    inline val eaAfterBranch: ULong get() = core.cpu.pc + 8u

    inline var vcc: Boolean
        get() = core.fpu.cntrls.fccr.fccn(cc.value.int)
        set(value) = core.fpu.cntrls.fccr.fccn(cc.value.int, value)
}

abstract class Code19bitInsn(core: MipsCore, data: ULong, type: Type, val imm: MipsImmediate) :
        AMipsInstruction(core, data, type, imm) {

    inline val cop0: ACOP0 get() = core.cop

    inline val index: Int
        get() = (cop0.regs.Index.index % core.mmu.tlbEntries.ulong_z).int

    inline val random: ULong get() = cop0.regs.Random.value

    inline var pageMask: ULong
        get() = cop0.regs.PageMask.value
        set(value) {
            cop0.regs.PageMask.value = value
        }
    inline var entryHi: ULong
        get() = cop0.regs.EntryHi.value
        set(value) {
            cop0.regs.EntryHi.value = value
        }
    inline var entryLo0: UInt
        get() = cop0.regs.EntryLo0.value.uint
        set(value) {
            cop0.regs.EntryLo0.value = value.ulong_z
        }
    inline var entryLo1: UInt
        get() = cop0.regs.EntryLo1.value.uint
        set(value) {
            cop0.regs.EntryLo1.value = value.ulong_z
        }
    inline var memoryMapId: ULong
        get() = cop0.regs.MMID.mmid
        set(value) {
            cop0.regs.MMID.value = value[15..0]
        }
}

abstract class Code20bitInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val code: MipsImmediate
) : AMipsInstruction(core, data, type, code)

abstract class Cofun25BitInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val cofun: MipsImmediate
) : AMipsInstruction(core, data, type, cofun)

abstract class FdFsInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val fd: MipsRegister,
        val fs: MipsRegister
) : AMipsInstruction(core, data, type, fd, fs) {
    inline var vfs: ULong
        get() = fs.value(core)
        set(value) = fs.value(core, value)
    inline var vfd: ULong
        get() = fd.value(core)
        set(value) = fd.value(core, value)
    var dfd : ULong by DoubleRegister(1)
    var dfs : ULong by DoubleRegister(2)
}

abstract class FdFsFtInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val fd: MipsRegister,
        val fs: MipsRegister,
        val ft: MipsRegister
) : AMipsInstruction(core, data, type, fd, fs, ft) {

    inline var vfd: ULong
        get() = fd.value(core)
        set(value) = fd.value(core, value)
    inline var vfs: ULong
        get() = fs.value(core)
        set(value) = fs.value(core, value)
    inline var vft: ULong
        get() = ft.value(core)
        set(value) = ft.value(core, value)

    var dfd : ULong by DoubleRegister(1)
    var dfs : ULong by DoubleRegister(2)
    var dft : ULong by DoubleRegister(3)
}

abstract class IndexInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val index: MipsNear
) : AMipsInstruction(core, data, type, index) {

    val address: ULong get() = when (core.cpu.mode) {
        MipsCPU.Mode.R32 -> (core.cpu.pc and 0xF0000000u) + index.offset
        MipsCPU.Mode.R64 -> (core.cpu.pc and 0xFFFFFFFF_F0000000u) + index.offset
    }

    inline var vra: ULong
        get() = core.cpu.regs.ra.value
        set(value) { core.cpu.regs.ra.value = value }

    inline val eaAfterBranch: ULong get() = core.cpu.pc + 8u
}

abstract class OpOffsetBaseInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val imm: MipsImmediate,
        val off: MipsDisplacement
) : AMipsInstruction(core, data, type, imm, off)

abstract class RdInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rd: MipsRegister
) : AMipsInstruction(core, data, type, rd) {
    inline var vrd: ULong
        get() = rd.value(core)
        set(value) = rd.value(core, value)
}

abstract class RdRsHintInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rd: MipsRegister,
        val rs: MipsRegister,
        val hint: MipsImmediate
) : AMipsInstruction(core, data, type, rd, rs, hint) {
    inline var vrd: ULong
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline var vrs: ULong
        get() = rs.value(core)
        set(value) = rs.value(core, value)
    val eaAfterBranch: ULong get() = core.cpu.pc + 8u
}

abstract class RdRsCcInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rd: MipsRegister,
        val rs: MipsRegister,
        val cc: MipsImmediate,
) : AMipsInstruction(core, data, type, rd, rs, cc) {
    inline var vrd: ULong
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline var vrs: ULong
        get() = rs.value(core)
        set(value) = rs.value(core, value)
    inline var vcc: Boolean
        get() = core.fpu.cntrls.fccr.fccn(cc.value.int)
        set(value) = core.fpu.cntrls.fccr.fccn(cc.value.int, value)
}

abstract class RdRsRtInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rd: MipsRegister,
        val rs: MipsRegister,
        val rt: MipsRegister
) : AMipsInstruction(core, data, type, rd, rs, rt) {
    inline var vrd: ULong
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline val vrs: ULong get() = rs.value(core)
    inline val vrt: ULong get() = rt.value(core)
}

abstract class RdRtInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rd: MipsRegister,
        val rt: MipsRegister
) : AMipsInstruction(core, data, type, rd, rt) {
    inline var vrd: ULong
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline val vrt: ULong get() = rt.value(core)
}

abstract class RdRtRsInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rd: MipsRegister,
        val rt: MipsRegister,
        val rs: MipsRegister
) : AMipsInstruction(core, data, type, rd, rt, rs) {
    inline var vrd: ULong
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline val vrt: ULong get() = rt.value(core)
    inline val vrs: ULong get() = rs.value(core)
}

abstract class RdRtSaInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rd: MipsRegister,
        val rt: MipsRegister,
        val sa: MipsImmediate
) : AMipsInstruction(core, data, type, rd, rt, sa) {
    inline var vrd: ULong
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline val vrt: ULong get() = rt.value(core)
    inline val vsa: Int get() = sa.value.int
}

abstract class RsInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rs: MipsRegister
) : AMipsInstruction(core, data, type, rs) {
    inline var vrs: ULong
        get() = rs.value(core)
        set(value) = rs.value(core, value)
}

abstract class RsImmInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rs: MipsRegister,
        val imm: MipsImmediate
) : AMipsInstruction(core, data, type, rs, imm) {
    inline var vrs: ULong
        get() = rs.value(core)
        set(value) = rs.value(core, value)
}

abstract class RsOffsetInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rs: MipsRegister,
        val off: MipsNear
) : AMipsInstruction(core, data, type, rs, off) {
    inline val vrs: ULong get() = rs.value(core)
    inline val address: ULong get() = when {
        core.is32bit -> (core.cpu.pc + size) + off.offset
        else -> core.cpu.pc + size.uint + off.usext(core)
    }
    inline val eaAfterBranch: ULong get() = core.cpu.pc + 8u

    inline var vra: ULong
        get() = core.cpu.regs.ra.value
        set(value) { core.cpu.regs.ra.value = value }
}

abstract class RsRtCodeInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rs: MipsRegister,
        val rt: MipsRegister,
        val code: MipsImmediate
) : AMipsInstruction(core, data, type, rs, rt, code) {
    inline var vrs: ULong
        get() = rs.value(core)
        set(value) = rs.value(core, value)
    inline val vrt: ULong get() = rt.value(core)
    inline val vcode: ULong get() = code.value
}

abstract class RsRtOffsetInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rs: MipsRegister,
        val rt: MipsRegister,
        val off: MipsNear
) : AMipsInstruction(core, data, type, rs, rt, off) {
    inline val vrs: ULong get() = rs.value(core)
    inline val vrt: ULong get() = rt.value(core)
    inline val address: ULong get() = when {
        core.is32bit -> (core.cpu.pc + size) + off.offset
        else -> core.cpu.pc + size.uint + off.usext(core)
    }
    inline val eaAfterBranch: ULong get() = core.cpu.pc + 8u
}

abstract class RsRtPosSizeInsn(
    core: MipsCore,
    data: ULong,
    type: Type,
    val rt: MipsRegister,
    val rs: MipsRegister,
    val pos: MipsImmediate,
    val siz: MipsImmediate
) : AMipsInstruction(core, data, type, rt, rs, pos, siz) {
    inline var vrt: ULong
        get() = rt.value(core)
        set(value) = rt.value(core, value)
    inline val vrs: ULong get() = rs.value(core)
    inline val lsb: Int get() = pos.value.int
    inline val msb: Int get() = siz.value.int
}

abstract class RtInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rt: MipsRegister
) : AMipsInstruction(core, data, type, rt) {
    inline val cop0: ACOP0 get() = core.cop
    inline var vrt: ULong
        get() = rt.value(core)
        set(value) = rt.value(core, value)
}

abstract class RtImmInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rt: MipsRegister,
        val imm: MipsImmediate
) : AMipsInstruction(core, data, type, rt, imm) {
    inline var vrt: ULong
        get() = rt.value(core)
        set(value) = rt.value(core, value)
}

abstract class FtOffsetInsn(
    core: MipsCore,
    data: ULong,
    type: Type,
    val rt: MipsRegister,
    val off: MipsDisplacement
) : AMipsInstruction(core, data, type, rt, off) {
    inline var vrt: ULong
        get() = rt.value(core)
        set(value) = rt.value(core, value)
    inline var memword: ULong
        get() = off.value(core)
        set(value) = off.value(core, value)
    inline val address: ULong get() = off.effectiveAddress(core)
    var dft : ULong by DoubleRegister(1)
}

@Deprecated("Same as FtOffsetInsn", replaceWith = ReplaceWith("FtOffsetInsn"))
abstract class RtOffsetInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rt: MipsRegister,
        val off: MipsDisplacement
) : AMipsInstruction(core, data, type, rt, off) {
    inline var vrt: ULong
        get() = rt.value(core)
        set(value) = rt.value(core, value)
    inline var memword: ULong
        get() = off.value(core)
        set(value) = off.value(core, value)
    inline val address: ULong get() = off.effectiveAddress(core)
    var dft : ULong by DoubleRegister(1)
}

abstract class RtRdSelInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rt: MipsRegister,
        val rd: MipsRegister
) : AMipsInstruction(core, data, type, rt, rd) {
    inline var vrt: ULong
        get() = rt.value(core)
        set(value) = rt.value(core, value)
    inline var vrd: ULong
        get() = rd.value(core)
        set(value) = rd.value(core, value)
}

abstract class RtRsImmInsn(
        core: MipsCore,
        data: ULong,
        type: Type,
        val rt: MipsRegister,
        val rs: MipsRegister,
        val imm: MipsImmediate
) : AMipsInstruction(core, data, type, rt, rs, imm) {
    inline var vrt: ULong
        get() = rt.value(core)
        set(value) = rt.value(core, value)
    inline val vrs: ULong get() = rs.value(core)
}

abstract class EmptyInsn(core: MipsCore, data: ULong) : AMipsInstruction(core, data, VOID)