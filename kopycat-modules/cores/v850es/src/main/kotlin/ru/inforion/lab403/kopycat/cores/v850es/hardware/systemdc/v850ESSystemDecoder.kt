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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc

import gnu.trove.map.hash.THashMap
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.InstructionTable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders.*
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.arithm.*
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.bitman.Clr1
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.bitman.Not1
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.bitman.Set1
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.bitman.Tst1
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.branch.Bcond
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.branch.Jarl
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.branch.Jmp
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.debug.Dbret
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.debug.Dbtrap
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.load.*
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.logic.*
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.multiply.Mul
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.multiply.Mulh
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.multiply.Mulhi
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.multiply.Mulu
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.saturated.Satadd
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.saturated.Satsub
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.saturated.Satsubi
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special.*
import ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.store.*
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore
import java.util.logging.Level
import kotlin.collections.set


class v850ESSystemDecoder(val core: v850ESCore) : ICoreUnit {
    companion object {
        @Transient val log = logger(Level.FINE)
    }
    override val name: String = "v850ES System Decoder"
    private val cache = THashMap<Long, AV850ESInstruction>(1024*1024)

    // verified
    private val addRrDc = FormatI(core, ::Add)
    private val andRrDc = FormatI(core, ::And)
    private val cmpRrDc = FormatI(core, ::Cmp)
    private val dbtrapDc = FormatI(core, ::Dbtrap)
    private val divhRrDc = FormatI(core, ::Divh)
    private val jmpRDc = FormatI(core, ::Jmp)
    private val movRrDc = FormatI(core, ::Mov)
    private val mulhRrDc = FormatI(core, ::Mulh)
    private val nopDc = FormatI(core, ::Mov)                    // NOP command
    private val notRrDc = FormatI(core, ::Not)
    private val orRrDc = FormatI(core, ::Or)
    private val satsubrRrDc = FormatI(core, ::Satsub)           //not implemented
    private val subRrDc = FormatI(core, ::Sub)
    private val subrRrDc = FormatI(core, ::Subr)
    private val switchRDC = FormatI(core, ::Switch)
    private val sataddRrDC = FormatI(core, ::Satadd)            //not implemented
    private val sxbRDc = FormatI(core, ::Sxb)
    private val sxhRDc = FormatI(core, ::Sxh)
    private val tstRrDc = FormatI(core, ::Tst)
    private val xorRrDc = FormatI(core, ::Xor)
    private val zxbRDc = FormatI(core, ::Zxb)
    private val zxhRDc = FormatI(core, ::Zxh)

    // verified
    private val addIrDc = FormatII(core, ::Add, true)
    private val calltIDc = FormatII(core, ::Callt, false)
    private val cmpIrDc = FormatII(core, ::Cmp, true)
    private val movIrDc = FormatII(core, ::Mov, true)
    private val mulhIrDc = FormatII(core, ::Mulh, true)
    private val sarIrDc = FormatII(core, ::Sar, false)
    private val shrIrDc = FormatII(core, ::Shr, false)
    private val sataddIrDC = FormatII(core, ::Satadd, true)      //not implemented
    private val shlIrDc = FormatII(core, ::Shl, false)

    // verified
    private val bcondDDc = FormatIII(core, ::Bcond)

    // verified
    private val addiIrrDc = FormatVI(core, ::Addi, true)
    private val andiIrrDc = FormatVI(core, ::Andi, false)
    private val moveaIrrDc = FormatVI(core, ::Movea, true)
    private val movhiIrrDc = FormatVI(core, ::Movhi, false)
    private val mulhiIrrDc = FormatVI(core, ::Mulhi, false)
    private val oriIrrDc = FormatVI(core, ::Ori, false)
    private val satsubiIrrDC = FormatVI(core, ::Satsubi, true)  //not implemented
    private val xoriIrrDc = FormatVI(core, ::Xori, false)

    // verified
    private val clr1BdDc = FormatVIIIBits(core, ::Clr1)
    private val not1BdDc = FormatVIIIBits(core, ::Not1)
    private val set1BdDc = FormatVIIIBits(core, ::Set1)
    private val tst1BdDc = FormatVIIIBits(core, ::Tst1)

    // verified
    private val sasfCrDc = FormatIXCond(core, ::Sasf)        //not implemented
    private val setfCrDc = FormatIXCond(core, ::Setf)

    // verified
    private val clr1RrDc = FormatIXBits(core, ::Clr1)
    private val not1RrDc = FormatIXBits(core, ::Not1)
    private val set1RrDc = FormatIXBits(core, ::Set1)
    private val tst1RrDc = FormatIXBits(core, ::Tst1)

    private val ldsrRrDc = FormatIXCR(core, ::Ldsr, false)
    private val stsrRrDc = FormatIXCR(core, ::Stsr, true)

    private val sarRrDc = FormatIXRR(core, ::Sar)
    private val shlRrDc = FormatIXRR(core, ::Shl)
    private val shrRrDc = FormatIXRR(core, ::Shr)

    // verified
    private val ctretDc = FormatIXSys(core, ::Ctret)
    private val dbretDc = FormatIXSys(core, ::Dbret)
    private val diDc = FormatIXSys(core, ::Di)
    private val eiDc = FormatIXSys(core, ::Ei)
    private val haltDc = FormatIXSys(core, ::Halt)
    private val retiDc = FormatIXSys(core, ::Reti)
    private val trapVDc = FormatIXSys(core, ::Trap)

    // verified
    private val divRrDc = FormatIXRRR(core, ::Div)
    private val divuRrDc = FormatIXRRR(core, ::Divu)
    private val divhRrrDc = FormatIXRRR(core, ::Divh)
    private val divhuRrDc = FormatIXRRR(core, ::Divhu)
    private val mulIrrDc = FormatIXRRR(core, ::Mul)
    private val muluRrrDc = FormatIXRRR(core, ::Mulu)

    // verified
    private val mulRrrDc = FormatIXIRR(core, ::Mul)
    private val muluIrrDc = FormatIXIRR(core, ::Mulu)
    private val bswRrDc = FormatIXIRR(core, ::Bsw)   // immediate doesn't use. it's ok
    private val bshRrDc = FormatIXIRR(core, ::Bsh)   // immediate doesn't use. it's ok
    private val hswRrDc = FormatIXIRR(core, ::Hsw)   // immediate doesn't use. it's ok

    // verified
    private val ldbDrrDc = FormatVII(core, ::Ldb, BYTE, 16)
    private val ldbuDrrDc = FormatVII(core, ::Ldbu, BYTE, 5)
    private val ldhDrrDc = FormatVII(core, ::Ldh, WORD)
    private val ldhuDrrDc = FormatVII(core, ::Ldhu, WORD)
    private val ldwDrrDc = FormatVII(core, ::Ldw, DWORD)

    private val stbRdrDc = FormatVII(core, ::Stb, BYTE, 16)
    private val sthRdrDc = FormatVII(core, ::Sth, WORD)
    private val stwRdrDc = FormatVII(core, ::Stw, DWORD)

    // verified
    private val sldbuDrDc = FormatIV(core, ::Sldbu, BYTE, 3..0, 0)
    private val sldbDrDc = FormatIV(core, ::Sldb, BYTE, 6..0, 0)
    private val sldhuDrDc = FormatIV(core, ::Sldhu, WORD, 3..0, 0)
    private val sldhDrDc = FormatIV(core, ::Sldh, WORD, 6..0, 1)
    private val sldwDrDc = FormatIV(core, ::Sldw, DWORD, 6..1, 2)

    private val sstbRdDc = FormatIV(core, ::Sstb, BYTE, 6..0, 0)
    private val ssthRdDc = FormatIV(core, ::Ssth, WORD, 6..0, 1)
    private val sstwRdDc = FormatIV(core, ::Sstw, DWORD,6..1, 2)

    // verified
    private val cmovCrrrDc = FormatXI(core, ::Cmov)               //not implemented
    private val cmovCirrDc = FormatXII(core, ::Cmov)               //not implemented
    private val disposeIlrDc = FormatXIIID(core, ::Dispose)
    private val jarlDrDc = FormatV(core, ::Jarl)                  // JR is the same
    private val movIrDc32 = FormatVIM(core, ::Mov)
    private val prepareLiiDc = FormatXIIIP(core, ::Prepare)

    private val k_opcode = InstructionTable(
            2, 4,
            { data: Long -> data[17] },
            { data: Long -> data[22..21] },
            /////           0,0                 0,1                 1,0                 1,1
            /*0*/          divhRrrDc,          divhRrDc,           divRrDc,            divRrDc,
            /*1*/          divhuRrDc,          divhuRrDc,          divuRrDc,           divuRrDc
    )

    private val j_opcode = InstructionTable(
            2, 4,
            { data: Long -> data[17] },
            { data: Long -> data[22..21] },
            /////           0,0                 0,1                 1,0                 1,1
            /*0*/          sasfCrDc,           mulRrrDc,           mulIrrDc,           mulIrrDc,
            /*1*/          sasfCrDc,           muluRrrDc,          muluIrrDc,          muluIrrDc
    )

    private val i_opcode = InstructionTable(
            2, 2,
            { data: Long -> data[18] },
            { data: Long -> data[17] },
            /////           0                   1
            /*0*/          bswRrDc,            bshRrDc,
            /*1*/          hswRrDc,            null/*Undefined*/
    )

    private val h_opcode = InstructionTable(
            4, 2,
            { data: Long -> data[15..14] },
            { data: Long -> if(data[13..11] == 0L) 0 else 1},
            /////           0                   1
            /*0,0*/        diDc,               null, /*Undefined*/
            /*0,1*/        null, /*Undefined*/ null, /*Undefined*/
            /*1,0*/        eiDc,               null, /*Undefined*/
            /*1,1*/        null, /*Undefined*/ null  /*Undefined*/
    )

    private val g_opcode = InstructionTable(
            2, 2,
            { data: Long -> data[18] },
            { data: Long -> data[17] },
            /////           0                   1
            /*0,0*/        retiDc,             null,/*Undefined*/
            /*0,1*/        ctretDc,            dbretDc
    )

    private val f_opcode = InstructionTable(
            2, 2,
            { data: Long -> data[18] },
            { data: Long -> data[17] },
            /////           0                   1
            /*0,0*/        set1RrDc,           not1RrDc,
            /*0,1*/        clr1RrDc,           tst1RrDc
    )

    private val e_opcode = InstructionTable(
            16, 4,
            { data: Long -> data[26..23] },
            { data: Long -> data[22..21] },
            /////           0,0                 0,1                 1,0                 1,1
            /*0,0,0,0*/    setfCrDc,           ldsrRrDc,           stsrRrDc,           null,/*Undefined*/
            /*0,0,0,1*/    shrRrDc,            sarRrDc,            shlRrDc,            f_opcode,
            /*0,0,1,0*/    trapVDc,            haltDc,             g_opcode,           h_opcode,
            /*0,0,1,1*/    null,/*Undefined*/  null,/*Undefined*/  null,/*Undefined*/  null,/*Undefined*/
            /*0,1,0,0*/    j_opcode,           j_opcode,           j_opcode,           j_opcode,
            /*0,1,0,1*/    k_opcode,           k_opcode,           k_opcode,           k_opcode,
            /*0,1,1,0*/    cmovCirrDc,         cmovCrrrDc,         i_opcode,           null, /*Undefined*/
            /*0,1,1,1*/    null,               null,               null,               null,
            /*1,0,0,0*/    null,               null,               null,               null,
            /*1,0,0,1*/    null,               null,               null,               null,
            /*1,0,1,0*/    null,               null,               null,               null,
            /*1,0,1,1*/    null,               null,               null,               null,
            /*1,1,0,0*/    null,               null,               null,               null,
            /*1,1,0,1*/    null,               null,               null,               null,
            /*1,1,1,0*/    null,               null,               null,               null,
            /*1,1,1,1*/    null,               null,               null,               null
    )

    private val a_1_opcode = InstructionTable(
            3, 2,
            { data: Long -> if (data[15..11] == 0L) 0 else if (data[15..11] == 0x1FL) 1 else 2 },   // reg2
            { data: Long -> if(data[4..0] == 0L) 0 else 1},                                         // reg1
            /////           0                   1
            /*0*/          null,               switchRDC,/*switch*/
            /*1*/          dbtrapDc,/*dbtrap*/ divhRrDc,/*divh*/
            /*2*/          null,               divhRrDc /*divh*/
    )

    private val a_2_opcode = InstructionTable(
            2, 2,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },    // reg2
            { data: Long -> data[4]},
            /////           0                   1
            /*0*/          jmpRDc,/*jmp*/      jmpRDc,/*jmp*/
            /*1*/          sldbuDrDc,/*SLD.BU*/sldhuDrDc/*SLD.HU*/
    )

    private val a_3_opcode = InstructionTable(
            2, 4,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },    // reg2
            { data: Long -> data[6..5]},
            /////           0,0                 0,1                 1,0                 1,1
            /*0*/          zxbRDc,             sxbRDc,             zxhRDc,             sxhRDc,
            /*1*/          satsubrRrDc,        satsubrRrDc,        sataddRrDC,         mulhRrDc
    )

    private val a_4_opcode = InstructionTable(
            2, 2,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },// reg2
            { data: Long -> data[5]},
            /////           0                   1
            /*0*/          calltIDc,           calltIDc,
            /*1*/          movIrDc,            sataddIrDC
    )

    private val a_5_opcode = InstructionTable(
            2, 1,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },// reg2
            { data: Long -> 0 },
            /////           0
            /*0*/          null,
            /*1*/          mulhIrDc
    )

    private val a_6_opcode = InstructionTable(
            2, 1,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },// reg2
            { data: Long -> 0 },
            /////           0
            /*0*/          movIrDc32,
            /*1*/          moveaIrrDc
    )

    private val a_7_opcode = InstructionTable(
            2, 2,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },// reg2
            { data: Long -> data[5] },
            /////           0                   1
            /*0*/          disposeIlrDc,       disposeIlrDc,
            /*1*/          movhiIrrDc,         satsubiIrrDC
    )

    private val a_8_opcode = InstructionTable(
            2, 1,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },// reg2
            { data: Long -> 0 },
            /////           0
            /*0*/          null,/*Undefined*/
            /*1*/          mulhiIrrDc
    )

    private val a_9_opcode = InstructionTable(
            2, 2,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },// reg2
            { data: Long -> data[16] },
            /////           0                 1
            /*0*/          jarlDrDc,         prepareLiiDc,
            /*1*/          jarlDrDc,         ldbuDrrDc
    )

    private val a_10_opcode = InstructionTable(
            1, 2,
            { data: Long -> 0 },
            { data: Long -> if (data[15..11] == 0L && data[4..0] == 0L) 0 else 1},      // reg1
            /////           0                   1
            /*0*/          nopDc,/*Nop*/      movRrDc
    )

    private val ae_opcode = InstructionTable(
            2, 2,
            { data: Long -> if (data[15..11] == 0L) 0 else 1 },
            { data: Long -> data[16] },
            /////           0                   1
            /*0*/          e_opcode,           null,/*Undefined*/
            /*1*/          e_opcode,           ldhuDrrDc/*HERE LD.HU!!!*/
    )

    private val d_opcode = InstructionTable(
            2, 2,
            { data: Long -> data[15] },
            { data: Long -> data[14] },
            /////           0                   1
            /*0*/          set1BdDc,           not1BdDc,
            /*1*/          clr1BdDc,           tst1BdDc
    )

    private val c_opcode = InstructionTable(
            4, 2,
            { data: Long -> data[6..5] },
            { data: Long -> data[16] },
            /////           0                   1
            /*0,1*/        null,               null,
            /*1,1*/        ldhDrrDc,           ldwDrrDc,
            /*1,1*/        null,               null,
            /*1,1*/        sthRdrDc,           stwRdrDc
    )

    private val b_opcode = InstructionTable(
            1, 2,
            { data: Long -> 0 },
            { data: Long -> data[0] },
            /////           0                   1
            /*1,0,1,0*/    sldwDrDc,           sstwRdDc
    )

    private val a_opcode = InstructionTable(
            16, 4,
            { data: Long -> data[10..7] },
            { data: Long -> data[6..5] },
            /////           0,0                 0,1                 1,0                 1,1
            /*0,0,0,0*/    a_10_opcode,        notRrDc,            a_1_opcode,         a_2_opcode,
            /*0,0,0,1*/    a_3_opcode,         a_3_opcode,         a_3_opcode,         a_3_opcode,
            /*0,0,1,0*/    orRrDc,             xorRrDc,            andRrDc,            tstRrDc,
            /*0,0,1,1*/    subrRrDc,           subRrDc,            addRrDc,            cmpRrDc,
            /*0,1,0,0*/    a_4_opcode,         a_4_opcode,         addIrDc,            cmpIrDc,
            /*0,1,0,1*/    shrIrDc,            sarIrDc,            shlIrDc,            a_5_opcode,
            /*0,1,1,0*/    sldbDrDc,           sldbDrDc,           sldbDrDc,           sldbDrDc,
            /*0,1,1,1*/    sstbRdDc,           sstbRdDc,           sstbRdDc,           sstbRdDc,
            /*1,0,0,0*/    sldhDrDc,           sldhDrDc,           sldhDrDc,           sldhDrDc,
            /*1,0,0,1*/    ssthRdDc,           ssthRdDc,           ssthRdDc,           ssthRdDc,
            /*1,0,1,0*/    b_opcode,           b_opcode,           b_opcode,           b_opcode,
            /*1,0,1,1*/    bcondDDc,           bcondDDc,           bcondDDc,           bcondDDc,
            /*1,1,0,0*/    addiIrrDc,          a_6_opcode,         a_7_opcode,         a_7_opcode,
            /*1,1,0,1*/    oriIrrDc,           xoriIrrDc,          andiIrrDc,          a_8_opcode,
            /*1,1,1,0*/    ldbDrrDc,           c_opcode,           stbRdrDc,           c_opcode,
            /*1,1,1,1*/    a_9_opcode,         a_9_opcode,         d_opcode,           ae_opcode
    )

    fun fetch(where: Long): Long = core.fetch(where, 0, 8)

    fun decode(where: Long): AV850ESInstruction {
        val data = fetch(where)
        var insn = cache[data]
        if (insn != null) return insn
        val entry = a_opcode.lookup(data) ?: throw DecoderException(data, where)
        insn = (entry as ADecoder<AV850ESInstruction>).decode(data)
        cache[data] = insn
        return insn
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        throw UnsupportedOperationException("not implemented")
    }
}
