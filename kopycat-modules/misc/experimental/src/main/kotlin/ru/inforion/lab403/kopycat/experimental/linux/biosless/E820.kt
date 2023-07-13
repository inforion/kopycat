/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.experimental.linux.biosless

import ru.inforion.lab403.kopycat.interfaces.outl
import ru.inforion.lab403.kopycat.interfaces.outq
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/** Memory map table entry */
class E820(private val base: ULong, private val length: ULong, private val type: E820Type) {
    companion object {
        /** Memory range type */
        enum class E820Type(val typ: ULong) {
            Usable(1uL),
            Reserved(2uL),
            ACPIReclaimable(3uL),
            ACPINVS(4uL),
            BadMemory(5uL),
        }
    }

    /** Writes table entry to memory at es:di */
    fun write(core: x86Core) {
        val es = core.cpu.sregs.es.id
        val di = core.cpu.regs.di.value

        core.outq(di, base, es)
        core.outq(di + 8uL, length, es)
        core.outl(di + 16uL, type.typ, es)
        core.outl(di + 20uL, 0uL, es)
    }
}
