package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.arm.enums.StatusReg
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 16.01.18
 */

class CPSRBank(cpu: AARMCore) : ARegistersBank<AARMCore, StatusReg>(cpu, StatusReg.values(), bits = 32) {
    override val name: String = "Status"

    var q by bitOf(ARMRegister.PSR.CPSR, StatusReg.Q.msb)
    var ITSTATE by fieldOf(
            ARMRegister.PSR.CPSR,
            StatusReg.IT1.msb..StatusReg.IT1.lsb to 7..2,
            StatusReg.IT2.msb..StatusReg.IT2.lsb to 1..0)
    var j by bitOf(ARMRegister.PSR.CPSR, StatusReg.J.msb)
    var ge by fieldOf(ARMRegister.PSR.CPSR, StatusReg.GE.msb, StatusReg.GE.lsb)
    var ENDIANSTATE by bitOf(ARMRegister.PSR.CPSR, StatusReg.E.msb)
    var a by bitOf(ARMRegister.PSR.CPSR, StatusReg.A.msb)
    var i by bitOf(ARMRegister.PSR.CPSR, StatusReg.I.msb)
    var f by bitOf(ARMRegister.PSR.CPSR, StatusReg.F.msb)
    var ISETSTATE by fieldOf(
            ARMRegister.PSR.CPSR,
            StatusReg.J.msb..StatusReg.J.lsb to 1..1,
            StatusReg.T.msb..StatusReg.T.lsb to 0..0)
    var m by fieldOf(ARMRegister.PSR.CPSR, StatusReg.M.msb, StatusReg.M.lsb)
}