package ru.inforion.lab403.kopycat.cores.ppc.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.ppc.enums.eXER
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class XERBank(core : PPCCore) : ARegistersBank<PPCCore, eXER>(core, arrayOf(), bits = 32) {
    override val name: String = "XER Register"

    var SO by bitOf(PPCRegister.UISA.XER, eXER.SO.bit)
    var OV by bitOf(PPCRegister.UISA.XER, eXER.OV.bit)
    var CA by bitOf(PPCRegister.UISA.XER, eXER.CA.bit)
    var BC by fieldOf(PPCRegister.UISA.XER, eXER.BCmsb.bit, eXER.BClsb.bit)

}