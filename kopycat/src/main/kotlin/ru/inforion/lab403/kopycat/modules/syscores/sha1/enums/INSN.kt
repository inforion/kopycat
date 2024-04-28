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
package ru.inforion.lab403.kopycat.modules.syscores.sha1.enums

object INSN {
    /**
     * Reset ABCDE registers only in SHA1 engine and loads data from memory where MF CPU register points into SHA1
     */
    const val SHA1_INIT = 0x0AA0_BEEFuL

    /**
     * Load into SHA1 engine data from memory where MF CPU register points
     */
    const val SHA1_UPDATE = 0x0AA0_DEADuL

    /**
     * Do one round in SHA1 engine
     */
    const val SHA1_ROUND = 0x0AA0_C0DEuL

    /**
     * Do final round in SHA1 engine and store data to memory where MF CPU register points
     */
    const val SHA1_FINAL = 0x0AA0_AFFEuL

    /**
     * Read registers ABCDE from SHA1 engine to CPU registers
     */
    const val SHA1_READ = 0x0AA0_FEE1uL

    /**
     * Write registers ABCDE from CPU registers to SHA1 engine
     */
    const val SHA1_WRITE = 0x0AA0_CAFEuL

    /**
     * No operation
     */
    const val NOP = 0x0AA0_CCCCuL
}