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
@file:Suppress("PropertyName", "unused")

package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.BUS32

/**
 * Created by shiftdj on 27.01.2021.
 */

// Enhanced three-speed Ethernet controller
class ETSEC(parent: Module, name: String, val n: Int) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val ctrl = Slave("ctrl", BUS32)
    }

    override val ports = Ports()

    val baseAddress: ULong = 0x2_4000uL + (n - 1).uint * 0x1000u

    open inner class ETSECx_Register(offset: ULong, name: String, default: ULong = 0u, writable: Boolean = true, readable: Boolean = true) :
            Register(ports.ctrl, baseAddress + offset, Datatype.DWORD, "ETSECx_$name", default, writable, readable)

    val ETSECx_MACCFG1 = object : ETSECx_Register(0x500u, "MACCFG1") {
        var Soft_Reset by bit(31)
        var Reset_Rx_MC by bit(19)
        var Reset_Tx_MC by bit(18)
        var Reset_Rx_Fun by bit(17)
        var Reset_Tx_Fun by bit(16)
        var Loop_Back by bit(8)
        var Rx_Flow by bit(5)
        var Tx_Flow by bit(4)
        var Syncd_Rx_EN by bit(3)
        var Rx_EN by bit(2)
        var Syncd_Tx_EN by bit(1)
        var Tx_EN by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "${this.name}: Soft_Reset = $Soft_Reset" }
            log.severe { "${this.name}: Reset Rx MC = $Reset_Rx_MC" }
            log.severe { "${this.name}: Reset Tx MC = $Reset_Tx_MC" }
            log.severe { "${this.name}: Reset Rx Fun = $Reset_Rx_Fun" }
            log.severe { "${this.name}: Reset Tx Fun = $Reset_Tx_Fun" }
            log.severe { "${this.name}: Loop Back = $Loop_Back" }
            log.severe { "${this.name}: Rx_Flow = $Rx_Flow" }
            log.severe { "${this.name}: Tx_Flow = $Tx_Flow" }
            log.severe { "${this.name}: Sync'd Rx EN = $Syncd_Rx_EN" }
            log.severe { "${this.name}: Rx_EN = $Rx_EN" }
            log.severe { "${this.name}: Sync'd Tx EN = $Syncd_Tx_EN" }
            log.severe { "${this.name}: Tx_EN = $Tx_EN" }
        }

    }

    val ETSECx_MIIMCOM  = object : ETSECx_Register(0x524u, "MIIMCOM") {
        var Scan_Cycle by bit(1)
        var Read_Cycle by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "${this.name}: Scan_Cycle = $Scan_Cycle" }
            log.severe { "${this.name}: Read_Cycle = $Read_Cycle" }
        }

    }

    val ETSECx_MIIMADD  = object : ETSECx_Register(0x528u, "MIIMADD") {
        var PHY_Address by field(12..8)
        var Register_Address by field(4..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "${this.name}: PHY_Address = $PHY_Address" }
            log.severe { "${this.name}: Register_Address = $Register_Address" }
        }

    }

    val ETSECx_MIIMCON  = object : ETSECx_Register(0x52Cu, "MIIMCON", readable = false) {
        var PHY_Control by field(15..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "${this.name}: PHY_Control = $PHY_Control" }
        }
    }

    val ETSECx_MIIMSTAT  = object : ETSECx_Register(0x530u, "MIIMSTAT", writable = false) {
        var PHY_Status by field(15..0)
    }

    // Always ready status
    val ETSECx_MIIMIND  = object : ETSECx_Register(0x534u, "MIIMIND", writable = false) {
        var Not_Valid by bit(2)
        var Scan by bit(1)
        var Busy by bit(0)
    }

}