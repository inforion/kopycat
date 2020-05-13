package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.mips.enums.eFPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class FPRBank(core: MipsCore) : ARegistersBank<MipsCore, eFPR>(core, eFPR.values(), bits = 64) {
    override val name: String = "FPU General Purpose Registers"
}