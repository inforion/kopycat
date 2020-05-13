package ru.inforion.lab403.kopycat.cores.ppc.enums

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR


//Special purpose register instructions
enum class eSPR(val id: Int,
                val dest: PPCRegister,
                val moveTo: SPR.Access,
                val moveFrom: SPR.Access) {
    //TODO: Length for 64 bit systems

    //Base subsystem
    XER(1, PPCRegister.UISA.XER, SPR.Access.yes, SPR.Access.yes),       //64 bit by default
    LR(8, PPCRegister.UISA.LR, SPR.Access.yes, SPR.Access.yes),         //64 bit by default
    CTR(9, PPCRegister.UISA.CTR, SPR.Access.yes, SPR.Access.yes),       //64 bit by default
    DEC(22, PPCRegister.OEA.DEC, SPR.Access.yes, SPR.Access.yes),
    SRR0(26, PPCRegister.OEA.SRR0, SPR.Access.yes, SPR.Access.yes),     //64 bit by default
    SRR1(27, PPCRegister.OEA.SRR1, SPR.Access.yes, SPR.Access.yes),     //64 bit by default
    SPRG3(259, PPCRegister.OEA.SPRG3, SPR.Access.no, SPR.Access.yes),   //64 bit by default
    //TB(268, PPCRegister.OEA.TB, Access.no, Access.yes),           //Not supported because of 64bit-access,
    TBL(268, PPCRegister.VEA.TBL, SPR.Access.no, SPR.Access.yes),       //So we will fill only low halfword (32 bits)
    TBU(269, PPCRegister.VEA.TBU, SPR.Access.no, SPR.Access.yes),
    SPRG0(272, PPCRegister.OEA.SPRG0, SPR.Access.yes, SPR.Access.yes),  //64 bit by default
    SPRG1(273, PPCRegister.OEA.SPRG1, SPR.Access.yes, SPR.Access.yes),  //64 bit by default
    SPRG2(274, PPCRegister.OEA.SPRG2, SPR.Access.yes, SPR.Access.yes),  //64 bit by default
    TBL2(284, PPCRegister.OEA.TBL, SPR.Access.yes, SPR.Access.no),      //TODO: IN <S> IT IS HYPV RESOURCE
    TBU2(285, PPCRegister.OEA.TBU, SPR.Access.yes, SPR.Access.no),      //TODO: IN <S> IT IS HYPV RESOURCE
    PVR(287, PPCRegister.OEA.PVR, SPR.Access.no, SPR.Access.yes)
    //HDSISR(306, PPCRegister.OEA.HDSISR, Access.hypv, Access.yes), //TODO: supervisor
    //HDAR(307, PPCRegister.OEA.HDAR, Access.no, Access.yes),      //64 bit by default, TODO: supervisor
    ;
    fun toSPR() : SPR = SPR(this.name, this.id, this.dest, this.moveTo, this.moveFrom)

    companion object {
        fun toList() : List<SPR> = values().map { it.toSPR() }
    }

}