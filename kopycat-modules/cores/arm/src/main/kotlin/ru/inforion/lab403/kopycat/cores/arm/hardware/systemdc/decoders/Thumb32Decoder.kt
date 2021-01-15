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
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.ExceptionDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support.Stub
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support.Table
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb32.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.immediate.ADCi
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.register.ADCr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch.BLXi
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hint.NOP
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.immediate.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.register.ANDr
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.media.MOVT
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore.PUSH
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb.CMPi
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb.MRS
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb.MSRr
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class Thumb32Decoder(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
    companion object {
        @Transient private val log = logger()
    }

    private val undefined = ExceptionDecoder.Undefined(cpu)
    private val unpredictable = ExceptionDecoder.Unpredictable(cpu)

    private val srs = Stub("")
    private val rfe = Stub("")
    private val stm = Stub("")
    private val pop = Stub("")
    private val ldm = Stub("")
    private val push = Thumb32PushDecoder.T2(cpu, ::PUSH)
    private val stmdb = Stub("")
    private val ldmdb = Stub("")

    private val loadStoreMultiple = Table("Load/store multiple",
            arrayOf(24..23, 20, 21, 19..16),
            arrayOf("00, 0, -,  -  " to srs,
                    "00, 1, -,  -  " to rfe,
                    "01, 0, -,  -  " to stm,
                    "01, 1, 1, 1101" to pop,      // Difference with datasheet
                    "01, 1, -,  -  " to ldm,      // W:Rn not 11101
                    "10, 0, 1, 1101" to push,     // Difference with datasheet
                    "10, 0, -,  -  " to stmdb,    // W:Rn not 11101
                    "10, 1, -,  -  " to ldmdb,
                    "11, 0, -,  -  " to srs,
                    "11, 1, -,  -  " to rfe))

    private val strex = Stub("")
    private val ldrex = Stub("")
    private val strdi = Stub("")
    private val ldrdi = Stub("")
    private val ldrdl = Stub("")
    private val strexb = Stub("")
    private val strexh = Stub("")
    private val strexd = Stub("")
    private val tbx = Stub("")
    private val ldrexb = Stub("")
    private val ldrexh = Stub("")
    private val ldrexd = Stub("")

    private val loadStoreTableBranch = Table("Load/store dual, load/store exclusive, table branch",
            arrayOf(24..23, 21..20, 7..4, 19..16),
            arrayOf("00, 00,   - ,       - " to strex,
                    "00, 01,   - ,       - " to ldrex,
                    "0x, 10,   - ,       - " to strdi,
                    "1x, x0,   - ,       - " to strdi,
                    "0x, 11,   - , not 1111" to ldrdi,
                    "1x, x1,   - , not 1111" to ldrdi,
                    "0x, 11,   - ,     1111" to ldrdl,
                    "1x, x1,   - ,     1111" to ldrdl,
                    "01, 00, 0100,    -    " to strexb,
                    "01, 00, 0101,    -    " to strexh,
                    "01, 00, 0111,    -    " to strexd,
                    "01, 01, 0000,    -    " to tbx,
                    "01, 01, 0001,    -    " to tbx,
                    "01, 01, 0100,    -    " to ldrexb,
                    "01, 01, 0101,    -    " to ldrexh,
                    "01, 01, 0111,    -    " to ldrexd))

    private val movr = Stub("")
    private val lsli = Stub("")
    private val lsri = Stub("")
    private val asri = Stub("")
    private val rrx = Stub("")
    private val rori = Stub("")

    private val moveRegImmShift = Table("Move register and immediate shifts",
            arrayOf(5..4, 14..12, 7..6),
            arrayOf("00, 000, 00 " to movr,
                    "00,  - , -  " to lsli, // imm3:imm2 not 00000
                    "01,  - , -  " to lsri,
                    "01,  - , -  " to asri,
                    "00, 000, 00 " to rrx,
                    "00,  - , -  " to rori)) // imm3:imm2 not 00000

    private val tstr = Stub("")
    private val andr = Thumb32DataProcessingRegDecoder(cpu, ::ANDr)
    private val bicr = Stub("")
    private val orrr = Stub("")
    private val ornr = Stub("")
    private val mvnr = Stub("")
    private val teqr = Stub("")
    private val eorr = Stub("")
    private val pkh = Stub("")
    private val cmnr = Stub("")
    private val addr = Stub("")
    private val adcr = Thumb32DataProcessingRegDecoder(cpu, ::ADCr)
    private val sbcr = Stub("")
    private val cmpr = Stub("")
    private val subr = Stub("")
    private val rsbr = Stub("")

    private val dataProcessingShiftedRegister = Table("Data-processing (shifted register)",
            arrayOf(24..21, 19..16, 11..8, 4),
            arrayOf("0000,     -   , 1111, 1 " to tstr,
                    "0000,     -   ,   - , - " to andr, // Rd:S not 11111
                    "0001,     -   ,   - , - " to bicr,
                    "0010, not 1111,   - , - " to orrr,
                    "0010,     1111,   - , - " to moveRegImmShift,
                    "0011, not 1111,   - , - " to ornr,
                    "0011,     1111,   - , - " to mvnr,
                    "0100,     -   , 1111, 1 " to teqr,
                    "0100,     -   ,   - , - " to eorr, // Rd:S not 11111
                    "0110,     -   ,   - , - " to pkh,
                    "1000,     -   , 1111, 1 " to cmnr,
                    "1000,     -   ,   - , - " to addr, // Rd:S not 11111
                    "1010,     -   ,   - , - " to adcr,
                    "1011,     -   ,   - , - " to sbcr,
                    "1101,     -   , 1111, 1 " to cmpr,
                    "1101,     -   ,   - , - " to subr, // Rd:S not 11111
                    "1110,     -   ,   - , - " to rsbr))

    private val copAdvancedFP = Table("Coprocessor, Advanced SIMD, and Floating-point instructions")

    private val tsti = Thumb32DataProcessingImmCarryDecoder(cpu, ::TSTi)
    private val andi = Thumb32DataProcessingImmCarryDecoder(cpu, ::ANDi)
    private val bici = Thumb32DataProcessingImmCarryDecoder(cpu, ::BICi)
    private val orri = Thumb32DataProcessingImmCarryDecoder(cpu, ::ORRi)
    private val moviT2 = Thumb32MovImmDecoder.T2(cpu, ::MOVi)
    private val orni = Stub("")
    private val mvni = Stub("")
    private val teqi = Stub("")
    private val eori = Stub("")
    private val cmni = Stub("")
    private val addi = Stub("")
    private val adci = Thumb32DataProcessingImmDecoder(cpu, ::ADCi)
    private val sbci = Stub("")
    private val cmpi = Thumb32CmpDecoder(cpu, ::CMPi)
    private val subi = Stub("")
    private val rsbi = Stub("")

    private val dataProcessingModImm = Table("Data-processing (modified immediate)",
            arrayOf(24..21, 19..16, 11..8, 4),
            arrayOf("0000,     -   , 1111, - " to tsti,
                    "0000,     -   ,   - , - " to andi, // Rd:S not 11111
                    "0001,     -   ,   - , - " to bici,
                    "0010, not 1111,   - , - " to orri,
                    "0010,     1111,   - , - " to moviT2,
                    "0011, not 1111,   - , - " to orni,
                    "0011,     1111,   - , - " to mvni,
                    "0100,     -   , 1111, 1 " to teqi,
                    "0100,     -   ,   - , - " to eori, // Rd:S not 11111
                    "1000,     -   , 1111, 1 " to cmni,
                    "1000,     -   ,   - , - " to addi, // Rd:S not 11111
                    "1010,     -   ,   - , - " to adci,
                    "1011,     -   ,   - , - " to sbci,
                    "1101,     -   , 1111, - " to cmpi,
                    "1101,     -   ,   - , - " to subi, // Rd:S not 11111
                    "1110,     -   ,   - , - " to rsbi))

    private val moviT3 = Thumb32MovImmDecoder.T3(cpu, ::MOVi)
    private val adr = Stub("")
    private val movt = Thumb32MovtDecoder.MOVT(cpu, ::MOVT)
    private val ssat = Stub("")
    private val ssat16 = Stub("")
    private val sbfx = Stub("")
    private val bfi = Stub("")
    private val bfc = Stub("")
    private val usat = Stub("")
    private val usat16 = Stub("")
    private val ubfx = Stub("")

    private val dataProcessingPlainBinary = Table("Data-processing (plain binary immediate)",
            arrayOf(24..20, 19..16, 14..12, 7..6),
            arrayOf("00000, not 1111,  - ,  - " to addi,
                    "00000,     1111,  - ,  - " to adr,
                    "00100,     -   ,  - ,  - " to moviT3,
                    "01010, not 1111,  - ,  - " to subi,
                    "01010,     1111,  - ,  - " to adr,
                    "01100,     -   ,  - ,  - " to movt,
                    "10000,     -   ,  - ,  - " to ssat,
                    "10010,     -   , 000, 00 " to ssat16,
                    "10010,     -   ,  - ,  - " to ssat, // bits[14:12, 7:6] != 0b00000
                    "10100,     -   ,  - ,  - " to sbfx,
                    "10110, not 1111,  - ,  - " to bfi,
                    "10100,     1111,  - ,  - " to bfc,
                    "11000,     -   ,  - ,  - " to usat,
                    "11010,     -   , 000, 00 " to usat16,
                    "11010,     -   ,  - ,  - " to usat, // bits[14:12, 7:6] != 0b00000
                    "11100,     -   ,  - ,  - " to ubfx))

    private val changeProcessorStateHints = Table("Change Processor State, and hints")

    private val dsb = Thumb32HintsDecoder(cpu, ::NOP)
    private val dmb = Thumb32HintsDecoder(cpu, ::NOP)
    private val isb = Thumb32HintsDecoder(cpu, ::NOP)

    private val miscellaneousControl = Table("Miscellaneous control instructions",
            arrayOf(7..4),
            arrayOf("0100" to dsb,
                    "0101" to dmb,
                    "0110" to isb))

    private val b = Stub("")
    private val bxj = Stub("")
    private val eret = Stub("")
    private val subs_pc = Stub("")
    private val msrr = Thumb32MSRDecoder(cpu, ::MSRr)
    private val mrs = Thumb32MRSDecoder(cpu, ::MRS)
    private val hvc = Stub("")
    private val smc = Stub("")
    private val blx = Thumb32blxImm.T2(cpu, ::BLXi)
    private val bl = Thumb32blxImm.T1(cpu, ::BLXi)

    private val branchesMiscellaneousCtrl = Table("Branches and miscellaneous control",
            arrayOf(14..12, 7..0, 26..20, 11..8),
            arrayOf(
//                    "0x0,         -   , not x111xxx,  -   " to b,
                    "0x0,         -   ,     011100x,  -   " to msrr,
                    "0x0,         -   ,     0111010,  -   " to changeProcessorStateHints,
                    "0x0,         -   ,     0111011,  -   " to miscellaneousControl,
                    "0x0,         -   ,     0111100,  -   " to bxj,
                    "0x0,     00000000,     0111101,  -   " to eret,
                    "0x0, not 00000000,     0111101,  -   " to subs_pc,
                    "0x0,         -   ,     011111x,  -   " to mrs,
                    "000,         -   ,     1111110,  -   " to hvc,
                    "000,         -   ,     1111111,  -   " to smc,
                    "0x1,         -   ,       -    ,  -   " to b,
                    "010,         -   ,     1111111,  -   " to undefined,
                    "1x0,         -   ,       -    ,  -   " to blx,
                    "1x1,         -   ,       -    ,  -   " to bl))

    private val storeSingleDataItem = Table("Store single data item")
    private val loadByteMemHints = Table("Load byte, memory hints")
    private val loadHalfwordsMemHints = Table("Load halfword, memory hints")
    private val loadWords = Table("Load word")
    private val structureLoadStoreASIMD = Table("Advanced SIMD element or structure load/store instructions")
    private val dataProcessingRegister = Table("Data-processing (register)")
    private val multiplyAccumulateDifference = Table("Multiply, multiply accumulate, and absolute difference")
    private val longMulAccumulateDiv = Table("Long multiply, long multiply accumulate, and divide")

    private val iset = Table("32-bit Thumb instruction encoding",
            arrayOf(28..27, 26..20, 15),
            arrayOf("01, 00xx0xx, -" to loadStoreMultiple,
                    "01, 00xx1xx, -" to loadStoreTableBranch,
                    "01, 01xxxxx, -" to dataProcessingShiftedRegister,
                    "01, 1xxxxxx, -" to copAdvancedFP,
                    "10, x0xxxxx, 0" to dataProcessingModImm,
                    "10, x1xxxxx, 0" to dataProcessingPlainBinary,
                    "10,    -   , 1" to branchesMiscellaneousCtrl,
                    "11, 000xxx0, -" to storeSingleDataItem,
                    "11, 00xx001, -" to loadByteMemHints,
                    "11, 00xx011, -" to loadHalfwordsMemHints,
                    "11, 00xx101, -" to loadWords,
                    "11, 00xx111, -" to undefined,
                    "11, 001xxx0, -" to structureLoadStoreASIMD,
                    "11, 010xxxx, -" to dataProcessingRegister,
                    "11, 0110xxx, -" to multiplyAccumulateDifference,
                    "11, 0111xxx, -" to longMulAccumulateDiv,
                    "11, 1xxxxxx, -" to copAdvancedFP))

    override fun decode(data: Long): AARMInstruction {
        return iset.lookup(data, core.cpu.pc).decode(data)
    }
}