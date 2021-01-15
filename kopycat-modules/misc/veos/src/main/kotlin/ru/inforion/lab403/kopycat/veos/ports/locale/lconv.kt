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
package ru.inforion.lab403.kopycat.veos.ports.locale

import ru.inforion.lab403.kopycat.veos.api.pointers.StructPointer
import ru.inforion.lab403.kopycat.veos.kernel.System

class lconv(sys: System, address: Long) : StructPointer(sys, address) {
    companion object {
        const val sizeOf = 267

        fun nullPtr(sys: System) = lconv(sys, 0)

        fun allocate(sys: System) = lconv(sys, sys.allocateClean(sizeOf))
    }

    var decimal_point by pointer(0x00)                /* Decimal point character.  */
    var thousands_sep by pointer(0x04)                /* Thousands separator.  */
    /* Each element is the number of digits in each group;
       elements with higher indices are farther left.
       An element with value CHAR_MAX means that no further grouping is done.
       An element with value 0 means that the previous element is used
       for all groups farther left.  */
    var grouping by pointer(0x08)
    /* Monetary information.  */
    /* First three chars are a currency symbol from ISO 4217.
       Fourth char is the separator.  Fifth char is '\0'.  */
    var int_curr_symbol by pointer(0x0C)
    var currency_symbol by pointer(0x10)        /* Local currency symbol.  */
    var mon_decimal_point by pointer(0x14)        /* Decimal point character.  */
    var mon_thousands_sep by pointer(0x18)        /* Thousands separator.  */
    var mon_grouping by pointer(0x1C)                /* Like `grouping' element (above).  */
    var positive_sign by pointer(0x20)                /* Sign for positive values.  */
    var negative_sign by pointer(0x24)                /* Sign for negative values.  */

    var int_frac_digits by byte(0x28)                /* Int'l fractional digits.  */
    var frac_digits by byte(0x29)                /* Local fractional digits.  */
    /* 1 if currency_symbol precedes a positive value, 0 if succeeds.  */
    var p_cs_precedes by byte(0x2A)
    /* 1 iff a space separates currency_symbol from a positive value.  */
    var p_sep_by_space by byte(0x2B)
    /* 1 if currency_symbol precedes a negative value, 0 if succeeds.  */
    var n_cs_precedes by byte(0x2C)
    /* 1 iff a space separates currency_symbol from a negative value.  */
    var n_sep_by_space by byte(0x2D)
    /* Positive and negative sign positions:
       0 Parentheses surround the quantity and currency_symbol.
       1 The sign string precedes the quantity and currency_symbol.
       2 The sign string follows the quantity and currency_symbol.
       3 The sign string immediately precedes the currency_symbol.
       4 The sign string immediately follows the currency_symbol.  */
    var p_sign_posn by byte(0x2E)
    var n_sign_posn by byte(0x2F)
    /* 1 if int_curr_symbol precedes a positive value, 0 if succeeds.  */
    var int_p_cs_precedes by byte(0x30)
    /* 1 iff a space separates int_curr_symbol from a positive value.  */
    var int_p_sep_by_space by byte(0x31)
    /* 1 if int_curr_symbol precedes a negative value, 0 if succeeds.  */
    var int_n_cs_precedes by byte(0x32)
    /* 1 iff a space separates int_curr_symbol from a negative value.  */
    var int_n_sep_by_space by byte(0x33)
    /* Positive and negative sign positions:
       0 Parentheses surround the quantity and int_curr_symbol.
       1 The sign string precedes the quantity and int_curr_symbol.
       2 The sign string follows the quantity and int_curr_symbol.
       3 The sign string immediately precedes the int_curr_symbol.
       4 The sign string immediately follows the int_curr_symbol.  */
    var int_p_sign_posn by byte(0x34)
    var int_n_sign_posn by byte(0x35)
}