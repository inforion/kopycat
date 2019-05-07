package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.arm.enums.GPR
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 13.01.18.
 */

class GPRBank(core: AARMCore) : ARegistersBank<AARMCore, GPR>(core, GPR.values(), bits = 32) {
    override val name: String = "ARM General Purpose Registers Bank"

    var r0        by valueOf(ARMRegister.GPR.R0)
    var r1        by valueOf(ARMRegister.GPR.R1)
    var r2        by valueOf(ARMRegister.GPR.R2)
    var r3        by valueOf(ARMRegister.GPR.R3)
    var r4        by valueOf(ARMRegister.GPR.R4)
    var r5        by valueOf(ARMRegister.GPR.R5)
    var r6        by valueOf(ARMRegister.GPR.R6)
    var r7        by valueOf(ARMRegister.GPR.R7)
    var r8        by valueOf(ARMRegister.GPR.R8)
    var r9        by valueOf(ARMRegister.GPR.R9)
    var r10       by valueOf(ARMRegister.GPR.R10)
    var r11       by valueOf(ARMRegister.GPR.R11)
    var r12       by valueOf(ARMRegister.GPR.R12)
    var spMain    by valueOf(ARMRegister.GPR.SPMain)
    var lr        by valueOf(ARMRegister.GPR.LR)
    var pc        by valueOf(ARMRegister.GPR.PC)
    var spProcess by valueOf(ARMRegister.GPR.SPProcess)
}
