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
package ru.inforion.lab403.kopycat.veos.api.abstracts

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.veos.VEOS

class APIVariable(
        val os: VEOS<*>,
        name: String,
        val datatype: Datatype,
        address: Long? = null
): APIObject(name, address) {

    companion object {
        val log = logger()

        fun char(os: VEOS<*>, name: String) = APIVariable(os, name, os.abi.types.char)
        fun short(os: VEOS<*>, name: String) = APIVariable(os, name, os.abi.types.short)
        fun int(os: VEOS<*>, name: String) = APIVariable(os, name, os.abi.types.int)
        fun long(os: VEOS<*>, name: String) = APIVariable(os, name, os.abi.types.long)
        fun longLong(os: VEOS<*>, name: String) = APIVariable(os, name, os.abi.types.longLong)
        fun pointer(os: VEOS<*>, name: String) = APIVariable(os, name, os.abi.types.pointer)
    }

    fun allocate(data: Long = 0): Long {
        val address = os.sys.allocateSystemSymbol(name, datatype)
        log.config { "Allocating variable '${name}' at 0x${address.hex8} with value 0x${data.hex8}" }
        value = data
        return address
    }

    // TODO: auto allocatable?
    val allocated: APIVariable get() {
        if (!linked)
            allocate()
        return this
    }

    override val address get() = os.sys.addressOfSymbol(name)

    var value: Long
        get() = os.abi.readMemory(address!!, datatype)
        set(value) = os.abi.writeMemory(address!!, value, datatype)
}