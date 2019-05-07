package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Flags
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 16.01.18
 */

class SPRBank(cpu: AARMCore) : ARegistersBank<AARMCore, Flags>(cpu, Flags.values(), bits = 32) {
    override val name: String = "ARM Special Purpose Registers Bank"

//    var pm by bitOf(ARMRegister.SPR.PRIMASK, 0)
//    var npriv by bitOf(ARMRegister.SPR.CONTROL, 0)
//    var spsel by bitOf(ARMRegister.SPR.CONTROL, 1)

    // FOR PERFORMANCE!

    inline var pm: Boolean
        get() = ARMRegister.SPR.PRIMASK.bit(core, 0) == 1
        set(value) { ARMRegister.SPR.PRIMASK.bit(core, 0, value.asInt) }

    inline var npriv: Boolean
        get() = ARMRegister.SPR.CONTROL.bit(core, 0) == 1
        set(value) { ARMRegister.SPR.CONTROL.bit(core, 0, value.asInt) }

    inline var spsel: Boolean
        get() = ARMRegister.SPR.CONTROL.bit(core, 1) == 1
        set(value) { ARMRegister.SPR.CONTROL.bit(core, 1, value.asInt) }
}