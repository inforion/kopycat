package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.exceptions

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.DecodeImmShift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.exceptions.SUBPCLR
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See B9.3.20
object SubstractExceptionReturn {

    class A1(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            TODO("Not implemented")
        }
    }

    class A2(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
            val opc = data[24..21]
            val rn = GPRBank.Operand(data[19..16].toInt())
            val rm = GPRBank.Operand(data[3..0].toInt())
            val registerForm = true
            val type = data[6..5]
            val imm5 = data[11..7]
            val (shiftT, shiftN) = DecodeImmShift(type, imm5)
            return SUBPCLR(
                    core,
                    data,
                    cond,
                    opc,
                    rn,
                    rm,
                    registerForm,
                    shiftT,
                    shiftN.asInt,
                    ARMImmediate(0L, false),
                    4
            )
        }
    }


}