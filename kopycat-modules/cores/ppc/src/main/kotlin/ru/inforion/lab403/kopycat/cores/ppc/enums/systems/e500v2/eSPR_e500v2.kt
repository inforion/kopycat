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
package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2

import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_e500v2



enum class eSPR_e500v2(val id: Int,
                       val dest: PPCRegister,
                       val moveTo: SPR.Access,
                       val moveFrom: SPR.Access) {

    //Interrupt registers

    //Critical SRR 0/1
    CSRR0(eOEA_e500v2.CSRR0.id, PPCRegister_e500v2.OEAext.CSRR0, SPR.Access.yes, SPR.Access.yes),
    CSRR1(eOEA_e500v2.CSRR1.id, PPCRegister_e500v2.OEAext.CSRR1, SPR.Access.yes, SPR.Access.yes),

    //Machine check SRR 0/1 [1]
    MCSRR0(eOEA_e500v2.MCSRR0.id, PPCRegister_e500v2.OEAext.MCSRR0, SPR.Access.yes, SPR.Access.yes),
    MCSRR1(eOEA_e500v2.MCSRR1.id, PPCRegister_e500v2.OEAext.MCSRR1, SPR.Access.yes, SPR.Access.yes),

    //Exception syndrome register
    ESR(eOEA_e500v2.ESR.id, PPCRegister_e500v2.OEAext.ESR, SPR.Access.yes, SPR.Access.yes),

    //Machine check syndrome register [1]
    MCSR(eOEA_e500v2.MCSR.id, PPCRegister_e500v2.OEAext.MCSR, SPR.Access.yes, SPR.Access.yes),

    //MCAR - 573

    //Data exception address register
    DEAR(eOEA_e500v2.DEAR.id, PPCRegister_e500v2.OEAext.DEAR, SPR.Access.yes, SPR.Access.yes),

    //Interrupt vector offset registers 0-15
    IVOR0(eOEA_e500v2.IVOR0.id, PPCRegister_e500v2.OEAext.IVOR0, SPR.Access.yes, SPR.Access.yes),
    IVOR1(eOEA_e500v2.IVOR1.id, PPCRegister_e500v2.OEAext.IVOR1, SPR.Access.yes, SPR.Access.yes),
    IVOR2(eOEA_e500v2.IVOR2.id, PPCRegister_e500v2.OEAext.IVOR2, SPR.Access.yes, SPR.Access.yes),
    IVOR3(eOEA_e500v2.IVOR3.id, PPCRegister_e500v2.OEAext.IVOR3, SPR.Access.yes, SPR.Access.yes),
    IVOR4(eOEA_e500v2.IVOR4.id, PPCRegister_e500v2.OEAext.IVOR4, SPR.Access.yes, SPR.Access.yes),
    IVOR5(eOEA_e500v2.IVOR5.id, PPCRegister_e500v2.OEAext.IVOR5, SPR.Access.yes, SPR.Access.yes),
    IVOR6(eOEA_e500v2.IVOR6.id, PPCRegister_e500v2.OEAext.IVOR6, SPR.Access.yes, SPR.Access.yes),
    IVOR7(eOEA_e500v2.IVOR7.id, PPCRegister_e500v2.OEAext.IVOR7, SPR.Access.yes, SPR.Access.yes),
    IVOR8(eOEA_e500v2.IVOR8.id, PPCRegister_e500v2.OEAext.IVOR8, SPR.Access.yes, SPR.Access.yes),
    IVOR9(eOEA_e500v2.IVOR9.id, PPCRegister_e500v2.OEAext.IVOR9, SPR.Access.yes, SPR.Access.yes),
    IVOR10(eOEA_e500v2.IVOR10.id, PPCRegister_e500v2.OEAext.IVOR10, SPR.Access.yes, SPR.Access.yes),
    IVOR11(eOEA_e500v2.IVOR11.id, PPCRegister_e500v2.OEAext.IVOR11, SPR.Access.yes, SPR.Access.yes),
    IVOR12(eOEA_e500v2.IVOR12.id, PPCRegister_e500v2.OEAext.IVOR12, SPR.Access.yes, SPR.Access.yes),
    IVOR13(eOEA_e500v2.IVOR13.id, PPCRegister_e500v2.OEAext.IVOR13, SPR.Access.yes, SPR.Access.yes),
    IVOR14(eOEA_e500v2.IVOR14.id, PPCRegister_e500v2.OEAext.IVOR14, SPR.Access.yes, SPR.Access.yes),
    IVOR15(eOEA_e500v2.IVOR15.id, PPCRegister_e500v2.OEAext.IVOR15, SPR.Access.yes, SPR.Access.yes),

    // L1 Cache (Read-Only)
    L1CFG0(eOEA_e500v2.L1CFG0.id, PPCRegister_e500v2.OEAext.L1CFG0, SPR.Access.yes, SPR.Access.no),
    L1CFG1(eOEA_e500v2.L1CFG1.id, PPCRegister_e500v2.OEAext.L1CFG1, SPR.Access.yes, SPR.Access.no),


    //Interrupt vector offset registers 32-35 [1]
    IVOR32(eOEA_e500v2.IVOR32.id, PPCRegister_e500v2.OEAext.IVOR32, SPR.Access.yes, SPR.Access.yes),
    IVOR33(eOEA_e500v2.IVOR33.id, PPCRegister_e500v2.OEAext.IVOR33, SPR.Access.yes, SPR.Access.yes),
    IVOR34(eOEA_e500v2.IVOR34.id, PPCRegister_e500v2.OEAext.IVOR34, SPR.Access.yes, SPR.Access.yes),
    IVOR35(eOEA_e500v2.IVOR35.id, PPCRegister_e500v2.OEAext.IVOR35, SPR.Access.yes, SPR.Access.yes),

    //Debug registers

    //Debug control registers 0-2
    DBCR0(eOEA_e500v2.DBCR0.id, PPCRegister_e500v2.OEAext.DBCR0, SPR.Access.yes, SPR.Access.yes),
    DBCR1(eOEA_e500v2.DBCR1.id, PPCRegister_e500v2.OEAext.DBCR1, SPR.Access.yes, SPR.Access.yes),
    DBCR2(eOEA_e500v2.DBCR2.id, PPCRegister_e500v2.OEAext.DBCR2, SPR.Access.yes, SPR.Access.yes),

    //Debug status register
    DBSR(eOEA_e500v2.DBSR.id, PPCRegister_e500v2.OEAext.DBSR, SPR.Access.yes, SPR.Access.yes),

    //Instruction address compare registers 1 and 2
    IAC1(eOEA_e500v2.IAC1.id, PPCRegister_e500v2.OEAext.IAC1, SPR.Access.yes, SPR.Access.yes),
    IAC2(eOEA_e500v2.IAC2.id, PPCRegister_e500v2.OEAext.IAC2, SPR.Access.yes, SPR.Access.yes),

    //Data address compare registers 1 and 2
    DAC1(eOEA_e500v2.DAC1.id, PPCRegister_e500v2.OEAext.DAC1, SPR.Access.yes, SPR.Access.yes),
    DAC2(eOEA_e500v2.DAC2.id, PPCRegister_e500v2.OEAext.DAC2, SPR.Access.yes, SPR.Access.yes),




    //MMU control and status (Read only)

    //MMU configuration [1]
    //MMUCFG(1015),

    //TLB configuration 0/1 [1]
    //TLB0CFG - 688
    //TLB1CFG - 689

    //L1 Cache (Read/Write)

    //L1 cache control/status 0/1 [1]
    L1CSR0(eOEA_e500v2.L1CSR0.id, PPCRegister_e500v2.OEAext.L1CSR0, SPR.Access.yes, SPR.Access.yes),
    L1CSR1(eOEA_e500v2.L1CSR1.id, PPCRegister_e500v2.OEAext.L1CSR1, SPR.Access.yes, SPR.Access.yes),

    // Configuration registers
    SVR(eOEA_e500v2.SVR.id, PPCRegister_e500v2.OEAext.SVR, SPR.Access.no, SPR.Access.yes),
    PIR(eOEA_e500v2.PIR.id, PPCRegister_e500v2.OEAext.PIR, SPR.Access.yes, SPR.Access.yes),
    // PVR - 287

    //TODO: Many skipped

    //Timer/decrement registers
    //Miscellaneous registers

    //Hardware implementation-dependent register 1
    HID0(eOEA_e500v2.HID0.id, PPCRegister_e500v2.OEAext.HID0, SPR.Access.yes, SPR.Access.yes),
    HID1(eOEA_e500v2.HID1.id, PPCRegister_e500v2.OEAext.HID1, SPR.Access.yes, SPR.Access.yes),

    //Branch control and status register [3]
    BUCSR(eOEA_e500v2.BUCSR.id, PPCRegister_e500v2.OEAext.BUCSR, SPR.Access.yes, SPR.Access.yes);


    fun toSPR() : SPR = SPR(this.name, this.id, this.dest, this.moveTo, this.moveFrom)

    companion object {
        fun toList() : List<SPR> = values().map { it.toSPR() }
    }

}