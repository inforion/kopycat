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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class FormM(core: PPCCore,
            val construct:  (PPCCore, Int, Int, Int, Boolean, Array<AOperand<PPCCore>>) -> APPCInstruction
) : APPCDecoder(core) {

    override fun decode(s: Long): APPCInstruction {

        val rS = PPCRegister.gpr(s[25..21].toInt())
        val rA =  PPCRegister.gpr(s[20..16].toInt())
        val shift = s[15..11].toInt()
        val rB =  PPCRegister.gpr(shift)
        val maskFst = s[10..6].toInt()
        val maskSnd = s[5..1].toInt()
        val record = s[0].toBool()

        return construct(core,
                shift,
                maskFst,
                maskSnd,
                record,
                arrayOf(rS, rA, rB)
        )
    }
}