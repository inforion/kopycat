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
package ru.inforion.lab403.kopycat.experimental.linux.biosless

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.x86.*
import ru.inforion.lab403.kopycat.cores.x86.config.IA32_MTRR_PHYSBASE
import ru.inforion.lab403.kopycat.cores.x86.config.IA32_MTRR_PHYSMASK
import ru.inforion.lab403.kopycat.cores.x86.config.IA32_PAT
import ru.inforion.lab403.kopycat.cores.x86.enums.cpuid.MemoryType
import ru.inforion.lab403.kopycat.experimental.common.SparseRAM
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.nio.ByteOrder

abstract class BioslessDevice(parent: Module?, name: String) : Module(parent, name) {
    companion object {
        // Taken from qemu (SeaBIOS) boot log
        val DEFAULT_MEMORY_MAP = arrayOf(
            E820(0uL, 0x9fc00uL, E820.Companion.E820Type.Usable),
            E820(0x9fc00uL, 0x400uL, E820.Companion.E820Type.Reserved),
            E820(0xf0000uL, 0x10000uL, E820.Companion.E820Type.Reserved),
            E820(0x100000uL, 0x7ff00000uL, E820.Companion.E820Type.Usable),
            E820(0xb0000000uL, 0x10000000uL, E820.Companion.E820Type.Reserved),
            E820(0xfeffc000uL, 0x4000uL, E820.Companion.E820Type.Reserved),
            E820(0xfffc0000uL, 0x40000uL, E820.Companion.E820Type.Reserved),
            E820(0x100000000uL, 0x80000000uL, E820.Companion.E820Type.Usable),
        )
    }

    abstract val x86: x86Core
    abstract val bzImage: ByteArray
    abstract val cmdline: String
    open val ramdiskAddress: ULong get() = 0xC000_0000uL - ramdiskSize
    abstract val ramdisk: ByteArray?
    open val initramfsSize: ULong = 0uL
    open val e820: Array<E820> = DEFAULT_MEMORY_MAP

    private val ramdiskSize by lazy { ramdisk?.size?.ulong_z ?: initramfsSize }

    private fun makeRAM(ramName: String, size: Int, fill: ByteArray.() -> Unit) = RAM(
        this,
        ramName,
        size,
        ByteArray(size) { 0 }.apply(fill)
    )

    protected fun buildMemoryLayout() = bzImage(bzImage).let { kernel ->
        val ramdiskRamSize = ((ramdiskSize + cmdline.length + 1u) ceil (0x1000uL)) * 0x1000uL

        val (low, high) = kernel.prepareBoot(
            0xde00uL, // taken from SeaBIOS
            ramdiskAddress + ramdiskSize,
            ramdiskAddress,
            ramdiskSize,
        )

        val highRamSize = (high.size ceil 0x1000) * 0x1000

        arrayOf(
            // RAM 1: 0..1_0000
            (0x00uL to 0x1_0000uL).let {
                it.first to makeRAM("ram1", (it.second - it.first).int) {
                    for (i in 0 until 256) {
                        // Interrupt handler is at 0x400
                        putUInt16(i * 4, 0x400uL, ByteOrder.LITTLE_ENDIAN)
                    }
                    // int3; WARN: hangs by default!
                    putUInt8(0x400, 0xccuL)
                }.ports.mem
            },
            // Setup code, gap, protected mode kernel code: 1_0000..10_0000 + ??
            (0x1_0000uL to 0x10_0000uL + highRamSize).let {
                it.first to makeRAM("kernel", (it.second - it.first).int) {
                    putArray(0, low)
                    putArray(0xf_0000, high)
                }.ports.mem
            },
            // RAM 2: 10_0000 + ??..ramdiskAddress
            (0x10_0000uL + highRamSize to ramdiskAddress).let {
                it.first to SparseRAM(
                    this,
                    "ram2",
                    it.second - it.first,
                    0x0800_0000uL,
                ).ports.mem
            },
            // Ramdisk: ramdiskAddress..??
            (ramdiskAddress to ramdiskAddress + ramdiskRamSize).let {
                it.first to makeRAM("ramdisk", (it.second - it.first).int) {
                    val tmp = ramdisk
                    if (tmp != null) putArray(0, tmp)
                    putString(ramdiskSize.int, cmdline)
                }.ports.mem
            },
            // RAM 3: ??..C000_0000
            (ramdiskAddress + ramdiskRamSize to 0xC000_0000uL).let {
                it.first to SparseRAM(
                    this,
                    "ram3",
                    it.second - it.first,
                    0x0800_0000uL,
                ).ports.mem
            },
            // Hole: C000_0000..FFFF_0000
            // RAM 4: FFFF_0000..1_8000_0000
            (0xFFFF_0000uL to 0x1_8000_0000uL).let {
                it.first to SparseRAM(
                    this,
                    "ram4",
                    it.second - it.first,
                    0x0800_0000uL,
                ).ports.mem
            },
        )
    }

    override fun reset() {
        super.reset()

        x86.run {
            // Setup registers
            cpu.run {
                sregs.run {
                    // Kernel setup code segment
                    cs.value = 0x1020uL

                    // Kernel setup code start segment
                    ds.value = 0x1000uL
                    es.value = 0x1000uL
                    fs.value = 0x1000uL
                    gs.value = 0x1000uL
                    ss.value = 0x1000uL
                }

                pc = 0uL
            }

            // Interrupts
            cop.idtr.limit = 255uL
            SetupCodeInterrupts(this, e820)

            // MTRR, taken from qemu (SeaBIOS) boot log
            config.run {
                msr(IA32_MTRR_PHYSBASE0, IA32_MTRR_PHYSBASE(0x080000000uL, MemoryType.Uncachable))
                msr(IA32_MTRR_PHYSBASE1, IA32_MTRR_PHYSBASE())
                msr(IA32_MTRR_PHYSBASE2, IA32_MTRR_PHYSBASE())

                msr(IA32_MTRR_PHYSMASK0, IA32_MTRR_PHYSMASK(0xFFFFFFFF80000000uL))
                msr(IA32_MTRR_PHYSMASK1, IA32_MTRR_PHYSMASK(valid = false))
                msr(IA32_MTRR_PHYSMASK2, IA32_MTRR_PHYSMASK(valid = false))

                msr(IA32_MTRR_DEF_TYPE, 0x0c06u)

                msr(IA32_PAT, IA32_PAT(6u, 1u, 7u, 0u, 6u, 1u, 7u, 4u))
            }
        }
    }
}
