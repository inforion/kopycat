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
@file:Suppress("PrivatePropertyName", "UNUSED_ANONYMOUS_PARAMETER")

package ru.inforion.lab403.kopycat.cores.mips.hardware.systemdc

import ru.inforion.lab403.common.extensions.dictionary
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.common.InstructionTable
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.STORE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.mips.Microarchitecture
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation.Control
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation.General
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.branch.bc2f
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.branch.bc2fl
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.branch.bc2t
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.branch.bc2tl
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.jtag.deret
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.jtag.sdbbp
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.memory.ldc2
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.memory.lwc2
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.memory.sdc2
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.memory.swc2
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.move.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.bitwise.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.control.nop
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.logical.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.mips.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.arith.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.branch.bc1f
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.branch.bc1fl
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.branch.bc1t
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.branch.bc1tl
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.convert.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.memory.ldc1
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.memory.lwc1
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.memory.sdc1
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.memory.swc1
import ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.move.*
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.io.Serializable
import kotlin.collections.set


class Mips32SystemDecoder(core: MipsCore): Serializable {

    private val stubd = object : ADecoder(core) {
        override fun decode(data: ULong) = object : AMipsInstruction(core, 0xFFFFFFFFu, Type.VOID) {
            override val mnem = "stub"
            override fun execute() = throw IllegalStateException("Instruction was not decoded properly and can't be executed")
        }
    }

    private val nopd = object : ADecoder(core) {
        override fun decode(data: ULong) = nop(core, data)
    }

    // e_bc2
    private val  bc2fd = CcOffset(core, ::bc2f)
    private val  bc2td = CcOffset(core, ::bc2t)
    private val  bc2fld = CcOffset(core, ::bc2fl)
    private val  bc2tld = CcOffset(core, ::bc2tl)

    // e_bc1
    private val  bc1fd = CcOffset(core, ::bc1f)
    private val  bc1td = CcOffset(core, ::bc1t)
    private val  bc1fld = CcOffset(core, ::bc1fl)
    private val  bc1tld = CcOffset(core, ::bc1tl)

    // e_cop2
    private val  cfc2d = RtRdCop2(core, ::cfc2, ImplementSpecCop, Control)
    private val  mfc2d = RtRdCop2(core, ::mfc2, ImplementSpecCop, General)
    private val  dmfc2d = RtRdCop2(core, ::dmfc2, ImplementSpecCop, General)
    private val  mfhc2d = RtRdCop2(core, ::mfhc2, ImplementSpecCop, General)
    private val  mtc2d = RtRdCop2(core, ::mtc2, ImplementSpecCop, General)
    private val  dmtc2d = RtRdCop2(core, ::dmtc2, ImplementSpecCop, General)
    private val  ctc2d = RtRdCop2(core, ::ctc2, ImplementSpecCop, Control)
    private val  mthc2d = RtRdCop2(core, ::mthc2, ImplementSpecCop, General)

    // e_cop1
    private val  mfc1d = RtRdSel(core, ::mfc1, FloatingPointCop, General)
    private val  dmfc1d = RtRdSel(core, ::dmfc1, FloatingPointCop, General)
    private val  cfc1d = RtRdSel(core, ::cfc1, FloatingPointCop, Control)
    private val  mfhc1d = RtRdSel(core, ::mfhc1, FloatingPointCop, General)
    private val  mtc1d = RtRdSel(core, ::mtc1, FloatingPointCop, General)
    private val  dmtc1d = RtRdSel(core, ::dmtc1, FloatingPointCop, General)
    private val  ctc1d = RtRdSel(core, ::ctc1, FloatingPointCop, Control)
    private val  mthc1d = RtRdSel(core, ::mthc1, FloatingPointCop, General)

    // e_c0
    private val  tlbrd = Code19bit(core, ::tlbr)
    private val  tlbwid = Code19bit(core, ::tlbwi)
    private val  tlbwrd = Code19bit(core, ::tlbwr)
    private val  tlbpd = Code19bit(core, ::tlbp)
    private val  deretd = Code19bit(core, ::deret)
    private val  eretd = Code19bit(core, ::eret)
    private val  waitd = Code19bit(core, ::wait)

    // e_mfmc0
    private val  did = Rt(core, ::di)
    private val  eid = Rt(core, ::ei)

    // e_cop0
    private val  mfc0d = RtRdSel(core, ::mfc0, SystemControlCop, General)
    private val  rdpgprd = RdRt(core, ::rdpgpr)
    private val  mtc0d = RtRdSel(core, ::mtc0, SystemControlCop, General)
    private val  wrpgprd = RdRt(core, ::wrpgpr)

    // e_cop0: mips 64 specific
    private val dmtc0d = RtRdSel(core, ::dmtc0, SystemControlCop, General)
    private val dmfc0d = RtRdSel(core, ::dmfc0, SystemControlCop, General)

    // e_bshfl
    private val  wsbhd = RdRt(core, ::wsbh)
    private val  sebd = RdRt(core, ::seb)
    private val  sehd = RdRt(core, ::seh)

    // e_dbshfl
    private val  dsbhd = RdRt(core, ::dsbh)
    private val  dshdd = RdRt(core, ::dshd)

    // e_srl
    private val  srld = RdRtSa(core, ::srl)
    private val  rotrd = RdRtSa(core, ::rotr)

    // e_dsrl
    private val  dsrld = RdRtSa(core, ::dsrl)
    private val  drotrd = RdRtSa(core, ::drotr)

    // e_dsrl32d
    private val dsrl32d = RdRtSa(core, ::dsrl32)
    private val drotr32d = RdRtSa(core, ::drotr32)

    // e_srlv
    private val  srlvd = RdRtRs(core, ::srlv)
    private val  rotrvd = RdRtRs(core, ::rotrv)

    // e_dsrlv
    private val dsrlvd = RdRtRs(core, ::dsrlv)

    // e_movci
    private val  movfd = RdRsCc(core, ::movf)
    private val  movtd = RdRsCc(core, ::movt)

    // e_special3
    private val  extd = RsRtPosSize(core, ::ext)
    private val  insd = RsRtPosSize(core, ::ins)
    private val  rdhwrd = RtRdSel(core, ::rdhwr, ImplementSpecCop, General)
    // mips64 specific
    private val  dextd = RsRtPosSize(core, ::dext)
    private val  dextud = RsRtPosSize(core, ::dextu)
    private val  dextmd = RsRtPosSize(core, ::dextm)
    private val  dinsd = RsRtPosSize(core, ::dins)
    private val  dinsud = RsRtPosSize(core, ::dinsu)
    private val  dinsmd = RsRtPosSize(core, ::dinsm)

    // DSP extension
    private val  rddspd = RdMask(core, ::rddsp, Position.MLEFT)
    private val  wrdspd = RdMask(core, ::wrdsp, Position.MRIGHT)

    private val extrw_d = InstructionTable(
        4, 8,
        { data -> data[10..9] },
        { data -> data[8..6] },
        null,      null,      null,      null,      null,      null,      null,      null,
        null,      null,      null,      null,      null,      null,      null,      null,
        null,      null,      rddspd,    wrdspd,    null,      null,      null,      null,
        null,      null,      null,      null,      null,      null,      null,      null
    )

    // N.B.: Octeons do not have DSP extension, but implement LBX-and-others
    private val ldxd = RdIndex(core, ::ldx)
    private val lwxd = RdIndex(core, ::lwx)
    private val lwuxd = RdIndex(core, ::lwux)
    private val lhxd = RdIndex(core, ::lhx)
    private val lhuxd = RdIndex(core, ::lhux)
    private val lbxd = RdIndex(core, ::lbx)
    private val lbuxd = RdIndex(core, ::lbux)

    private val  lxd = InstructionTable(
        4, 8,
        { data -> data[10..9] },
        { data -> data[8..6] },
        lwxd,      null,      null,      null,      lhxd,      null,      lbuxd,      null,
        ldxd,      null,      null,      null,      null,      null,      null,      null,
        lwuxd,     null,      null,      null,      lhuxd,     null,      lbxd,      null,
        null,      null,      null,      null,      null,      null,      null,      null
    )

    // e_special2
    private val  maddd = RdRsRt(core, ::madd)
    private val  maddud = RdRsRt(core, ::maddu)
    private val  muld = RdRsRt(core, ::mul)
    private val  msubd = RdRsRt(core, ::msub)
    private val  msubud = RdRsRt(core, ::msubu)
    private val  clzd = RdRsRt(core, ::clz)
    private val  clod = RdRsRt(core, ::clo)
    private val  dclzd = RdRsRt(core, ::dclz)
    private val  sdbbpd = Code19bit(core, ::sdbbp)

    private val  saad = when (core.microarchitecture) {
        Microarchitecture.cnMips -> CvmRtBase(core, DWORD, ::saa)
        else -> Unimplemented(core)
    }

    private val  saadd = when (core.microarchitecture) {
        Microarchitecture.cnMips -> CvmRtBase(core, QWORD, ::saa)
        else -> Unimplemented(core)
    }

    private val  laxd = when (core.microarchitecture) {
        Microarchitecture.cnMips -> InstructionTable(
            10..6,
            *Array(32) {
                when (it) {
                    0x12 -> CvmRdBaseRt(core, DWORD, ::laa)
                    0x13 -> CvmRdBaseRt(core, QWORD, ::laa)
                    0x16 -> CvmRdBaseRt(core, DWORD, ::law)
                    0x17 -> CvmRdBaseRt(core, QWORD, ::law)
                    0x02 -> CvmRdBase(core, DWORD, ::lai)
                    0x03 -> CvmRdBase(core, QWORD, ::lai)
                    0x06 -> CvmRdBase(core, DWORD, ::lad)
                    0x07 -> CvmRdBase(core, QWORD, ::lad)
                    0x0a -> CvmRdBase(core, DWORD, ::las)
                    0x0b -> CvmRdBase(core, QWORD, ::las)
                    0x0e -> CvmRdBase(core, DWORD, ::lac)
                    0x0f -> CvmRdBase(core, QWORD, ::lac)
                    0x1d -> CvmRdBase(core, DWORD, ::zcbt)
                    else -> Unimplemented(core)
                }
            },
        )
        else -> Unimplemented(core)
    }

    private val  baddud = when (core.microarchitecture) {
        Microarchitecture.cnMips -> RdRsRt(core, ::baddu)
        else -> Unimplemented(core)
    }

    private val  dmuld = when (core.microarchitecture) {
        Microarchitecture.cnMips -> RdRsRt(core, ::dmul)
        else -> Unimplemented(core)
    }

    private val  extsd = when (core.microarchitecture) {
        Microarchitecture.cnMips -> RsRtPosSize(core, ::exts)
        else -> Unimplemented(core)
    }

    private val  cinsd = when (core.microarchitecture) {
        Microarchitecture.cnMips -> RsRtPosSize(core, ::cins)
        else -> Unimplemented(core)
    }

    private val  popd = when (core.microarchitecture) {
        Microarchitecture.cnMips -> RdRsRt(core, ::pop)
        else -> Unimplemented(core)
    }

    private val  dpopd = when (core.microarchitecture) {
        Microarchitecture.cnMips -> RdRsRt(core, ::dpop)
        else -> Unimplemented(core)
    }

    private val  seqd = when (core.microarchitecture) {
        Microarchitecture.cnMips -> RdRsRt(core, ::seq)
        else -> Unimplemented(core)
    }

    private val  sned = when (core.microarchitecture) {
        Microarchitecture.cnMips -> RdRsRt(core, ::sne)
        else -> Unimplemented(core)
    }

    private val  seqid = when (core.microarchitecture) {
        Microarchitecture.cnMips -> CvmRtRsImm(core, ::seqi)
        else -> Unimplemented(core)
    }

    private val  sneid = when (core.microarchitecture) {
        Microarchitecture.cnMips -> CvmRtRsImm(core, ::snei)
        else -> Unimplemented(core)
    }

    // e_regimm
    private val  bltzd = RsOffset(core, ::bltz)
    private val  bgezd = RsOffset(core, ::bgez)
    private val  bltzld = RsOffset(core, ::bltzl)
    private val  bgezld = RsOffset(core, ::bgezl)
    private val  tgeid = RsImm(core, ::tgei, true)
    private val  tgeiud = RsImm(core, ::tgeiu, false)
    private val  tltid = RsImm(core, ::tlti, true)
    private val  tltiud = RsImm(core, ::tltiu, false)
    private val  teqid = RsImm(core, ::teqi, true)
    private val  tneid = RsImm(core, ::tnei, true)
    private val  bltzald = RsOffset(core, ::bltzal)
    private val  bgezald = RsOffset(core, ::bgezal)
    private val  bltzalld = RsOffset(core, ::bltzall)
    private val  bgezalld = RsOffset(core, ::bgezall)
    private val  syncid = Empty(core, ::synci)

    // e_special
    private val  slld = RdRtSa(core, ::sll)
    private val  srad = RdRtSa(core, ::sra)
    private val  sllvd = RdRtRs(core, ::sllv)
    private val  sravd = RdRtRs(core, ::srav)
    private val  jrd = Rs(core, ::jr)
    private val  jalrd = RdRsHint(core, ::jalr)
    private val  movzd = RdRsRt(core, ::movz)
    private val  movnd = RdRsRt(core, ::movn)
    private val  syscalld = Code20bit(core, ::syscall)
    private val  breakrd = Code20bit(core, ::breakr)
    private val  syncd = Empty(core, ::sync)
    private val  mfhid = Rd(core, ::mfhi)
    private val  mthid = Rs(core, ::mthi)
    private val  mflod = Rd(core, ::mflo)
    private val  mtlod = Rs(core, ::mtlo)
    private val  multd = RdRsRt(core, ::mult)
    private val  multud = RdRsRt(core, ::multu)
    private val  divd = RdRsRt(core, ::div)
    private val  divud = RdRsRt(core, ::divu)
    private val  addd = RdRsRt(core, ::add)
    private val  addud = RdRsRt(core, ::addu)
    private val  subd = RdRsRt(core, ::sub)
    private val  subud = RdRsRt(core, ::subu)
    private val  andrd = RdRsRt(core, ::andr)
    private val  orrd = RdRsRt(core, ::orr)
    private val  xord = RdRsRt(core, ::xorr)
    private val  nord = RdRsRt(core, ::nor)
    private val  sltd = RdRsRt(core, ::slt)
    private val  sltud = RdRsRt(core, ::sltu)
    private val  tged = RsRtCode(core, ::tge)
    private val  tgeud = RsRtCode(core, ::tgeu)
    private val  tltd = RsRtCode(core, ::tlt)
    private val  tltud = RsRtCode(core, ::tltu)
    private val  teqd = RsRtCode(core, ::teq)
    private val  tned = RsRtCode(core, ::tne)
    // mips64 specific
    private val  daddud = RdRsRt(core, ::daddu)
    private val  daddd = RdRsRt(core, ::dadd)
    private val  dsubud = RdRsRt(core, ::dsubu)
    private val  dssld = RdRtSa(core, ::dsll)
    private val  dsllvd = RdRtRs(core, ::dsllv)
    private val  dsrad = RdRtSa(core, ::dsra)
    private val  dsra32d = RdRtSa(core, ::dsra32)
    private val  dsravd = RdRtRs(core, ::dsrav)
    private val  dsll32d = RdRtSa(core, ::dsll32)
    private val  dmultd = RdRsRt(core, ::dmult)
    private val  ddivd = RdRsRt(core, ::ddiv)
    private val  ddivud = RdRsRt(core, ::ddivu)
    private val  dmultud = RdRsRt(core, ::dmultu)

    // e_opcode
    private val  jd = Index(core, ::j)
    private val  jald = Index(core, ::jal)
    private val  beqd = RsRtOffset(core, ::beq)
    private val  bned = RsRtOffset(core, ::bne)
    private val  blezd = RsOffset(core, ::blez)
    private val  bgtzd = RsOffset(core, ::bgtz)
    private val  addid = RtRsImm(core, ::addi, true)
    private val  daddiud = RtRsImm(core, ::daddiu, true)
    private val  daddid = RtRsImm(core, ::daddi, true)
    private val  addiud = RtRsImm(core, ::addiu, false)
    private val  sltid = RtRsImm(core, ::slti, true)
    private val  sltiud = RtRsImm(core, ::sltiu, false)
    private val  andid = RtRsImm(core, ::andi, false)
    private val  orid = RtRsImm(core, ::ori, false)
    private val  xorid = RtRsImm(core, ::xori, false)
    private val  luid = RtImm(core, ::lui)
    private val  beqld = RsRtOffset(core, ::beql)
    private val  bneld = RsRtOffset(core, ::bnel)
    private val  blezld = RsOffset(core, ::blezl)
    private val  bgtzld = RsOffset(core, ::bgtzl)

    private val  lbd = RtOffset(core, ::lb, BYTE, LOAD)
    private val  lhd = RtOffset(core, ::lh, WORD, LOAD)
    private val  lwld = RtOffset(core, ::lwl, DWORD, LOAD)
    private val  ulwd = RtOffset(core, ::ulw, DWORD, LOAD)
    private val  opc4_2d = Conditional(core) {
        when {
            !useun -> lwld
            usely || be -> ulwd
            else -> nopd
        }
    }
    private val  ldld = RtOffset(core, ::ldl, QWORD, LOAD)
    private val  uldd = RtOffset(core, ::uld, QWORD, LOAD)
    private val  opc3_2d = Conditional(core) {
        when {
            !useun -> ldld
            usely || be -> uldd
            else -> nopd
        }
    }
    private val  lwd = RtOffset(core, ::lw, DWORD, LOAD)
    private val  lbud = RtOffset(core, ::lbu, BYTE, LOAD)
    private val  lhud = RtOffset(core, ::lhu, WORD, LOAD)
    private val  lwrd = RtOffset(core, ::lwr, DWORD, LOAD)
    private val  opc4_6d = Conditional(core) {
        when {
            !useun -> lwrd
            usely || be -> nopd
            else -> ulwd
        }
    }
    private val  ldrd = RtOffset(core, ::ldr, QWORD, LOAD)
    private val  opc3_3d = Conditional(core) {
        when {
            !useun -> ldrd
            usely || be -> nopd
            else -> uldd
        }
    }
    private val  lwud = RtOffset(core, ::lwu, DWORD, LOAD)
    private val  sbd = RtOffset(core, ::sb, BYTE, STORE)
    private val  shd = RtOffset(core, ::sh, WORD, STORE)
    private val  swld = RtOffset(core, ::swl, DWORD, STORE)
    private val  uswd = RtOffset(core, ::usw, DWORD, STORE)
    private val  opc5_2d = Conditional(core) {
        when {
            !useun -> swld
            usely || be -> uswd
            else -> nopd
        }
    }
    private val  sdld = RtOffset(core, ::sdl, QWORD, STORE)
    private val  usdd = RtOffset(core, ::usd, QWORD, STORE)
    private val  opc5_4d = Conditional(core) {
        when {
            !useun -> sdld
            usely || be -> usdd
            else -> nopd
        }
    }
    private val  sdrd = RtOffset(core, ::sdr, QWORD, STORE)
    private val  opc5_5d = Conditional(core) {
        when {
            !useun -> sdrd
            usely || be -> nopd
            else -> usdd
        }
    }
    private val  swd = RtOffset(core, ::sw, DWORD, STORE)
    private val  swrd = RtOffset(core, ::swr, DWORD, STORE)
    private val  opc5_6d = Conditional(core) {
        when {
            !useun -> swrd
            usely || be -> nopd
            else -> uswd
        }
    }
    private val  cached = OpOffsetBase(core, ::cache)
    private val  lld = RtOffset(core, ::ll, DWORD, LOAD)
    private val  lwc1d = FtOffset(core, ::lwc1, DWORD, LOAD, FloatingPointCop)
    private val  opc6_2d = when (core.microarchitecture) {
        Microarchitecture.cnMips -> Bbit(core, ::bbit)
        else -> FtOffset(core, ::lwc2, DWORD, LOAD, ImplementSpecCop)
    }
    private val  prefd = OpOffsetBase(core, ::pref)
    private val  ldc1d = FtOffset(core, ::ldc1, QWORD, LOAD, FloatingPointCop)
    private val  opc6_6d = when (core.microarchitecture) {
        Microarchitecture.cnMips -> Bbit(core, ::bbit)
        else -> FtOffset(core, ::ldc2, QWORD, LOAD, ImplementSpecCop)
    }
    private val  scd = RtOffset(core, ::sc, DWORD, STORE)
    private val  swc1d = FtOffset(core, ::swc1, DWORD, STORE, FloatingPointCop)
    private val  opc7_2d = when (core.microarchitecture) {
        Microarchitecture.cnMips -> Bbit(core, ::bbit)
        else -> FtOffset(core, ::swc2, DWORD, STORE, ImplementSpecCop)
    }
    private val  sdc1d = FtOffset(core, ::sdc1, QWORD, STORE, FloatingPointCop)
    private val  opc7_6d = when (core.microarchitecture) {
        Microarchitecture.cnMips -> Bbit(core, ::bbit)
        else -> FtOffset(core, ::sdc2, QWORD, STORE, ImplementSpecCop)
    }

    private val c_cond_dd = CcFsFt(core, ::c_cond_d)
    private val c_cond_sd = CcFsFt(core, ::c_cond_s)

    private val add_sd = FdFsFt(core, ::add_s)
    private val sub_sd = FdFsFt(core, ::sub_s)
    private val mul_sd = FdFsFt(core, ::mul_s)
    private val div_sd = FdFsFt(core, ::div_s)
    private val mov_sd = FdFs(core, ::mov_s)
    private val trunc_w_sd = FdFs(core, ::trunc_w_s)
    private val cvt_d_sd = FdFs(core, ::cvt_d_s)

    private val add_dd = FdFsFt(core, ::add_d)
    private val sub_dd = FdFsFt(core, ::sub_d)
    private val mul_dd = FdFsFt(core, ::mul_d)
    private val div_dd = FdFsFt(core, ::div_d)
    private val mov_dd = FdFs(core, ::mov_d)
    private val trunc_l_dd = FdFs(core, ::trunc_l_d)
    private val trunc_w_dd = FdFs(core, ::trunc_w_d)
    private val cvt_s_dd = FdFs(core, ::cvt_s_d)

    private val cvt_s_wd = FdFs(core, ::cvt_s_w)
    private val cvt_d_wd = FdFs(core, ::cvt_d_w)

    private val cvt_s_ld = FdFs(core, ::cvt_s_l)
    private val cvt_d_ld = FdFs(core, ::cvt_d_l)

    private val e_cop1xd = null
//    private val e_movcf_ps = null
//    private val e_movcf_d = null
//    private val e_movcf_s = null

    private val e_c2d = null

    // mips64 specific
    private val sdd = RtOffset(core, ::sd, QWORD, STORE)
    private val ldd = RtOffset(core, ::ld, QWORD, LOAD)
    private val lldd = RtOffset(core, ::lld, QWORD, LOAD)
    private val scdd = RtOffset(core, ::scd, QWORD, STORE)

    private val e_bc2d = InstructionTable(
            2, 2,
            { data -> data[17] },
            { data -> data[16] },
            bc2fd,  bc2td,
            bc2fld, bc2tld
    )

    private val e_bc1d = InstructionTable(
            2, 2,
            { data -> data[17] },
            { data -> data[16] },
            bc1fd,  bc1td,
            bc1fld, bc1tld
    )

    private val e_cop2d = InstructionTable(
            4, 8,
            { data -> data[25..24] },
            { data -> data[23..21] },
            mfc2d,  dmfc2d,cfc2d, mfhc2d, mtc2d, dmtc2d,ctc2d, mthc2d,
            e_bc2d, null,  null,  null,   null,  null,  null,  null,
            e_c2d,  e_c2d, e_c2d, e_c2d,  e_c2d, e_c2d, e_c2d, e_c2d,
            e_c2d,  e_c2d, e_c2d, e_c2d,  e_c2d, e_c2d, e_c2d, e_c2d
    )

    private val e_fmt_sd = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            add_sd,    sub_sd,    mul_sd,    div_sd,    null,      null,      mov_sd,    null,
            null,      null,      null,      null,      null,      trunc_w_sd,null,      null,
            null,      null,      null,      null,      null,      null,      null,      null,
            null,      null,      null,      null,      null,      null,      null,      null,
            null,      cvt_d_sd,  null,      null,      null,      null,      null,      null,
            null,      null,      null,      null,      null,      null,      null,      null,
            c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd,
            c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd, c_cond_sd
    )

    private val e_fmt_wd = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            null,     null,     null,     null,   null,   null,   null,   null,
            null,     null,     null,     null,   null,   null,   null,   null,
            null,     null,     null,     null,   null,   null,   null,   null,
            null,     null,     null,     null,   null,   null,   null,   null,
            cvt_s_wd, cvt_d_wd, null,     null,   null,   null,   null,   null,
            null,     null,     null,     null,   null,   null,   null,   null,
            null,     null,     null,     null,   null,   null,   null,   null,
            null,     null,     null,     null,   null,   null,   null,   null
    )



    private val e_fmt_dd = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            add_dd,    sub_dd,    mul_dd,    div_dd,    null,      null,       mov_dd,    null,
            null,      trunc_l_dd,null,      null,      null,      trunc_w_dd, null,      null,
            null,      null,      null,      null,      null,      null,       null,      null,
            null,      null,      null,      null,      null,      null,       null,      null,
            cvt_s_dd,  null,      null,      null,      null,      null,       null,      null,
            null,      null,      null,      null,      null,      null,       null,      null,
            c_cond_dd, c_cond_dd, c_cond_dd, c_cond_dd, c_cond_dd, c_cond_dd,  c_cond_dd, c_cond_dd,
            c_cond_dd, c_cond_dd, c_cond_dd, c_cond_dd, c_cond_dd, c_cond_dd,  c_cond_dd, c_cond_dd
    )

    private val e_fmt_ld = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            null,     null,      null,     null,   null,   null,   null,   null,
            null,     null,      null,     null,   null,   null,   null,   null,
            null,     null,      null,     null,   null,   null,   null,   null,
            null,     null,      null,     null,   null,   null,   null,   null,
            cvt_s_ld, cvt_d_ld,  null,     null,   null,   null,   null,   null,
            null,     null,      null,     null,   null,   null,   null,   null,
            null,     null,      null,     null,   null,   null,   null,   null,
            null,     null,      null,     null,   null,   null,   null,   null
    )

    private val e_fmt_psd = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            null,   null,     null,     null,   null,   null,   null,   null,
            null,   null,     null,     null,   null,   null,   null,   null,
            null,   null,     null,     null,   null,   null,   null,   null,
            null,   null,     null,     null,   null,   null,   null,   null,
            null,   null,     null,     null,   null,   null,   null,   null,
            null,   null,     null,     null,   null,   null,   null,   null,
            null,   null,     null,     null,   null,   null,   null,   null,
            null,   null,     null,     null,   null,   null,   null,   null
    )

    private val e_cop1d = InstructionTable(
            4, 8,
            { data -> data[25..24] },
            { data -> data[23..21] },
            mfc1d,       dmfc1d,     cfc1d,      mfhc1d,  mtc1d,      dmtc1d,     ctc1d,      mthc1d,
            e_bc1d,      null,       null,       null,    null,       null,       null,       null,
            e_fmt_sd,    e_fmt_dd,   null,       null,    e_fmt_wd,   e_fmt_ld,   e_fmt_psd,  null,
            null,        null,       null,       null,    null,       null,       null,       null
    )

    private val e_c0d = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            null,  tlbrd, tlbwid, null, null, null, tlbwrd, null,
            tlbpd, null,  null,   null, null, null, null,   null,
            null,  null,  null,   null, null, null, null,   null,
            eretd, null,  null,   null, null, null, null,   deretd,
            waitd, null,  null,   null, null, null, null,   null,
            null,  null,  null,   null, null, null, null,   null,
            null,  null,  null,   null, null, null, null,   null,
            null,  null,  null,   null, null, null, null,   null
    )

    private val e_mfmc0d = InstructionTable(
            1, 2,
            { data -> 0u },
            { data -> data[5] },
            did, eid
    )

    private val e_cop0d = InstructionTable(
            4, 8,
            { data -> data[25..24] },
            { data -> data[23..21] },
            mfc0d, dmfc0d,  null,    null,     mtc0d, dmtc0d,  null,    null,
            null,  null,    rdpgprd, e_mfmc0d, null,  null,    wrpgprd, null,
            e_c0d, e_c0d,   e_c0d,   e_c0d,    e_c0d, e_c0d,   e_c0d,   e_c0d,
            e_c0d, e_c0d,   e_c0d,   e_c0d,    e_c0d, e_c0d,   e_c0d,   e_c0d
    )

    private val e_bshfld = InstructionTable(
            4, 8,
            { data -> data[10..9] },
            { data -> data[8..6] },
            null,  null,  wsbhd,  null,  null,  null,  null,  null,
            null,  null,  null,   null,  null,  null,  null,  null,
            sebd,  null,  null,   null,  null,  null,  null,  null,
            sehd,  null,  null,   null,  null,  null,  null,  null
    )

    // mips64 specific
    private val e_dbshfld = InstructionTable(
            4, 8,
            { data -> data[10..9] },
            { data -> data[8..6] },
            null,  null,  dsbhd,  null,  null,  dshdd,  null,  null,
            null,  null,  null,   null,  null,  null,  null,    null,
            null,  null,  null,   null,  null,  null,  null,    null,
            null,  null,  null,   null,  null,  null,  null,    null
    )

    private val e_srld = InstructionTable(
            1, 2,
            { data -> 0u },
            { data -> data[21] },
            srld, rotrd
    )

    // mips64 specific
    private val e_dsrld = InstructionTable(
            1, 2,
            { data -> 0u },
            { data -> data[21] },
            dsrld, drotrd
    )

    // mips64 specific
    private val e_dsrl32d = InstructionTable(
        1, 2,
        { data -> 0u },
        { data -> data[21] },
        dsrl32d, drotr32d,
    )

    private val e_srlvd = InstructionTable(
            1, 2,
            { data -> 0u },
            { data -> data[6] },
            srlvd, rotrvd
    )

    // mips64 specific
    private val e_dsrlvd = InstructionTable(
            1, 2,
            { data -> 0u },
            { data -> data[6] },
            dsrlvd, null
    )

    private val e_movcid = InstructionTable(
            1, 2,
            { data -> 0u },
            { data -> data[16] },
            movfd, movtd
    )

    private val e_special3d = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            extd,     dextmd,  dextud,  dextd,  insd,      dinsmd,  dinsud,  dinsd,
            null,     null,    lxd,     null,   null,      null,    null,    null,
            null,     null,    null,    null,   null,      null,    null,    null,
            null,     null,    null,    null,   null,      null,    null,    null,
            e_bshfld, null,    null,    null,   e_dbshfld, null,    null,    null,
            null,     null,    null,    null,   null,      null,    null,    null,
            null,     null,    null,    null,   null,      null,    null,    null,
            extrw_d,   null,    null,   rdhwrd, null,      null,    null,    null
    )

    private val e_special2d = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            maddd,  maddud, muld,   dmuld,  msubd, msubud, null,    null,
            null,   null,   null,   null,   null,  null,   null,    null,
            null,   null,   null,   null,   null,  null,   null,    null,
            saad,   saadd,  null,   null,   null,  null,   null,    laxd,
            clzd,   clod,   null,   null,   dclzd, null,   null,    null,
            baddud, null,   seqd,   sned,   popd,  dpopd,  seqid,   sneid,
            null,   null,   cinsd,  cinsd,  null,  null,   null,    null,
            null,   null,   extsd,  extsd,  null,  null,   null,    sdbbpd
    )

    private val e_regimmd = InstructionTable(
            4, 8,
            { data -> data[20..19] },
            { data -> data[18..16] },
            bltzd,   bgezd,   bltzld,   bgezld,   null,  null, null,  null,
            tgeid,   tgeiud,  tltid,    tltiud,   teqid, null, tneid, null,
            bltzald, bgezald, bltzalld, bgezalld, null,  null, null,  null,
            null,    null,    null,     null,     null,  null, null,  syncid
    )

    private val e_speciald = InstructionTable(
            8, 8,
            { data -> data[5..3] },
            { data -> data[2..0] },
            slld,  e_movcid, e_srld,  srad,   sllvd,    null,    e_srlvd,    sravd,
            jrd,   jalrd,    movzd,   movnd,  syscalld, breakrd, null,       syncd,
            mfhid, mthid,    mflod,   mtlod,  dsllvd,   null,    e_dsrlvd,   dsravd,
            multd, multud,   divd,    divud,  dmultd,   dmultud, ddivd,      ddivud,
            addd,  addud,    subd,    subud,  andrd,    orrd,    xord,       nord,
            null,  null,     sltd,    sltud,  daddd,    daddud,  null,       dsubud,
            tged,  tgeud,    tltd,    tltud,  teqd,     null,    tned,       null,
            dssld, null,     e_dsrld, dsrad,  dsll32d,  null,    e_dsrl32d,  dsra32d
    )

    private val e_opcoded = InstructionTable(
            8, 8,
            { data -> data[31..29] },
            { data -> data[28..26] },
            e_speciald, e_regimmd, jd,      jald,     beqd,         bned,    blezd,  bgtzd,
            addid,      addiud,    sltid,   sltiud,   andid,        orid,    xorid,  luid,
            e_cop0d,    e_cop1d,   e_cop2d, e_cop1xd, beqld,        bneld,   blezld, bgtzld,
            daddid,     daddiud,   opc3_2d, opc3_3d,  e_special2d,  null,    null,   e_special3d,
            lbd,        lhd,       opc4_2d, lwd,      lbud,         lhud,    opc4_6d,lwud,
            sbd,        shd,       opc5_2d, swd,      opc5_4d,      opc5_5d, opc5_6d,cached,
            lld,        lwc1d,     opc6_2d, prefd,    lldd,         ldc1d,   opc6_6d,ldd,
            scd,        swc1d,     opc7_2d, null,     scdd,         sdc1d,   opc7_6d,sdd
    )

    private val dcCache = dictionary<ULong, AMipsInstruction>(0x100000)

    fun decode(data: ULong, pc: ULong): AMipsInstruction {
        var insn = dcCache[data]
        if (insn != null) return insn
        val entry = e_opcoded.lookup(data) ?: throw DecoderException(data, pc)
        insn = (entry as ADecoder).decode(data)
        insn.ea = pc
        dcCache[data] = insn
        return insn
    }
}