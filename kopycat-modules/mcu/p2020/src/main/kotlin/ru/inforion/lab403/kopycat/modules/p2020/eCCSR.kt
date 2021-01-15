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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException



enum class eCCSR(val offset: Long, val size: Long = 0x1000) {

    // General utilities (0x0_0000..0x3_FFFF)

    LCCA(0x0_0000),     // Local configuration control and access
    ECM(0x0_1000),      // ECM (e500 coherency module)
    DDR(0x0_2000),      // DDRController memory controller
    I2C(0x0_3000),      // I2C controllers (2)
    DUART(0x0_4000),    // DUART
    ELBC(0x0_5000),     // Enhanced local bus controller (eLBC)

    //0x0_6000..0x0_6FFF - reserved

    ESPI(0x0_7000),     // Enhanced serial peripheral interface (eSPI)
    PCIEC3(0x0_8000),   // PCI Express controller 3
    PCIEC2(0x0_9000),   // PCI Express controller 2
    PCIEC1(0x0_A000),   // PCI Express controller 1

    //0x0_B000..0x0_BFFF - reserved

    DMAC2(0x0_C000),    // DMA controller 2

    //0x0_D000..0x0_EFFF - reserved

    GPIO(0x0_F000),     // GPIO

    //0x1_0000..0x1_FFFF - reserved

    L2C(0x2_0000),      // L2 Cache
    DMAC1(0x2_1000),    // DMA controller 1
    USBDRC(0x2_2000),   // USB DR controller

    //0x2_3000..0x2_3FFF - reserved

    ETSEC1(0x2_4000),   // eTSEC 1
    ETSEC2(0x2_5000),   // eTSEC 2
    ETSEC3(0x2_6000),   // eTSEC 3

    //0x2_7000..0x2_DFFF - reserved

    ESDHC(0x2_E000),    // eSDHC (SD/MMC)

    //0x2_F000..0x2_FFFF - reserved

    SE(0x3_0000, 0x1_0000), // Security engine

    // Programmable interrupt controller (PIC) (0x4_0000..0x6_FFFF)

    PIC(0x4_0000, 0x2_0000),    // PIC
    PICPR(0x6_0000, 0x1_0000),  // PIC-processor (per-CPU) registers

    //0x7_0000..0xB_FFFF - reserved

    // Serial RapidlO (0xC_0000..0xD_FFFF)

    ROAR(0xC_0000, 0x1_0000),   // RapidlO architectural registers
    ROIR(0xD_0000, 0x1_0000),   // RapidlO implementation registers

    // Device-specific utilities (0xE_0000..0xF_FFFF)

    GU(0xE_0000),               // Global utilities
    PM(0xE_1000),               // Performance monitor
    DWMTB(0xE_2000),            // Debug/watchpoint monitor and trace buffer
    SDC(0xE_3000, 0x100),  // SerDes control

    //0xE_3100..0xE_FFFF - reserved

    IBROM(0xF_E000, 0x2000);    // Internal boot ROM

    val range = offset until (offset + size)

    companion object {
        fun find(offset: Long): eCCSR = values()
                .find { offset in it.range }
                .sure { "Access in reserved area: ${offset.hex8}" }
    }
}