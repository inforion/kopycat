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
package ru.inforion.lab403.kopycat.cores.x86.hardware

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR.*
import ru.inforion.lab403.kopycat.cores.x86.operands.x86MMXRegister
import ru.inforion.lab403.kopycat.cores.x86.operands.x86XMMRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by shiftdj on 05.07.2021.
 */

abstract class ADecodable(val core: x86Core) {

    fun gpr(reg: x86GPR, dtype: Datatype, alt: Boolean = false) = core.cpu.regs.gpr(reg, dtype, alt).toOperand()

    fun gpr(index: Int, dtype: Datatype, alt: Boolean = false) = gpr(x86GPR.byIndex(index), dtype, alt)

    fun gprr(index: Int, rexB: Boolean, dtype: Datatype, alt: Boolean = false) =
        gpr(index or (rexB.int shl 3), dtype, alt)

    fun gprr8(index: Int, rexB: Boolean, alt: Boolean) = gprr(index, rexB, BYTE, alt)

    fun creg(reg: Int) = core.cpu.cregs[reg].toOperand()
    fun sreg(reg: Int) = core.cpu.sregs[reg].toOperand()
    fun dreg(reg: Int) = core.cpu.dregs[reg].toOperand()

    fun mmx(reg: Int) = x86MMXRegister(reg)
    fun xmm(reg: Int) = x86XMMRegister(reg)

    val al get() = core.cpu.regs.al.toOperand()
    val cl get() = core.cpu.regs.cl.toOperand()

    val ax get() = core.cpu.regs.ax.toOperand()
    val bx get() = core.cpu.regs.bx.toOperand()
    val dx get() = core.cpu.regs.dx.toOperand()
    val bp get() = core.cpu.regs.bp.toOperand()
    val si get() = core.cpu.regs.si.toOperand()
    val di get() = core.cpu.regs.di.toOperand()

    val ebp get() = core.cpu.regs.ebp.toOperand()

    val es get() = core.cpu.sregs.es.toOperand()
    val cs get() = core.cpu.sregs.cs.toOperand()
    val ss get() = core.cpu.sregs.ss.toOperand()
    val ds get() = core.cpu.sregs.ds.toOperand()
    val fs get() = core.cpu.sregs.fs.toOperand()
    val gs get() = core.cpu.sregs.gs.toOperand()

    fun none(dtype: Datatype) = gpr(NONE, dtype)

    fun xsi(dtype: Datatype) = gpr(RSI, dtype)
    fun xdi(dtype: Datatype) = gpr(RDI, dtype)
    fun xdx(dtype: Datatype) = gpr(RDX, dtype)
    fun xax(dtype: Datatype) = gpr(RAX, dtype)

    fun xip(dtype: Datatype) = gpr(RIP, dtype)
}