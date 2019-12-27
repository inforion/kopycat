package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.mips.enums.eFCR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 05/06/16.
 */
class FCRBank(core: MipsCore) : ARegistersBank<MipsCore, eFCR>(core, eFCR.values(), bits = 32) {
    override val name: String = "FPU Control Registers"
}
