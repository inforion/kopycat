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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.memory

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.v850es.enums.Flags
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore


class FLBank(core: v850ESCore) : ARegistersBank<v850ESCore, Flags>(core, Flags.values(), bits = 32) {
    override val name: String = "Flags Register"

    var value by valueOf(v850esRegister.CTRLR.PSW)

    var z by bitOf(v850esRegister.CTRLR.PSW, Flags.Z.bit)
    var s by bitOf(v850esRegister.CTRLR.PSW, Flags.S.bit)
    var ov by bitOf(v850esRegister.CTRLR.PSW, Flags.OV.bit)
    var cy by bitOf(v850esRegister.CTRLR.PSW, Flags.CY.bit)
    var sat by bitOf(v850esRegister.CTRLR.PSW, Flags.SAT.bit)
    var id by bitOf(v850esRegister.CTRLR.PSW, Flags.ID.bit)
    var ep by bitOf(v850esRegister.CTRLR.PSW, Flags.EP.bit)
    var np by bitOf(v850esRegister.CTRLR.PSW, Flags.NP.bit)
}