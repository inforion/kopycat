package ru.inforion.lab403.kopycat.cores.v850es.hardware.memory

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.v850es.enums.Flags
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore


class FLBank(core: v850ESCore) : ARegistersBank<v850ESCore, Flags>(core, Flags.values(), bits = 32) {
    override val name: String = "Flags Register"

    var z by bitOf(v850esRegister.CTRLR.PSW, Flags.Z.bit)
    var s by bitOf(v850esRegister.CTRLR.PSW, Flags.S.bit)
    var ov by bitOf(v850esRegister.CTRLR.PSW, Flags.OV.bit)
    var cy by bitOf(v850esRegister.CTRLR.PSW, Flags.CY.bit)
    var sat by bitOf(v850esRegister.CTRLR.PSW, Flags.SAT.bit)
    var id by bitOf(v850esRegister.CTRLR.PSW, Flags.ID.bit)
    var ep by bitOf(v850esRegister.CTRLR.PSW, Flags.EP.bit)
    var np by bitOf(v850esRegister.CTRLR.PSW, Flags.NP.bit)
}