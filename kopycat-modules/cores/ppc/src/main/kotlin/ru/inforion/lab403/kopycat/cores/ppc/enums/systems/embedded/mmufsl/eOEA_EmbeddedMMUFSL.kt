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
package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl

enum class eOEA_EmbeddedMMUFSL(val id: Int) {

    //MMU assist registers
    MAS0(624),
    MAS1(625),
    MAS2(626),
    MAS3(627),
    MAS4(628),
    MAS6(630),
    MAS7(944),

    //Process ID registers
    PID1(633),
    PID2(634),

    //TLB Configuration Registers
    TLB0CFG(688),
    TLB1CFG(689),
    TLB2CFG(690),
    TLB3CFG(691),

    //MMU control and status(Read/Write)
    MMUCSR0(1012),

    MMUCFG(1015)

}