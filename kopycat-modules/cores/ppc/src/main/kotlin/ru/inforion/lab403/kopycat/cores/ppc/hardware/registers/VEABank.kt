package ru.inforion.lab403.kopycat.cores.ppc.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.ppc.enums.eVEA
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Virtual environment architecture
class VEABank(core: PPCCore) : ARegistersBank<PPCCore, eVEA>(core, eVEA.values(), bits = 32) {
    override val name: String = "VEA registers"

    var TBL by valueOf(PPCRegister.VEA.TBL)
    var TBU by valueOf(PPCRegister.VEA.TBU)
}