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
package ru.inforion.lab403.kopycat.cores.x86.instructions

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core



abstract class AX86Instruction(
        core: x86Core,
        type: Type,
        val opcode: ByteArray,
        val prefs: Prefixes,
        vararg operands: AOperand<x86Core>) : AInstruction<x86Core>(core, type, *operands) {
    final override val size: Int = opcode.size
    final override fun toString(): String {
        val spref = if (prefs.string != StringPrefix.NO) "${prefs.string.toString().toLowerCase()} " else ""
        val lpref = if (prefs.lock) "lock " else ""
        val address = if (ea != WRONGL) "[%08X]".format(ea.toInt()) else "[ UNDEF  ]"
        val opstr = "%-8s".format(opcode.hexlify())
        return "$address $opstr $lpref$spref$mnem ${joinToString()}"
    }

    open val cfChg = false
    open val pfChg = false
    open val afChg = false
    open val zfChg = false
    open val sfChg = false
    open val tfChg = false
    open val ifqChg = false
    open val dfChg = false
    open val ofChg = false
}