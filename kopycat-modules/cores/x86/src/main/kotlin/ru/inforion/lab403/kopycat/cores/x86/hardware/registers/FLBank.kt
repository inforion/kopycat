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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.FR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class FLBank(core: x86Core) : ARegistersBank<x86Core, FR>(core, FR.values(), bits = 32) {
    override val name: String = "Flags Register"

    var eflags: Long
        get() = x86Register.eflags.value(core)
        set(value) = x86Register.eflags.value(core, value)

    var iopl: Int
        get() = x86Register.eflags.iopl(core)
        set(value) = x86Register.eflags.iopl(core, value)

    var ac: Boolean
        get() = x86Register.eflags.ac(core)
        set(value) = x86Register.eflags.ac(core, value)
    var rf: Boolean
        get() = x86Register.eflags.rf(core)
        set(value) = x86Register.eflags.rf(core, value)
    var vm: Boolean
        get() = x86Register.eflags.vm(core)
        set(value) = x86Register.eflags.vm(core, value)
    var vif: Boolean
        get() = x86Register.eflags.vif(core)
        set(value) = x86Register.eflags.vif(core, value)
    var vip: Boolean
        get() = x86Register.eflags.vip(core)
        set(value) = x86Register.eflags.vip(core, value)
    var id: Boolean
        get() = x86Register.eflags.id(core)
        set(value) = x86Register.eflags.id(core, value)


    var cf: Boolean
        get() = x86Register.eflags.cf(core)
        set(value) = x86Register.eflags.cf(core, value)
    var pf: Boolean
        get() = x86Register.eflags.pf(core)
        set(value) = x86Register.eflags.pf(core, value)
    var af: Boolean
        get() = x86Register.eflags.af(core)
        set(value) = x86Register.eflags.af(core, value)
    var zf: Boolean
        get() = x86Register.eflags.zf(core)
        set(value) = x86Register.eflags.zf(core, value)
    var sf: Boolean
        get() = x86Register.eflags.sf(core)
        set(value) = x86Register.eflags.sf(core, value)
    var tf: Boolean
        get() = x86Register.eflags.tf(core)
        set(value) = x86Register.eflags.tf(core, value)
    var ifq: Boolean
        get() = x86Register.eflags.ifq(core)
        set(value) = x86Register.eflags.ifq(core, value)
    var df: Boolean
        get() = x86Register.eflags.df(core)
        set(value) = x86Register.eflags.df(core, value)
    var of: Boolean
        get() = x86Register.eflags.of(core)
        set(value) = x86Register.eflags.of(core, value)
}