package ru.inforion.lab403.kopycat.cores.ppc.enums.systems

import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_SPE





enum class eSPR_SPE(val id: Int,
                    val dest: PPCRegister,
                    val moveTo: SPR.Access,
                    val moveFrom: SPR.Access) {

    // Signal Processing
    //Accumulator(eUISA_SPE.Accumulator.id, PPCRegister_SPE.UISAext.Accumulator, SPR.Access.yes, SPR.Access.yes),
    SPEFSCR(eUISA_SPE.SPEFSCR.id, PPCRegister_SPE.UISAext.SPEFSCR, SPR.Access.yes, SPR.Access.yes);

    fun toSPR() : SPR = SPR(this.name, this.id, this.dest, this.moveTo, this.moveFrom)

    companion object {
        fun toList() : List<SPR> = values().map { it.toSPR() }
    }

}