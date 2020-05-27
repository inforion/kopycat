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
package ru.inforion.lab403.kopycat.cores.ppc.enums


 
enum class eTCR(val bit: Int) {

    WPHigh(31),     // Watchdog Timer Period
    WPLow(30),
    WRCHigh(29),    // Watchdog Timer Reset Control
    WRCLow(28),
    WIE(27),        // Watchdog Timer Interrupt Enable
    DIE(26),        // Decrementer Interrupt Enable
    FPHigh(25),     // Fixed-Interval Timer Period
    FPLow(24),
    FIE(23),        // Fixed-Interval Timer Interrupt Enable
    ARE(22),        // Auto-Reload Enable
    IMPDEP(21);     // Implementation-dependent

    companion object {
        val WP = WPHigh.bit..WPLow.bit
        val WRC = WRCHigh.bit..WRCLow.bit
        val FP = FPHigh.bit..FPLow.bit
    }

}