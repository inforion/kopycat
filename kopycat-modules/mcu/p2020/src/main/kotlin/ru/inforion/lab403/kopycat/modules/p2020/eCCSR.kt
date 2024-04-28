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

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException



enum class eCCSR(val offset: ULong, val size: ULong = 0x1000u) {

    // General utilities (0x0_0000..0x3_FFFF)

    LCCA(0x0_0000u),     // Local configuration control and access
    ECM(0x0_1000u),      // ECM (e500 coherency module)
    DDR(0x0_2000u),      // DDRController memory controller
    I2C(0x0_3000u),      // I2C controllers (2)
    DUART(0x0_4000u),    // DUART
    ELBC(0x0_5000u),     // Enhanced local bus controller (eLBC)

    //0x0_6000..0x0_6FFF - reserved in P2020
    DDR2(0x0_6000u), // DDRController memory controller 2 in MPC8572E

    ESPI(0x0_7000u),     // Enhanced serial peripheral interface (eSPI) in P2020
    //0x0_7000..0x0_7FFF - reserved in MPC8572E

    PCIEC3(0x0_8000u),   // PCI Express controller 3
    PCIEC2(0x0_9000u),   // PCI Express controller 2
    PCIEC1(0x0_A000u),   // PCI Express controller 1

    //0x0_B000..0x0_BFFF - reserved

    DMAC2(0x0_C000u),    // DMA controller 2

    //0x0_D000..0x0_EFFF - reserved

    GPIO(0x0_F000u),     // GPIO

    //0x1_0000..0x1_FFFF - reserved

    L2C(0x2_0000u),      // L2 Cache
    DMAC1(0x2_1000u),    // DMA controller 1
    USBDRC(0x2_2000u),   // USB DR controller

    //0x2_3000..0x2_3FFF - reserved

    ETSEC1(0x2_4000u),   // eTSEC 1
    ETSEC2(0x2_5000u),   // eTSEC 2
    ETSEC3(0x2_6000u),   // eTSEC 3

    //0x2_7000..0x2_DFFF - reserved

    ESDHC(0x2_E000u),    // eSDHC (SD/MMC)

    //0x2_F000..0x2_FFFF - reserved

    SE(0x3_0000u, 0x1_0000u), // Security engine

    // Programmable interrupt controller (PIC) (0x4_0000..0x6_FFFF)

    PIC(0x4_0000u, 0x2_0000u),    // PIC
    PICPR(0x6_0000u, 0x1_0000u),  // PIC-processor (per-CPU) registers

    //0x7_0000..0xB_FFFF - reserved

    // Serial RapidlO (0xC_0000..0xD_FFFF)

    ROAR(0xC_0000u, 0x1_0000u),   // RapidlO architectural registers
    ROIR(0xD_0000u, 0x1_0000u),   // RapidlO implementation registers

    // Device-specific utilities (0xE_0000..0xF_FFFF)

    GU(0xE_0000u),               // Global utilities
    PM(0xE_1000u),               // Performance monitor
    DWMTB(0xE_2000u),            // Debug/watchpoint monitor and trace buffer
    SDC(0xE_3000u, 0x100u),  // SerDes control

    //0xE_3100..0xE_FFFF - reserved

    IBROM(0xF_E000u, 0x2000u);    // Internal boot ROM

    val range = offset until (offset + size)

    companion object {
        fun find(offset: ULong): eCCSR = values()
                .find { offset in it.range }
                .sure { "Access in reserved area: ${offset.hex8}" }
    }
}