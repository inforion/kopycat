package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.arm.enums.PSR
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 18.01.18
 */

class PSRBank(cpu: AARMCore) : ARegistersBank<AARMCore, PSR>(cpu, PSR.values(), bits = 32) {
    override val name: String = "ARM Program Status Registers Bank"

    var apsr by valueOf(ARMRegister.PSR.APSR)
    var ipsr by valueOf(ARMRegister.PSR.IPSR)
    var epsr by valueOf(ARMRegister.PSR.EPSR)
    var cpsr by valueOf(ARMRegister.PSR.CPSR)
    var spsr by valueOf(ARMRegister.PSR.SPSR)
}