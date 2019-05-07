package ru.inforion.lab403.kopycat.cores.msp430.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.msp430.enums.Flags
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Register
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by shiftdj on 5/02/18.
 */

class FLBank(core : MSP430Core) : ARegistersBank<MSP430Core, Flags>(core, Flags.values(), bits = 16) {
    override val name: String = "Flags Register"

    var c by bitOf(MSP430Register.GPR.r2, Flags.C.bit)
    var z by bitOf(MSP430Register.GPR.r2, Flags.Z.bit)
    var n by bitOf(MSP430Register.GPR.r2, Flags.N.bit)
    var gie by bitOf(MSP430Register.GPR.r2, Flags.GIE.bit)
    var cpuoff by bitOf(MSP430Register.GPR.r2, Flags.CPUOFF.bit)
    var oscoff by bitOf(MSP430Register.GPR.r2, Flags.OSCOFF.bit)
    var scg0 by bitOf(MSP430Register.GPR.r2, Flags.SCG0.bit)
    var scg1 by bitOf(MSP430Register.GPR.r2, Flags.SCG1.bit)
    var v by bitOf(MSP430Register.GPR.r2, Flags.V.bit)


}