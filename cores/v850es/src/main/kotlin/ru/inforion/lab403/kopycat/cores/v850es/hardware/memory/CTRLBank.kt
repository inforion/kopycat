package ru.inforion.lab403.kopycat.cores.v850es.hardware.memory

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.v850es.enums.CTRLR
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 25.05.17.
 */

class CTRLBank(core: v850ESCore) : ARegistersBank<v850ESCore, CTRLR>(core, CTRLR.values(), bits = 32) {
    override val name: String = "Control Registers"
    var eipc by valueOf(v850esRegister.CTRLR.EIPC)
    var eipsw by valueOf(v850esRegister.CTRLR.EIPSW)
    var fepc by valueOf(v850esRegister.CTRLR.FEPC)
    var fepsw by valueOf(v850esRegister.CTRLR.FEPSW)
    var ecr by valueOf(v850esRegister.CTRLR.ECR)
    var psw by valueOf(v850esRegister.CTRLR.PSW)
    var ctpc by valueOf(v850esRegister.CTRLR.CTPC)
    var ctpsw by valueOf(v850esRegister.CTRLR.CTPSW)
    var dbpc by valueOf(v850esRegister.CTRLR.DBPC)
    var dbpsw by valueOf(v850esRegister.CTRLR.DBPSW)
    var ctbp by valueOf(v850esRegister.CTRLR.CTBP)
    var dir by valueOf(v850esRegister.CTRLR.DIR)
}