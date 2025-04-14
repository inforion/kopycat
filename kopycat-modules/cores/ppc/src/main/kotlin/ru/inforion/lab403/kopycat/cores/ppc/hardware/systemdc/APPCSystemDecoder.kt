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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc

import ru.inforion.lab403.common.extensions.dictionary
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.InstructionTable
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.PatternTable
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


abstract class APPCSystemDecoder(val core: PPCCore) : ICoreUnit {

    private val cache = dictionary<ULong, APPCInstruction>(1024 * 1024)

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        throw UnsupportedOperationException("not implemented")
    }

    companion object {
        @Transient val log = logger(FINE)
    }


    internal open val group13 = PatternTable("Group of opcode 13")
    internal open val group31 = PatternTable("Group of opcode 31")
    //InstructionTable - because it is faster
    internal open val baseOpcode = InstructionTable(
            8, 8,
            { data -> data[31..29] },
            { data -> data[28..26] }
    )

    private fun fetch(where: ULong) = core.fetch(where, 0, 4)

    fun decode(where: ULong): APPCInstruction {
        val data = fetch(where)
        //var insn = cache[data]  TODO! cache is disabled for easy debugging
        //if (insn != null) return insn
        val entry = baseOpcode.lookup(data, where) //?: throw DecoderException(data, core.cpu.pc)
        val insn = entry.decode(data)
        //println("${where.hex8}\t[${data.hex8}]\t$insn")
        //cache[data] = insn    TODO! cache is disabled for easy debugging
        return insn
    }
}
