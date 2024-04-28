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
package ru.inforion.lab403.kopycat.cores.x86.config

import ru.inforion.lab403.common.extensions.bytes
import ru.inforion.lab403.common.extensions.getUInt
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.*
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.MemoryType.*
import ru.inforion.lab403.kopycat.modules.cores.x86Core

fun CPUID0(highestFunctionParameter: UInt, id: VENDOR) = CPUID(
    highestFunctionParameter,
    id.getUInt(0),
    id.getUInt(2),
    id.getUInt(1)
)

fun CPUID1EAX(
    extendedFamilyID: UInt,
    extendedModelID: UInt,
    procType: ProcType,
    familyID: UInt,
    model: UInt,
    steppingID: UInt
) = steppingID
    .insert(model, 7..4)
    .insert(familyID, 11..8)
    .insert(procType.id, 13..12)
    .insert(extendedModelID, 19..16)
    .insert(extendedFamilyID, 27..20)

fun CPUID1EBX(brandIndex: BrandIndex, clflushLineSize: UInt, maxIDNum: UInt, localAPICID: UInt) =
    brandIndex.id
        .insert(clflushLineSize, 15..8)
        .insert(maxIDNum, 23..16)
        .insert(localAPICID, 31..24)

fun CPUIDEDX(vararg features: EDXFeatures) =
    features.fold(0u) { acc, feature -> acc.insert(1, feature.id) }

fun CPUIDECX(vararg features: ECXFeatures) =
    features.fold(0u) { acc, feature -> acc.insert(1, feature.id) }

fun IA32_MTRR_PHYSBASE(physBase: ULong = 0uL, type: MemoryType = Uncachable) =
    physBase.insert(type.id, 7..0)

fun IA32_MTRR_PHYSMASK(physMask: ULong = 0uL, valid: Boolean = true) = physMask.insert(valid, 11)

fun IA32_MTRR_FIX(
    a0: MemoryType,
    a1: MemoryType = a0,
    a2: MemoryType = a1,
    a3: MemoryType = a2,
    a4: MemoryType = a3,
    a5: MemoryType = a4,
    a6: MemoryType = a5,
    a7: MemoryType = a6
) = a0.id.insert(a1.id, 15..8)
    .insert(a2.id, 23..16)
    .insert(a3.id, 31..24)
    .insert(a4.id, 39..32)
    .insert(a5.id, 47..40)
    .insert(a6.id, 55..48)
    .insert(a7.id, 63..56)

fun IA32_PAT(pa0: ULong, pa1: ULong, pa2: ULong, pa3: ULong, pa4: ULong, pa5: ULong, pa6: ULong, pa7: ULong) =
    0uL.insert(pa0, 2..0)
        .insert(pa1, 10..8)
        .insert(pa2, 18..16)
        .insert(pa3, 26..24)
        .insert(pa4, 34..32)
        .insert(pa5, 42..40)
        .insert(pa6, 50..48)
        .insert(pa7, 58..56)


fun String.getUInt(ind: Int): UInt {
    val remaining = length - ind * 4
    if (remaining <= 0) return 0u
    val size = if (remaining > 4) 4 else remaining
    return bytes.getUInt(ind * 4, size).uint
}

fun Configuration.setModelName(name: String) {
    cpuid(0x80000002u, name.getUInt(0), name.getUInt(1), name.getUInt(2), name.getUInt(3))
    cpuid(0x80000004u, name.getUInt(8), name.getUInt(9), name.getUInt(10), name.getUInt(11))
    cpuid(0x80000003u, name.getUInt(4), name.getUInt(5), name.getUInt(6), name.getUInt(7))
}
