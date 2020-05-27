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

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eCR
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//WARNING: do not forget, that PPC counts bits from msb to lsb as from 0 to n.
//Same thing with index of CR group
//For example, CR2 defines range of bits from 23 to 20 of CR register
class CRBank(core : PPCCore) : ARegistersBank<PPCCore, eCR>(core, arrayOf(), bits = 64)  {
    override val name: String = "CR Register"

    //CRn
    open inner class RegularGroup(n: Int) : ARegistersBank<PPCCore, eCR>(core, eCR.values(), bits = 64) {
        override val name: String = "CR$n regular group"

        var field by fieldOf(PPCRegister.UISA.CR, eCR.msb(n), eCR.lsb(n))
        var LT by bitOf(PPCRegister.UISA.CR, eCR.LTbit(n))  //Negative
        var GT by bitOf(PPCRegister.UISA.CR, eCR.GTbit(n))  //Positive
        var EQ by bitOf(PPCRegister.UISA.CR, eCR.EQbit(n))  //Equal
        var SO by bitOf(PPCRegister.UISA.CR, eCR.SObit(n))  //Summary overflow
    }

    //CR1
    inner class ExceptionGroup : RegularGroup(1) {
        override val name: String = "CR1 exception group"

        var FX by bitOf(PPCRegister.UISA.CR, eCR.CR1_FX.bit)    //Floating-point exception
        var FEX by bitOf(PPCRegister.UISA.CR, eCR.CR1_FEX.bit)   //Floating-point enabled exception
        var VX by bitOf(PPCRegister.UISA.CR, eCR.CR1_VX.bit)    //Floating-point invalid exception
        var OX by bitOf(PPCRegister.UISA.CR, eCR.CR1_OX.bit)    //Floating-point overflow exception
    }

    fun cr(ind: Int): RegularGroup = when (ind) {
        0 -> CR0
        1 -> CR1
        2 -> CR2
        3 -> CR3
        4 -> CR4
        5 -> CR5
        6 -> CR6
        7 -> CR7
        else -> throw GeneralException("Wrong CR index")
    }

    //Bit operations in PPC notation
    fun bit(index: Int, value: Boolean) = PPCRegister.UISA.CR.bit(core, 31 - index, value.toInt())
    fun bit(index: Int) = PPCRegister.UISA.CR.bit(core, 31 - index).toBool()


    val CR0 = RegularGroup(0)
    val CR1 = ExceptionGroup()
    val CR2 = RegularGroup(2)
    val CR3 = RegularGroup(3)
    val CR4 = RegularGroup(4)
    val CR5 = RegularGroup(5)
    val CR6 = RegularGroup(6)
    val CR7 = RegularGroup(7)
}

