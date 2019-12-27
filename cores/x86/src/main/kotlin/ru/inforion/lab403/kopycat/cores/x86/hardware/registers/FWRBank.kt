package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.FWR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 09.02.18.
 */

class FWRBank(core: x86Core) : ARegistersBank<x86Core, FWR>(core, FWR.values(), bits = 16) {
    override val name: String = "FWR Control Registers"

    var FPUStatusWord by valueOf(x86Register.FWR.SWR)
    var FPUControlWord by valueOf(x86Register.FWR.CWR)
    var FPUTagWord by valueOf(x86Register.FWR.TWR)
    var FPUDataPointer by valueOf(x86Register.FWR.FDP)
    var FPUInstructionPointer by valueOf(x86Register.FWR.FIP)
    var FPULastInstructionOpcode by valueOf(x86Register.FWR.LIO)

    override fun reset() {
        super.reset()
        FPUStatusWord = 0
        FPUControlWord = 0
        FPUTagWord = 0
        FPUDataPointer = 0
        FPUInstructionPointer = 0
        FPULastInstructionOpcode = 0
    }
}