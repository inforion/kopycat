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
package ru.inforion.lab403.kopycat.cores.x86

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.*
import ru.inforion.lab403.kopycat.interfaces.outb
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import kotlin.math.absoluteValue


object x86utils {
    fun bitBase(
            core: x86Core,
            base: AOperand<x86Core>,
            offset: AOperand<x86Core>,
            prefs: Prefixes
    ): Pair<AOperand<x86Core>, Int> {
        val a2 = offset.value(core)
        val a1: AOperand<x86Core>
        val bitpos: Int
        if (base is x86Register) {
            a1 = base
            bitpos = (a2 % prefs.opsize.bits.uint).int
        } else if (base is x86Displacement || base is x86Memory || base is x86Phrase) {
            val address = base.effectiveAddress(core) // get start pointer of memory
            val byteOffset = a2 / 8u // get byte-offset from start pointer (can be positive or negative)
            bitpos = (a2.int.absoluteValue % 8)  // bit offset in selected byte
            // TODO: always DWORD?
            a1 = x86Memory(BYTE, prefs.addrsize, address + byteOffset, base.ssr) // create fake operand
        } else {
            throw GeneralException("First operand bust be Register, Memory or Displacement. ")
        }

        return a1 to bitpos
    }

    fun isWithinCodeSegmentLimits(ea: ULong): Boolean = true

    fun getSegmentSelector(dev: x86Core, operand: AOperand<x86Core>): ULong = when (operand) {
        is x86Far -> operand.ss
        is x86Displacement -> operand.ssr.value(dev)
        else -> throw GeneralException("Bogus decoded jmp")
    }

    fun push(core: x86Core, value: ULong, dtyp: Datatype, prefs: Prefixes, isSSR: Boolean = false) {
        val sp = core.cpu.regs.gpr(x86GPR.RSP, prefs.addrsize)

        require(prefs.addrsize != WORD || dtyp != QWORD) { "Wrong mode" }
        require(dtyp != BYTE) { "Byte push isn't allowed" }

        // Do not subtract right away; write() may cause page fault
        core.write(sp.value - dtyp.bytes.uint, core.cpu.sregs.ss.id, dtyp.bytes, value)
        sp.value -= dtyp.bytes.uint
    }

    fun pop(core: x86Core, dtyp: Datatype, prefs: Prefixes, offset: ULong = 0u, isSSR: Boolean = false): ULong {
        val sp = core.cpu.regs.gpr(x86GPR.RSP, prefs.addrsize)

        require((prefs.addrsize != QWORD || dtyp != DWORD) ||
                    (prefs.addrsize != DWORD || dtyp != QWORD) ||
                    (prefs.addrsize != WORD || dtyp != QWORD)) { "Wrong mode" }

        require(dtyp != BYTE) { "Byte pop isn't allowed" }

        val data = core.read(sp.value, core.cpu.sregs.ss.id, dtyp.bytes)

        sp.value += dtyp.bytes.uint + offset
        return data
    }

    fun modeToDatatype(core: x86Core) = modeToDatatype(core.cpu.mode)

    fun modeToDatatype(mode: x86CPU.Mode) = when (mode) {
        x86CPU.Mode.R16 -> WORD
        x86CPU.Mode.R32 -> DWORD
        x86CPU.Mode.R64 -> QWORD
    }
}

fun x86Core.fillNops(range: ULongRange) {
    range.forEach { ea ->
        core.outb(ea, 0x90uL)
    }
}

fun x86Core.fillNops(ea: ULong, size: ULong) {
    this.fillNops(ea until ea + size)
}