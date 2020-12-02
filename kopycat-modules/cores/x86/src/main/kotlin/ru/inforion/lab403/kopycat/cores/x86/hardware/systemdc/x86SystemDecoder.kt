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
package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc

import gnu.trove.map.hash.THashMap
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.CachedMemoryStream
import ru.inforion.lab403.kopycat.cores.base.enums.AccessType.INSTRUCTION
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders.*
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.compare.Cmp
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop.Loop
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop.Loopnz
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop.Loopz
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.misc.Cld
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.misc.Std
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack.*
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system.CpuId
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system.Hlt
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.*
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.*
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class x86SystemDecoder(val core: x86Core, val cpu: x86CPU) : ICoreUnit {
    companion object {
        @Transient val log = logger()
    }

    private class Prefix : ITableEntry {
        class Segment(val ssr: x86Register): ITableEntry
        class Operand: ITableEntry
        class Address: ITableEntry
        class Repz: ITableEntry
        class Repnz: ITableEntry
        class Lock: ITableEntry
    }

    override val name: String = "x86 System Decoder"

    private val lock = Prefix.Lock()
    private val repz = Prefix.Repz()
    private val repnz = Prefix.Repnz()
    private val operOvr = Prefix.Operand()
    private val addrOvr = Prefix.Address()
    private val csOvr   = Prefix.Segment(cs)
    private val dsOvr   = Prefix.Segment(ds)
    private val esOvr   = Prefix.Segment(es)
    private val ssOvr   = Prefix.Segment(ss)
    private val fsOvr   = Prefix.Segment(fs)
    private val gsOvr   = Prefix.Segment(gs)

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
    private val leaveDc = SimpleDC(core, ::Leave)

    private val cmpxchgDc = CmpxchgDC(core)
    private val xaddDc = XaddDC(core)
    private val movDc = MovDC(core)
    private val xchgDc = XchgDC(core)
    private val BswapDc = BswapDC(core)
    private val movdbg = MovDbgDC(core)
    private val movctrl = MovCtrlDC(core)
    private val movsx = MovsxDC(core)
    private val movzx = MovzxDC(core)
    private val leaDc = LeaDC(core)
    private val cwdeDc = SimpleDC(core, ::Cwde)
    private val cdqDc = SimpleDC(core, ::Cdq)

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

    private val nopDc = SimpleDC(core, ::Nop)
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
    private val lahfDc = SimpleDC(core, ::Lahf)
    private val larDc = LarDC(core)
    private val lslDc = LslDC(core)
    private val outDc = OutDC(core)
    private val inDc = InDC(core)

    private val hltDc = SimpleDC(core, ::Hlt)

    private val loopDc = LoopDC(core, ::Loop)
    private val loopzDc = LoopDC(core, ::Loopz)
    private val loopnzDc = LoopDC(core, ::Loopnz)

    private val lgdtDc = LSdtDC(core, ::Lgdt)
    private val lidtDc = LSdtDC(core, ::Lidt)
    private val sgdtDc = LSdtDC(core, ::Sgdt)
    private val sidtDc = LSdtDC(core, ::Sidt)
    private val sldtDc = SldtDC(core)
    private val lldtDc = LldtDC(core)
    private val smswDc = MswDc(core, ::Smsw)
    private val lmswDc = MswDc(core, ::Lmsw)
    private val invlpgDc = InvlpgDC(core)

    private val ltrDc = LtrDC(core)

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
    private val stdDc = SimpleDC(core, ::Std)

    private val fildDc = FildDC(core)
    private val fstDc = FstDC(core)
    private val fldDC = FldDC(core)
    private val finitDC = SimpleDC(core, ::Finit, 2)
    private val fclexDC = SimpleDC(core, ::Fclex, 2)
    private val fstswDc = FstswDC(core)
    private val fsaveDc = FsaveRstorDC(core, ::Fsave)
    private val frstorDc = FsaveRstorDC(core, ::Frstor)
    private val fstcwDc = FstcwDC(core)
    private val fucomDc = FucomDC(core)
    private val fldcwDc = FldcwDC(core)
    private val fistDc = FistDC(core)
    private val faddDc = FArithmDC(core, ::Fadd)
    private val fsubDc = FArithmDC(core, ::Fsub)
    private val fsubrDc = FArithmDC(core, ::Fsubr)
    private val fmulDc = FArithmDC(core, ::Fmul)
    private val fdivDc = FArithmDC(core, ::Fdiv)
    private val fwaitDc = SimpleDC(core, ::Fwait)
    private val fsetpmDc = FLoadConstDC(core, ::Fsetpm)

    private val fld1Dc = FLoadConstDC(core, ::Fld1)
    private val fldl2tDc = FLoadConstDC(core, ::Fldl2t)
    private val fldl2eDc = FLoadConstDC(core, ::Fldl2e)
    private val fldpiDc = FLoadConstDC(core, ::Fldpi)
    private val fldlg2Dc = FLoadConstDC(core, ::Fldlg2)
    private val fldln2Dc = FLoadConstDC(core, ::Fldln2)
    private val fldzDc = FLoadConstDC(core, ::Fldz)


    private val cell_df_a21 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/         fildDc,      null,      fistDc,     fistDc,       null,      fildDc,      null,     fistDc
    )

    private val cell_df_a22 = x86InstructionTable(
            0x04, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair((opcode - 0xC0)[7..4], opcode[3..0]) },
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fstswDc,    null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_df = x86InstructionTable(
            0x01, 0x04,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [7..6]) },
            /////           0             1             2             3
            /*0*/      cell_df_a21,   cell_df_a21, cell_df_a21,   cell_df_a22
    )

    private val cell_de_a19 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/         faddDc,     fmulDc,      null,       null,       fsubDc,   fsubrDc,     fdivDc,       null
    )

    private val cell_de_a20 = x86InstructionTable(
            0x04, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair((opcode - 0xC0)[7..4], opcode[3..0]) },
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,       fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc
    )

    private val cell_de = x86InstructionTable(
            0x01, 0x04,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [7..6]) },
            /////           0             1             2             3
            /*0*/     cell_de_a19,    cell_de_a19,  cell_de_a19,  cell_de_a20
    )

    private val cell_dd_a17 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/         fldDC,     null,      fstDc,      fstDc,   frstorDc,       null,    fsaveDc,    fstswDc
    )

    private val cell_dd_a18 = x86InstructionTable(
            0x04, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair((opcode - 0xC0)[7..4], opcode[3..0]) },
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*D*/ fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,      fstDc,
            /*E*/ fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,    fucomDc,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_dd = x86InstructionTable(
            0x01, 0x04,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [7..6]) },
            /////           0             1             2             3
            /*0*/     cell_dd_a17,    cell_dd_a17,  cell_dd_a17,  cell_dd_a18
    )

    private val cell_dc_a15 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/         faddDc,     fmulDc,     null,       null,       fsubDc,    fsubrDc,     fdivDc,     null
    )

    private val cell_dc_a16 = x86InstructionTable(
            0x04, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair((opcode - 0xC0)[7..4], opcode[3..0]) },
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc
    )

    private val cell_dc = x86InstructionTable(
            0x01, 0x04,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [7..6]) },
            /////           0             1             2             3
            /*0*/      cell_dc_a15,  cell_dc_a15,  cell_dc_a15,  cell_dc_a16
    )

    private val cell_db_a13 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/        fildDc,      null,      fistDc,      fistDc,       null,       fldDC,       null,       fstDc
    )

    private val cell_db_a14 = x86InstructionTable(
            0x04, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair((opcode - 0xC0)[7..4], opcode[3..0]) },
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ null,       null,       fclexDC,    finitDC,    fsetpmDc,   null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_db = x86InstructionTable(
            0x01, 0x04,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode[7..6]) },
            /////           0             1             2             3
            /*0*/      cell_db_a13,  cell_db_a13,   cell_db_a13,  cell_db_a14
    )

    private val cell_da_a11 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/         faddDc,     fmulDc,     null,       null,       fsubDc,    fsubrDc,      fdivDc,      null
    )

    private val cell_da_a12 = x86InstructionTable(
            0x04, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair((opcode - 0xC0)[7..4], opcode[3..0]) },
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       fucomDc,    null,       null,       null,       null,       null,       null,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_da = x86InstructionTable(
            0x01, 0x04,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [7..6]) },
            /////           0             1             2             3
            /*0*/      cell_da_a11,  cell_da_a11,   cell_da_a11,  cell_da_a12
    )

    private val cell_d9_a9 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/          fldDC,      null,       fstDc,      fstDc,      null,     fldcwDc,      null,      fstcwDc
    )

    private val cell_d9_a10 = x86InstructionTable(
            0x04, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair((opcode - 0xC0)[7..4], opcode[3..0]) },
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ fldDC,      fldDC,      fldDC,      fldDC,      fldDC,      fldDC,      fldDC,       fldDC,     null,       null,       null,       null,       null,       null,       null,       null,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ null,       null,       null,       null,       null,       null,       null,       null,       fld1Dc,     fldl2tDc,   fldl2eDc,   fldpiDc,    fldlg2Dc,   fldln2Dc,   fldzDc,     null,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_d9 = x86InstructionTable(
            0x01, 0x04,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [7..6]) },
            /////           0             1             2             3
            /*0*/      cell_d9_a9,    cell_d9_a9,   cell_d9_a9,   cell_d9_a10
    )

    private val cell_d8_a7 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/         faddDc,     fmulDc,     null,       null,       fsubDc,    fsubrDc,     fdivDc,     null
    )

    private val cell_d8_a8 = x86InstructionTable(
            0x04, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair((opcode - 0xC0)[7..4], opcode[3..0]) },
            /////    0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*C*/ faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     faddDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,     fmulDc,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubDc,     fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,    fsubrDc,
            /*F*/ fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     fdivDc,     null,       null,       null,       null,       null,       null,       null,       null
    )

    private val cell_d8 = x86InstructionTable(
            0x01, 0x04,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(0, opcode [7..6]) },
            /////           0             1             2             3
            /*0*/      cell_d8_a7,   cell_d8_a7,    cell_d8_a7,      cell_d8_a8
    )

    private val cell_ba = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                stream.position += 1
                val opcode = stream.peekOpcode()
                stream.position -= 1
                Pair(0, opcode [5..3]) },
            /////           0           1           2           3           4           5           6           7
            /*0*/ null,       null,       null,       null,        btDc,      btsDc,      btrDc,       null
    )

    private val rm_dpnd = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream -> Pair(0, stream.peekOpcode()[5..3]) },
            /////   0           1           2           3           4           5           6           7
            /*0*/ addDc,      orDc,       adcDc,      sbbDc,       andDc,     subDc,       xorDc,      cmpDc
    )

    private val cell_f6f7 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream -> Pair(0, stream.peekOpcode()[5..3]) },
            /////   0           1           2           3           4           5           6           7
            /*0*/ testDc,     null,       notDc,      negDc,       mulDc,     imulDC,      divDc,     idivDc
    )
    private val cell_ShtRl = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream -> Pair(0, stream.peekOpcode()[5..3]) },
            /////   0           1           2           3           4           5           6           7
            /*0*/ rolDc,       rorDc,      rclDc,      rcrDc,     shlDc,       shrDc,     salDc,        sarDc
    )

    private val cell_ff = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream -> Pair(0, stream.peekOpcode()[5..3]) },
            /////   0           1           2           3           4           5           6           7
            /*0*/ incDc,      decDc,     callDc,      callDc,      jmpDc,      jmpDc,       pushDc,      null
    )

    private val cell_fe = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream -> Pair(0, stream.peekOpcode()[5..3]) },
            /////   0           1           2           3           4           5           6           7
            /*0*/ incDc,      decDc,       null,       null,       null,      null,        null,       null
    )

    private val cell_01 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                stream.position += 1
                val opcode = stream.peekOpcode()
                stream.position -= 1
                Pair(0, opcode[5..3]) },
            /////   0           1           2           3           4           5           6           7
            /*0*/  sgdtDc,   sidtDc,      lgdtDc,     lidtDc,      smswDc,     null,       lmswDc,     invlpgDc
    )

    private val cell_00 = x86InstructionTable(
            0x01, 0x08,
            { stream: x86OperandStream ->
                stream.position += 1
                val opcode = stream.peekOpcode()
                stream.position -= 1
                Pair(0, opcode[5..3]) },
            /////   0           1           2           3           4           5           6           7
            /*0*/  sldtDc,     null,      lldtDc,      ltrDc,      null,       null,        null,       null
    )

    private val s_opcode = x86InstructionTable(
            0x10, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.peekOpcode()
                Pair(opcode[7..4], opcode[3..0]) },
            /////   0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*0*/ cell_00,    cell_01,    larDc,      lslDc,      null,       null,       cltsDc,     null,       invdDc,     wbinvdDc,   null,       null,       null,       null,       null,       null,
            /*1*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*2*/ movctrl,    movdbg,     movctrl,    movdbg,     null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*3*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*4*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*5*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*6*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*7*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*8*/ joDc,       jnoDc,      jbDc,       jnbDc,      jeDc,       jneDc,      jbeDc,      jaDc,       jsDc,       jnsDc,      jpeDc,      jpoDc,      jlDc,       jgeDc,      jleDc,      jgDc,
            /*9*/ setoDc,     setnoDc,    setbDc,     setnbDc,    setzDc,     setneDc,    setbeDc,    setaDc,     setsDc,     setnsDc,    setpeDc,    setpoDc,    setlDc,     setgeDc,    setleDc,    setgDc,
            /*A*/ pushDc,     popDc,      CpuidDc,    btDc,       shldDc,     shldDc,     null,       null,       pushDc,     popDc,      null,       btsDc,      shrdDc,     shrdDc,     null,       imulDC,
            /*B*/ cmpxchgDc,  cmpxchgDc,  lssDc,      btrDc,      lfsDc,      lgsDc,      movzx,      movzx,      null,       null,       cell_ba,    null,       bsfDc,      bsrDc,      movsx,      movsx,
            /*C*/ xaddDc,     xaddDc,     null,       null,       null,       null,       null,       null,       BswapDc,    BswapDc,    BswapDc,    BswapDc,    BswapDc,    BswapDc,    BswapDc,    BswapDc,
            /*D*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*E*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,
            /*F*/ null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null
    )

    private val e_opcode = x86InstructionTable(
            0x10, 0x10,
            { stream: x86OperandStream ->
                val opcode = stream.readOpcode()
                Pair(opcode[7..4], opcode[3..0]) },
            /////   0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
            /*0*/ addDc,      addDc,      addDc,      addDc,      addDc,      addDc,      pushDc,     popDc,      orDc,       orDc,       orDc,       orDc,       orDc,       orDc,       pushDc,     s_opcode,
            /*1*/ adcDc,      adcDc,      adcDc,      adcDc,      adcDc,      adcDc,      pushDc,     popDc,      sbbDc,      sbbDc,      sbbDc,      sbbDc,      sbbDc,      sbbDc,      pushDc,     popDc,
            /*2*/ andDc,      andDc,      andDc,      andDc,      andDc,      andDc,      esOvr,      daaDc,      subDc,      subDc,      subDc,      subDc,      subDc,      subDc,      csOvr,      dasDc,
            /*3*/ xorDc,      xorDc,      xorDc,      xorDc,      xorDc,      xorDc,      ssOvr,      aaaDc,      cmpDc,      cmpDc,      cmpDc,      cmpDc,      cmpDc,      cmpDc,      dsOvr,      aasDc,
            /*4*/ incDc,      incDc,      incDc,      incDc,      incDc,      incDc,      incDc,      incDc,      decDc,      decDc,      decDc,      decDc,      decDc,      decDc,      decDc,      decDc,
            /*5*/ pushDc,     pushDc,     pushDc,     pushDc,     pushDc,     pushDc,     pushDc,     pushDc,     popDc,      popDc,      popDc,      popDc,      popDc,      popDc,      popDc,      popDc,
            /*6*/ pushaDc,    popaDc,     null,       null,       fsOvr,      gsOvr,      operOvr,    addrOvr,    pushDc,     imulDC,     pushDc,     imulDC,     null,       inswDc,     null,       outswDC,
            /*7*/ joDc,       jnoDc,      jbDc,       jnbDc,      jeDc,       jneDc,      jbeDc,      jaDc,       jsDc,       jnsDc,      jpeDc,      jpoDc,      jlDc,       jgeDc,      jleDc,      jgDc,
            /*8*/ rm_dpnd,    rm_dpnd,    rm_dpnd,    rm_dpnd,    testDc,     testDc,     xchgDc,     xchgDc,     movDc,      movDc,      movDc,      movDc,      movDc,      leaDc,      movDc,      popDc,
            /*9*/ nopDc,      xchgDc,     xchgDc,     xchgDc,     xchgDc,     xchgDc,     xchgDc,     xchgDc,     cwdeDc,     cdqDc,      callDc,     fwaitDc,    pushfDc,    popfDc,     null,       lahfDc,
            /*A*/ movDc,      movDc,      movDc,      movDc,      movsDc,     movsDc,     cmpsDc,     cmpsDc,     testDc,     testDc,     stosDc,     stosDc,     lodsDc,     lodsDc,     scasDc,     scasDc,
            /*B*/ movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,      movDc,
            /*C*/ cell_ShtRl, cell_ShtRl, retDc,      retDc,      lesDc,      ldsDc,      movDc,      movDc,      enterDc,    leaveDc,    retDc,      retDc,      int3Dc,     intDC,      intoDc,     iretDc,
            /*D*/ cell_ShtRl, cell_ShtRl, cell_ShtRl, cell_ShtRl, null,       null,       null,       null,       cell_d8,    cell_d9,    cell_da,    cell_db,    cell_dc,    cell_dd,    cell_de,    cell_df,
            /*E*/ loopnzDc,   loopzDc,    loopDc,     jecxzDc,    inDc,       inDc,       outDc,      outDc,      callDc,     jmpDc,      jmpDc,      jmpDc,      inDc,       inDc,       outDc,      outDc,
            /*F*/ lock,       null,       repnz,      repz,       hltDc,      null,       cell_f6f7,  cell_f6f7,  null,       stcDc,      cliDc,      stiDc,      cldDc,      stdDc,      cell_fe,    cell_ff
    )

    // should be faster http://www.onjava.com/2002/06/12/trove ...
    // private val cache = HashMap<Long, AX86Instruction>(1024*1024)
    private val cache = THashMap<Long, AX86Instruction>(1024*1024)

    private val cacheStream = CachedMemoryStream(cpu.ports.mem, 0, cs.reg, INSTRUCTION)

    fun decode(where: Long): AX86Instruction {
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
        val stream = x86OperandStream(cacheStream)

        val prefixes = Prefixes(core)
        while (true) {
            val entry = e_opcode.lookup(stream)
            if (entry == null) {
                var result = 0L
                stream.data.forEach { result = (result shl 8) or it.asULong }
                throw DecoderException(result, where)
            }
            when (entry) {
                is ADecoder<*> -> {
                    val insn = entry.decode(stream, prefixes)
                    insn.ea = core.cpu.regs.eip
//                    cache[(ss shl(32)) or where] = insn
                    return insn
                }
                is Prefix.Repnz -> prefixes.string = StringPrefix.REPNZ
                is Prefix.Repz -> prefixes.string = StringPrefix.REPZ
                is Prefix.Segment -> prefixes.segmentOverride = entry.ssr
                is Prefix.Operand -> prefixes.operandOverride = true
                is Prefix.Address -> prefixes.addressOverride = true
                is Prefix.Lock -> prefixes.lock = true
            }
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        throw UnsupportedOperationException("not implemented")
    }
}