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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.interfaces.*


class Lidt(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "lidt"

    override fun execute() {
        val dtype = if (op1.dtyp == Datatype.DFWORD) Datatype.QWORD else Datatype.DWORD

        val limit = core.read(Datatype.WORD, op1.effectiveAddress(core), op1.ssr.reg)
        val base = core.read(dtype, op1.effectiveAddress(core) + Datatype.WORD.bytes.uint, op1.ssr.reg)
        when (prefs.opsize) {
            Datatype.QWORD -> {
                core.cop.idtr.limit = limit
                core.cop.idtr.base = base
            }
            Datatype.DWORD ->  {
                core.cop.idtr.limit = limit
                core.cop.idtr.base = base
            }
            Datatype.WORD -> {
                core.cop.idtr.limit = limit
                core.cop.idtr.base = base and 0xFFFFFFu
            }
            else -> throw GeneralException("Unknown operand size: ${prefs.opsize}")
        }

        log.info { "[0x${core.cpu.pc.hex}] IDTR changed ${core.cop.idtr}" }
    }
}