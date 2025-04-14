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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field


/**
 * Created by shiftdj on 25.01.2021.
 */


class eCoherencyModule(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val ctrl = Port("ctrl")
    }

    override val ports = Ports()

    val ECM_EEBACR = object : Register(ports.ctrl, 0x0_1000u, DWORD, "ECM_EEBACR", default = 0x0000_0003u) {
        var A_STRM_DIS by bit(3)
        var CORE_STRM_DIS by bit(2)
        var A_STRM_CNT by field(1..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value and 0xFuL)
            log.severe { "$name: Streaming is ${if (A_STRM_DIS.truth) "diabled" else "enabled"}" }
            log.severe {
                "$name: ${
                    if (CORE_STRM_DIS.truth)
                        "Streaming of address tenures initiated by the e500 cores not allowed"
                    else
                        "Stream address tenures initiated by the e500 cores , provided A_STRM_DIS is cleared"
                }"
            }
            log.severe {
                "$name: ${
                    when (A_STRM_CNT.int) {
                        0b00 -> "Reserved"
                        0b01 -> "One transaction can be streamed with the initial transaction"
                        0b10 -> "Two transactions can be streamed with the initial transaction"
                        else /* 0b11 */ -> "Three transactions can be streamed with the initial transaction. Default"
                    }
                }"
            }
        }
    }

    val ECM_EEBPCR = object : Register(ports.ctrl, 0x0_1010u, DWORD, "ECM_EEBPCR") {
        var CPU1_EN by bit(25)
        var CPU0_EN by bit(24)
        var CPU1_PRI by field(5..4)
        var CPU_RD_HI_DIS by bit(2)
        var CPU0_PRI by field(1..0)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value and 0xFu)
            log.severe {
                "$name: ${
                    if (CPU1_EN.truth)
                        "CPU1 is enabled and receives bus grants in response to bus requests for the boot vector."
                    else
                        "Boot holdoff mode. CPU1 arbitration is disabled on the CCB and no bus grants are issued."
                }"
            }
            log.severe {
                "$name: ${
                    if (CPU0_EN.truth)
                        "CPU0 is enabled and receives bus grants in response to bus requests for the boot vector."
                    else
                        "Boot holdoff mode. CPU0 arbitration is disabled on the CCB and no bus grants are issued."
                }"
            }
            log.severe {
                "$name: CPU1 - ${
                    when (CPU1_PRI.int) {
                        0b00 -> "Lowest priority level"
                        0b01 -> "Second lowest priority level"
                        0b10 -> "Highest priority level"
                        else -> throw GeneralException("Reserved: $CPU1_PRI")
                    }
                }"
            }
            log.severe {
                "$name: CPU0 - ${
                    when (CPU0_PRI.int) {
                        0b00 -> "Lowest priority level"
                        0b01 -> "Second lowest priority level"
                        0b10 -> "Highest priority level"
                        else -> throw GeneralException("Reserved: $CPU0_PRI")
                    }
                }"
            }
            log.severe {
                "$name: ${
                    if (CPU_RD_HI_DIS.truth)
                        "Read low queue (lower bandwidth DDR queue) is assigned for the e500 cores' read transactions"
                    else
                        "Read high queue (higher bandwidth DDR queue) is assigned for the e500 cores' read transactions"
                }"
            }
        }
    }
}