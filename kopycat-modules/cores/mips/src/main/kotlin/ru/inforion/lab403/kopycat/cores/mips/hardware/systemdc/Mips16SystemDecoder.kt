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
@file:Suppress("PrivatePropertyName", "UNUSED_ANONYMOUS_PARAMETER")

package ru.inforion.lab403.kopycat.cores.mips.hardware.systemdc

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.common.InstructionTable
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.arith.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.branch.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.coproc.di
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.coproc.ei
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.coproc.mfc0
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.memory.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.misc.`break`
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.misc.sdbbp
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.move.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.svrs.restore
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.svrs.restoreExt
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.svrs.save
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.svrs.saveExt
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.mips16.*
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.io.Serializable

/**
 * Created by shiftdj on 18.06.2021.
 */

class Mips16SystemDecoder(core: MipsCore): Serializable {

    private val jald = JALaJALX(core, ::jal)
    private val jalxd = JALaJALX(core, ::jalx)
    private val slld = SHIFT(core, ::sll, ::sllExt)
    private val srld = SHIFT(core, ::srl, ::srlExt)
    private val srad = SHIFT(core, ::sra)
    private val addiud = RRI_A(core, ::addiu, ::addiuExt)
    private val restored = I8_SVRS(core, ::restore, ::restoreExt)
    private val saved = I8_SVRS(core, ::save, ::saveExt)
    private val bteqzd = I8(core, ::bteqz, ::bteqzExt)
    private val btnezd = I8(core, ::btnez, ::btnezExt)
    private val swraspd = I8(core, ::swrasp, ::swraspExt)
    private val adjspd = I8(core, ::adjsp)
    private val mov32rd = I8_MOV32R(core, ::move)
    private val movr32d = I8_MOVR32(core, ::move)
    private val addud = RRR(core, ::addu)
    private val subud = RRR(core, ::subu)
    private val jrrxd = JALRC(core, ::jrrx)
    private val jrrad = JALRC(core, ::jrra)
    private val jalrd = JALRC(core, ::jalr)
    private val jrcrxd = JALRC(core, ::jrcrx)
    private val jrcrad = JALRC(core, ::jrcra)
    private val jalrcd = JALRC(core, ::jalrc)
    private val zebd = CNVT(core, ::zeb)
    private val zehd = CNVT(core, ::zeh)
    private val sebd = CNVT(core, ::seb)
    private val sehd = CNVT(core, ::seh)
    private val sdbbpd = SDBBP(core, ::sdbbp)
    private val sltd = RR(core, ::slt)
    private val sltud = RR(core, ::sltu)
    private val sllvd = RR(core, ::sllv)
    private val breakd = SDBBP(core, ::`break`)
    private val srlvd = RR(core, ::srlv)
    private val sravd = RR(core, ::srav)
    private val cmpd = RR(core, ::cmp)
    private val negd = RR(core, ::neg)
    private val andd = RR(core, ::and)
    private val ord = RR(core, ::or)
    private val xord = RR(core, ::xor)
    private val notd = RR(core, ::not)
    private val mfhid = RR(core, ::mfhi)
    private val mflod = RR(core, ::mflo)
    private val multd = RR(core, ::mult)
    private val multud = RR(core, ::multu)
    private val divd = RR(core, ::div)
    private val divud = RR(core, ::divu)
    private val addiuspd = RI(core, ::addiusp)
    private val addiupcd = RI(core, ::addiupc)
    private val bd = I(core, ::b)
    private val beqzd = RI(core, ::beqz, ::beqzExt)
    private val bnezd = RI(core, ::bnez, ::bnezExt)
    private val addiu8d = RI(core, ::addiu8, ::addiu8Ext)
    private val sltid = RI(core, ::slti)
    private val sltiud = RI(core, ::sltiu, ::sltiuExt)
    private val lid = RI(core, ::li, ::liExt)
    private val cmpid = RI(core, ::cmpi, ::cmpiExt)
    private val lbd = RRI(core, ::lb)
    private val lhd = RRI(core, ::lh)
    private val lwspd = RI(core, ::lwsp, ::lwspExt)
    private val lwd = RRI(core, ::lw, ::lwExt)
    private val lbud = RRI(core, ::lbu, ::lbuExt)
    private val lhud = RRI(core, ::lhu, ::lhuExt)
    private val lwpcd = RI(core, ::lwpc, ::lwpcExt)
    private val sbd = RRI(core, ::sb, ::sbExt)
    private val shd = RRI(core, ::sh, ::shExt)
    private val swspd = RI(core, ::swsp, ::swspExt)
    private val swd = RRI(core, ::sw, ::swExt)

    private val mfc0d = MOVR32_EXT(core, ::mfc0)
    private val mtc0d = null //MOVR32_EXT core, mtc0
    private val dxed = MOVR32DE_EXT(core, constructThird = ::di)
    private val exed = MOVR32DE_EXT(core, constructThird = ::ei)

    private val luid = RI(core, constructHigh = ::lui)
    private val orid = RI(core, constructHigh = ::ori)
    private val andid = RI(core, constructHigh = ::andi)
    private val xorid = RI(core, constructHigh = ::xori)

    private val insd = SLL_EXT(core, ::ins)
    private val extd = SLL_EXT(core, ::ext)
    private val rdhwrd = null
    private val ehbd = SLL_EXT(core, ::ehb)
    private val syncd = SLL_EXT(core, ::sync)
    private val paused = SLL_EXT(core, ::pause)

    private val movzd = SRL_EXT(core, ::movz)
    private val movnd = SRL_EXT(core, ::movn)
    private val movtzd = SRL_EXT(core, ::movtz)
    private val movtnd = SRL_EXT(core, ::movtn)

    object Extend : ITableEntry
    object ADDIUSP : ITableEntry
    object LI : ITableEntry
    object SWSP : ITableEntry
    object LWSP : ITableEntry
    object MOVR32 : ITableEntry
    object SLL : ITableEntry
    object SRL : ITableEntry


    private val e_jalx = InstructionTable(
            1, 2,
            { data: ULong -> 0u },
            { data: ULong -> data[10] },
            jald, jalxd
    )

    private val e_shift = InstructionTable(
            1, 4,
            { data: ULong -> 0u },
            { data: ULong -> data[1..0] },
            SLL, null, SRL, srad
    )

    private val e_rri_a = InstructionTable(
            1, 2,
            { data: ULong -> 0u },
            { data: ULong -> data[10] },
            addiud, null
    )

    private val e_svrs = InstructionTable(
            1, 2,
            { data: ULong -> 0u },
            { data: ULong -> data[7] },
            restored, saved
    )

    private val e_i8 = InstructionTable(
            1, 8,
            { data: ULong -> 0u },
            { data: ULong -> data[10..8] },
            bteqzd, btnezd, swraspd, adjspd, e_svrs, mov32rd, null, MOVR32
    )

    private val e_rrr = InstructionTable(
            1, 4,
            { data: ULong -> 0u },
            { data: ULong -> data[1..0] },
            null, addud, null, subud
    )

    private val e_jalrc = InstructionTable(
            1, 8,
            { data: ULong -> 0u },
            { data: ULong -> data[7..5] },
            jrrxd, jrrad, jalrd, null, jrcrxd, jrcrad, jalrcd, null
    )

    private val e_cnvt = InstructionTable(
            1, 8,
            { data: ULong -> 0u },
            { data: ULong -> data[7..5] },
            zebd, zehd, null, null, sebd, sehd, null, null
    )

    private val e_rr = InstructionTable(
            4, 8,
            { data: ULong -> data[4..3] },
            { data: ULong -> data[2..0] },
            e_jalrc,    sdbbpd,  sltd,    sltud,   sllvd,   breakd,    srlvd,   sravd,
            null,       null,   cmpd,    negd,    andd,    ord,         xord,    notd,
            mfhid,       e_cnvt, mflod,   null,   null,   null,       null,   null,
            multd,       multud,  divd,    divud,   null,   null,       null,   null
    )


//    private val e_extend  = InstructionTable(
//        4, 8,
//        { data: Long -> data[15..14] },
//        { data: Long -> data[13..11] },
//        null,    null,    null,      null, null,   null,   null,    null,
//        null,    null,     null,   null,  null,   null,     null,       null,
//        null,         null,         null,   null,     null,    null,    null,       null,
//        null         null,         null,   null,     null,  null,   null,   null
//    )


    private val e_opcoded = InstructionTable(
            4, 8,
            { data: ULong -> data[15..14] },
            { data: ULong -> data[13..11] },
            ADDIUSP,    addiupcd,    bd,      e_jalx, beqzd,   bnezd,   e_shift,    null,
            e_rri_a,    addiu8d,     sltid,   sltiud,  e_i8,   LI,     cmpid,       null,
            lbd,         lhd,         LWSP,   lwd,     lbud,    lhud,    lwpcd,       null,
            sbd,         shd,         SWSP,   swd,     e_rrr,  e_rr,   Extend,   null
    )


    private val stub = null

    private val e_extended_addiusp: InstructionTable get() = TODO("Not implemented")

    private val e_extended_swsp = InstructionTable(
            1, 8,
            { data: ULong -> 0u },
            { data: ULong -> data[7..5] },
            swspd, stub, stub, stub, stub, stub, stub, stub
    )

    private val e_extended_lwsp = InstructionTable(
            1, 8,
            { data: ULong -> 0u },
            { data: ULong -> data[7..5] },
            lwspd, stub, stub, stub, stub, stub, stub, stub
    )

    private val e_extended_sll = InstructionTable(
            1, 8,
            { data: ULong -> 0u },
            { data: ULong -> data[4..2] },
            slld, insd, extd, rdhwrd, ehbd, syncd, paused, null
    )

    private val e_extended_srl = InstructionTable(
            1, 8,
            { data: ULong -> 0u },
            { data: ULong -> data[4..2] },
            srld, movzd, movnd, null, null, movtzd, movtnd, null
    )

    private val e_extended_movr32 = InstructionTable(
            1, 4,
            { data: ULong -> 0u },
            { data: ULong -> data[1..0] },
            mfc0d, mtc0d, dxed, exed
    )

    private val e_extended_li = InstructionTable(
            1, 8,
            { data: ULong -> 0u },
            { data: ULong -> data[23..21] },
            lid, luid, orid, andid, xorid, null, null, null
    )

    fun decode(data: ULong, pc: ULong): AMipsInstruction {
        val stage1 = e_opcoded.lookup(data)

        val stage2 = when (stage1) {
            is ADDIUSP -> addiuspd
            is LI -> lid
            is SWSP -> swspd
            is LWSP -> lwspd
            is MOVR32 -> movr32d
            is SLL -> slld
            is SRL -> srld
            is Extend -> e_opcoded.lookup(data[31..16])
            else -> stage1
        }

        val stage3 = when (stage2) {
            is ADDIUSP -> e_extended_addiusp.lookup(data)
            is LI -> e_extended_li.lookup(data)
            is SWSP -> e_extended_swsp.lookup(data)
            is LWSP -> e_extended_lwsp.lookup(data)
            is MOVR32 -> e_extended_movr32.lookup(data)
            is SLL -> e_extended_sll.lookup(data)
            is SRL -> e_extended_srl.lookup(data)
            else -> stage2
        } ?: throw DecoderException(data, pc)

        return (stage3 as ADecoder).decode(data).also { it.ea = pc }
    }
}