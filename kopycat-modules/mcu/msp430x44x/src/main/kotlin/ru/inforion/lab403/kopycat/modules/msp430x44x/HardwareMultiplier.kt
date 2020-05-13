package ru.inforion.lab403.kopycat.modules.msp430x44x

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.BUS16



class HardwareMultiplier(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS16)
    }

    override val ports = Ports()

    var mode = 0

    //Unsigned multiplication
    val MPY = object : Register(ports.mem, 0x130, WORD, "MPY") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            mode = 0
        }
    }

    //Signed multiplication
    val MPYS = object : Register(ports.mem, 0x132, WORD, "MPYS") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            mode = 1
        }
    }

    //Unsigned multiply-and-accumulate
    val MAC = object : Register(ports.mem, 0x134, WORD, "MAC") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            mode = 2
        }
    }

    //Second operand
    val OP2 = object : Register(ports.mem, 0x138, WORD, "OP2") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            when(mode) {
                0 -> {
                    val result = MPY.data * data
                    RESLO.data = result[15..0]
                    RESHI.data = result[31..16]
                    SUMEXT.data = 0
                }
                1 -> {
                    var sign = false

                    val op1Signed = signext(MPY.data, 16)
                    val op1Unsigned = if (op1Signed < 0) {
                        sign = true
                        -op1Signed
                    }
                    else
                        op1Signed

                    val op2Signed = signext(data, 16)
                    val op2Unsigned = if (op2Signed < 0) {
                        sign = !sign
                        -op2Signed
                    }
                    else
                        op2Signed

                    val resultUnsigned = (op1Unsigned * op2Unsigned).toLong()
                    val resultSigned = if (sign) -resultUnsigned else resultUnsigned

                    RESLO.data = resultSigned[15..0]
                    RESHI.data = resultSigned[31..16]
                    SUMEXT.data = resultSigned[47..32]
                }
                2 -> throw GeneralException("MAC mode isn't implemented in HardwareMultiplier.kt")
                else -> throw GeneralException("Unknown mode in HardwareMultiplier.kt")
            }
        }

    }

    //Result low word
    val RESLO = Register(ports.mem, 0x13A, WORD, "RESLO")

    //Result high word
    val RESHI = Register(ports.mem, 0x13C, WORD, "RESHI")

    //Sum extend
    val SUMEXT = object : Register(ports.mem, 0x13E, WORD, "SUMEXT") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            throw GeneralException("try to write in SUMEXT that is read-only register")
        }
    }
}