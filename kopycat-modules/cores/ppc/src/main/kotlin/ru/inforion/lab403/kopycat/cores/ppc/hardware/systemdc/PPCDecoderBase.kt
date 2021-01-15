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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.*
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.InstructionTable
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.PatternTable
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.arithmInt.*
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.branch.bcctrx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.branch.bclrx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.branch.bcx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.branch.bx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.compInt.cmp
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.compInt.cmpi
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.compInt.cmpl
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.compInt.cmpli
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.instCache.icbi
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.ldstReverse.lhbrx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.ldstReverse.lwbrx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.ldstReverse.sthbrx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.ldstReverse.stwbrx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt.*
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicCondRegs.*
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicInt.*
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.memBarier.sync
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.memSync.isync
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.memSync.lwarx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.procCtrl.*
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.rotateInt.rlwimix
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.rotateInt.rlwinmx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.rotateInt.rlwnmx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.shiftInt.slwx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.shiftInt.srawix
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.shiftInt.srawx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.shiftInt.srwx
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.storageCtrl.dcbst
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.storeInt.*
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.sysLink.sc
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.tlbmanage.tlbsync
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.trap.twi
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


class PPCDecoderBase(core: PPCCore) : APPCSystemDecoder(core) {

    override val name: String = "PowerPC Base Decoder"

    private val bxDc = FormI(core, ::bx)

    private val bcxDc = FormB(core, ::bcx)

    private val scDc = FormSC(core, ::sc)

    private val addiDc = FormD(core, ::addi)
    private val addicDc = FormD(core, ::addic)
    private val addicdotDc = FormD(core, ::addicdot)
    private val addisDc = FormD(core, ::addis)
    private val andidotDc = FormD(core, ::andidot)
    private val andisdotDc = FormD(core, ::andisdot)
    private val cmpiDc = FormD(core, ::cmpi)
    private val cmpliDc = FormD(core, ::cmpli)
    private val lbzDc = FormD(core, ::lbz)
    private val lbzuDc = FormD(core, ::lbzu)
    private val lhaDc = FormD(core, ::lha)
    private val lhauDc = FormD(core, ::lhau)
    private val lhzDc = FormD(core, ::lhz)
    private val lhzuDc = FormD(core, ::lhzu)
    private val lmwDc = FormD(core, ::lmw)
    private val lwzDc = FormD(core, ::lwz)
    private val lwzuDc = FormD(core, ::lwzu)
    private val mulliDc = FormD(core, ::mulli)
    private val oriDc = FormD(core, ::ori)
    private val orisDc = FormD(core, ::oris)
    private val stbDc = FormD(core, ::stb)
    private val stbuDc = FormD(core, ::stbu)
    private val sthDc = FormD(core, ::sth)
    private val sthuDc = FormD(core, ::sthu)
    private val stmwDc = FormD(core, ::stmw)
    private val stwDc = FormD(core, ::stw)
    private val stwuDc = FormD(core, ::stwu)
    private val subficDc = FormD(core, ::subfic)
    private val twiDc = FormD(core, ::twi)
    private val xoriDc = FormD(core, ::xori)
    private val xorisDc = FormD(core, ::xoris)

    private val andxDc = FormX(core, ::andx)
    private val andcxDc = FormX(core, ::andcx)
    private val cmpDc = FormX(core, ::cmp)
    private val cmpbDc = FormX(core, ::cmpb)
    private val cmplDc = FormX(core, ::cmpl)
    private val cntlzwxDc = FormX(core, ::cntlzwx)
    private val dcbfDc = null //FormX(core, ::dcbf)
    private val dcbstDc = FormX(core, ::dcbst)
    private val dcbtDc = null //FormX(core, ::dcbt)
    private val dcbtstDc = null //FormX(core, ::dcbtst)
    private val dcbzDc = null //FormX(core, ::dcbz)
    private val eqvxDc = FormX(core, ::eqvx)
    private val extsbxDc = FormX(core, ::extsbx)
    private val extshxDc = FormX(core, ::extshx)
    private val icbiDc = FormX(core, ::icbi)
    private val lbzuxDc = FormX(core, ::lbzux)
    private val lbzxDc = FormX(core, ::lbzx)
    private val lhauxDc = FormX(core, ::lhaux)
    private val lhaxDc = FormX(core, ::lhax)
    private val lhbrxDc = FormX(core, ::lhbrx)
    private val lhzuxDc = FormX(core, ::lhzux)
    private val lhzxDc = FormX(core, ::lhzx)
    private val lwarxDc = FormX(core, ::lwarx)
    private val lwbrxDc = FormX(core, ::lwbrx)
    private val lwzuxDc = FormX(core, ::lwzux)
    private val lwzxDc = FormX(core, ::lwzx)
    private val mfmsrDc = FormX(core, ::mfmsr)
    private val nandxDc = FormX(core, ::nandx)
    private val norxDc = FormX(core, ::norx)
    private val orxDc = FormX(core, ::orx)
    private val orcxDc = FormX(core, ::orcx)
    private val prtywDc = FormX(core, ::prtyw)
    private val slwxDc = FormX(core, ::slwx)
    private val srawxDc = FormX(core, ::srawx)
    private val srawixDc = FormX(core, ::srawix)
    private val srwxDc = FormX(core, ::srwx)
    private val stbuxDc = FormX(core, ::stbux)
    private val stbxDc = FormX(core, ::stbx)
    private val sthbrxDc = FormX(core, ::sthbrx)
    private val sthuxDc = FormX(core, ::sthux)
    private val sthxDc = FormX(core, ::sthx)
    private val stwbrxDc = FormX(core, ::stwbrx)
    private val stwcxdotDc = null //FormX(core, ::stwcxdot)
    private val stwuxDc = FormX(core, ::stwux)
    private val stwxDc = FormX(core, ::stwx)
    private val syncDc = FormX(core, ::sync)
    private val tlbsyncDc = FormX(core, ::tlbsync)
    private val twDc = null //FormX(core, ::tw)
    private val xorxDc = FormX(core, ::xorx)

    private val bcctrxDc = FormXL(core, ::bcctrx)
    private val bclrxDc = FormXL(core, ::bclrx)
    private val crandDc = FormXL(core, ::crand)
    private val crandcDc = FormXL(core, ::crandc)
    private val creqvDc = FormXL(core, ::creqv)
    private val crnandDc = FormXL(core, ::crnand)
    private val crnorDc = FormXL(core, ::crnor)
    private val crorDc = FormXL(core, ::cror)
    private val crorcDc = FormXL(core, ::crorc)
    private val crxorDc = FormXL(core, ::crxor)
    private val isyncDc = FormXL(core, ::isync)
    private val mcrfDc = FormXL(core, ::mcrf)

    private val mfsprDc = FormXFX(core, ::mfspr)
    private val mtsprDc = FormXFX(core, ::mtspr)
    private val mfcrDc = FormXFX(core, ::mfcr)
    private val mtcrfDc = FormXFX(core, ::mtcrf)

    private val addxDc = FormXO(core, ::addx)
    private val addcxDc = FormXO(core, ::addcx)
    private val addexDc = FormXO(core, ::addex)
    private val addmexDc = FormXO(core, ::addmex)
    private val addzexDc = FormXO(core, ::addzex)
    private val divwxDc = FormXO(core, ::divwx)
    private val divwuxDc = FormXO(core, ::divwux)
    private val mulhwxDc = FormXO(core, ::mulhwx)
    private val mulhwuxDc = FormXO(core, ::mulhwux)
    private val mullwxDc = FormXO(core, ::mullwx)
    private val negxDc = FormXO(core, ::negx)
    private val subfxDc = FormXO(core, ::subfx)
    private val subfcxDc = FormXO(core, ::subfcx)
    private val subfexDc = FormXO(core, ::subfex)
    private val subfmexDc = FormXO(core, ::subfmex)
    private val subfzexDc = FormXO(core, ::subfzex)

    private val rlwimixDc = FormM(core, ::rlwimix)
    private val rlwinmxDc = FormM(core, ::rlwinmx)
    private val rlwnmxDc = FormM(core, ::rlwnmx)

    private val iselDc = FormA(core, ::isel)


    override val group13 = PatternTable("Group of opcode 13",
            arrayOf(10..0),
            arrayOf("00000000000" to mcrfDc,
                    "0000010000x" to bclrxDc,
                    "00001000010" to crnorDc,
                    "00100000010" to crandcDc,
                    "00100101100" to isyncDc,
                    "00110000010" to crxorDc,
                    "00111000010" to crnandDc,
                    "01000000010" to crandDc,
                    "01001000010" to creqvDc,
                    "01101000010" to crorcDc,
                    "01110000010" to crorDc,
                    "1000010000x" to bcctrxDc))

    override val group31 = PatternTable("Group of opcode 31",
            arrayOf(10..0),
            arrayOf("00000000000" to cmpDc,
                    "00000001000" to twDc,
                    "0000001000x" to subfcxDc,
                    "0000001010x" to addcxDc,
                    "0000001011x" to mulhwuxDc,
                    "00000100110" to mfcrDc,
                    "00000101000" to lwarxDc,
                    "00000101110" to lwzxDc,
                    "0000011000x" to slwxDc,
                    "0000011010x" to cntlzwxDc,
                    "0000011100x" to andxDc,
                    "00001000000" to cmplDc,
                    "0000101000x" to subfxDc,
                    "00001101100" to dcbstDc,
                    "00001101110" to lwzuxDc,
                    "0000111100x" to andcxDc,
                    "0001001011x" to mulhwxDc,
                    "00010100110" to mfmsrDc,
                    "00010101100" to dcbfDc,
                    "00010101110" to lbzxDc,
                    "0001101000x" to negxDc,
                    "00011101110" to lbzuxDc,
                    "0001111100x" to norxDc,
                    "0010001000x" to subfexDc,
                    "0010001010x" to addexDc,
                    "00100100000" to mtcrfDc,
                    "00100101101" to stwcxdotDc,
                    "00100101110" to stwxDc,
                    "00100110100" to prtywDc,
                    "00101101110" to stwuxDc,
                    "0011001000x" to subfzexDc,
                    "0011001010x" to addzexDc,
                    //"00110100100" to mtsrDc,
                    "00110101110" to stbxDc,
                    "0011101000x" to subfmexDc,
                    "0011101010x" to addmexDc,
                    "0011101011x" to mullwxDc,
                    //"00111100100" to mtsrinDc,
                    "00111101100" to dcbtstDc,
                    "00111101110" to stbuxDc,
                    "0100001010x" to addxDc,
                    "01000101100" to dcbtDc,
                    "01000101110" to lhzxDc,
                    "0100011100x" to eqvxDc,
                    //"01001100100" to tlbieDc,
                    //"01001101100" to eciwxDc,
                    "01001101110" to lhzuxDc,
                    "0100111100x" to xorxDc,
                    "01010100110" to mfsprDc,
                    "01010101110" to lhaxDc,
                    //"01011100100" to tlbiaDc,
                    "01011101110" to lhauxDc,
                    "01100101110" to sthxDc,
                    "0110011100x" to orcxDc,
                    //"01101101100" to ecowxDc,
                    "01101101110" to sthuxDc,
                    "0110111100x" to orxDc,
                    "0111001011x" to divwuxDc,
                    "01110100110" to mtsprDc,
                    "0111011100x" to nandxDc,
                    "0111101011x" to divwxDc,
                    "01111111000" to cmpbDc,
                    //"10000101010" to lswxDc,
                    "10000101100" to lwbrxDc,
                    "1000011000x" to srwxDc,
                    "10001101100" to tlbsyncDc,
                    //"10010100110" to mfsrDc,
                    //"10010101010" to lswiDc,
                    "10010101100" to syncDc,
                    //"10100100110" to mfsrinDc,
                    //"10100101010" to stswxDc,
                    "10100101100" to stwbrxDc,
                    //"10110101010" to stswiDc,
                    "11000101100" to lhbrxDc,
                    "1100011000x" to srawxDc,
                    "1100111000x" to srawixDc,
                    "11100101100" to sthbrxDc,
                    "1110011010x" to extshxDc,
                    "1110111010x" to extsbxDc,
                    "11110101100" to icbiDc,
                    "11111101100" to dcbzDc,
                    "xxxxx01111x" to iselDc))

    override val baseOpcode = InstructionTable(
            8, 8,
            { data: Long -> data[31..29] },
            { data: Long -> data[28..26] },
            /////               0,0,0       0,0,1       0,1,0       0,1,1       1,0,0       1,0,1       1,1,0       1,1,1
            /*0,0,0*/  null,       null,       null,       twiDc,      null,       null,       null,       mulliDc,
            /*1,0,0*/           subficDc,   null,       cmpliDc,    cmpiDc,     addicDc,    addicdotDc, addiDc,     addisDc,
            /*0,1,0*/           bcxDc,      scDc,       bxDc,       null,       rlwimixDc,  rlwinmxDc,  null,       rlwnmxDc,
            /*1,1,0*/           oriDc,      orisDc,     xoriDc,     xorisDc,    andidotDc,  andisdotDc, null,       null,
            /*0,0,1*/           lwzDc,      lwzuDc,     lbzDc,      lbzuDc,     stwDc,      stwuDc,     stbDc,      stbuDc,
            /*1,0,1*/           lhzDc,      lhzuDc,     lhaDc,      lhauDc,     sthDc,      sthuDc,     lmwDc,      stmwDc,
            /*0,1,1*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*1,1,1*/           null,       null,       null,       null,       null,       null,       null,       null
    )

}
