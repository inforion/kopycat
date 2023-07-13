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
package ru.inforion.lab403.kopycat.experimental.x86

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.outb
import ru.inforion.lab403.kopycat.modules.cores.x86Core

fun <R : ACore<R, U, P>, U : ACPU<U, R, *, *>, P : ACOP<P, R>> ACore<R, U, P>.stepOrFail() {
    step().also { status ->
        if (!status.resume) {
            info.dump()
            throw GeneralException("Unable to continue the execution")
        }
    }
}

inline fun <R : ACore<R, U, P>, U : ACPU<U, R, *, *>, P : ACOP<P, R>> ACore<R, U, P>.stepOrFailWhile(block: () -> Boolean) {
    while (block()) {
        stepOrFail()
    }
}

inline fun <R : ACore<R, U, P>, U : ACPU<U, R, *, *>, P : ACOP<P, R>> ACore<R, U, P>.stepOrFailToPc(expectedPc: ULong) {
    return stepOrFailWhile {
        pc != expectedPc
    }
}

fun x86Core.fillNops(range: ULongRange) {
    range.forEach { ea ->
        core.outb(ea, 0x90uL)
    }
}

fun x86Core.fillNops(ea: ULong, size: ULong) {
    this.fillNops(ea until ea + size)
}
