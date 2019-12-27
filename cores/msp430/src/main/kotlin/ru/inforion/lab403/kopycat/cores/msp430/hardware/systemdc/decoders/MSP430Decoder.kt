package ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.READ
import ru.inforion.lab403.kopycat.cores.msp430.MSP430Operand
import ru.inforion.lab403.kopycat.cores.msp430.enums.MSP430GPR
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.*
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 12/02/18.
 */

abstract class MSP430Decoder(core: MSP430Core) : ADecoder<AMSP430Instruction>(core) {

    fun decodeFirstOp(aSrc : Int, regInd : Int, nextWord : Long, dtype : Datatype, insSize : Long) : MSP430Operand {
        val imm = MSP430Immediate(dtype, nextWord, true)

        return when (aSrc) {
            0b00 -> when (regInd) {
                MSP430GPR.r3.id -> zero(dtype)
                else -> MSP430Register.gpr(dtype, regInd)
            }
            0b01 -> when (regInd) {
                MSP430GPR.r0.id -> MSP430Displacement(
                        dtype,
                        MSP430Register.gpr(Datatype.WORD, regInd),
                        MSP430Immediate(dtype, nextWord - insSize + Datatype.WORD.bytes, true),
                        READ,
                        0)
                MSP430GPR.r2.id -> MSP430Memory(dtype, READ, nextWord)
                MSP430GPR.r3.id -> one(dtype)
                else -> MSP430Displacement(dtype, MSP430Register.gpr(Datatype.WORD, regInd), imm, READ, 0)
            }
            0b10 -> when (regInd) {
                MSP430GPR.r0.id -> throw GeneralException("Immediate can't be instruction value (MSP430Decoder.kt)")
                MSP430GPR.r2.id -> four(dtype)
                MSP430GPR.r3.id -> two(dtype)
                else -> MSP430Displacement(dtype, MSP430Register.gpr(Datatype.WORD, regInd), zero(dtype), READ, 0)
            }
            0b11 -> when (regInd) {
                MSP430GPR.r0.id -> imm
                MSP430GPR.r2.id -> eight(dtype)
                MSP430GPR.r3.id -> negOne(dtype)
                else -> MSP430Displacement(dtype, MSP430Register.gpr(Datatype.WORD, regInd), zero(dtype), READ, dtype.bytes)
            }
            else -> throw GeneralException("Incorrect optype (MSP430Decoder.kt)")
        }

    }

}