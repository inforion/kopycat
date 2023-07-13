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
package ru.inforion.lab403.kopycat.cores.x86.instructions

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.interfaces.IValuable
import unicorn.Unicorn
import java.math.BigInteger

internal class UnicornEmu {
    private val unicorn = Unicorn(Unicorn.UC_ARCH_X86, Unicorn.UC_MODE_64)

    fun stop() = unicorn.emu_stop()
    fun mmap(size: Long = 0x2000) = unicorn.mem_map(0, size, Unicorn.UC_PROT_ALL)
    fun unmap(size: Long = 0x2000) = unicorn.mem_unmap(0, size)

    data class Flags(override var data: ULong) : IValuable {
        var cf by bit(0)
        var pf by bit(2)
        var af by bit(4)
        var zf by bit(6)
        var sf by bit(7)
        var df by bit(10)
        var of by bit(11)

        override fun toString() = "cf = $cf, pf = $pf, af = $af, zf = $zf, sf = $sf, df = $df, of = $of"
    }

    fun gpr(reg: Int): ULong = (unicorn.reg_read(reg) as Long).ulong
    fun gpr(reg: Int, value: ULong) = unicorn.reg_write(reg, value.long)
    fun flags() = Flags((unicorn.reg_read(Unicorn.UC_X86_REG_RFLAGS) as Long).ulong)
    fun flags(f: Flags) = unicorn.reg_write(Unicorn.UC_X86_REG_RFLAGS, f.data.long)

    @Suppress("DEPRECATION")
    fun fpcw() = BigInteger(1, unicorn.reg_read(Unicorn.UC_X86_REG_FPCW, 2).reversedArray()).ulong
    fun fpcw(i: ULong) = gpr(Unicorn.UC_X86_REG_FPCW, i)

    @Suppress("DEPRECATION")
    fun fpsw() = BigInteger(1, unicorn.reg_read(Unicorn.UC_X86_REG_FPSW, 2).reversedArray()).ulong
    fun fpsw(i: ULong) = gpr(Unicorn.UC_X86_REG_FPSW, i)

    @Suppress("DEPRECATION")
    fun fptag() = BigInteger(1, unicorn.reg_read(Unicorn.UC_X86_REG_FPTAG, 2).reversedArray()).ulong
    fun fptag(i: ULong) = gpr(Unicorn.UC_X86_REG_FPTAG, i)

    private fun ByteArray.pad(to: Int) = if (this.size < to) {
        val padded = ByteArray(to)
        copyInto(padded, to - size)
        padded
    } else this

    // Надо дополнять до 16 байтов, иначе перезапишется только часть регистра
    @Suppress("DEPRECATION")
    fun xmm(i: Int, value: BigInteger) =
        unicorn.reg_write(Unicorn.UC_X86_REG_XMM0 + i, value.toByteArray().pad(16).reversedArray())

    @Suppress("DEPRECATION")
    fun xmm(i: Int): BigInteger = BigInteger(1, unicorn.reg_read(Unicorn.UC_X86_REG_XMM0 + i, 16).reversedArray())

    @Suppress("DEPRECATION")
    fun st(i: Int, value: BigInteger) = unicorn.reg_write(
        Unicorn.UC_X86_REG_ST0 + i,
        value.toByteArray().pad(10).reversedArray()
    )

    @Suppress("DEPRECATION")
    fun st(i: Int) = BigInteger(
        1,
        unicorn.reg_read(
            Unicorn.UC_X86_REG_ST0 + i,
            10,
        ).reversedArray(),
    )

    fun mmx(i: Int, value: ULong) = st(i, value.bigint mask 63..0)

    fun mmx(i: Int): ULong = st(i)[63..0].ulong

    fun store(addr: ULong, data: ByteArray) = unicorn.mem_write(addr.long, data)

    fun storeExecute(addr: ULong, data: ByteArray) {
        store(addr, data)
        unicorn.emu_start(addr.long, (addr + data.size.uint).long, 0, 1)
    }

    fun ine(addr: ULong, size: Int): BigInteger =
        BigInteger(1, unicorn.mem_read(addr.long, size.long_z).reversedArray())
}
