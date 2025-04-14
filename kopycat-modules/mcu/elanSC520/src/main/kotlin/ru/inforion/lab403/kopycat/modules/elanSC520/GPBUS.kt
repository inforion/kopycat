/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.field

@Suppress("unused")
/**
 *
 * General-purpose bus controller
 */
class GPBUS(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Port("mmcr")
        val io = Port("io")
    }

    override val ports = Ports()

    val GPECHO = object : Register(ports.mmcr, 0xC00u, BYTE, "GPECHO") {
        var GP_ECHO_ENB by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val bit = value[1]
            super.write(ea, ss, size, bit)
            if (GP_ECHO_ENB != 0) throw NotImplementedError()
        }
    }

    val GPCSDW = object : Register(ports.mmcr , 0xC01u, BYTE, "GPCSDW") {
        var GPCS7_DW by bit(7)
        var GPCS6_DW by bit(6)
        var GPCS5_DW by bit(5)
        var GPCS4_DW by bit(4)
        var GPCS3_DW by bit(3)
        var GPCS2_DW by bit(2)
        var GPCS1_DW by bit(1)
        var GPCS0_DW by bit(0)
    }

    val GPCSQUAL = object : Register(ports.mmcr , 0xC02u, BYTE, "GPCSQUAL") {
        var GPCS7_RW by field(15..14)
        var GPCS6_RW by field(13..12)
        var GPCS5_RW by field(11..10)
        var GPCS4_RW by field(9..8)
        var GPCS3_RW by field(7..6)
        var GPCS2_RW by field(5..4)
        var GPCS1_RW by field(3..2)
        var GPCS0_RW by field(1..0)
    }

    val GPCSRT = object : Register(ports.mmcr , 0xC08u, BYTE, "GPCSRT") {
        var GPCS_RECOVR by field(7..0)
    }

    val GPCSPW = object : Register(ports.mmcr , 0xC09u, BYTE, "GPCSPW") {
        var GPCS_WIDTH by field(7..0)
    }

    val GPCSOFF = object : Register(ports.mmcr , 0xC0Au, BYTE, "GPCSOFF") {
        var GPCS_WIDTH by field(7..0)
    }

    val GPRDW = object : Register(ports.mmcr , 0xC0Bu, BYTE, "GPRDW") {
        var GP_RD_WIDTH by field(7..0)
    }

    val GPRDOFF = object : Register(ports.mmcr , 0xC0Cu, BYTE, "GPRDOFF") {
        var GP_RD_OFF by field(7..0)
    }

    val GPWRW = object : Register(ports.mmcr , 0xC0Du, BYTE, "GPWRW") {
        var GP_WR_WIDTH by field(7..0)
    }

    val GPWROFF = object : Register(ports.mmcr , 0xC0Eu, BYTE, "GPWROFF") {
        var GP_WR_OFF by field(7..0)
    }

    val GPALEW = object : Register(ports.mmcr , 0xC0Fu, BYTE, "GPALEW") {
        var GP_ALE_WIDTH by field(7..0)
    }

    val GPALEOFF = object : Register(ports.mmcr , 0xC10u, BYTE, "GPALEOFF") {
        var GP_ALE_OFF by field(7..0)
    }
}