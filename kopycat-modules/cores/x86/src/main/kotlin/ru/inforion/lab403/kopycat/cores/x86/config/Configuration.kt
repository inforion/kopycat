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
package ru.inforion.lab403.kopycat.cores.x86.config

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import kotlin.reflect.KProperty

class Configuration(val core: x86Core) : IAutoSerializable {
    companion object {
        @Transient val log = logger(FINE)
        const val ECX_DEPEND = 4u
        const val ECX_OFFSET = 0xF000_0000u
    }

    enum class eMSR(val offset: ULong) {
        EFER(0xC000_0080uL),
        STAR(0xC000_0081uL),
        LSTAR(0xC000_0082uL),
        FMASK(0xC000_0084uL),
        FS_BASE(0xC000_0100uL),
        GS_BASE(0xC000_0101uL),
        KERNEL_GS_BASE(0xC000_0102uL),

    }

    private val cpuid = dictionary<UInt, CPUID>()
    val msr = dictionary<ULong, ULong>()

    var mxcsr: ULong = 0x1F80u

    fun cpuid(id: UInt, ecx: UInt = 0u) = if (id == ECX_DEPEND) cpuid[ECX_OFFSET + ecx] else cpuid[id]
    fun cpuid(id: UInt, value: CPUID, ecx: UInt = 0u) {
        val realId = if (id == ECX_DEPEND) ECX_OFFSET + ecx else id
        if (realId in cpuid)
            log.warning { "CPUID[0x${id.hex8}] will be rewritten with $value" }
        cpuid[realId] = value
    }
    fun cpuid(id: UInt, eax: UInt, ebx: UInt, ecx: UInt, edx: UInt) = cpuid(id, CPUID(eax, ebx, ecx, edx))
    fun cpuid4(id: UInt, eax: UInt, ebx: UInt, ecx: UInt, edx: UInt) = cpuid(ECX_DEPEND, CPUID(eax, ebx, ecx, edx), id)

    fun rdmsr(id: ULong) = msr[id]
    fun wrmsr(id: ULong, value: ULong) {
        if (id !in msr)
            log.severe { "MSR[0x${id.hex16}] wasn't initialized (set as ${value.hex16})" }
        msr[id] = value
    }

    fun msr(id: ULong, value: ULong) {
        if (id in msr)
            log.warning { "MSR[0x${id.hex16}] will be rewritten with 0x${value.hex8}" }
        msr[id] = value
    }

    inner class MSRDelegate(val msr: eMSR) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) =
            rdmsr(msr.offset) ?: throw GeneralException("Can't read MSR $msr")
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ULong) = wrmsr(msr.offset, value)
    }

    @DontAutoSerialize var efer by MSRDelegate(eMSR.EFER)
    @DontAutoSerialize var star by MSRDelegate(eMSR.STAR)
    @DontAutoSerialize var lstar by MSRDelegate(eMSR.LSTAR)
    @DontAutoSerialize var fmask by MSRDelegate(eMSR.FMASK)
    @DontAutoSerialize var fs_base by MSRDelegate(eMSR.FS_BASE)
    @DontAutoSerialize var gs_base by MSRDelegate(eMSR.GS_BASE)
    @DontAutoSerialize var kernel_gs_base by MSRDelegate(eMSR.KERNEL_GS_BASE)
}