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
package ru.inforion.lab403.kopycat.cores.x86

import ru.inforion.lab403.kopycat.cores.base.abstracts.AContext
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class x86Context(abi: x86ABI): AContext<x86Core>(abi) {

    var eflags = 0L
    override var returnAddressValue: Long = 0L

    override fun save() {
        super.save()
        eflags = abi.core.cpu.flags.eflags
        returnAddressValue = abi.returnAddressValue.also { abi.pop() }
    }

    override fun load() {
        super.load()
        abi.core.cpu.flags.eflags = eflags
        abi.returnAddressValue = returnAddressValue
    }

    override fun store(address: Long) {
        abi.writeMemory(address, eflags, abi.gprDatatype)
        abi.writeMemory(address + abi.gprDatatype.bytes, returnAddressValue, abi.gprDatatype)
        super.store(address + abi.gprDatatype.bytes * 2)
    }

    override fun restore(address: Long) {
        eflags = abi.readMemory(address, abi.gprDatatype)
        returnAddressValue = abi.readMemory(address + abi.gprDatatype.bytes, abi.gprDatatype)
        super.restore(address + abi.gprDatatype.bytes * 2)
    }

    override fun ret() {
        TODO("Not implemented")
    }

}