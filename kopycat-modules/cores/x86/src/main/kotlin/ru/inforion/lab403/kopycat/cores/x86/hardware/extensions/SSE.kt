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
package ru.inforion.lab403.kopycat.cores.x86.hardware.extensions

import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger


class SSE(val x86: x86Core) : ICoreUnit, IAutoSerializable {
    override val name = "SSExtension"

    // TODO: Be careful when extending to AVX: you should leave high 128 bits unmodified in case of using SSE-instructions
    // TODO: for ex: (operation definition of MOVUPS in Intel manual (volume 2))
    // TODO: MOVUPS (128-bit load- and register-copy- form Legacy SSE version)
    // TODO: DEST[127:0] := SRC[127:0]
    // TODO: DEST[MAXVL-1:128] (Unmodified)
    // TODO: also search for copyOf usage with xmm. It also may cause bugs
    val xmm = Array<BigInteger>(16) { BigInteger.ZERO }

    override fun reset() {
        super.reset()
        xmm.fill(BigInteger.ZERO)
    }
}