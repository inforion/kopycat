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
@file:Suppress("unused")

package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.field


 
class GlobalUtilities(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val ctrl = Port("ctrl")
    }

    override val ports = Ports()

    val GUTS_PORPLLSR = object : Register(ports.ctrl, 0xE_0000u, DWORD, "GUTS_PORPLLSR", writable = false) {
        var e500_1_Ratio by field(29..24)
        var e500_0_Ratio by field(21..16)
        var DDR_Ratio by field(13..9)
        var Plat_Ratio by field(5..1)

        override fun reset() {
            super.reset()
            e500_0_Ratio = 0b10u // 1:1
            e500_1_Ratio = 0b10u // 1:1
            DDR_Ratio = 0b11u    // 3:1
            Plat_Ratio = 0b100u  // 4:1
        }

    }

    val GUTS_ECTRSTCR = object : Register(ports.ctrl, 0xE_0098u, DWORD, "GUTS_ECTRSTCR") {
        var RST_CKSTP_P0_EN by bit(31) // Enable automatic reset of core 0 in response to checkstop
        var RST_CKSTP_P1_EN by bit(30) // Enable automatic reset of core 1 in response to checkstop

        var CKSTP_OUT0_DIS by bit(27) // Disable assertion of CKSTP_OUT0_B pin
        var CKSTP_OUT1_DIS by bit(26) // Disable assertion of CKSTP_OUT1_B pin

        var MCP0_CKSTP_P1_EN by bit(23) // Enable machine check to core 0 in response to check stop from core 1
        var MCP1_CKSTP_P0_EN by bit(22) // Enable machine check to core 1 in response to check stop from core 0

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            if (RST_CKSTP_P0_EN.truth)
                log.severe { "Enabled automatic reset of core 0 in response to checkstop" }
            if (RST_CKSTP_P1_EN.truth)
                log.severe { "Enabled automatic reset of core 1 in response to checkstop" }
            if (CKSTP_OUT0_DIS.truth)
                log.severe { "Disable assertion of CKSTP_OUT0_B pin" }
            if (CKSTP_OUT1_DIS.truth)
                log.severe { "Disable assertion of CKSTP_OUT1_B pin" }
            if (MCP0_CKSTP_P1_EN.truth)
                log.severe { "Enable machine check to core 0 in response to check stop from core 1" }
            if (MCP1_CKSTP_P0_EN.truth)
                log.severe { "Enable machine check to core 1 in response to check stop from core 1" }
        }
    }

    val GUTS_AUTORSTSR = object : Register(ports.ctrl, 0xE_009Cu, DWORD, "GUTS_AUTORSTSR", writable = false) {
        var RST_CKSTP_P0 by bit(31) // Enable automatic reset of core 0 in response to checkstop
        var RST_CKSTP_P1 by bit(30) // Enable automatic reset of core 1 in response to checkstop

        var RST_MPIC_P0 by bit(23) // Core 0 was reset in response to MPIC reset request
        var RST_MPIC_P1 by bit(22) // Core 1 was reset in response to MPIC reset request

        var RST_CORE_P0 by bit(19) // Core 0 was reset in response to internal core request to reset itself by setting bit DBCR0[RST] register
        var RST_CORE_P1 by bit(18) // Core 1 was reset in response to internal core request to reset itself by setting bit DBCR0[RST] register

        var READY_P0 by bit(15) // Core 0 ready pin. This bit reflects what is driven on the READY_P0 external signal
        var READY_P1 by bit(14) // Core 1 ready pin. This bit reflects what is driven on the READY_P0 external signal
    }
}