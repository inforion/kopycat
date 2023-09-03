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
@file:Suppress("PrivatePropertyName")

package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.CachedMemoryStreamByteArray
import ru.inforion.lab403.kopycat.cores.base.enums.AccessType.INSTRUCTION
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders.*
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.cmov.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.compare.Cmp
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop.Loop
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop.Loopnz
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop.Loopz
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.misc.Clc
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.misc.Cld
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.misc.Enbr32
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.misc.Std
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.*
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.interfaces.IMemoryStream
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.x86Core


@Suppress("NOTHING_TO_INLINE")
class x86SystemDecoder(val core: x86Core, val cpu: x86CPU) : ICoreUnit {
    companion object {
        @Transient val log = logger()
    }

    private class Prefix : ITableEntry {
        class Segment(val ssr: Int): ITableEntry
        class Operand: ITableEntry
        class Address: ITableEntry
        class Repz: ITableEntry
        class Repnz: ITableEntry
        class Lock: ITableEntry
        class REX(val W: Boolean, val R: Boolean, val X: Boolean, val B: Boolean): ITableEntry
    }

    override val name: String = "x86 System Decoder"

    private val rex     = Prefix.REX(false, false, false, false)
    private val rexB    = Prefix.REX(false, false, false, true)
    private val rexX    = Prefix.REX(false, false, true, false)
    private val rexXB   = Prefix.REX(false, false, true, true)
    private val rexR    = Prefix.REX(false, true, false, false)
    private val rexRB   = Prefix.REX(false, true, false, true)
    private val rexRX   = Prefix.REX(false, true, true, false)
    private val rexRXB  = Prefix.REX(false, true, true, true)
    private val rexW    = Prefix.REX(true, false, false, false)
    private val rexWB   = Prefix.REX(true, false, false, true)
    private val rexWX   = Prefix.REX(true, false, true, false)
    private val rexWXB  = Prefix.REX(true, false, true, true)
    private val rexWR   = Prefix.REX(true, true, false, false)
    private val rexWRB  = Prefix.REX(true, true, false, true)
    private val rexWRX  = Prefix.REX(true, true, true, false)
    private val rexWRXB = Prefix.REX(true, true, true, true)
    private val lock = Prefix.Lock()
    private val repz = Prefix.Repz()
    private val repnz = Prefix.Repnz()
    private val operOvr = Prefix.Operand()
    private val addrOvr = Prefix.Address()
    private val csOvr   = Prefix.Segment(SSR.CS.id)
    private val dsOvr   = Prefix.Segment(SSR.DS.id)
    private val esOvr   = Prefix.Segment(SSR.ES.id)
    private val ssOvr   = Prefix.Segment(SSR.SS.id)
    private val fsOvr   = Prefix.Segment(SSR.FS.id)
    private val gsOvr   = Prefix.Segment(SSR.GS.id)

    private val aaaDc = SimpleDC(core, ::Aaa)
    private val aasDc = SimpleDC(core, ::Aas)
    private val addDc = ArithmDC(core, ::Add)
    private val adcDc = ArithmDC(core, ::Adc)
    private val daaDc = SimpleDC(core, ::Daa)
    private val dasDc = SimpleDC(core, ::Das)
    private val decDc = DecDC(core)
    private val divDc = DivDC(core)
    private val incDc = IncDC(core)
    private val mulDc = MulDC(core)
    private val negDc = NegDC(core)
    private val notDc = NotDC(core)
    private val sbbDc = ArithmDC(core, ::Sbb)
    private val subDc = ArithmDC(core, ::Sub)
    private val imulDC = ImulDC(core)
    private val idivDc = IdivDC(core)

    private val andDc = ArithmDC(core, ::And)
    private val orDc = ArithmDC(core, ::Or)
    private val xorDc = ArithmDC(core, ::Xor)
    private val testDc = TestDC(core)
    private val cmpDc = ArithmDC(core, ::Cmp)
    private val btDc = BtDC(core)
    private val btsDc = BtsDC(core)
    private val btrDc = BtrDC(core)
    private val bsfDc = BitScanDC(core, ::Bsf)
    private val bsrDc = BitScanDC(core, ::Bsr)
    private val btcDc = BtcDC(core)

    private val shldDc = ShxdDC(core, ::Shld)
    private val shrdDc = ShxdDC(core, ::Shrd)

    private val salDc = ShiftRotateDC(core, ::Shl)
    private val sarDc = ShiftRotateDC(core, ::Sar)
    private val shlDc = ShiftRotateDC(core, ::Shl)
    private val shrDc = ShiftRotateDC(core, ::Shr)
    private val rclDc = ShiftRotateDC(core, ::Rcl)
    private val rcrDc = ShiftRotateDC(core, ::Rcr)
    private val rolDc = ShiftRotateDC(core, ::Rol)
    private val rorDc = ShiftRotateDC(core, ::Ror)

    private val pushDc = PushDC(core)
    private val popDc = PopDC(core)
    private val pushfDc = SimpleDC(core, ::Pushf)
    private val popfDc = SimpleDC(core, ::Popf)
    private val pushaDc = SimpleDC(core, ::Pusha)
    private val popaDc = SimpleDC(core, ::Popa)
    private val enterDc = EnterDC(core)
    private val leaveDc = LeaveDC(core)

    private val cmpxchgDc = CmpxchgDC(core)
    private val cmpxchgxbDc = CmpxchgxbDC(core)
    private val xaddDc = XaddDC(core)
    private val cmpsxDc = CmpsxDC(core)
    private val movnti = MovntiDC(core)
    private val pinsrwDc = PinsrwDC(core)
    private val shufpsDc = ShufpsDC(core)
    private val movDc = MovDC(core)
    private val xchgDc = XchgDC(core)
    private val bswapDc = BswapDC(core)
    private val movdbg = MovDbgDC(core)
    private val movctrl = MovCtrlDC(core)
    private val movsx = MovsxDC(core)
    private val movzx = MovzxDC(core)
    private val leaDc = LeaDC(core)
    private val cwdeDc = SimpleDC(core, ::Cwde)
    private val cdqDc = SimpleDC(core, ::Cdq)
    private val psrlwDc = PsrlwDC(core)

    private val lodsDc = LodsDC(core)
    private val stosDc = StosDC(core)
    private val scasDc = ScasDC(core)
    private val cmpsDc = CmpsDC(core)
    private val movsDc = MovsDC(core)
    private val inswDc = InswDC(core)
    private val outswDC = OutswDC(core)

    private val callDc = CallDC(core)
    private val joDc = JccDC(core, ::Jo)
    private val jnoDc = JccDC(core, ::Jno)
    private val jbDc = JccDC(core, ::Jb)
    private val jnbDc = JccDC(core, ::Jnb)
    private val jeDc = JccDC(core, ::Je)
    private val jneDc = JccDC(core, ::Jne)
    private val jbeDc = JccDC(core, ::Jbe)
    private val jaDc = JccDC(core, ::Ja)
    private val jsDc = JccDC(core, ::Js)
    private val jnsDc = JccDC(core, ::Jns)
    private val jpeDc = JccDC(core, ::Jpe)
    private val jpoDc = JccDC(core, ::Jpo)
    private val jlDc = JccDC(core, ::Jl)
    private val jgeDc = JccDC(core, ::Jge)
    private val jleDc = JccDC(core, ::Jle)
    private val jgDc = JccDC(core, ::Jg)
    private val jecxzDc = JccDC(core, ::Jecxz)
    private val jmpDc = JmpDC(core)
    private val retDc = RetDC(core)
    private val iretDc = IRetDC(core)

    private val nopDc = NopDC(core)
    private val intoDc = SimpleDC(core, ::Into)
    private val intDC = IntDC(core)
    private val int3Dc = SimpleDC(core, ::Int3)
    private val invdDc = SimpleDC(core, ::Invd, 2)
    private val wbinvdDc = SimpleDC(core, ::Wbinvd, 2)
    private val CpuidDc = SimpleDC(core, ::CpuId, 2)

    private val cltsDc = SimpleDC(core, ::Clts, 2)

    private val cliDc = SimpleDC(core, ::Cli)
    private val stcDc = SimpleDC(core, ::Stc)
    private val stiDc = SimpleDC(core, ::Sti)
    private val sahfDc = SimpleDC(core, ::Sahf)
    private val lahfDc = SimpleDC(core, ::Lahf)
    private val larDc = LarDC(core)
    private val lslDc = LslDC(core)
    private val outDc = OutDC(core)
    private val inDc = InDC(core)

    private val hltDc = SimpleDC(core, ::Hlt)

    private val loopDc = LoopDC(core, ::Loop)
    private val loopzDc = LoopDC(core, ::Loopz)
    private val loopnzDc = LoopDC(core, ::Loopnz)

    private val lgdtDc = LSdtDC(core, ::Lgdt, true)
    private val lidtDc = LSdtDC(core, ::Lidt, true)
    private val sgdtDc = LSdtDC(core, ::Sgdt)
    private val sidtDc = LSdtDC(core, ::Sidt)
    private val sldtDc = SldtDC(core)
    private val lldtDc = LldtDC(core)
    private val smswDc = MswDc(core, ::Smsw)
    private val lmswDc = MswDc(core, ::Lmsw)
    private val invlpgDc = InvlpgDC(core)
    private val verrDc = VerrDC(core)
    private val verwDc = VerwDC(core)

    private val ltrDc = LtrDC(core)
    private val strDc = StrDC(core)

    private val ldsDc = LFPDC(core, ::Lds)
    private val lesDc = LFPDC(core, ::Les)
    private val lfsDc = LFPDC(core, ::Lfs)
    private val lgsDc = LFPDC(core, ::Lgs)
    private val lssDc = LFPDC(core, ::Lss)

    private val setaDc = SetccDC(core, ::SetA)
    private val setbDc = SetccDC(core, ::SetB)
    private val setbeDc = SetccDC(core, ::SetBe)
    private val setgDc = SetccDC(core, ::SetG)
    private val setgeDc = SetccDC(core, ::SetGe)
    private val setlDc = SetccDC(core, ::SetL)
    private val setleDc = SetccDC(core, ::SetLe)
    private val setnbDc = SetccDC(core, ::SetNb)
    private val setneDc = SetccDC(core, ::SetNe)
    private val setnoDc = SetccDC(core, ::SetNo)
    private val setnsDc = SetccDC(core, ::SetNs)
    private val setoDc = SetccDC(core, ::SetO)
    private val setpeDc = SetccDC(core, ::SetPe)
    private val setpoDc = SetccDC(core, ::SetPo)
    private val setsDc = SetccDC(core, ::SetS)
    private val setzDc = SetccDC(core, ::SetZ)

    private val cldDc = SimpleDC(core, ::Cld)
    private val clcDc = SimpleDC(core, ::Clc)
    private val stdDc = SimpleDC(core, ::Std)

    private val fildDc = FildDC(core)
    private val fstDc = FstDC(core)
    private val fldDC = FldDC(core)
    private val finitDC = SimpleDC(core, ::Finit, 2)
    private val fclexDC = SimpleDC(core, ::Fclex, 2)
    private val fstswDc = FstswDC(core)
    private val fsaveDc = FsaveRstorDC(core, ::Fsave)
    private val frstorDc = FsaveRstorDC(core, ::Frstor)
    private val fldenvDc = FldenvDC(core)
    private val fstenvDc = FstenvDC(core)
    private val fstcwDc = FstcwDC(core)
    private val fucomDc = FucomDC(core)
    private val fldcwDc = FldcwDC(core)
    private val fistDc = FistDC(core)
    private val faddDc = FArithmDC(core, ::Fadd)
    private val fsubDc = FArithmDC(core, ::Fsub)
    private val fsubrDc = FArithmDC(core, ::Fsubr)
    private val fmulDc = FArithmDC(core, ::Fmul)
    private val fdivDc = FArithmDC(core, ::Fdiv)
    private val fdivrDc = FArithmDC(core, ::Fdivr)
    private val fwaitDc = SimpleDC(core, ::Fwait)
    private val fsetpmDc = FLoadConstDC(core, ::Fsetpm)
    private val frndintDc = FrndintDC(core)
    private val fscaleDc = FscaleDC(core)
    private val fcmovDc = FcmovDC(core)
    private val fsqrtDc = FsqrtDC(core)
    private val fabsDc = FabsDC(core)
    private val fchsDc = FchsDC(core)

    private val fld1Dc = FLoadConstDC(core, ::Fld1)
    private val fldl2tDc = FLoadConstDC(core, ::Fldl2t)
    private val fldl2eDc = FLoadConstDC(core, ::Fldl2e)
    private val fldpiDc = FLoadConstDC(core, ::Fldpi)
    private val fldlg2Dc = FLoadConstDC(core, ::Fldlg2)
    private val fldln2Dc = FLoadConstDC(core, ::Fldln2)
    private val fldzDc = FLoadConstDC(core, ::Fldz)

    private val movdDc = MovdDC(core)
    private val movdqDc = MovdqDC(core)
    private val movntdqDc = MovntdqDC(core)
    private val movupsDc = MovupsDC(core)
    private val movapsDc = MovapsDC(core)
    private val movlpdDc = MovlpdDC(core)
    private val movhpdDc = MovhpdDC(core)

    private val movmskpdDc = MovmskpdDC(core)
    private val sqrtsdDc = SqrtsdDC(core)
    private val andpdDc = AndpdDC(core)
    private val andnpdDc = AndnpdDC(core)
    private val orpdDc = OrpdDC(core)
    private val pandDc = PandDC(core)
    private val pandnDc = PandnDC(core)
    private val pxorDc = PxorDC(core)
    private val porDc = PorDC(core)
    private val pcmpeqbDc = PcmpeqbDC(core)
    private val pcmpeqdDc = PcmpeqdDC(core)
    private val pcmpgtbDc = PcmpgtbDC(core)
    private val pcmpgtdDc = PcmpgtdDC(core)
    private val paddqDc = PaddqDC(core)
    private val pmovmskbDc = PmovmskbDC(core)
    private val psubusxDc = PsubusxDC(core)
    private val pmaxubDc = PmaxubDC(core)
    private val pminubDc = PminubDC(core)
    private val pslldqDc = PslldqDC(core)
    private val psrldqDc = PsrldqDC(core)
    private val pmuludqDc = PmuludqDC(core)
    private val psubDc = PsubDC(core)
    private val packuswbDc = PackuswbDC(core)
    private val punpcklDc = PunpcklDC(core)
    private val punpckhDc = PunpckhDC(core)
    private val pshufdDc = PshufdDC(core)

    private val unpcklpdDc = UnpcklpdDC(core)

    private val rdmsrDc = SimpleDC(core, ::Rdmsr, 2)
    private val wrmsrDc = SimpleDC(core, ::Wrmsr, 2)
    private val rdtscDc = SimpleDC(core, ::Rdtsc, 2)

    private val pshufbDc = PshufbDC(core)
    private val phaddxDc = PhaddxDC(core)
    private val movbeDc = MovbeDC(core)

    private val ldmxcsrDc = LdmxcsrDC(core)
    private val sfenceDc = SimpleDC(core, ::Sfence, 3)
    private val lfenceDc = SimpleDC(core, ::Lfence, 3)
    private val mfenceDc = SimpleDC(core, ::Mfence, 3)

    private val addsdDc     = AddsdDC(core)
    private val mulsxDc     = MulsxDC(core)
    private val subxxDc     = SubxxDC(core)
    private val minsdDc     = MinsdDC(core)
    private val divsxDc     = DivsxDC(core)
    private val maxsxDc     = MaxsxDC(core)
    private val cvttsx2siDC = Cvttsx2siDC(core)
    private val cvtsi2sxDC  = Cvtsi2sxDC(core)
    private val cvtsx2sxDC  = Cvtsx2sxDC(core)
    private val xorpsDC     = XorpsDC(core)
    private val comisxDc    = ComisxDC(core)
    private val palignrDc   = PalignrDC(core)

    private val haddpdDc    = HaddpdDC(core)

    private val cmovoDc     = CmovccDC(core,    ::Cmovo)
    private val cmovnoDc    = CmovccDC(core,    ::Cmovno)
    private val cmovbDc     = CmovccDC(core,    ::Cmovb)
    private val cmovnbDc    = CmovccDC(core,    ::Cmovnb)
    private val cmoveDc     = CmovccDC(core,    ::Cmove)
    private val cmovneDc    = CmovccDC(core,    ::Cmovne)
    private val cmovbeDc    = CmovccDC(core,    ::Cmovbe)
    private val cmovaDc     = CmovccDC(core,    ::Cmova)
    private val cmovsDc     = CmovccDC(core,    ::Cmovs)
    private val cmovnsDc    = CmovccDC(core,    ::Cmovns)
    private val cmovpeDc    = CmovccDC(core,    ::Cmovpe)
    private val cmovpoDc    = CmovccDC(core,    ::Cmovpo)
    private val cmovlDc     = CmovccDC(core,    ::Cmovl)
    private val cmovgeDc    = CmovccDC(core,    ::Cmovge)
    private val cmovleDc    = CmovccDC(core,    ::Cmovle)
    private val cmovgDc     = CmovccDC(core,    ::Cmovg)

    private val fxsaveDc = FxsaveDC(core)
    private val fxrstorDc = FxrstorDC(core)
    private val prefetchtDc = PrefetchtDC(core)
    private val swapgsDc = SimpleDC(core, ::Swapgs, 3)
    private val syscallDc = SimpleDC(core, ::Syscall, 2)
    private val sysretDc = SimpleDC(core, ::Sysret, 2)
    private val sysenterDc = SimpleDC(core, ::Sysenter, 2)
    private val sysexitDc = SimpleDC(core, ::Sysexit, 2)

    private val stmxcsrDc = StmxcsrDC(core)
    private val enbr32Dc = SimpleDC(core, ::Enbr32, 3)
    private val bndmovDc = BndmovDC(core)
    private val monitorDc = SimpleDC(core, ::Monitor, 3)
    private val mwaitDc = SimpleDC(core, ::Mwait, 3)

    private fun notImplementedDecoder(name: String) = object: ADecoder<AX86Instruction>(core) {
        override fun decode(s: x86OperandStream, prefs: Prefixes) =
            throw NotImplementedError("$name is not implemented")
    }

    private val pconfigDc = notImplementedDecoder("pconfigDc")
    private val clacDc = notImplementedDecoder("clacDc")
    private val stacDc = notImplementedDecoder("stacDc")
    private val xgetbvDc = notImplementedDecoder("xgetbvDc")
    private val xsetbvDc = notImplementedDecoder("xsetbvDc")
    private val xendDc = notImplementedDecoder("xendDc")
    private val xtestDc = notImplementedDecoder("xtestDc")
    private val setssbsyDc = notImplementedDecoder("setssbsyDc")
    private val saveprevsspDc = notImplementedDecoder("saveprevsspDc")
    private val rdpkruDc = notImplementedDecoder("rdpkruDc")
    private val wrpkruDc = notImplementedDecoder("wrpkruDc")
    private val rdtscpDc = notImplementedDecoder("rdtscpDc")
    private val xabortbeginDc = notImplementedDecoder("xabortbeginDc")
    private val xsaveDc = notImplementedDecoder("xsaveDc")
    private val xrstorDc = notImplementedDecoder("xrstorDc")
    private val xsaveoptDc = notImplementedDecoder("xsaveoptDc")
    private val clflushDc = notImplementedDecoder("clflushDc")
    private val rdfsbaseDc = notImplementedDecoder("rdfsbaseDc")
    private val rdgsbaseDc = notImplementedDecoder("rdgsbaseDc")
    private val wrfsbaseDc = notImplementedDecoder("wrfsbaseDc")
    private val wrgsbaseDc = notImplementedDecoder("wrgsbaseDc")
    private val psrlqDc = PsrlqDC(core)
    private val psllxDc = PsllxDC(core)
    private val psrldDc = PsrldDC(core)
    private val psraxDc = PsraxDC(core)

    private val emmsDC = EmmsDC(core)
    private val fxchDC = FxchDC(core)
    private val fxamDC = FxamDC(core)

    private val group_10 = notImplementedDecoder("group_10")

    inline fun x86OperandStream.peekOpcodeForward(): Int {
        position += 1u
        val opcode = peekOpcode()
        position -= 1u
        return opcode
    }

    inline fun RMDCMemory(noinline rc: (x86OperandStream) -> Pair<Int, Int>, vararg entries: ITableEntry?) =
        x86InstructionTable(0x01, 0x08, rc, *entries)
    inline fun RMDC11B(noinline rc: (x86OperandStream) -> Pair<Int, Int>, vararg entries: ITableEntry?) =
        x86InstructionTable(0x04, 0x10, rc, *entries)
    inline fun RMDC(noinline rc: (x86OperandStream) -> Pair<Int, Int>, memEntry: ITableEntry?, b11Entry: ITableEntry?) =
        x86InstructionTable(0x01, 0x04, rc, memEntry, memEntry, memEntry, b11Entry)


    inline fun byte1RMDC_Memory(vararg entries: ITableEntry?) = RMDCMemory({ stream: x86OperandStream ->
        val opcode = stream.peekOpcode()
        0 to opcode [5..3]
    }, *entries)
    inline fun byte1RMDC_11B(vararg entries: ITableEntry?) = RMDC11B({ stream: x86OperandStream ->
        val opcode = stream.peekOpcode()
        opcode[5..4] to opcode[3..0]
    }, *entries)
    inline fun byte1RMDC(memEntry: ITableEntry?, b11Entry: ITableEntry?) = RMDC({ stream: x86OperandStream ->
        val opcode = stream.peekOpcode()
        0 to opcode [7..6]
    }, memEntry, b11Entry)

    /**
     * Table A-6. Opcode Extensions for One- and Two-byte Opcodes by Group Number, p. 2672
     */
    inline fun byte2RMDC_Memory(vararg entries: ITableEntry?) = RMDCMemory({ stream: x86OperandStream ->
        val opcode = stream.peekOpcodeForward()
        0 to opcode [5..3]
    }, *entries)
    inline fun byte2RMDC_11B(vararg entries: ITableEntry?) = RMDC11B({ stream: x86OperandStream ->
        val opcode = stream.peekOpcodeForward()
        opcode[5..4] to opcode[3..0]
    }, *entries)
    inline fun byte2RMDC(memEntry: ITableEntry?, b11Entry: ITableEntry?) = RMDC({ stream: x86OperandStream ->
        val opcode = stream.peekOpcodeForward()
        0 to opcode [7..6]
    }, memEntry, b11Entry)


    private val cell_df_mem = byte1RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/         fildDc,      null,      fistDc,     fistDc,       null,      fildDc,      null,     fistDc
    )

    private val cell_df_11b = byte1RMDC_11B(
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fstswDc,    null,       null,       null,       null,       null,       null,       null,       null,       fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_df = byte1RMDC(cell_df_mem,  cell_df_11b)

    private val cell_de_a19 = byte1RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/         faddDc,     fmulDc,      null,       null,       fsubDc,   fsubrDc,     fdivDc,    fdivrDc,
    )

    private val cell_de_a20 = byte1RMDC_11B(
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,
            /*F*/ fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc
    )

    private val cell_de = byte1RMDC(cell_de_a19,  cell_de_a20)

    private val cell_dd_a17 = byte1RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/         fldDC,     null,      fstDc,      fstDc,   frstorDc,       null,    fsaveDc,    fstswDc
    )

    private val cell_dd_a18 = byte1RMDC_11B(
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*D*/ fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,
            /*E*/ fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_dd = byte1RMDC(cell_dd_a17,  cell_dd_a18)

    private val cell_dc_a15 = byte1RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/         faddDc,     fmulDc,     null,       null,       fsubDc,    fsubrDc,     fdivDc,     fdivrDc
    )

    private val cell_dc_a16 = byte1RMDC_11B(
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,
            /*F*/ fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc
    )

    private val cell_dc = byte1RMDC(cell_dc_a15,  cell_dc_a16)

    private val cell_db_a13 = byte1RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/        fildDc,      null,      fistDc,      fistDc,       null,       fldDC,       null,       fstDc
    )

    private val cell_db_a14 = byte1RMDC_11B(
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,
            /*D*/ fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,
            /*E*/ null,       null,       fclexDC,    finitDC,    fsetpmDc,   null,       null,       null,       fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_db = byte1RMDC(cell_db_a13,  cell_db_a14)

    private val cell_da_a11 = byte1RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/         faddDc,     fmulDc,     null,       null,       fsubDc,    fsubrDc,      fdivDc,   fdivrDc
    )

    private val cell_da_a12 = byte1RMDC_11B(
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,
            /*D*/ fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,    fcmovDc,
            /*E*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       fucomDc,    null,       null,       null,       null,       null,       null,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_da = byte1RMDC(cell_da_a11,  cell_da_a12)

    private val cell_d9_a9 = byte1RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/          fldDC,      null,       fstDc,      fstDc,  fldenvDc,     fldcwDc,      fstenvDc,  fstcwDc
    )

    private val cell_d9_a10 = byte1RMDC_11B(
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ fldDC,      fldDC,      fldDC,      fldDC,      fldDC,      fldDC,      fldDC,      fldDC,      null,       fxchDC,     fxchDC,     fxchDC,     fxchDC,     fxchDC,     fxchDC,     fxchDC,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fchsDc,     fabsDc,     null,       null,       null,       fxamDC,       null,       null,       fld1Dc,     fldl2tDc,   fldl2eDc,   fldpiDc,    fldlg2Dc,   fldln2Dc,   fldzDc,     null,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       fsqrtDc,       null,       frndintDc,  fscaleDc,   null,       null
    )

    private val cell_d9 = byte1RMDC(cell_d9_a9,  cell_d9_a10)

    private val cell_d8_a7 = byte1RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/         faddDc,     fmulDc,     null,       null,       fsubDc,    fsubrDc,     fdivDc,     fdivrDc,
    )

    private val cell_d8_a8 = byte1RMDC_11B(
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,
            /*F*/ fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc,    fdivrDc
    )

    private val cell_d8 = byte1RMDC(cell_d8_a7,  cell_d8_a8)

    private val group_8 = byte2RMDC_Memory(
            /////           0           1           2           3           4           5           6           7
            /*0*/ null,       null,       null,       null,        btDc,      btsDc,      btrDc,       null
    )

    private val group_9 = byte2RMDC_Memory(
        ///// 0           1           2           3           4           5           6           7
        /*0*/ null,       cmpxchgxbDc,null,       null,       null,       null,       null,       null,
    )

    private val group_1 = byte1RMDC_Memory(
            /////   0           1           2           3           4           5           6           7
            /*0*/ addDc,      orDc,       adcDc,      sbbDc,       andDc,     subDc,       xorDc,      cmpDc
    )

    private val group_3 = byte1RMDC_Memory(
            /////   0           1           2           3           4           5           6           7
            /*0*/ testDc,     null,       notDc,      negDc,       mulDc,     imulDC,      divDc,     idivDc
    )
    private val group_2 = byte1RMDC_Memory(
            /////   0           1           2           3           4           5           6           7
            /*0*/ rolDc,       rorDc,      rclDc,      rcrDc,     shlDc,       shrDc,     salDc,        sarDc
    )

    private val group_5 = byte1RMDC_Memory(
            /////   0           1           2           3           4           5           6           7
            /*0*/ incDc,      decDc,     callDc,      callDc,      jmpDc,      jmpDc,       pushDc,      null
    )

    private val group_4 = byte1RMDC_Memory(
            /////   0           1           2           3           4           5           6           7
            /*0*/ incDc,      decDc,       null,       null,       null,      null,        null,       null
    )

    private val group_7_mem = byte2RMDC_Memory(
            /////   0           1           2           3           4           5           6           7
            /*0*/  sgdtDc,   sidtDc,      lgdtDc,     lidtDc,      smswDc,     null,       lmswDc,     invlpgDc
    )

    private val group_7_11b = byte2RMDC_11B(
        /////   0               1           2       3       4       5           6           7       8           9           A               B           C       D       E           F
        /*C*/  null,    null,       null,   null,   null,   pconfigDc,  null,       null,   monitorDc,  mwaitDc,    clacDc,         stacDc,     null,   null,   null,       null,
        /*D*/  xgetbvDc,        xsetbvDc,   null,   null,   null,   xendDc,     xtestDc,    null,   null,       null,       null,           null,       null,   null,   null,       null,
        /*E*/  null,            null,       null,   null,   null,   null,       null,       null,   setssbsyDc, null,       saveprevsspDc,  null,       null,   null,   rdpkruDc,   wrpkruDc,
        /*F*/  null,            null,       null,   null,   null,   null,       null,       null,   swapgsDc,   rdtscpDc,   null,           null,       null,   null,   null,       null,
    )

    private val group_7 = byte2RMDC(group_7_mem,  group_7_11b)

    private val group_6 = byte2RMDC_Memory(
            /////   0           1           2           3           4           5           6           7
            /*0*/  sldtDc,     strDc,      lldtDc,      ltrDc,      verrDc,     verwDc,     null,       null
    )

    private val group_11_mem = byte1RMDC_Memory(
        /////   0           1           2           3           4           5           6           7
        /*0*/   movDc,     null,      null,      null,      null,       null,        null,       null
    )

    private val group_11_11b = byte1RMDC_11B(
        /////   0       1        2        3        4        5        6        7        8               9       A       B       C       D       E       F
        /*C*/  movDc,   movDc,   movDc,   movDc,   movDc,   movDc,   movDc,   movDc,   null,           null,   null,   null,   null,   null,   null,   null,
        /*D*/  null,    null,    null,    null,    null,    null,    null,    null,    null,           null,   null,   null,   null,   null,   null,   null,
        /*E*/  null,    null,    null,    null,    null,    null,    null,    null,    null,           null,   null,   null,   null,   null,   null,   null,
        /*F*/  null,    null,    null,    null,    null,    null,    null,    null,    xabortbeginDc,  null,   null,   null,   null,   null,   null,   null,
    )

    private val group_11 = byte1RMDC(group_11_mem, group_11_11b)

    private val group_12 = byte2RMDC_Memory(
        /////    0              1               2               3               4           5           6           7
        /*0*/    null,          null,          psrlwDc,         null,           psraxDc,    null,       psllxDc,    null
    )

    private val group_13 = byte2RMDC_Memory(
        ///// 0        1        2        3        4        5        6           7
        /*0*/ null,    null,    psrldDc, null,    psraxDc, null,    psllxDc,    null,
    )

    private val group_14_mem = byte2RMDC_Memory(
        /////   0           1           2           3           4           5           6           7
        /*0*/   null,       null,      null,      null,      null,       null,        null,       null
    )

    private val group_14_11b = byte2RMDC_11B(
        /////   0       1        2        3        4        5        6        7        8       9       A       B       C       D       E       F
        /*C*/  null,    null,    null,    null,    null,    null,    null,    null,    null,   null,   null,   null,   null,   null,   null,   null,
        /*D*/  psrlqDc, psrlqDc, psrlqDc, psrlqDc, psrlqDc, psrlqDc, psrlqDc, psrlqDc, psrldqDc, psrldqDc, psrldqDc, psrldqDc, psrldqDc, psrldqDc, psrldqDc, psrldqDc,
        /*E*/  null,    null,    null,    null,    null,    null,    null,    null,    null,   null,   null,   null,   null,   null,   null,   null,
        /*F*/  psllxDc, psllxDc, psllxDc, psllxDc, psllxDc, psllxDc, psllxDc, psllxDc, pslldqDc, pslldqDc, pslldqDc, pslldqDc, pslldqDc, pslldqDc, pslldqDc, pslldqDc,
    )

    private val group_14 = byte2RMDC(group_14_mem,  group_14_11b)

    private val group_15_mem = byte2RMDC_Memory(
        /////    0           1           2           3           4           5           6           7
        /*0*/    fxsaveDc,   fxrstorDc,  ldmxcsrDc,  stmxcsrDc,  xsaveDc,    xrstorDc,   xsaveoptDc, clflushDc
    )

    private val group_15_11b = byte2RMDC_11B(
        /////  0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
        /*C*/  rdfsbaseDc, rdfsbaseDc, rdfsbaseDc, rdfsbaseDc, rdfsbaseDc, rdfsbaseDc, rdfsbaseDc, rdfsbaseDc, rdgsbaseDc, rdgsbaseDc, rdgsbaseDc, rdgsbaseDc, rdgsbaseDc, rdgsbaseDc, rdgsbaseDc, rdgsbaseDc,
        /*D*/  wrfsbaseDc, wrfsbaseDc, wrfsbaseDc, wrfsbaseDc, wrfsbaseDc, wrfsbaseDc, wrfsbaseDc, wrfsbaseDc, wrgsbaseDc, wrgsbaseDc, wrgsbaseDc, wrgsbaseDc, wrgsbaseDc, wrgsbaseDc, wrgsbaseDc, wrgsbaseDc,
        /*E*/  null,       null,   null,   null,   null,   null,   null,   null,                               lfenceDc,  null,   null,   null,   null,   null,   null,   null,
        /*F*/  mfenceDc,   null,   null,   null,   null,   null,   null,   null,                               sfenceDc,  null,   null,   null,   null,   null,   null,   null,
    )

    private val group_15 = byte2RMDC(group_15_mem, group_15_11b)

    private val group_16_mem = byte2RMDC_Memory(
        /////    0              1               2               3               4           5           6           7
        /*0*/    prefetchtDc,   prefetchtDc,    prefetchtDc,    prefetchtDc,    null,       null,      null,       null
    )

    private val group_16 = byte2RMDC(group_16_mem, null)

    private val group_0x38 = x86InstructionTable(
        0x10, 0x10,
        { stream: x86OperandStream ->
            stream.readByte()
            val opcode = stream.readOpcode()
            opcode[7..4] to opcode[3..0]
        },
        ///// 0        1        2        3        4        5        6        7        8        9        A        B        C        D        E        F
        /*0*/ pshufbDc,phaddxDc,phaddxDc,null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*1*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*2*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*3*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*4*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*5*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*6*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*7*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*8*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*9*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*A*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*B*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*C*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*D*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*E*/ null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
        /*F*/ movbeDc, movbeDc, null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,    null,
    )

    private val s_opcode = x86InstructionTable(
            0x10, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                opcode[7..4] to opcode[3..0] },
            /////   0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*0*/ group_6,    group_7,    larDc,      lslDc,      null,       syscallDc,  cltsDc,     sysretDc,   invdDc,     wbinvdDc,   null,       null,       null,       null,       null,       null,
            /*1*/ movupsDc,   movupsDc,   movlpdDc,   movlpdDc,   unpcklpdDc, null,       movhpdDc,   null,       group_16,   null,       bndmovDc,   bndmovDc,   null,       null,       enbr32Dc,   nopDc,
            /*2*/ movctrl,    movdbg,     movctrl,    movdbg,     null,       null,       null,       null,       movapsDc,   movapsDc,   cvtsi2sxDC, null,       cvttsx2siDC,null,       comisxDc,   comisxDc,
            /*3*/ wrmsrDc,    rdtscDc,    rdmsrDc,    null,       sysenterDc, sysexitDc,  null,       null,       group_0x38, null,       palignrDc,  null,       null,       null,       null,       null,
            /*4*/ cmovoDc,    cmovnoDc,   cmovbDc,    cmovnbDc,   cmoveDc,    cmovneDc,   cmovbeDc,   cmovaDc,    cmovsDc,    cmovnsDc,   cmovpeDc,   cmovpoDc,   cmovlDc,    cmovgeDc,   cmovleDc,   cmovgDc,
            /*5*/ movmskpdDc, sqrtsdDc,   null,       null,       andpdDc,    andnpdDc,   orpdDc,     xorpsDC,    addsdDc,    mulsxDc,    cvtsx2sxDC, null,       subxxDc,    minsdDc,    divsxDc,    maxsxDc,
            /*6*/ punpcklDc,  punpcklDc,  punpcklDc,  null,       pcmpgtbDc,  null,       pcmpgtdDc,  packuswbDc, punpckhDc,  punpckhDc,  punpckhDc,  null,       punpcklDc,  punpckhDc,  movdDc,     movdqDc,
            /*7*/ pshufdDc,   group_12,   group_13,   group_14,   pcmpeqbDc,  null,       pcmpeqdDc,  emmsDC,     null,       null,       null,       null,       haddpdDc,   null,       movdDc,     movdqDc,
            /*8*/ joDc,       jnoDc,      jbDc,       jnbDc,      jeDc,       jneDc,      jbeDc,      jaDc,       jsDc,       jnsDc,      jpeDc,      jpoDc,      jlDc,       jgeDc,      jleDc,      jgDc,
            /*9*/ setoDc,     setnoDc,    setbDc,     setnbDc,    setzDc,     setneDc,    setbeDc,    setaDc,     setsDc,     setnsDc,    setpeDc,    setpoDc,    setlDc,     setgeDc,    setleDc,    setgDc,
            /*A*/ pushDc,     popDc,      CpuidDc,    btDc,       shldDc,     shldDc,     null,       null,       pushDc,     popDc,      null,       btsDc,      shrdDc,     shrdDc,     group_15,   imulDC,
            /*B*/ cmpxchgDc,  cmpxchgDc,  lssDc,      btrDc,      lfsDc,      lgsDc,      movzx,      movzx,      null,       group_10,   group_8,    btcDc,      bsfDc,      bsrDc,      movsx,      movsx,
            /*C*/ xaddDc,     xaddDc,     cmpsxDc,    movnti,     pinsrwDc,   null,       shufpsDc,   group_9,    bswapDc,    bswapDc,    bswapDc,    bswapDc,    bswapDc,    bswapDc,    bswapDc,    bswapDc,
            /*D*/ null,       null,       null,       psrlqDc,    paddqDc,    null,       movdDc,     pmovmskbDc, psubusxDc,  psubusxDc,  pminubDc,   pandDc,     null,       null,       pmaxubDc,   pandnDc,
            /*E*/ null,       psraxDc,    psraxDc,    null,       null,       null,       null,       movntdqDc,  null,       null,       null,       porDc,      null,       null,       null,       pxorDc,
            /*F*/ null,       psllxDc,    psllxDc,    psllxDc,    pmuludqDc,  null,       null,       null,       psubDc,     psubDc,     psubDc,     psubDc,     paddqDc,    paddqDc,    paddqDc,    null
    )

    // For groups definition see table A-6 "Opcode Extensions for One- and Two-byte Opcodes by Group Number", vol. 2
    private val e_opcode = x86InstructionTable(
            0x10, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.readOpcode()
                opcode[7..4] to opcode[3..0] },
            /////   0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*0*/ addDc,      addDc,      addDc,      addDc,      addDc,      addDc,      pushDc,     popDc,      orDc,       orDc,       orDc,       orDc,       orDc,       orDc,       pushDc,     s_opcode,
            /*1*/ adcDc,      adcDc,      adcDc,      adcDc,      adcDc,      adcDc,      pushDc,     popDc,      sbbDc,      sbbDc,      sbbDc,      sbbDc,      sbbDc,      sbbDc,      pushDc,     popDc,
            /*2*/ andDc,      andDc,      andDc,      andDc,      andDc,      andDc,      esOvr,      daaDc,      subDc,      subDc,      subDc,      subDc,      subDc,      subDc,      csOvr,      dasDc,
            /*3*/ xorDc,      xorDc,      xorDc,      xorDc,      xorDc,      xorDc,      ssOvr,      aaaDc,      cmpDc,      cmpDc,      cmpDc,      cmpDc,      cmpDc,      cmpDc,      dsOvr,      aasDc,
            /*4*/ rex,        rexB,       rexX,       rexXB,      rexR,       rexRB,      rexRX,      rexRXB,     rexW,       rexWB,      rexWX,      rexWXB,     rexWR,      rexWRB,     rexWRX,     rexWRXB,
            /*5*/ pushDc,     pushDc,     pushDc,     pushDc,     pushDc,     pushDc,     pushDc,     pushDc,     popDc,      popDc,      popDc,      popDc,      popDc,      popDc,      popDc,      popDc,
            /*6*/ pushaDc,    popaDc,     null,       movsx,      fsOvr,      gsOvr,      operOvr,    addrOvr,    pushDc,     imulDC,     pushDc,     imulDC,     null,       inswDc,     null,       outswDC,
            /*7*/ joDc,       jnoDc,      jbDc,       jnbDc,      jeDc,       jneDc,      jbeDc,      jaDc,       jsDc,       jnsDc,      jpeDc,      jpoDc,      jlDc,       jgeDc,      jleDc,      jgDc,
            /*8*/ group_1,    group_1,    group_1,    group_1,    testDc,     testDc,     xchgDc,     xchgDc,     movDc,      movDc,      movDc,      movDc,      movDc,      leaDc,      movDc,      popDc,
            /*9*/ nopDc,      xchgDc,     xchgDc,     xchgDc,     xchgDc,     xchgDc,     xchgDc,     xchgDc,     cwdeDc,     cdqDc,      callDc,     fwaitDc,    pushfDc,    popfDc,     sahfDc,     lahfDc,
            /*A*/ movDc,      movDc,      movDc,      movDc,      movsDc,     movsDc,     cmpsDc,     cmpsDc,     testDc,     testDc,     stosDc,     stosDc,     lodsDc,     lodsDc,     scasDc,     scasDc,
            /*B*/ movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,
            /*C*/ group_2,    group_2,    retDc,      retDc,      lesDc,      ldsDc,      group_11,   group_11,   enterDc,    leaveDc,    retDc,      retDc,      int3Dc,     intDC,      intoDc,     iretDc,
            /*D*/ group_2,    group_2,    group_2,    group_2,    null,       null,       null,       null,       cell_d8,    cell_d9,    cell_da,    cell_db,    cell_dc,    cell_dd,    cell_de,    cell_df,
            /*E*/ loopnzDc,   loopzDc,    loopDc,     jecxzDc,    inDc,       inDc,       outDc,      outDc,      callDc,     jmpDc,      jmpDc,      jmpDc,      inDc,       inDc,       outDc,      outDc,
            /*F*/ lock,       null,       repnz,      repz,       hltDc,      null,       group_3,    group_3,    clcDc,      stcDc,      cliDc,      stiDc,      cldDc,      stdDc,      group_4,    group_5
    )

    private val cache = dictionary<Long, AX86Instruction>(1024*1024)

    private val cacheStream = CachedMemoryStreamByteArray(cpu.ports.mem, 0u, SSR.CS.id, INSTRUCTION)

    fun IMemoryStream.cachedOpcode() = data
        .fold(0uL) { result, byte -> (result shl 8) or byte.ulong_z }
        .let { if (it == 0x0FuL) (it shl 8) or this.peekOpcode().ulong_z else it }

    fun decode(where: ULong): AX86Instruction {
        // check previously decoded cached value
//        val result = cache[(cs.value(cpu) shl(32)) or where]
//        if (result != null) {
//            val pAddr = dev.mmu.virtual2physical(where, INSTRUCTION, LOAD, cs.reg)
//            val address = dev.mapping?.physicalMapping(pAddr, io = false) ?: pAddr
//            if (dev.memBus.compareAtPhysical(address, result.opcode)) {
//                val bpt = dev.memBus.breakpoints.lookup(pAddr, 0)
//                if (bpt != null && bpt.check(Breakpoint.Access.EXEC, Breakpoint.Type.ANY)) {
//                    log.severe { "Breakpoint found = $bpt" }
//                    bpt.onBreak?.invoke()
//                    throw BreakpointException(bpt)
//                }
//                return result
//            }
//        }
//        val stream = x86OperandStream(DeviceMemoryStream(dev.memory, where, cs.reg, INSTRUCTION, false))

//        val stream = x86OperandStream(CachedMemoryStream(parent, cpu.mmu, cpu.memBus, where, cs.reg, INSTRUCTION, false))

        cacheStream.reset(where)
        val stream = x86OperandStream(core, cacheStream)
        val prefixes = Prefixes(core)

        while (true) {
            when (val entry = e_opcode.lookup(stream)) {
                null -> throw DecoderException(stream.cachedOpcode(), where)
                is ADecoder<*> -> {
                    val insn = entry.decode(stream, prefixes)
                    insn.ea = core.cpu.regs.rip.value
//                    cache[(ss shl(32)) or where] = insn
                    return insn
                }
                is Prefix.Repnz -> prefixes.string = StringPrefix.REPNZ
                is Prefix.Repz -> prefixes.string = StringPrefix.REPZ
                is Prefix.Segment -> prefixes.segmentOverride = core.cpu.sregs[entry.ssr].toOperand()
                is Prefix.Operand -> prefixes.operandOverride = true
                is Prefix.Address -> prefixes.addressOverride = true
                is Prefix.Lock -> prefixes.lock = true
                is Prefix.REX -> if (core.is64bit) {
                    prefixes.rexB = entry.B
                    prefixes.rexX = entry.X
                    prefixes.rexR = entry.R
                    prefixes.rexW = entry.W
                    prefixes.rex = true
                } else {
                    val newEntry = if (entry.W) decDc else incDc
                    return newEntry.decode(stream, prefixes).also { it.ea = core.cpu.regs.rip.value }
                }
            }
        }
    }
}
