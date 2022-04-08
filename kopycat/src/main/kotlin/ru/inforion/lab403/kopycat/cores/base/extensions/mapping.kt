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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.base.extensions

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.common.Module

const val BRIDGE_MEM_BUS_INDEX = 0xF0
const val BRIDGE_IO_BUS_INDEX = 0xF1

const val PCI_ECAM_BUS_INDEX = 0xEC

const val MAPPING_RIGHTS_R = 4
const val MAPPING_RIGHTS_W = 2
const val MAPPING_RIGHTS_E = 1

const val MAPPING_RIGHTS_RW = MAPPING_RIGHTS_R or MAPPING_RIGHTS_W
const val MAPPING_RIGHTS_RE = MAPPING_RIGHTS_R or MAPPING_RIGHTS_E
const val MAPPING_RIGHTS_RWE = MAPPING_RIGHTS_R or MAPPING_RIGHTS_W or MAPPING_RIGHTS_E

const val MAPPING_MAP_CMD = 0
const val MAPPING_UNMAP_CMD = 1

val MAPPING_SS_RIGHTS_RANGE = 2..0
val MAPPING_SS_OPERATION_RANGE = 4..3
val MAPPING_SS_AREA_RANGE = 7..5
val MAPPING_SS_OUTPUT_RANGE = 15..8
val MAPPING_SS_TRANSLATION_RANGE = 19..16  // translation type
val MAPPING_SS_WIDTH_RANGE = 24..20

const val MAPPING_TRANSLATION_OFFSET = 0

val MAPPING_ECAM_SEGMENT_RANGE = 63..20
val MAPPING_ECAM_OFFSET_RANGE = 19..0

inline fun Int.canCramInto(range: IntRange): Boolean {
    val maxValue = ubitMask32(range.length)
    return uint <= maxValue
}

inline fun Int.cram(value: Int, range: IntRange): Int {
    require(value.canCramInto(range)) { "Value $this can't be packed into range $range" }
    return insert(value, range)
}

inline fun cram(value: Int, range: IntRange): Int = 0.cram(value, range)

inline fun cmdMap(area: Int, output: Int, width: Int, rights: Int, translation: Int) =
    cram(rights, MAPPING_SS_RIGHTS_RANGE)
        .cram(MAPPING_MAP_CMD, MAPPING_SS_OPERATION_RANGE)
        .cram(output, MAPPING_SS_OUTPUT_RANGE)
        .cram(area, MAPPING_SS_AREA_RANGE)
        .cram(translation, MAPPING_SS_TRANSLATION_RANGE)
        .cram(width, MAPPING_SS_WIDTH_RANGE)

inline fun cmdUnmap(area: Int, output: Int) =
    cram(MAPPING_UNMAP_CMD, MAPPING_SS_OPERATION_RANGE)
        .cram(area, MAPPING_SS_AREA_RANGE)
        .cram(output, MAPPING_SS_OUTPUT_RANGE)
        .cram(output, MAPPING_SS_WIDTH_RANGE)


inline fun translationOffset(offset: ULong) = offset


fun MasterPort.mapOffset(
    name: String,
    from: ULong,
    size: Int,
    area: Int,
    output: Int,
    offset: ULong = 0u,
    rights: Int = MAPPING_RIGHTS_RW
): Boolean {
    if (size <= 0 || area < 0 || output < 0) {
        Module.log.severe { "Ignore mapping of $name to area=$area output=$output size=0x${size.hex}" }
        return false
    }

    val width = size.log2()
    val cmd = cmdMap(area, output, width, rights, MAPPING_TRANSLATION_OFFSET)
    val translation = translationOffset(offset)
    write(from, cmd, 0, translation)

    return true
}


fun MasterPort.unmap(name: String, area: Int, output: Int): Boolean {
    if (area < 0 || output < 0) {
        Module.log.severe { "Ignore unmapping of $name to area=$area output=$output size=0x${size.hex}" }
        return false
    }

    val cmd = cmdUnmap(area, output)
    write(0u, cmd, 0, 0u)

    return true
}