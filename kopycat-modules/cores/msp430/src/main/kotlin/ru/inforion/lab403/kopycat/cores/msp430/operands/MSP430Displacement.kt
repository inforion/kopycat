package ru.inforion.lab403.kopycat.cores.msp430.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Displacement
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class MSP430Displacement(
        dtyp: Datatype,
        reg : MSP430Register,
        off : Immediate<MSP430Core>,
        access: AOperand.Access,
        val inc: Int,
        num: Int = WRONGI
) : Displacement<MSP430Core>(dtyp, reg, off, access, num) {

    override fun value(core: MSP430Core): Long {
        val ea = effectiveAddress(core)
        reg.value(core, reg.value(core) + inc)
        return core.read(dtyp, ea)
    }
}

