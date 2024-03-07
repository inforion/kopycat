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
package ru.inforion.lab403.kopycat.experimental.linux.biosless

import org.jetbrains.kotlin.library.impl.buffer
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.auxiliary.fields.common.AbsoluteField
import ru.inforion.lab403.kopycat.auxiliary.fields.delegates.absoluteField
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IMemoryRef
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import java.nio.ByteOrder

/**
 * bzImage boot preparation class.
 *
 * Related [kernel documentation](https://www.kernel.org/doc/html/latest/arch/x86/boot.html#the-real-mode-kernel-header).
 * @throws bzImageAncientProtocolException when boot protocol version is less than 2.04 (Linux 2.6.14)
 */
class bzImage(private val bytes: ByteArray) : IMemoryRef, IReadWrite {
    override val memory = this

    class LoadFlagsClass(mem: IReadWrite, name: String) : AbsoluteField(mem, name, 0x211uL, BYTE.bytes) {
        /**
         * If 0, the protected-mode code is loaded at 0x10000.
         *
         * If 1, the protected-mode code is loaded at 0x100000.
         */
        val loadedHigh by bit(0)

        /**
         * Set this bit to 1 to indicate that the value entered in the
         * [heapEndPtr] is valid. If this field is clear,
         * some setup code functionality will be disabled.
         */
        var canUseHeap by bit(7)
    }

    /**
     * The size of the setup code in 512-byte sectors.
     * If this field is 0, the real value is 4.
     * The real-mode code consists of the boot sector (always one 512-byte sector) plus the setup code.
     */
    private var setupSects by absoluteField("setupSects", 0x1f1uL, BYTE.bytes)

    /**
     * The size of the protected-mode code in units of 16-byte paragraphs.
     * For protocol versions older than 2.04 this field is only two bytes wide,
     * and therefore cannot be trusted for the size of a kernel
     * if the [LoadFlagsClass.loadedHigh] flag is set.
     */
    private val sysSize by absoluteField("sysSize", 0x1f4uL, DWORD.bytes)

    /**
     * Contains the boot protocol version, in (major << 8)+minor format,
     * e.g. 0x0204 for version 2.04, and 0x0a11 for a hypothetical version 10.17.
     */
    private val bootVersion by absoluteField("bootVersion", 0x206uL, WORD.bytes)

    /**
     * If set to a nonzero value, contains a pointer to a NUL-terminated human-readable
     * kernel version number string, less 0x200. This can be used to display the kernel version to the user.
     * This value should be less than (0x200*setup_sects).
     */
    private val kernelVersion by absoluteField("kernelVersion", 0x20euL, WORD.bytes)

    /**
     * If your boot loader has an assigned id (see table below), enter 0xTV here, where T is an identifier for
     * the boot loader and V is a version number. Otherwise, enter 0xFF here.
     */
    private var typeOfLoader by absoluteField("typeOfLoader", 0x210uL, BYTE.bytes)

    /** This field is a bitmask. */
    private val loadFlags = LoadFlagsClass(this, "loadFlags")

    /**
     * Set this field to the offset (from the beginning of the real-mode code) of
     * the end of the setup stack/heap, minus 0x0200.
     */
    private var heapEndPtr by absoluteField("heapEndPtr", 0x224uL, WORD.bytes)

    /**
     * This field is used as an extension of the type number in [typeOfLoader] field.
     * If the type in [typeOfLoader] is 0xE, then the actual type is ([extLoaderType] + 0x10).
     */
    private var extLoaderType by absoluteField("extLoaderType", 0x227uL, BYTE.bytes)

    /**
     * Set this field to the linear address of the kernel command line.
     * The kernel command line can be located anywhere between the end of the setup heap and 0xA0000;
     * it does not have to be located in the same 64K segment as the real-mode code itself.
     */
    private var cmdLinePtr by absoluteField("cmdLinePtr", 0x228uL, DWORD.bytes)

    /**
     * The 32-bit linear address of the initial ramdisk or ramfs.
     * Leave at zero if there is no initial ramdisk/ramfs.
     */
    private var ramdiskImage by absoluteField("ramdiskImage", 0x218uL, DWORD.bytes)

    /**
     * Size of the initial ramdisk or ramfs.
     * Leave at zero if there is no initial ramdisk/ramfs.
     */
    private var ramdiskSize by absoluteField("ramdiskSize", 0x21cuL, DWORD.bytes)

    /** Kernel version string or null if [kernelVersion] is zero */
    private val kernelVersionString by lazy {
        if (kernelVersion.truth) {
            (0..INT_MAX)
                .asSequence()
                .map { bytes[it + kernelVersion.int + 0x200].char }
                .takeWhile { it.truth }
                .joinToString(separator = "").trim()
        } else {
            null
        }
    }

    init {
        // For backwards compatibility, if the setup_sects field contains 0, the real value is 4.
        if (setupSects == 0uL) {
            setupSects = 4uL
        }

        // For boot protocol prior to 2.04, the upper two bytes of the syssize field are
        // unusable, which means the size of a bzImage kernel cannot be determined.
        if (bootVersion < 0x0204uL) {
            throw bzImageAncientProtocolException(bootVersion)
        }
    }

    /**
     * Prepares bzImage for boot.
     *
     * @return pair of [ByteArray]s: setup code to be loaded at 0x10000 and protected mode code
     * to be loaded at 0x10000 or 0x100000 depending on [LoadFlagsClass.loadedHigh]
     * @throws NotImplementedError when [LoadFlagsClass.loadedHigh] is zero; loading at 0x10000 is not implemented
     */
    fun prepareBoot(
        heapEndPtr: ULong,
        cmdLinePtr: ULong,
        ramdiskImage: ULong,
        ramdiskSize: ULong,
    ): Pair<ByteArray, ByteArray> {
        if (loadFlags.loadedHigh.untruth) {
            TODO("Load protected mode code at 0x10000")
        }
        typeOfLoader = 0xe1uL
        extLoaderType = 0x01uL
        loadFlags.canUseHeap = 1
        this.heapEndPtr = heapEndPtr
        this.cmdLinePtr = cmdLinePtr
        this.ramdiskImage = ramdiskImage
        this.ramdiskSize = ramdiskSize

        return setupCode() to protectedModeCode()
    }

    /** Code to be loaded at 0x10000 */
    private fun setupCode() = bytes[0 until 512 * (setupSects.int + 1)]

    /** Code to be loaded at or 0x10000 or 0x100000 depending on [LoadFlagsClass.loadedHigh] */
    private fun protectedModeCode() = (512 * (setupSects.int + 1)).let { bytes[it until it + sysSize.int * 16] }

    override fun read(ea: ULong, ss: Int, size: Int) = with(bytes.buffer.order(ByteOrder.LITTLE_ENDIAN)) {
        when (size) {
            BYTE.bytes -> get(ea.int).ulong_z
            WORD.bytes -> getShort(ea.int).ulong_z
            DWORD.bytes -> getInt(ea.int).ulong_z
            else -> TODO("Unexpected read size")
        }
    }

    override fun write(ea: ULong, ss: Int, size: Int, value: ULong): Unit =
        with(bytes.buffer.order(ByteOrder.LITTLE_ENDIAN)) {
            when (size) {
                BYTE.bytes -> put(ea.int, value.byte)
                WORD.bytes -> putShort(ea.int, value.short)
                DWORD.bytes -> putInt(ea.int, value.int)
                else -> TODO("Unexpected write size")
            }
        }

    override fun toString() = "bzImage: boot protocol " +
            "version = ${bootVersion[15..8]}.${bootVersion[7..0]}, " +
            "kernel version = $kernelVersionString, " +
            "setup_sects = $setupSects, " +
            "syssize: $sysSize, " +
            "protected mode is in high memory: ${loadFlags.loadedHigh.truth}"
}
