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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.enums.ShiftType.*
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.ExceptionDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support.Table
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.immediate.RSBi
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.register.ADCr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.register.CMNr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.register.CMPr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.register.SBCr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch.B
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch.BLXr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch.BX
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.exceptions.SVC
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hint.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.immediate.MOVi
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.register.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.multiply.MUL
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal.REV
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal.REV16
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal.REVSH
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.shift.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special.ADR
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special.IT
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special.SETEND
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system.BKPT
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system.LDM
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system.STM
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unpacking.SXTB
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unpacking.SXTH
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unpacking.UXTB
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unpacking.UXTH
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class Thumb16Decoder(cpu: AARMCore): ADecoder<AARMInstruction>(cpu) {
    companion object {
        @Transient private val log = logger()
    }

    private val undefined = ExceptionDecoder.Undefined(cpu)
    private val lsli   = ThumbShiftImmDecoder(core, LSL, ::LSLi)
    private val lsri   = ThumbShiftImmDecoder(core, LSR, ::LSRi)
    private val asri   = ThumbShiftImmDecoder(core, ASR, ::ASRi)
    private val addrt1 = ThumbArithmRegDecoder(core, ::ADDr)
    private val subr   = ThumbArithmRegDecoder(core, ::SUBr)
    private val addi3  = ThumbArithmImmDecoder.T1(core, ::ADDi)
    private val subi3  = ThumbArithmImmDecoder.T1(core, ::SUBi)
    private val movi   = ThumbMovDecoder.ImmT1(core, ::MOVi)
    private val cmpi   = ThumbCmpDecoder.ImmT1(core, ::CMPi)
    private val addi8  = ThumbArithmImmDecoder.T2(core, ::ADDi)
    private val subi8  = ThumbArithmImmDecoder.T2(core, ::SUBi)

    private val shiftImmAddSubMovCmp = Table("Shift (immediate), add, subtract, move, and compare",
            arrayOf(13..9),
            arrayOf("000xx" to lsli,
                    "001xx" to lsri,
                    "010xx" to asri,
                    "01100" to addrt1,
                    "01101" to subr,
                    "01110" to addi3,
                    "01111" to subi3,
                    "100xx" to movi,
                    "101xx" to cmpi,
                    "110xx" to addi8,
                    "111xx" to subi8))

    private val andr = ThumbLogicRegDecoder(core, ::ANDr)
    private val eorr = ThumbLogicRegDecoder(core, ::EORr)
    private val lslr = ThumbShiftRegDecoder(core, ::LSLr)
    private val lsrr = ThumbShiftRegDecoder(core, ::LSRr)
    private val asrr = ThumbShiftRegDecoder(core, ::ASRr)
    private val adcr = ThumbLogicRegDecoder(core, ::ADCr)
    private val sbcr = ThumbLogicRegDecoder(core, ::SBCr)
    private val rorr = ThumbShiftRegDecoder(core, ::RORr)
    private val tstr = ThumbLogicRegDecoder(core, ::TSTr)
    private val rsbi = ThumbReversalImmDecoder(core, ::RSBi)
    private val cmpr = ThumbCmpDecoder.RegT1(core, ::CMPr)
    private val cmnr = ThumbLogicRegDecoder(core, ::CMNr)
    private val orrr = ThumbLogicRegDecoder(core, ::ORRr)
    private val mul  = ThumbMulDecoder(core, ::MUL)
    private val bicr = ThumbLogicRegDecoder(core, ::BICr)
    private val mvnr = ThumbLogicRegDecoder(core, ::MVNr)

    private val dataProcessing = Table("Data-processing",
            arrayOf(9..6),
            arrayOf("0000" to andr,
                    "0001" to eorr,
                    "0010" to lslr,
                    "0011" to lsrr,
                    "0100" to asrr,
                    "0101" to adcr,
                    "0110" to sbcr,
                    "0111" to rorr,
                    "1000" to tstr,
                    "1001" to rsbi,
                    "1010" to cmpr,
                    "1011" to cmnr,
                    "1100" to orrr,
                    "1101" to mul,
                    "1110" to bicr,
                    "1111" to mvnr))

    private val addrt2 = ThumbAddT2Decoder(cpu, ::ADDr)
    private val cmprh  = ThumbCmpDecoder.RegT2(cpu, ::CMPr)
    private val movrt1 = ThumbMovDecoder.RegT1(cpu, ::MOVr)
    private val bx     = ThumbBranchDecoder(cpu, ::BX)
    private val blxr   = ThumbBranchDecoder(cpu, ::BLXr)

    private val specialDataBranchEx = Table("Special data instructions and branch and exchange",
            arrayOf(9..6),
            arrayOf("00xx" to addrt2,
                    "01xx" to cmprh,
                    "1000" to movrt1,
                    "1001" to movrt1,
                    "101x" to movrt1,
                    "110x" to bx,
                    "111x" to blxr))

    private val strr = ThumbLoadStoreRegDecoder(core, ::STRr)
    private val strhr = ThumbLoadStoreRegDecoder(core, ::STRHr)
    private val strbr = ThumbLoadStoreRegDecoder(core, ::STRBr)
    private val ldrsbr = ThumbLoadStoreRegDecoder(core, ::LDRSBr)
    private val ldrr = ThumbLoadStoreRegDecoder(core, ::LDRr)
    private val ldrhr = ThumbLoadStoreRegDecoder(core, ::LDRHr)
    private val ldrbr = ThumbLoadStoreRegDecoder(core, ::LDRBr)
    private val ldrshr = ThumbLoadStoreRegDecoder(core, ::LDRSHr)
    private val striT1 = ThumbLoadStoreImmDecoder.T1(core, ::STRi)
    private val ldriT1 = ThumbLoadStoreImmDecoder.T1(core, ::LDRi)
    private val strbi = ThumbLoadStoreHBDecoder.Byte(core, ::STRBi)
    private val ldrbi = ThumbLoadStoreHBDecoder.Byte(core, ::LDRBi)
    private val strhi = ThumbLoadStoreHBDecoder.Half(core, ::STRHi)
    private val ldrhi = ThumbLoadStoreHBDecoder.Half(core, ::LDRHi)
    private val striT2 = ThumbLoadStoreImmDecoder.T2(core, ::STRi)
    private val ldriT2 = ThumbLoadStoreImmDecoder.T2(core, ::LDRi)

    private val loadStoreSingle = Table("Load/store single data item",
            arrayOf(15..12, 11..9),
            arrayOf("0101, 000" to strr,
                    "0101, 001" to strhr,
                    "0101, 010" to strbr,
                    "0101, 011" to ldrsbr,
                    "0101, 100" to ldrr,
                    "0101, 101" to ldrhr,
                    "0101, 110" to ldrbr,
                    "0101, 111" to ldrshr,
                    "0110, 0xx" to striT1,
                    "0110, 1xx" to ldriT1,
                    "0111, 0xx" to strbi,
                    "0111, 1xx" to ldrbi,
                    "1000, 0xx" to strhi,
                    "1000, 1xx" to ldrhi,
                    "1001, 0xx" to striT2,
                    "1001, 1xx" to ldriT2))

    private val it = ThumbItDecoder(core, ::IT)
    private val nop = ThumbHintsDecoder(core, ::NOP)
    private val yieldh = ThumbHintsDecoder(core, ::YIELD)
    private val wfe = ThumbHintsDecoder(core, ::WFE)
    private val wfi = ThumbHintsDecoder(core, ::WFI)
    private val sev = ThumbHintsDecoder(core, ::SEV)

    private val ifThenHints = Table("If-Then, and hints",
            arrayOf(7..4, 3..0),
            arrayOf("  - , not 0000" to it,
                    "0000,     0000" to nop,
                    "0001,     0000" to yieldh,
                    "0010,     0000" to wfe,
                    "0011,     0000" to wfi,
                    "0100,     0000" to sev))

    private val addspimm = ThumbSPImmDecoder.T2(core, ::ADDi)
    private val subspimm = ThumbSPImmDecoder.T2(core, ::SUBi)
    private val cbz = ThumbCbzDecoder(core, ::CBZ)
    private val sxth = ThumbExtendDecoder(core, ::SXTH)
    private val sxtb = ThumbExtendDecoder(core, ::SXTB)
    private val uxth = ThumbExtendDecoder(core, ::UXTH)
    private val uxtb = ThumbExtendDecoder(core, ::UXTB)
    private val push = ThumbPushDecoder(core, ::PUSH)
    private val setend = ThumbSetendDecoder(core, ::SETEND)
    private val cps = ThumbCpsDecoder(core, ::CPS)
    private val rev   = ThumbReversalDecoder(core, ::REV)
    private val rev16 = ThumbReversalDecoder(core, ::REV16)
    private val revsh = ThumbReversalDecoder(core, ::REVSH)
    private val pop = ThumbPopDecoder(core, ::POP)
    private val bkpt = ThumbSystemDecoder(core, ::BKPT)

    private val miscellaneous = Table("Miscellaneous 16-bit instructions",
            arrayOf(11..5),
            arrayOf("00000xx" to addspimm,
                    "00001xx" to subspimm,
                    "0001xxx" to cbz,
                    "001000x" to sxth,
                    "001001x" to sxtb,
                    "001010x" to uxth,
                    "001011x" to uxtb,
                    "0011xxx" to cbz,
                    "010xxxx" to push,
                    "0110010" to setend,
                    "0110011" to cps,
                    "1001xxx" to cbz,
                    "101000x" to rev,
                    "101001x" to rev16,
                    "101011x" to revsh,
                    "1011xxx" to cbz,
                    "110xxxx" to pop,
                    "1110xxx" to bkpt,
                    "1111xxx" to ifThenHints))

    private val bt1 = ThumbCondBranchDecoder.T1(cpu, ::B)
    private val svc = ThumbSystemDecoder(core, ::SVC)

    private val condBranch = Table("Conditional branch, and Supervisor Call",
            arrayOf(11..8),
            arrayOf("not 111x" to bt1,
                    "1110" to undefined,
                    "1111" to svc))

    private val ldrl = ThumbLoadLiteralDecoder(core)
    private val adr = ThumbAdrDecoder(core, ::ADR)
    private val addsp = ThumbSPImmDecoder.T1(core, ::ADDi)
    private val stm = ThumbMultipleDecoder(core, ::STM)
    private val ldm = ThumbMultipleDecoder(core, ::LDM)
    private val bt2 = ThumbCondBranchDecoder.T2(cpu, ::B)

    private val iset = Table("16-bit Thumb instruction encoding",
            arrayOf(15..10),
            arrayOf("00xxxx" to shiftImmAddSubMovCmp,
                    "010000" to dataProcessing,
                    "010001" to specialDataBranchEx,
                    "01001x" to ldrl,
                    "0101xx" to loadStoreSingle,
                    "011xxx" to loadStoreSingle,
                    "100xxx" to loadStoreSingle,
                    "10100x" to adr,
                    "10101x" to addsp,
                    "1011xx" to miscellaneous,
                    "11000x" to stm,
                    "11001x" to ldm,
                    "1101xx" to condBranch,
                    "11100x" to bt2))

    override fun decode(data: Long): AARMInstruction {
        val decoder = iset.lookup(data, core.cpu.pc)
//        log.info { "Decoder = $decoder" }
        return decoder.decode(data)
    }
}