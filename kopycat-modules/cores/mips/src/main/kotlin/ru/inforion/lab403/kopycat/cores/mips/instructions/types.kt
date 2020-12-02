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
package ru.inforion.lab403.kopycat.cores.mips.instructions

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.enums.COND
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ACOP0
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.io.Serializable
import kotlin.reflect.KProperty

// WARNING: Other classes made without delegate due to delegated property can't be made inline!

class DoubleRegister(val index: Int): Serializable {
    operator fun getValue(insn: AMipsInstruction, property: KProperty<*>): Long {
        val op1 = insn[index - 1] as MipsRegister
        val op2 = op1.desc.next.toOperand()
        return op1.value(insn.core).insert(op2.value(insn.core), 63..32)
    }

    operator fun setValue(insn: AMipsInstruction, property: KProperty<*>, value: Long) {
        val op1 = insn[index - 1] as MipsRegister
        val op2 = op1.desc.next.toOperand()
        op1.value(insn.core, value[31..0])
        op2.value(insn.core, value[63..32])
    }
}


abstract class CcFsFtInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val fs: MipsRegister,
        val ft: MipsRegister,
        val cc: MipsImmediate
) : AMipsInstruction(core, data, type, fs, ft, cc) {
    inline var vfs: Long
        get() = fs.value(core)
        set(value) = fs.value(core, value)
    inline var vft: Long
        get() = ft.value(core)
        set(value) = ft.value(core, value)

    var dfs : Long by DoubleRegister(1)
    var dft : Long by DoubleRegister(2)

    //  FCC0 bit is 23
    inline var vcc: Boolean
        get() = core.fpu.cntrls.fccr.fcc0
        set(value) { core.fpu.cntrls.fccr.fcc0 = value }

    val cond by lazy { COND.values().find { it.ordinal == (op3 as MipsImmediate).value.toInt() }!! }
}

abstract class CcOffsetInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val cc: MipsImmediate,
        val off: MipsNear
) : AMipsInstruction(core, data, type, cc, off) {
    inline val address: Long get() = off.offset + (core.cpu.pc + size)
    inline val eaAfterBranch: Long get() = core.cpu.pc + 8

    //  FCC0 bit is 23
    inline var vcc: Boolean
        get() = core.fpu.cntrls.fccr.fcc0
        set(value) { core.fpu.cntrls.fccr.fcc0 = value }
}

abstract class Code19bitInsn(core: MipsCore, data: Long, type: Type, val imm: MipsImmediate) :
        AMipsInstruction(core, data, type, imm) {

    inline val cop0: ACOP0 get() = core.cop

    inline var index: Long
        get() = cop0.regs.Index.value
        set(value) {
            cop0.regs.Index.value = value
        }

    inline val random: Long get() = cop0.regs.Random.value

    inline var pageMask: Long
        get() = cop0.regs.PageMask.value
        set(value) {
            cop0.regs.PageMask.value = value
        }
    inline var entryHi: Long
        get() = cop0.regs.EntryHi.value
        set(value) {
            cop0.regs.EntryHi.value = value
        }
    inline var entryLo0: Long
        get() = cop0.regs.EntryLo0.value
        set(value) {
            cop0.regs.EntryLo0.value = value
        }
    inline var entryLo1: Long
        get() = cop0.regs.EntryLo1.value
        set(value) {
            cop0.regs.EntryLo1.value = value
        }
}

abstract class Code20bitInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val code: MipsImmediate
) : AMipsInstruction(core, data, type, code)

abstract class Cofun25BitInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val cofun: MipsImmediate
) : AMipsInstruction(core, data, type, cofun)

abstract class FdFsInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val fd: MipsRegister,
        val fs: MipsRegister
) : AMipsInstruction(core, data, type, fd, fs) {
    inline var vfs: Long
        get() = fs.value(core)
        set(value) = fs.value(core, value)
    inline var vfd: Long
        get() = fd.value(core)
        set(value) = fd.value(core, value)
    var dfd : Long by DoubleRegister(1)
    var dfs : Long by DoubleRegister(2)
}

abstract class FdFsFtInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val fd: MipsRegister,
        val fs: MipsRegister,
        val ft: MipsRegister
) : AMipsInstruction(core, data, type, fd, fs, ft) {

    inline var vfd: Long
        get() = fd.value(core)
        set(value) = fd.value(core, value)
    inline var vfs: Long
        get() = fs.value(core)
        set(value) = fs.value(core, value)
    inline var vft: Long
        get() = ft.value(core)
        set(value) = ft.value(core, value)

    var dfd : Long by DoubleRegister(1)
    var dfs : Long by DoubleRegister(2)
    var dft : Long by DoubleRegister(3)
}

abstract class IndexInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val index: MipsNear
) : AMipsInstruction(core, data, type, index) {

    val address: Long get() = index.offset + (core.cpu.pc and 0xF0000000)

    inline var vra: Long
        get() = core.cpu.regs.ra.value
        set(value) { core.cpu.regs.ra.value = value }

    inline val eaAfterBranch: Long get() = core.cpu.pc + 8
}

abstract class OpOffsetBaseInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val imm: MipsImmediate,
        val off: MipsDisplacement
) : AMipsInstruction(core, data, type, imm, off)

abstract class RdInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rd: MipsRegister
) : AMipsInstruction(core, data, type, rd) {
    inline var vrd: Long
        get() = rd.value(core)
        set(value) = rd.value(core, value)
}

abstract class RdRsHintInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rd: MipsRegister,
        val rs: MipsRegister,
        val hint: MipsImmediate
) : AMipsInstruction(core, data, type, rd, rs, hint) {
    inline var vrd: Long
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline var vrs: Long
        get() = rs.value(core)
        set(value) = rs.value(core, value)
    val eaAfterBranch: Long get() = core.cpu.pc + 8
}

abstract class RdRsCcInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rd: MipsRegister,
        val rs: MipsRegister,
        val cc: MipsImmediate
) : AMipsInstruction(core, data, type, rd, rs, cc)

abstract class RdRsRtInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rd: MipsRegister,
        val rs: MipsRegister,
        val rt: MipsRegister
) : AMipsInstruction(core, data, type, rd, rs, rt) {
    inline var vrd: Long
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline val vrs: Long get() = rs.value(core)
    inline val vrt: Long get() = rt.value(core)
}

abstract class RdRtInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rd: MipsRegister,
        val rt: MipsRegister
) : AMipsInstruction(core, data, type, rd, rt) {
    inline var vrd: Long
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline val vrt: Long get() = rt.value(core)
}

abstract class RdRtRsInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rd: MipsRegister,
        val rt: MipsRegister,
        val rs: MipsRegister
) : AMipsInstruction(core, data, type, rd, rt, rs) {
    inline var vrd: Long
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline val vrt: Long get() = rt.value(core)
    inline val vrs: Long get() = rs.value(core)
}

abstract class RdRtSaInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rd: MipsRegister,
        val rt: MipsRegister,
        val sa: MipsImmediate
) : AMipsInstruction(core, data, type, rd, rt, sa) {
    inline var vrd: Long
        get() = rd.value(core)
        set(value) = rd.value(core, value)
    inline val vrt: Long get() = rt.value(core)
    inline val vsa: Int get() = sa.zext.toInt()
}

abstract class RsInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rs: MipsRegister
) : AMipsInstruction(core, data, type, rs) {
    inline var vrs: Long
        get() = rs.value(core)
        set(value) = rs.value(core, value)
}

abstract class RsImmInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rs: MipsRegister,
        val imm: MipsImmediate
) : AMipsInstruction(core, data, type, rs, imm) {
    inline var vrs: Long
        get() = rs.value(core)
        set(value) = rs.value(core, value)
}

abstract class RsOffsetInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rs: MipsRegister,
        val off: MipsNear
) : AMipsInstruction(core, data, type, rs, off) {
    inline val vrs: Long get() = rs.value(core)
    inline val address: Long get() = off.offset + (core.cpu.pc + size)
    inline val eaAfterBranch: Long get() = core.cpu.pc + 8

    inline var vra: Long
        get() = core.cpu.regs.ra.value
        set(value) { core.cpu.regs.ra.value = value }
}

abstract class RsRtCodeInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rs: MipsRegister,
        val rt: MipsRegister,
        val code: MipsImmediate
) : AMipsInstruction(core, data, type, rs, rt, code) {
    inline var vrs: Long
        get() = rs.value(core)
        set(value) = rs.value(core, value)
    inline val vrt: Long get() = rt.value(core)
    inline val vcode: Long get() = code.zext
}

abstract class RsRtOffsetInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rs: MipsRegister,
        val rt: MipsRegister,
        val off: MipsNear
) : AMipsInstruction(core, data, type, rs, rt, off) {
    inline val vrs: Long get() = rs.value(core)
    inline val vrt: Long get() = rt.value(core)
    inline val address: Long get() = off.offset + (core.cpu.pc + size)
    inline val eaAfterBranch: Long get() = core.cpu.pc + 8
}

abstract class RsRtPosSizeInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rs: MipsRegister,
        val rt: MipsRegister,
        val pos: MipsImmediate,
        val siz: MipsImmediate
) : AMipsInstruction(core, data, type, rs, rt, pos, siz) {
    inline var vrs: Long
        get() = rs.value(core)
        set(value) = rs.value(core, value)
    inline val vrt: Long get() = rt.value(core)
    inline val lsb: Int get() = pos.zext.toInt()
    inline val msb: Int get() = siz.zext.toInt()
}

abstract class RtInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rt: MipsRegister
) : AMipsInstruction(core, data, type, rt) {
    inline val cop0: ACOP0 get() = core.cop
    inline var vrt: Long
        get() = rt.value(core)
        set(value) = rt.value(core, value)
}

abstract class RtImmInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rt: MipsRegister,
        val imm: MipsImmediate
) : AMipsInstruction(core, data, type, rt, imm) {
    inline var vrt: Long
        get() = rt.value(core)
        set(value) = rt.value(core, value)
}

abstract class FtOffsetInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val ct: MipsRegister,
        val off: MipsDisplacement
) : AMipsInstruction(core, data, type, ct, off) {
    inline var vct: Long
        get() = ct.value(core)
        set(value) = ct.value(core, value)
    inline var memword: Long
        get() = off.value(core)
        set(value) = off.value(core, value)
    inline val address: Long get() = off.effectiveAddress(core)
    var dft : Long by DoubleRegister(1)
}

abstract class RtOffsetInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rt: MipsRegister,
        val off: MipsDisplacement
) : AMipsInstruction(core, data, type, rt, off) {
    inline var vrt: Long
        get() = rt.value(core)
        set(value) = rt.value(core, value)
    inline var memword: Long
        get() = off.value(core)
        set(value) = off.value(core, value)
    inline val address: Long get() = off.effectiveAddress(core)
    var dft : Long by DoubleRegister(1)
}

//abstract class RtRd(
//        core: MipsCore,
//        data: Long,
//        type: Type,
//        val rt: MipsRegister,
//        val rd: MipsRegister
//) : AMipsInstruction(core, data, type, rt, rd)

abstract class RtRdSelInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rt: MipsRegister,
        val rd: MipsRegister
) : AMipsInstruction(core, data, type, rt, rd) {
    inline var vrt: Long
        get() = rt.value(core)
        set(value) = rt.value(core, value)
    inline var vrd: Long
        get() = rd.value(core)
        set(value) = rd.value(core, value)
}

abstract class RtRsImmInsn(
        core: MipsCore,
        data: Long,
        type: Type,
        val rt: MipsRegister,
        val rs: MipsRegister,
        val imm: MipsImmediate
) : AMipsInstruction(core, data, type, rt, rs, imm) {
    inline var vrt: Long
        get() = rt.value(core)
        set(value) = rt.value(core, value)
    inline val vrs: Long get() = rs.value(core)
}

abstract class EmptyInsn(core: MipsCore, data: Long) : AMipsInstruction(core, data, VOID)