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
package ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc

import ru.inforion.lab403.common.extensions.dictionary
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders.FormatI
import ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders.FormatII
import ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders.FormatJI
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.arithm.*
import ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.branch.Call
import ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.branch.Jc
import ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.branch.Reti
import ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.common.Push
import ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.common.Swpb
import ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.common.Sxt
import ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.logic.*
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class MSP430SystemDecoder(val core: MSP430Core) : ICoreUnit {
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        @Transient val log = logger(FINE)
    }

    override val name: String = "MSP430 System Decoder"
    private val cache = dictionary<ULong, AMSP430Instruction>(1024*1024)

    //Soa - Single-operand arithmetic
    private val rrcSoaDc = FormatII(core, ::Rrc)
    private val swpbSoaDc = FormatII(core, ::Swpb)
    private val rraSoaDc = FormatII(core, ::Rra)
    private val sxtSoaDc = FormatII(core, ::Sxt)
    private val pushSoaDc = FormatII(core, ::Push)
    private val callSoaDc = FormatII(core, ::Call)
    private val retiSoaDc = FormatII(core, ::Reti)

    //Cj  - Conditional jump
    private val jcCjDc = FormatJI(core, ::Jc)

    //Toa - Two-operand arithmetic
    private val movToaDc = FormatI(core, ::Mov)
    private val addToaDc = FormatI(core, ::Add)
    private val addcToaDc = FormatI(core, ::Addc)
    private val subcToaDc = FormatI(core, ::Subc)
    private val subToaDc = FormatI(core, ::Sub)
    private val cmpToaDc = FormatI(core, ::Cmp)
    private val daddToaDc = FormatI(core, ::Dadd)
    private val bitToaDc = FormatI(core, ::Bit)
    private val bicToaDc = FormatI(core, ::Bic)
    private val bisToaDc = FormatI(core, ::Bis)
    private val xorToaDc = FormatI(core, ::Xor)
    private val andToaDc = FormatI(core, ::And)

    private val soa_reti_opcode = InstructionTable(
            1, 2,
            { data: ULong -> 0u },
            { data: ULong -> if (data[6..0] == 0uL) 1u else 0u }, //RETI has constant signature
            /////        0               1
            /*0*/       null,   retiSoaDc
    )


    private val soa_1_opcode = InstructionTable(
            2, 2,
            { data: ULong -> data[9] },
            { data: ULong -> data[8] },
            /////        0           1
            /*0*/       swpbSoaDc,  sxtSoaDc,
            /*1*/       callSoaDc,  null
    )


    private val soa_0_opcode = InstructionTable(
            2, 2,
            { data: ULong -> data[9] },
            { data: ULong -> data[8] },
            /////        0           1
            /*0*/       rrcSoaDc,   rraSoaDc,
            /*1*/       pushSoaDc,  soa_reti_opcode
    )


    private val soa_opcode = InstructionTable(
            4, 2,
            { data: ULong -> data[7..6] },  //7th bit defines purpose of 6th
            { data: ULong -> if (data[11..10] == 0uL) 1u else 0u }, //SOA opcodes signature: 000100...
            /////        0               1
            /*0,0*/     null,           soa_0_opcode,
            /*0,1*/     null,           soa_0_opcode,
            /*1,0*/     null,           soa_1_opcode,
            /*1,1*/     null,           null
    )

    private val base_opcode = InstructionTable(
            4, 4,
            { data: ULong -> data[15..14] },
            { data: ULong -> data[13..12] },
            /////        0,0             0,1         1,0         1,1
            /*0,0*/     null,   soa_opcode, jcCjDc,     jcCjDc,
            /*0,1*/     movToaDc,       addToaDc,   addcToaDc,  subcToaDc,
            /*1,0*/     subToaDc,       cmpToaDc,   daddToaDc,  bitToaDc,
            /*1,1*/     bicToaDc,       bisToaDc,   xorToaDc,   andToaDc
    )

    fun fetch(where: ULong): ULong = core.fetch(where, 0, 8)

    fun decode(where: ULong): AMSP430Instruction {
        val data = fetch(where)
//        var insn = cache[data]  TODO! cache is disabled for easy debugging
//        if (insn != null) return insn
        val entry = base_opcode.lookup(data) ?: throw DecoderException(data, where)
        val insn = (entry as ADecoder<AMSP430Instruction>).decode(data)
//        cache[data] = insn    TODO! cache is disabled for easy debugging
        return insn
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        throw UnsupportedOperationException("not implemented")
    }
}