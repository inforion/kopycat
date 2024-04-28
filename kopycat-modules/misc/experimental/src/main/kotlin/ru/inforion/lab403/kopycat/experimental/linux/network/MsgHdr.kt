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
package ru.inforion.lab403.kopycat.experimental.linux.network

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.auxiliary.fields.delegates.offsetField
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IMemoryRef
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IOffsetable
import ru.inforion.lab403.kopycat.interfaces.*

class MsgHdr(override val memory: IReadWrite, override val baseAddress: ULong, is32Bit: Boolean = false) : IMemoryRef,
    IOffsetable {
    companion object {
        enum class IovPutResult {
            Success,
            PageFault,
            Overflow,
        }
    }

    private val nativeWordSize = if (is32Bit) Datatype.DWORD else Datatype.QWORD

    val name by offsetField("name", 0uL, nativeWordSize)
    var nameLen by offsetField("nameLen", nativeWordSize.bytes.ulong_z, nativeWordSize)

    private val iov by offsetField("iov", nativeWordSize.bytes.ulong_z * 2u, nativeWordSize)
    private val iovLen by offsetField("iovLen", nativeWordSize.bytes.ulong_z * 3u, nativeWordSize)

    val control by offsetField("control", nativeWordSize.bytes.ulong_z * 4u, nativeWordSize)
    var controlLen by offsetField("controlLen", nativeWordSize.bytes.ulong_z * 5u, nativeWordSize)

    override fun toString() = buildString {
        append("MsgHdr(base=0x${baseAddress.hex}) { ")
        append(
            linkedMapOf(
                "name" to name,
                "nameLen" to nameLen,
                "iov" to iov,
                "iovLen" to iovLen,
                "control" to control,
                "controlLen" to controlLen,
            ).map { (name, value) -> "$name=0x${value.hex}" }.joinToString(separator = ", ")
        )
        append(" }")
    }

    inner class Iov(override val baseAddress: ULong) : IMemoryRef, IOffsetable {
        override val memory = this@MsgHdr.memory

        val base by offsetField("base", 0u, nativeWordSize)
        val len by offsetField("len", nativeWordSize.bytes.ulong_z, nativeWordSize)
    }

    fun <R : ACore<R, U, P>, P : ACOP<P, R>, U> iovPut(cop: ACOP<P, R>, data: ByteArray): IovPutResult {
        if (iovLen == 0uL) {
            return if (data.isEmpty()) {
                IovPutResult.Success
            } else {
                IovPutResult.Overflow
            }
        }

        var currentIov = Iov(iov)
        var currentIovOfft = 0uL
        var currentIovIdx = 0uL

        for (byte in data) {
            if (currentIovOfft == currentIov.len) {
                currentIovOfft = 0uL
                currentIovIdx++
                if (currentIovIdx == iovLen) {
                    return IovPutResult.Overflow
                }

                currentIov = Iov(iov + currentIovIdx * nativeWordSize.bytes.ulong_z * 2u)
            }

            try {
                memory.outb(currentIov.base + currentIovOfft, byte.ulong_z)
            } catch (e: x86HardwareException.PageFault) {
                cop.handleException(e)
                return IovPutResult.PageFault
            }

            currentIovOfft++
        }

        return IovPutResult.Success
    }

    // Assumes no page faults occur
    fun iovGet() = sequence {
        for (currentIovIdx in 0uL until iovLen) {
            val (currentIovBase, currentIovLen) = Iov(iov + currentIovIdx).let {
                it.base to it.len
            }

            for (i in 0uL until currentIovLen) {
                yield(memory.inb(currentIovBase + i).byte)
            }
        }
    }.toList().toByteArray()
}
