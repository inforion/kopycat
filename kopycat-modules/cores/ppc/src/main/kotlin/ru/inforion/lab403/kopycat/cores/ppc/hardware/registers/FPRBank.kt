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


class FPRBank(core: PPCCore) : ARegistersBank<PPCCore, eUISA>(core, eUISA.values(), bits = 64) {
    override val name: String = "Floating-point registers"

    //Floating-point registers
    var FPR0 by valueOf(PPCRegister.UISA.FPR0)
    var FPR1 by valueOf(PPCRegister.UISA.FPR1)
    var FPR2 by valueOf(PPCRegister.UISA.FPR2)
    var FPR3 by valueOf(PPCRegister.UISA.FPR3)
    var FPR4 by valueOf(PPCRegister.UISA.FPR4)
    var FPR5 by valueOf(PPCRegister.UISA.FPR5)
    var FPR6 by valueOf(PPCRegister.UISA.FPR6)
    var FPR7 by valueOf(PPCRegister.UISA.FPR7)
    var FPR8 by valueOf(PPCRegister.UISA.FPR8)
    var FPR9 by valueOf(PPCRegister.UISA.FPR9)
    var FPR10 by valueOf(PPCRegister.UISA.FPR10)
    var FPR11 by valueOf(PPCRegister.UISA.FPR11)
    var FPR12 by valueOf(PPCRegister.UISA.FPR12)
    var FPR13 by valueOf(PPCRegister.UISA.FPR13)
    var FPR14 by valueOf(PPCRegister.UISA.FPR14)
    var FPR15 by valueOf(PPCRegister.UISA.FPR15)
    var FPR16 by valueOf(PPCRegister.UISA.FPR16)
    var FPR17 by valueOf(PPCRegister.UISA.FPR17)
    var FPR18 by valueOf(PPCRegister.UISA.FPR18)
    var FPR19 by valueOf(PPCRegister.UISA.FPR19)
    var FPR20 by valueOf(PPCRegister.UISA.FPR20)
    var FPR21 by valueOf(PPCRegister.UISA.FPR21)
    var FPR22 by valueOf(PPCRegister.UISA.FPR22)
    var FPR23 by valueOf(PPCRegister.UISA.FPR23)
    var FPR24 by valueOf(PPCRegister.UISA.FPR24)
    var FPR25 by valueOf(PPCRegister.UISA.FPR25)
    var FPR26 by valueOf(PPCRegister.UISA.FPR26)
    var FPR27 by valueOf(PPCRegister.UISA.FPR27)
    var FPR28 by valueOf(PPCRegister.UISA.FPR28)
    var FPR29 by valueOf(PPCRegister.UISA.FPR29)
    var FPR30 by valueOf(PPCRegister.UISA.FPR30)
    var FPR31 by valueOf(PPCRegister.UISA.FPR31)
}