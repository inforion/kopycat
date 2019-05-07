package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.arm.enums.Flags
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 16.01.18
 */

class IPSRBank(cpu: AARMCore) : ARegistersBank<AARMCore, Flags>(cpu, Flags.values(), bits = 32) {
    override val name: String = "ARM Interrupt Program Status Register Bank"
    var exceptionNumber by fieldOf(ARMRegister.PSR.IPSR, 5, 0)
}