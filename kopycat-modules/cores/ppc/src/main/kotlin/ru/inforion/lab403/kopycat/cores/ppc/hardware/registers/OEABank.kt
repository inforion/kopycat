/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.cores.ppc.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.ppc.enums.eOEA
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Operating environment architecture
class OEABank(core: PPCCore) : ARegistersBank<PPCCore, eOEA>(core, eOEA.values(), bits = 64) {
    override val name: String = "OEA registers"

    //<Configuration registers>
    //Machine state register
    var MSR by valueOf(PPCRegister.OEA.MSR)

    //Processor version register
    var PVR by valueOf(PPCRegister.OEA.PVR)


    //<Memory management registers>
    //Instruction BAT registers
    /*var IBAT0U by valueOf(PPCRegister.OEA.IBAT0U)
    var IBAT0L by valueOf(PPCRegister.OEA.IBAT0L)
    var IBAT1U by valueOf(PPCRegister.OEA.IBAT1U)
    var IBAT1L by valueOf(PPCRegister.OEA.IBAT1L)
    var IBAT2U by valueOf(PPCRegister.OEA.IBAT2U)
    var IBAT2L by valueOf(PPCRegister.OEA.IBAT2L)
    var IBAT3U by valueOf(PPCRegister.OEA.IBAT3U)
    var IBAT3L by valueOf(PPCRegister.OEA.IBAT3L)

    //Data BAT registers
    var DBAT0U by valueOf(PPCRegister.OEA.DBAT0U)
    var DBAT0L by valueOf(PPCRegister.OEA.DBAT0L)
    var DBAT1U by valueOf(PPCRegister.OEA.DBAT1U)
    var DBAT1L by valueOf(PPCRegister.OEA.DBAT1L)
    var DBAT2U by valueOf(PPCRegister.OEA.DBAT2U)
    var DBAT2L by valueOf(PPCRegister.OEA.DBAT2L)
    var DBAT3U by valueOf(PPCRegister.OEA.DBAT3U)
    var DBAT3L by valueOf(PPCRegister.OEA.DBAT3L)*/

    //SDR1
    var SDR1 by valueOf(PPCRegister.OEA.SDR1)

    //Address space register
    var ASR by valueOf(PPCRegister.OEA.ASR)

    //Segment registers
    var SR0 by valueOf(PPCRegister.OEA.SR0)
    var SR1 by valueOf(PPCRegister.OEA.SR1)
    var SR2 by valueOf(PPCRegister.OEA.SR2)
    var SR3 by valueOf(PPCRegister.OEA.SR3)
    var SR4 by valueOf(PPCRegister.OEA.SR4)
    var SR5 by valueOf(PPCRegister.OEA.SR5)
    var SR6 by valueOf(PPCRegister.OEA.SR6)
    var SR7 by valueOf(PPCRegister.OEA.SR7)
    var SR8 by valueOf(PPCRegister.OEA.SR8)
    var SR9 by valueOf(PPCRegister.OEA.SR9)
    var SR10 by valueOf(PPCRegister.OEA.SR10)
    var SR11 by valueOf(PPCRegister.OEA.SR11)
    var SR12 by valueOf(PPCRegister.OEA.SR12)
    var SR13 by valueOf(PPCRegister.OEA.SR13)
    var SR14 by valueOf(PPCRegister.OEA.SR14)
    var SR15 by valueOf(PPCRegister.OEA.SR15)


    //<Exception handling registers>
    //Data address register
    var DAR by valueOf(PPCRegister.OEA.DAR)

    //DSISR
    var DSISR by valueOf(PPCRegister.OEA.DSISR)

    //SPRGs
    var SPRG0 by valueOf(PPCRegister.OEA.SPRG0)
    var SPRG1 by valueOf(PPCRegister.OEA.SPRG1)
    var SPRG2 by valueOf(PPCRegister.OEA.SPRG2)
    var SPRG3 by valueOf(PPCRegister.OEA.SPRG3)

    //Save and restore registers
    var SRR0 by valueOf(PPCRegister.OEA.SRR0)
    var SRR1 by valueOf(PPCRegister.OEA.SRR1)

    //Floating-point exception cause register (optional)
    var FPECR by valueOf(PPCRegister.OEA.FPECR)


    //<Miscellaneous registers>
    //Time base facility (for writing)
    var TBL by valueOf(PPCRegister.OEA.TBL)
    var TBU by valueOf(PPCRegister.OEA.TBU)

    //Data address breakpoint register (optional)
    var DABR by valueOf(PPCRegister.OEA.DABR)

    //TimeBase
    var DEC by valueOf(PPCRegister.OEA.DEC)

    //External access register (optional)
    var EAR by valueOf(PPCRegister.OEA.EAR)

    //Processor identification register (optional))
    var PIR by valueOf(PPCRegister.OEA.PIR)
}