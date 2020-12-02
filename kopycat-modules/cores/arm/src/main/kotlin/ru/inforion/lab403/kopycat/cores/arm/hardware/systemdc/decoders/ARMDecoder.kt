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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition.UN
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.ExceptionDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.branch.ArmBranchDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.branch.ArmBranchWithLinkDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.coprcessor.MoveCoprocessorDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.coprcessor.MoveCoprocessorFromTwoDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.dataproc.*
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.exceptions.LoadMultipleExceptionReturnDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.exceptions.SubstractExceptionReturn
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.exceptions.SupervisorCallDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.extraloadstore.*
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.hints.HintsDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.hints.MSRImmDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.loadstore.*
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.media.BitFieldExtractDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.media.BitFieldOppDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.media.UnsignedSumAbsDiffDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.miscellaneous.CLZDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.miscellaneous.GetRmDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.miscellaneous.MRSRegDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.miscellaneous.MSRRegSLDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.multiply.*
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.packing.*
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.unconditional.ClearExclusiveDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.unconditional.CpsDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.unconditional.PldDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support.Table
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.immediate.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.register.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.registerShiftedRegister.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch.BLXr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch.BX
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.coprocessor.MCR
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.coprocessor.MCRR
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.coprocessor.MRC
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.exceptions.SVC
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hint.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hmultiply.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.immediate.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.register.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.registerShiftedRegister.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.media.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.misc.CLZ
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.misc.MRS
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.misc.MSRsl
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.multiply.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating.SSAT
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating.SSAT16
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating.USAT
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating.USAT16
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.shift.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special.PKH
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system.LDM
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system.LDMur
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system.STM
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system.STMur
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unconditional.CPS
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unconditional.PLD
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unpacking.*
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special.MSR as MSRApplication
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system.MSR as MSRSystem

class ARMDecoder(cpu: AARMCore): ADecoder<AARMInstruction>(cpu) {
    companion object {
        @Transient private val log = logger()
    }

    private val undefined = ExceptionDecoder.Undefined(cpu)
    private val unpredictable = ExceptionDecoder.Unpredictable(cpu)

    private val andr = DataProcessingRegDecoder(cpu, ::ANDr)
    private val eorr = DataProcessingRegDecoder(cpu, ::EORr)
    private val subr = DataProcessingRegDecoder(cpu, ::SUBr)
    private val rsbr = DataProcessingRegDecoder(cpu, ::RSBr)
    private val addr = DataProcessingRegDecoder(cpu, ::ADDr)
    private val adcr = DataProcessingRegDecoder(cpu, ::ADCr)
    private val sbcr = DataProcessingRegDecoder(cpu, ::SBCr)
    private val rscr = DataProcessingRegDecoder(cpu, ::RSCr)
    private val tstr = DataProcessingRegDecoder(cpu, ::TSTr)
    private val teqr = DataProcessingRegDecoder(cpu, ::TEQr)
    private val cmpr = DataProcessingRegDecoder(cpu, ::CMPr)
    private val cmnr = DataProcessingRegDecoder(cpu, ::CMNr)
    private val orrr = DataProcessingRegDecoder(cpu, ::ORRr)
    private val movr = DataProcessingRegDecoder(cpu, ::MOVr)
    private val movspclr = SubstractExceptionReturn.A2(cpu)
    private val bicr = DataProcessingRegDecoder(cpu, ::BICr)
    private val mvnr = DataProcessingRegDecoder(cpu, ::MVNr)
    private val dataProcessingAndMiscellaneousInstruction = null

    private val lsli = DataProcessingShiftImmDecoder(cpu, ::LSLi)
    private val asri = DataProcessingShiftImmDecoder(cpu, ::ASRi)
    private val lsri = DataProcessingShiftImmDecoder(cpu, ::LSRi)
    private val rrx  = DataProcessingShiftImmDecoder(cpu, ::RRX)
    private val rori = DataProcessingShiftImmDecoder(cpu, ::RORi)

    private val moveRegister = Table("Move (register)",
            arrayOf(20, 15..12),
            arrayOf("not 1, not 1111" to movr,
                    "    1,     1111" to movspclr)) // See B9.3.20


    private val dataProcessingRegister = Table("Data-processing (register)",
            arrayOf(24..20, 6..5, 11..7),
            arrayOf("0000x,  - ,       -   " to andr,
                    "0001x,  - ,       -   " to eorr,
                    "0010x,  - ,       -   " to subr,
                    "0011x,  - ,       -   " to rsbr,
                    "0100x,  - ,       -   " to addr,
                    "0101x,  - ,       -   " to adcr,
                    "0110x,  - ,       -   " to sbcr,
                    "0111x,  - ,       -   " to rscr,
                    "10xx0,  - ,       -   " to dataProcessingAndMiscellaneousInstruction,
                    "10001,  - ,       -   " to tstr,
                    "10011,  - ,       -   " to teqr,
                    "10101,  - ,       -   " to cmpr,
                    "10111,  - ,       -   " to cmnr,
                    "1100x,  - ,       -   " to orrr,
                    "1101x, 00 ,     00000 " to moveRegister,
                    "1101x, 00 , not 00000 " to lsli,
                    "1101x, 01 ,       -   " to lsri,
                    "1101x, 10 ,       -   " to asri,
                    "1101x, 11 ,     00000 " to rrx,
                    "1101x, 11 , not 00000 " to rori,
                    "1110x,  - ,       -   " to bicr,
                    "1111x,  - ,       -   " to mvnr))

    private val andrsr = DataProcessingRSRDecoder(cpu, ::ANDrsr)
    private val eorrsr = DataProcessingRSRDecoder(cpu, ::EORrsr)
    private val subrsr = DataProcessingRSRDecoder(cpu, ::SUBrsr)
    private val rsbrsr = DataProcessingRSRDecoder(cpu, ::RSBrsr)
    private val addrsr = DataProcessingRSRDecoder(cpu, ::ADDrsr)
    private val adcrsr = DataProcessingRSRDecoder(cpu, ::ADCrsr)
    private val sbcrsr = DataProcessingRSRDecoder(cpu, ::SBCrsr)
    private val rscrsr = DataProcessingRSRDecoder(cpu, ::RSCrsr)
    private val tstrsr = DataProcessingRSRDecoder(cpu, ::TSTrsr)
    private val teqrsr = DataProcessingRSRDecoder(cpu, ::TEQrsr)
    private val cmprsr = DataProcessingRSRDecoder(cpu, ::CMPrsr)
    private val cmnrsr = DataProcessingRSRDecoder(cpu, ::CMNrsr)
    private val orrrsr = DataProcessingRSRDecoder(cpu, ::ORRrsr)
    private val bicrsr = DataProcessingRSRDecoder(cpu, ::BICrsr)
    private val mvnrsr = DataProcessingRSRDecoder(cpu, ::MVNrsr)

    private val lslr = DataProcessingShiftRegDecoder(cpu, ::LSLr)
    private val lsrr = DataProcessingShiftRegDecoder(cpu, ::LSRr)
    private val asrr = DataProcessingShiftRegDecoder(cpu, ::ASRr)
    private val rorr = DataProcessingShiftRegDecoder(cpu, ::RORr)

    private val dataProcessingRegisterShiftedRegister = Table("Data-processing (register-shifted register)",
            arrayOf(24..20, 6..5),
            arrayOf("0000x, -  " to andrsr,
                    "0001x, -  " to eorrsr,
                    "0010x, -  " to subrsr,
                    "0011x, -  " to rsbrsr,
                    "0100x, -  " to addrsr,
                    "0101x, -  " to adcrsr,
                    "0110x, -  " to sbcrsr,
                    "0111x, -  " to rscrsr,
                    "10xx0, -  " to dataProcessingAndMiscellaneousInstruction,
                    "10001, -  " to tstrsr,
                    "10011, -  " to teqrsr,
                    "10101, -  " to cmprsr,
                    "10111, -  " to cmnrsr,
                    "1100x, -  " to orrrsr,
                    "1101x, 00 " to lslr,
                    "1101x, 01 " to lsrr,
                    "1101x, 10 " to asrr,
                    "1101x, 11 " to rorr,
                    "1110x, -  " to bicrsr,
                    "1111x, -  " to mvnrsr))

    private val bx     = GetRmDecoder(cpu, ::BX)
    private val blxi   = ArmBranchWithLinkDecoder.A1(cpu)
    private val blxr   = GetRmDecoder(cpu, ::BLXr)
//    private val mrsbr =
//    private val msrbr =
    private val mrs = MRSRegDecoder(cpu, ::MRS)
    private val msrrsl = MSRRegSLDecoder(cpu, ::MSRsl)
    private val clz = CLZDecoder(cpu, ::CLZ)

    // See A5.2.12
    private val miscellaneousInstructions = Table("Miscellaneous instructions",
            arrayOf(6..4, 9, 22..21, 19..16),
            arrayOf(//"000, 1, x0, xxxx" to mrsbr,
//                    "000, 1, x1, xxxx" to msrbr,
                    "000, 0, x0, xxxx" to mrs,
//                    "000, 1, 01, xx00" to msrr,
                    "000, 0, 01, xx01" to msrrsl, // See B9.3.12
                    "000, 0, 01, xx1x" to msrrsl,
                    "000, 0, 11,  -  " to msrrsl,
                    "001, -, 11,  -  " to clz,
                    "001, -, 01,  -  " to bx,
                    "011, -, 01,  -  " to blxr))

    private val smlaxy = Signed16BitMultiplyDecoder(cpu, false, ::SMLAxy)
    private val smulwx = Signed16x32BitMultiplyDecoder(cpu, ::SMULWx)
    private val smlawx = Signed16x32BitMultiplyDecoder(cpu, ::SMLAWx)
    private val smlalxy = Signed16BitMultiplyDecoder(cpu, true, ::SMLALxy)
    private val smulxy = Signed16BitMultiplyDecoder(cpu, false, ::SMULxy)

    private val halfwordMultiplyAccumulate = Table("Halfword multiply and multiply accumulate",
            arrayOf(22..21, 5),
            arrayOf("00, -" to smlaxy,
                    "01, 0" to smlawx,
                    "01, 1" to smulwx,
                    "10, -" to smlalxy,
                    "11, -" to smulxy))

    private val strex = StoreExclusiveDecoder(cpu, ::STREX)
    private val ldrex = LoadExclusiveDecoder(cpu, ::LDREX)
    private val strexd = StoreExclusiveDoublewordDecoder(cpu)
    private val ldrexd = LoadExclusiveDoublewordDecoder(cpu)
    private val strexb = StoreExclusiveDecoder(cpu, ::STREXB)
    private val ldrexb = LoadExclusiveDecoder(cpu, ::LDREXB)
    private val strexh = StoreExclusiveDecoder(cpu, ::STREXH)
    private val ldrexh = LoadExclusiveDecoder(cpu, ::LDREXH)

    private val synchronizationPrimitives = Table("Synchronization primitives",
            arrayOf(23..20),
            arrayOf("1000" to strex,
                    "1001" to ldrex,
                    "1010" to strexd,
                    "1011" to ldrexd,
                    "1100" to strexb,
                    "1101" to ldrexb,
                    "1110" to strexh,
                    "1111" to ldrexh))

    private val strhr = LoadStoreHalfwordRegDecoder(cpu, ::STRHr)
    private val ldrhr = LoadStoreHalfwordRegDecoder(cpu, ::LDRHr)
    private val strhi = LoadStoreHalfwordImmDecoder(cpu, true, ::STRHi)
    private val ldrhi = LoadStoreHalfwordImmDecoder(cpu, false, ::LDRHi)
    private val ldrhl = LoadStoreHalfwordLitDecoder(cpu, ::LDRHl)
    private val ldrdr = LoadStoreDualRegDecoder(cpu, ::LDRDr)
    private val ldrsbr = LoadStoreHalfwordRegDecoder(cpu, ::LDRSBr)
    private val ldrdi = LoadStoreDualImmDecoder(cpu, false, ::LDRDi)
    private val ldrdl = LoadStoreDualLitDecoder(cpu, ::LDRDl)
    private val ldrsbi = LoadStoreHalfwordImmDecoder(cpu, false, ::LDRSBi)
    private val ldrsbl = LoadStoreHalfwordLitDecoder(cpu, ::LDRSBl)
    private val strdr = LoadStoreDualRegDecoder(cpu, ::STRDr)
    private val ldrshr = LoadStoreHalfwordRegDecoder(cpu, ::LDRSHr)
    private val strdi = LoadStoreDualImmDecoder(cpu, true, ::STRDi)
    private val ldrshi = LoadStoreHalfwordImmDecoder(cpu, false, ::LDRSHi)
    private val ldrshl = LoadStoreHalfwordLitDecoder(cpu, ::LDRSHl)

    private val extraLoadStoreInstructions = Table("Extra load/store instructions",
            arrayOf(6..5, 24..20, 19..16),
            arrayOf("01, xx0x0,        -" to strhr,
                    "01, xx0x1,        -" to ldrhr,
                    "01, xx1x0,        -" to strhi,
                    "01, xx1x1, not 1111" to ldrhi,
                    "01, xx1x1,     1111" to ldrhl,
                    "10, xx0x0,        -" to ldrdr,
                    "10, xx0x1,        -" to ldrsbr,
                    "10, xx1x0, not 1111" to ldrdi,
                    "10, xx1x0,     1111" to ldrdl,
                    "10, xx1x1, not 1111" to ldrsbi,
                    "10, xx1x1,     1111" to ldrsbl,
                    "11, xx0x0,        -" to strdr,
                    "11, xx0x1,        -" to ldrshr,
                    "11, xx1x0,        -" to strdi,
                    "11, xx1x1, not 1111" to ldrshi,
                    "11, xx1x1,     1111" to ldrshl))

    private val extraLoadStoreInstructionsUnprivileged = Table("Extra load/store instructions, unprivileged")

    private val mul   = MulDecoder(cpu, ::MUL)
    private val mla   = MultipliesDecoder(cpu, false, ::MLA)
    private val umaal = MultipliesLongDecoder(cpu, true, ::UMAAL)
    private val mls   = MultipliesDecoder(cpu, true, ::MLS)
    private val umull = MultipliesLongDecoder(cpu, false, ::UMULL)
    private val umlal = MultipliesLongDecoder(cpu, false, ::UMLAL)
    private val smull = MultipliesLongDecoder(cpu, false, ::SMULL)
    private val smlal = MultipliesLongDecoder(cpu, false, ::SMLAL)

    private val multiplyAndMultiplyAccumulate = Table("Multiply and multiply accumulate",
            arrayOf(23..20),
            arrayOf("000x" to mul,
                    "001x" to mla,
                    "0100" to umaal,
                    "0101" to undefined,
                    "0110" to mls,
                    "0111" to undefined,
                    "100x" to umull,
                    "101x" to umlal,
                    "110x" to smull,
                    "111x" to smlal))

    private val andi = DataProcessingImmCarryDecoder(cpu, ::ANDi)
    private val eori = DataProcessingImmCarryDecoder(cpu, ::EORi)
    private val subi = DataProcessingImmDecoder(cpu, ::SUBi)
    private val rsbi = DataProcessingImmDecoder(cpu, ::RSBi)
    private val addi = DataProcessingImmDecoder(cpu, ::ADDi)
    private val adci = DataProcessingImmDecoder(cpu, ::ADCi)
    private val sbci = DataProcessingImmDecoder(cpu, ::SBCi)
    private val rsci = DataProcessingImmDecoder(cpu, ::RSCi)
    private val tsti = DataProcessingImmCarryDecoder(cpu, ::TSTi)
    private val teqi = DataProcessingImmCarryDecoder(cpu, ::TEQi)
    private val cmpi = DataProcessingImmDecoder(cpu, ::CMPi)
    private val cmni = DataProcessingImmDecoder(cpu, ::CMNi)
    private val orri = DataProcessingImmCarryDecoder(cpu, ::ORRi)
    private val bici = DataProcessingImmCarryDecoder(cpu, ::BICi)
    private val mvni = DataProcessingImmCarryDecoder(cpu, ::MVNi)
    private val movi = MovImmediateDecoder.A1(cpu)
    private val adra = ArmAdrDecoder.A1(cpu)
    private val adrs = ArmAdrDecoder.A2(cpu)

    private val dataProcessingImmediate = Table("Data-processing (immediate)",
            arrayOf(24..20, 19..16),
            arrayOf("0000x,         " to andi,
                    "0001x,         " to eori,
                    "0010x, not 1111" to subi,
                    "0010x,     1111" to adrs,
                    "0011x,         " to rsbi,
                    "0100x, not 1111" to addi,
                    "0100x,     1111" to adra,
                    "0101x,         " to adci,
                    "0110x,         " to sbci,
                    "0111x,         " to rsci,
                    "10xx0,         " to dataProcessingAndMiscellaneousInstruction,
                    "10001,         " to tsti,
                    "10011,         " to teqi,
                    "10101,         " to cmpi,
                    "10111,         " to cmni,
                    "1100x,         " to orri,
                    "1101x,         " to movi,
                    "1110x,         " to bici,
                    "1111x,         " to mvni))

    // "16-bit immediate load, MOV (immediate)"
    private val load16Immediate = MovImmediateDecoder.A2(cpu)
    // "High halfword 16-bit immediate load"
    private val highHalfword16Immediate = MovtDecoder(cpu)

    private val nop = HintsDecoder(cpu, ::NOP)
    private val yld = HintsDecoder(cpu, ::YIELD)
    private val wfe = HintsDecoder(cpu, ::WFE)
    private val wfi = HintsDecoder(cpu, ::WFI)
    private val sev = HintsDecoder(cpu, ::SEV)
    private val dbg = HintsDecoder(cpu, ::DBG)
    private val msriApplication = MSRImmDecoder(cpu, ::MSRApplication)
    private val msriSystem = MSRImmDecoder(cpu, ::MSRSystem)

    private val hintsMsrImm = Table("MSR (immediate), and hints",
            arrayOf(22, 19..16, 7..0),
            arrayOf("0, 0000,      00000000" to nop,
                    "0, 0000,      00000001" to yld,
                    "0, 0000,      00000010" to wfe,
                    "0, 0000,      00000010" to wfi,
                    "0, 0000,      00000010" to sev,
                    "0, 0000,      00000010" to dbg,
                    "0, 0100,      -"        to msriApplication,
                    "0, 1x00,      -"        to msriApplication,
                    "0, xx01,      -"        to msriSystem,
                    "0, xx1x,      -"        to msriSystem,
                    "1, -,         -"        to msriSystem))

    private val dataProcessing = Table("Data-processing and miscellaneous",
            arrayOf(25, 24..20, 7..4),
            arrayOf("0, not 10xx0, xxx0" to dataProcessingRegister,
                    "0, not 10xx0, 0xx1" to dataProcessingRegisterShiftedRegister,
                    "0,     10xx0, 0xxx" to miscellaneousInstructions,
                    "0,     10xx0, 1xx0" to halfwordMultiplyAccumulate,
                    "0,     0xxxx, 1001" to multiplyAndMultiplyAccumulate,
                    "0,     1xxxx, 1001" to synchronizationPrimitives,
                    "0, not 0xx1x, 1011" to extraLoadStoreInstructions,
                    "0, not 0xx1x, 11x1" to extraLoadStoreInstructions,
                    "0,     0xx1x, 1011" to extraLoadStoreInstructionsUnprivileged,
                    "0,     0xx1x, 11x1" to extraLoadStoreInstructions,
                    "1, not 10xx0,     " to dataProcessingImmediate,
                    "1,     10000,     " to load16Immediate,
                    "1,     10100,     " to highHalfword16Immediate,
                    "1,     10x10,     " to hintsMsrImm))

    private val stri = LoadStoreImmDecoder(cpu, true, false, ::STRi)
    private val strr = LoadStoreRegDecoder(cpu, false, ::STRr)
    private val strti = LoadStoreUnpriviledgedDecoder.A1(cpu, false, ::STRT)
    private val strtr = LoadStoreUnpriviledgedDecoder.A2(cpu, false, ::STRT)
    private val ldri = LoadStoreImmDecoder(cpu, false, false, ::LDRi)
    private val ldrl = LoadStoreLitDecoder(cpu, false, ::LDRL)
    private val ldrr = LoadStoreRegDecoder(cpu, false, ::LDRr)
    private val ldrti = LoadStoreUnpriviledgedDecoder.A1(cpu, true, ::LDRT)
    private val ldrtr = LoadStoreUnpriviledgedDecoder.A2(cpu, true, ::LDRT)
    private val strbi = LoadStoreImmDecoder(cpu, true, true, ::STRBi)
    private val strbr = LoadStoreRegDecoder(cpu, true, ::STRBr)
    private val strbti = LoadStoreUnpriviledgedDecoder.A1(cpu, true, ::STRBT)
    private val strbtr = LoadStoreUnpriviledgedDecoder.A2(cpu, true, ::STRBT)
    private val ldrbi = LoadStoreImmDecoder(cpu, false, true, ::LDRBi)
    private val ldrbl = LoadStoreLitDecoder(cpu, true, ::LDRBL)
    private val ldrbr = LoadStoreRegDecoder(cpu, true, ::LDRBr)
    private val ldrbti = LoadStoreUnpriviledgedDecoder.A1(cpu, true, ::LDRBT)
    private val ldrbtr = LoadStoreUnpriviledgedDecoder.A2(cpu, true, ::LDRBT)

    // careful: order of lines in table is important!
    private val loadStoreWordAndUnsignedByte = Table("Load/store word and unsigned byte",
            arrayOf(25, 24..20, 4, 19..16),
            arrayOf("0, xx0x0 not 0x010, -,        -" to stri,
                    "1, xx0x0 not 0x010, 0,        -" to strr,
                    "0, 0x010          , -,        -" to strti,
                    "1, 0x010          , 0,        -" to strtr,
                    "0, xx0x1 not 0x011, -,     1111" to ldrl,
                    "0, xx0x1 not 0x011, -, not 1111" to ldri,
                    "1, xx0x1 not 0x011, 0,        -" to ldrr,
                    "0, 0x011          , -,        -" to ldrti,
                    "1, 0x011          , 0,        -" to ldrtr,
                    "0, xx1x0 not 0x110, -,        -" to strbi,
                    "1, xx1x0 not 0x110, 0,        -" to strbr,
                    "0, 0x110          , -,        -" to strbti,
                    "1, 0x110          , 0,        -" to strbtr,
                    "0, xx1x1 not 0x111, -,     1111" to ldrbl,
                    "0, xx1x1 not 0x111, -, not 1111" to ldrbi,
                    "1, xx1x1 not 0x111, 0,        -" to ldrbr,
                    "0, 0x111          , -,        -" to ldrbti,
                    "1, 0x111          , 0,        -" to ldrbtr))

    private val parallelAddSubSigned = Table("Parallel addition and subtraction, signed")
    private val parallelAddSubUnsigned = Table("Parallel addition and subtraction, unsigned")

    private val pkh     = PackingDecoder(cpu, ::PKH)
    private val sxtab16 = xXTAxDecoder(cpu, ::SXTAB16)
    private val sxtb16  = xXTxDecoder(cpu, ::SXTB16)
    private val sel     = ByteSelDecoder(cpu, ::SEL)
    private val ssat    = SatDecoder(cpu,::SSAT)
    private val ssat16  = Sat16Decoder(cpu, ::SSAT16)
    private val sxtab   = xXTAxDecoder(cpu, ::SXTAB)
    private val sxtb    = xXTxDecoder(cpu, ::SXTB)
    private val rev     = ReverseDecoder(cpu, ::REV)
    private val sxtah   = xXTAxDecoder(cpu, ::SXTAH)
    private val sxth    = xXTxDecoder(cpu, ::SXTH)
    private val rev16   = ReverseDecoder(cpu, ::REV16)
    private val uxtab16 = xXTAxDecoder(cpu, ::UXTAB16)
    private val uxtb16  = xXTxDecoder(cpu, ::UXTB16)
    private val usat    = SatDecoder(cpu,::USAT)
    private val usat16  = Sat16Decoder(cpu, ::USAT16)
    private val uxtab   = xXTAxDecoder(cpu, ::UXTAB)
    private val uxtb    = xXTxDecoder(cpu, ::UXTB)
    private val rbit    = ReverseDecoder(cpu, ::RBIT)
    private val uxtah   = xXTAxDecoder(cpu, ::UXTAH)
    private val uxth    = xXTxDecoder(cpu, ::UXTH)
    private val revsh   = ReverseDecoder(cpu, ::REVSH)

    private val packUnpackSaturationRev = Table("Packing, unpacking, saturation, and reversal",
            arrayOf(22..20, 7..5, 19..16),
            arrayOf("000, xx0,        -" to pkh,
                    "000, 011, not 1111" to sxtab16,
                    "000, 011,     1111" to sxtb16,
                    "000, 101,        -" to sel,
                    "01x, xx0,        -" to ssat,
                    "010, 001,        -" to ssat16,
                    "010, 011, not 1111" to sxtab,
                    "010, 011,     1111" to sxtb,
                    "011, 001,        -" to rev,
                    "011, 011, not 1111" to sxtah,
                    "011, 011,     1111" to sxth,
                    "011, 101,        -" to rev16,
                    "100, 011, not 1111" to uxtab16,
                    "100, 011,     1111" to uxtb16,
                    "11x, xx0,        -" to usat,
                    "110, 001,        -" to usat16,
                    "110, 011, not 1111" to uxtab,
                    "110, 011,     1111" to uxtb,
                    "111, 001,        -" to rbit,
                    "111, 011, not 1111" to uxtah,
                    "111, 011,     1111" to uxth,
                    "111, 101,        -" to revsh))

    private val signedMulSignUnsignDiv = Table("Signed multiply, signed and unsigned divide")

    private val usad8 = UnsignedSumAbsDiffDecoder(cpu, ::USAD8)
    private val usada8 = UnsignedSumAbsDiffDecoder(cpu, ::USADA8)
    private val sbfx = BitFieldExtractDecoder(cpu, ::SBFX)
    private val bfc = BitFieldOppDecoder(cpu, ::BFC)
    private val bfi = BitFieldOppDecoder(cpu, ::BFI)
    private val ubfx = BitFieldExtractDecoder(cpu, ::UBFX)

    private val mediaInstructions = Table("Media instructions",
            arrayOf(24..20, 7..5, 15..12, 3..0, 31..28),
            arrayOf("000xx,  - ,      -  ,      -  ,      -   " to parallelAddSubSigned,
                    "001xx,  - ,      -  ,      -  ,      -   " to parallelAddSubUnsigned,
                    "01xxx,  - ,      -  ,      -  ,      -   " to packUnpackSaturationRev,
                    "10xxx,  - ,      -  ,      -  ,      -   " to signedMulSignUnsignDiv,
                    "11000, 000,     1111,      -  ,      -   " to usad8,
                    "11000, 000, not 1111,      -  ,      -   " to usada8,
                    "1101x, x10,      -  ,      -  ,      -   " to sbfx,
                    "1110x, x00,      -  ,     1111,      -   " to bfc,
                    "1110x, x00,      -  , not 1111,      -   " to bfi,
                    "1111x, x10,      -  ,      -  ,      -   " to ubfx,
                    "11111, 111,      -  ,      -  ,     1110 " to undefined,
                    "11111, 111,      -  ,      -  , not 1110 " to unpredictable))

    private val stmda = LoadStoreMultipleDecoder(cpu, false, ::STMDA)
    private val ldmda = LoadStoreMultipleDecoder(cpu, true, ::LDMDA)
    private val stm   = LoadStoreMultipleDecoder(cpu, false, ::STM)
    private val ldm   = LoadStoreMultipleDecoder(cpu, true, ::LDM)
    private val stmur = LoadStoreMultipleUserRegistersDecoder(cpu, ::STMur)
    private val ldmur = LoadStoreMultipleUserRegistersDecoder(cpu, ::LDMur)
    private val ldmer = LoadMultipleExceptionReturnDecoder(cpu)
    private val stmdb = LoadStoreMultipleDecoder(cpu, false, ::STMDB)
    private val pop   = PushPopMultipleRegistersDecoder(cpu, true, ::POPmr)
    private val push  = PushPopMultipleRegistersDecoder(cpu, false, ::PUSHmr)
    private val ldmdb = LoadStoreMultipleDecoder(cpu, true, ::LDMDB)
    private val stmib = LoadStoreMultipleDecoder(cpu, false, ::STMIB)
    private val ldmib = LoadStoreMultipleDecoder(cpu, true, ::LDMIB)
    private val b     = ArmBranchDecoder.A1(cpu)

    private val branchBranchWithLinkBlockDataTransfer = Table("Branch, branch with link, and block data transfer",
            arrayOf(25..20, 15, 19..16),
            arrayOf("0000x0, -,         " to stmda,
                    "0000x1, -,         " to ldmda,
                    "0010x0, -,         " to stm,
                    "001001, -,         " to ldm,
                    "001011, -, not 1101" to ldm,
                    "001011, -,     1101" to pop,
                    "010000, -,         " to stmdb,
                    "010010, -, not 1101" to stmdb,
                    "010010, -,     1101" to push,
                    "0100x1, -,         " to ldmdb,
                    "0110x0, -,         " to stmib,
                    "0110x1, -,         " to ldmib,
                    "0xx1x0, -,         " to stmur,
                    "0xx1x1, 0,         " to ldmur,
                    "0xx1x1, 1,         " to ldmer,
                    "10xxxx, -,         " to b,
                    "11xxxx, -,         " to blxi))

    private val svc = SupervisorCallDecoder(cpu, ::SVC)
    private val mcr = MoveCoprocessorDecoder(cpu, ::MCR)
    private val mrc = MoveCoprocessorDecoder(cpu, ::MRC)
    private val mcrr = MoveCoprocessorFromTwoDecoder(cpu, ::MCRR)


    private val coprocessorInstructions = Table("Coprocessor instructions, and Supervisor Call",
            arrayOf(11..8, 25..20, 4, 19..16),
            arrayOf("       -, 11xxxx, -, -" to svc,
                    "not 101x, 000100, -, -" to mcrr,
                    "not 101x, 10xxx0, 1, -" to mcr,
                    "not 101x, 10xxx1, 1, -" to mrc
                    ))

    private val cps = CpsDecoder(cpu, ::CPS)
    private val pld = PldDecoder(cpu, ::PLD)
    private val clrex = ClearExclusiveDecoder(cpu)

    private val advanced = Table("Memory hints, Advanced SIMD instructions, and miscellaneous instructions",
            arrayOf(26..20, 7..4, 19..16),
            arrayOf("0010000, xx0x,     xxx0" to cps,
                    "101x001,    -, not 1111" to pld,
                    "101x001,    -,     1111" to unpredictable,
                    "101x101,    -, not 1111" to pld,
                    "1010111, 0001,        -" to clrex))

    // See A5.7
    private val unconditional = Table("Unconditional instructions",
            arrayOf(27..20, 4, 19..16),
            arrayOf("0xxxxxxx, -, -" to advanced))

    private val iset = Table("Instruction Set",
            arrayOf(31..28, 27..25, 4),
            arrayOf("not 1111, 00x, x" to dataProcessing,
                    "not 1111, 010, x" to loadStoreWordAndUnsignedByte,
                    "not 1111, 011, 0" to loadStoreWordAndUnsignedByte,
                    "not 1111, 011, 1" to mediaInstructions,
                    "not 1111, 10x, x" to branchBranchWithLinkBlockDataTransfer,
                    "not 1111, 11x, x" to coprocessorInstructions,
                    "    1111, xxx, x" to unconditional))

    private val pass = NOP(cpu, 0, UN, 4)

    override fun decode(data: Long): AARMInstruction {
        val cbits = data[31..28].asInt
        val cond = find<Condition> { it.opcode == cbits }?: Condition.AL
        return if (core.cpu.ConditionPassed(cond)) iset.lookup(data, core.cpu.pc).decode(data) else pass
    }
}