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
package ru.inforion.lab403.kopycat.cores.ppc.operands

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class SPR(val name: String, val id: Int, val dest: PPCRegister, val moveTo: Access, val moveFrom: Access) {
    enum class Access {
        no,     //Not defined
        yes,    //Defined
        hypv    //Usable only in hypervisor state
    }

    fun value(core: PPCCore) = dest.value(core)
    fun value(core: PPCCore, data: Long) = dest.value(core, data)

    val sprH: Int
        get() = getHigh(id)
    val sprL: Int
        get() = getLow(id)
    val isPriveleged = id[4].toBool()

    companion object {
        private fun getHigh(data: Int) = ((data shr 5) and 0b11111)
        private fun getLow(data: Int) = (data and 0b11111)

        fun swap(data: Int) = (getLow(data) shl 5) or getHigh(data)

    }
}