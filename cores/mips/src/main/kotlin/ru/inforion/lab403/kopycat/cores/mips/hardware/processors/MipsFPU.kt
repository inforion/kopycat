package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.kopycat.cores.base.abstracts.AFPU
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.FCRBank
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.FPRBank
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class MipsFPU(core: MipsCore, name: String) : AFPU<MipsCore>(core, name) {
    val regs: FPRBank = FPRBank(core)
    val cntrls: FCRBank = FCRBank(core)
}