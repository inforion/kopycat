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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class GPRBank(core: x86Core) : ARegistersBank<x86Core, x86GPR>(core, x86GPR.values(), bits = 32) {
    override val name: String = "CPU General Purpose Registers"

    var eax by valueOf(x86Register.GPRDW.eax)
    var ecx by valueOf(x86Register.GPRDW.ecx)
    var ebx by valueOf(x86Register.GPRDW.ebx)
    var edx by valueOf(x86Register.GPRDW.edx)
    var esp by valueOf(x86Register.GPRDW.esp)
    var ebp by valueOf(x86Register.GPRDW.ebp)
    var esi by valueOf(x86Register.GPRDW.esi)
    var edi by valueOf(x86Register.GPRDW.edi)

    var ax by valueOf(x86Register.GPRW.ax)
    var cx by valueOf(x86Register.GPRW.cx)
    var bx by valueOf(x86Register.GPRW.bx)
    var dx by valueOf(x86Register.GPRW.dx)
    var sp by valueOf(x86Register.GPRW.sp)
    var bp by valueOf(x86Register.GPRW.bp)
    var si by valueOf(x86Register.GPRW.si)
    var di by valueOf(x86Register.GPRW.di)

    var al by valueOf(x86Register.GPRBL.al)
    var cl by valueOf(x86Register.GPRBL.cl)
    var bl by valueOf(x86Register.GPRBL.bl)
    var dl by valueOf(x86Register.GPRBL.dl)

    var ah by valueOf(x86Register.GPRBH.ah)
    var ch by valueOf(x86Register.GPRBH.ch)
    var bh by valueOf(x86Register.GPRBH.bh)
    var dh by valueOf(x86Register.GPRBH.dh)

    var eip by valueOf(x86Register.GPRDW.eip)
    var ip by valueOf(x86Register.GPRW.ip)

    var none by valueOf(x86Register.none)

    override fun reset() {
        super.reset()
        eip = 0xFFF0
    }
}