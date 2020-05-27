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
package ru.inforion.lab403.kopycat.modules.pic32mz

import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.AVariables
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.field


// TODO: base 0xBF80_0000
class OSC(parent: Module, name: String) : Module(parent, name) {

    inner class Variables: AVariables() {
        val div = array(8, "PBDIV", 0)
    }

    override val variables = Variables()

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
    }

    override val ports = Ports()

    val OSCCON = ComplexRegister(ports.mem,0x1200, "OSCCON")
    val SPLLCON = ComplexRegister(ports.mem,0x1220, "SPLLCON")

    inner class PBxDIV(val id: Int, offset: Long) : ComplexRegister(ports.mem, offset, "PB${id}DIV") {
        var ON by bit(15)
        val PBDIVRDY by bit(11)
        val PBDIV by field(6..0)

        override fun toString(): String = "$name[ON=$ON PBDIVRDY=$PBDIVRDY PBDIV=$PBDIV]"

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            variables.div[id].value = PBDIV + 1
            log.config { "$this" }
        }

        override fun reset() {
            super.reset()
            ON = 1
        }
    }

    val PB1DIV = PBxDIV(1, 0x1300)
    val PB2DIV = PBxDIV(2, 0x1310)
    val PB3DIV = PBxDIV(3, 0x1320)
    val PB4DIV = PBxDIV(4, 0x1330)
    val PB5DIV = PBxDIV(5, 0x1340)
    val PB6DIV = PBxDIV(6, 0x1350)
    val PB7DIV = PBxDIV(7, 0x1360)
    val PB8DIV = PBxDIV(8, 0x1370)
}