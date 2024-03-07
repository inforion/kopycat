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
package ru.inforion.lab403.kopycat.runtime.analyzer.stack

import ru.inforion.lab403.common.extensions.hex16

class StackAnalyzerPrinter(val stackAnalyzer: StackAnalyzer) {
    var limits: Pair<ULong, ULong> = 0x0uL to 0xFFFF_FFFF_FFFF_FFFFuL

    fun newLimits(from: ULong, to: ULong): StackAnalyzerPrinter {
        return newLimits(from to to)
    }

    fun newLimits(limits: Pair<ULong, ULong>): StackAnalyzerPrinter {
        this.limits = limits
        return this
    }

    fun toTable(): String {
        return stackAnalyzer.spToData
            .subMap(limits.first, limits.second)
            .toSortedMap(Comparator.reverseOrder())
            .map { (_, data) -> data }
            .sortedBy { data -> data.current.time }
            .map { data -> "SP=0x${data.current.sp.hex16} (was 0x${data.previous.sp.hex16})" +
                    "    PC=0x${data.current.pc.hex16} (was 0x${data.previous.pc.hex16})" +
                    "    RA=0x${data.current.ra.hex16}" }
            .joinToString("\n")
    }

    fun toIDAPython(): String {
        // TODO: переписать макаронную строку
        return stackAnalyzer.spToData
            .subMap(limits.first, limits.second)
            .toSortedMap(Comparator.reverseOrder())
            .map { (_, data) -> data }
            .sortedBy { data -> data.current.time }
            .map { data -> "(0x${data.current.sp.hex16}, 0x${data.previous.sp.hex16}, 0x${data.current.pc.hex16}, 0x${data.previous.pc.hex16}, 0x${data.current.ra.hex16})" }
            .joinToString(", ").let { "pcs = [$it]" } +
                "\nprint( '\\n'.join(" +
                "f'SP=0x{d[0]:016x} (was 0x{d[1]:016x})    " +
                "PC=0x{d[2]:016x} (was 0x{d[3]:016x})    " +
                "RA=0x{d[4]:016x}   " +
                "(PC function {ida_funcs.get_func_name(d[2])})    " +
                "(prevPC function {ida_funcs.get_func_name(d[3])})' for d in pcs) )"
    }
}
