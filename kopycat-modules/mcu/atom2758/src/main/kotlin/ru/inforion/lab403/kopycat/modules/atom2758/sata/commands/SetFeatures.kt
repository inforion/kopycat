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
package ru.inforion.lab403.kopycat.modules.atom2758.sata.commands

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.modules.atom2758.sata.IIDECommand
import ru.inforion.lab403.kopycat.modules.atom2758.sata.Port
import ru.inforion.lab403.kopycat.modules.atom2758.sata.commands.Identify.Extensions.set

internal class SetFeatures : IIDECommand {
    override val setDSC = true
    override val name: String = "SET_FEATURES"

    override fun execute(port: Port, slot: Int) = when (port.feature.int_z) {
        0x02 -> {
            // Write cache enable
            port.ideIdentify.identifyData[85] = ((1u shl 14) or (1u shl 5) or 1u).ushort
            true
        }
        0x82 -> {
            // Write cache disable
            port.ideIdentify.identifyData[85] = ((1u shl 14) or 1u).ushort
            true
        }
        0xcc, 0x66, 0xaa, 0x55, 0x05, 0x85, 0x69, 0x67, 0x96, 0x9a, 0x42, 0xc2 -> true
        0x03 -> {
            // Set transfer mode
            val v = port.nsector[2..0].int

            when (port.nsector ushr 3) {
                0x00uL, 0x01uL -> {
                    // pio default, pio mode
                    port.ideIdentify.identifyData[62] = 0x07u
                    port.ideIdentify.identifyData[63] = 0x07u
                    port.ideIdentify.identifyData[88] = 0x3fu
                    true
                }
                0x02uL -> {
                    // single word dma mode
                    port.ideIdentify.identifyData[62] = (0x07u or (1u shl (v + 8))).ushort
                    port.ideIdentify.identifyData[63] = 0x07u
                    port.ideIdentify.identifyData[88] = 0x3fu
                    true
                }
                0x04uL -> {
                    // mdma mode
                    port.ideIdentify.identifyData[62] = 0x07u
                    port.ideIdentify.identifyData[63] = (0x07u or (1u shl (v + 8))).ushort
                    port.ideIdentify.identifyData[88] = 0x3fu
                    true
                }
                0x08uL -> {
                    // udma mode
                    port.ideIdentify.identifyData[62] = 0x07u
                    port.ideIdentify.identifyData[63] = 0x07u
                    port.ideIdentify.identifyData[88] = (0x3fu or (1u shl (v + 8))).ushort
                    true
                }
                else -> {
                    port.ideAbortCmd(slot)
                    true
                }
            }
        }
        else -> {
            port.ideAbortCmd(slot)
            true
        }
    }
}
