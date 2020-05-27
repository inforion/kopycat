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
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore




//User instruction set architecture
class UISABank(core: PPCCore) : ARegistersBank<PPCCore, eUISA>(core, eUISA.values(), bits = 32) {
    override val name: String = "UISA registers"

    //General-purpose registers
    var GPR0 by valueOf(PPCRegister.UISA.GPR0)
    var GPR1 by valueOf(PPCRegister.UISA.GPR1)
    var GPR2 by valueOf(PPCRegister.UISA.GPR2)
    var GPR3 by valueOf(PPCRegister.UISA.GPR3)
    var GPR4 by valueOf(PPCRegister.UISA.GPR4)
    var GPR5 by valueOf(PPCRegister.UISA.GPR5)
    var GPR6 by valueOf(PPCRegister.UISA.GPR6)
    var GPR7 by valueOf(PPCRegister.UISA.GPR7)
    var GPR8 by valueOf(PPCRegister.UISA.GPR8)
    var GPR9 by valueOf(PPCRegister.UISA.GPR9)
    var GPR10 by valueOf(PPCRegister.UISA.GPR10)
    var GPR11 by valueOf(PPCRegister.UISA.GPR11)
    var GPR12 by valueOf(PPCRegister.UISA.GPR12)
    var GPR13 by valueOf(PPCRegister.UISA.GPR13)
    var GPR14 by valueOf(PPCRegister.UISA.GPR14)
    var GPR15 by valueOf(PPCRegister.UISA.GPR15)
    var GPR16 by valueOf(PPCRegister.UISA.GPR16)
    var GPR17 by valueOf(PPCRegister.UISA.GPR17)
    var GPR18 by valueOf(PPCRegister.UISA.GPR18)
    var GPR19 by valueOf(PPCRegister.UISA.GPR19)
    var GPR20 by valueOf(PPCRegister.UISA.GPR20)
    var GPR21 by valueOf(PPCRegister.UISA.GPR21)
    var GPR22 by valueOf(PPCRegister.UISA.GPR22)
    var GPR23 by valueOf(PPCRegister.UISA.GPR23)
    var GPR24 by valueOf(PPCRegister.UISA.GPR24)
    var GPR25 by valueOf(PPCRegister.UISA.GPR25)
    var GPR26 by valueOf(PPCRegister.UISA.GPR26)
    var GPR27 by valueOf(PPCRegister.UISA.GPR27)
    var GPR28 by valueOf(PPCRegister.UISA.GPR28)
    var GPR29 by valueOf(PPCRegister.UISA.GPR29)
    var GPR30 by valueOf(PPCRegister.UISA.GPR30)
    var GPR31 by valueOf(PPCRegister.UISA.GPR31)

    //Condition register
    var CR by valueOf(PPCRegister.UISA.CR)

    //Floating-point status and control register
    var FPSCR by valueOf(PPCRegister.UISA.FPSCR)

    //Fixed-point exception register
    var XER by valueOf(PPCRegister.UISA.XER)

    //Link register
    var LR by valueOf(PPCRegister.UISA.LR)

    //Count register
    var CTR by valueOf(PPCRegister.UISA.CTR)

    //Program counter
    //Not a real register in PPC system
    var PC by valueOf(PPCRegister.UISA.PC)

    //Memory synchronisation subsystem
    var RESERVE by valueOf(PPCRegister.UISA.RESERVE)
    var RESERVE_ADDR by valueOf(PPCRegister.UISA.RESERVE_ADDR)

    fun uisa(id: Int) = PPCRegister.uisa(id)
    fun gpr(id: Int) = PPCRegister.gpr(id)
}