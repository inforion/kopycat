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
package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


abstract class AARMCOP(core: AARMCore, name: String) : ACOP<AARMCOP, AARMCore>(core, name) {

    open fun Coproc_SendOneWord(opc1: Int, opc2: Int, crn: Int, crm: Int, cp_num: Int, value: Long) {
        throw NotImplementedError("Coproc_SendOneWord isn't implemented for this ARM arch")
    }

    open fun Coproc_GetOneWord(opc1: Int, opc2: Int, crn: Int, crm: Int, cp_num: Int): Long {
        throw NotImplementedError("Coproc_SendOneWord isn't implemented for this ARM arch")
    }

    open fun Coproc_SendTwoWords(word2: Long, word1: Long, cp_num: Int, opc1: Int, crm: Int) {
        throw NotImplementedError("Coproc_SendTwoWords isn't implemented for this ARM arch")
    }

}
