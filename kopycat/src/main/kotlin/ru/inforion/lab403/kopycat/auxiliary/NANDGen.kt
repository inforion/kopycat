/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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
package ru.inforion.lab403.kopycat.auxiliary

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.modules.common.NAND
import java.util.logging.Level



object NANDGen {

    @Transient val log = logger(Level.INFO)

    enum class Manufacturer(val id: Long) {
        TOSHIBA(0x98),
        SAMSUNG(0xec),
        FUJITSU(0x04),
        NATIONAL(0x8f),
        RENESAS(0x07),
        STMICRO(0x20),
        HYNIX(0xad),
        MICRON(0x2c),
        AMD(0x01),
        UNKNOW(0x0)
    }


    /**
     * NAND Flash Device IDs
     * id:		device ID code
     * pagesize:	Pagesize in bytes. Either 256 or 512 or 0
     *		If the pagesize is 0, then the real pagesize
     *		and the eraseize are determined from the
     *		extended id bytes in the chip
     * erasesize:	Size of an erase block in the flash device.
     * chipsize:	Total chipsize in Mega Bytes
     * wide:        Is IO-bus 16 bit
     */
    enum class NANDID(val id: Long, val pageSize: Long, val chipSize: Long, val eraseSize: Long, val wide: Boolean) {

        NAND_1MiB_5V_8_bit(0x6e, 256, 1, 0x1000, false),
        NAND_2MiB_5V_8_bit(0x64, 256, 2, 0x1000, false),
        NAND_4MiB_5V_8_bit(0x6b, 512, 4, 0x2000, false),
        NAND_1MiB_3_3V_8_bit_1(0xe8, 256, 1, 0x1000, false),
        NAND_1MiB_3_3V_8_bit_2(0xec, 256, 1, 0x1000, false),
        NAND_2MiB_3_3V_8_bit(0xea, 256, 2, 0x1000, false),
        NAND_4MiB_3_3V_8_bit_1(0xd5, 512, 4, 0x2000, false),
        NAND_4MiB_3_3V_8_bit_2(0xe3, 512, 4, 0x2000, false),
        NAND_4MiB_3_3V_8_bit_3(0xe5, 512, 4, 0x2000, false),
        NAND_8MiB_3_3V_8_bit_1(0xd6, 512, 8, 0x2000, false),

        NAND_8MiB_1_8V_8_bit(0x39, 512, 8, 0x2000, false),
        NAND_8MiB_3_3V_8_bit_2(0xe6, 512, 8, 0x2000, false),
        NAND_8MiB_1_8V_16_bit(0x49, 512, 8, 0x2000, true),
        NAND_8MiB_3_3V_16_bit(0x59, 512, 8, 0x2000, true),

        NAND_16MiB_1_8V_8_bit(0x33, 512, 16, 0x4000, false),
        NAND_16MiB_3_3V_8_bit(0x73, 512, 16, 0x4000, false),
        NAND_16MiB_1_8V_16_bit(0x43, 512, 16, 0x4000, true),
        NAND_16MiB_3_3V_16_bit(0x53, 512, 16, 0x4000, true),

        NAND_32MiB_1_8V_8_bit(0x35, 512, 32, 0x4000, false),
        NAND_32MiB_3_3V_8_bit(0x75, 512, 32, 0x4000, false),
        NAND_32MiB_1_8V_16_bit(0x45, 512, 32, 0x4000, true),
        NAND_32MiB_3_3V_16_bit(0x55, 512, 32, 0x4000, true),

        NAND_64MiB_1_8V_8_bit_1(0x36, 512, 64, 0x4000, false),
        NAND_64MiB_3_3V_8_bit_1(0x76, 512, 64, 0x4000, false),
        NAND_64MiB_1_8V_16_bit_1(0x46, 512, 64, 0x4000, true),
        NAND_64MiB_3_3V_16_bit_1(0x56, 512, 64, 0x4000, true),

        NAND_128MiB_1_8V_8_bit_1(0x78, 512, 128, 0x4000, false),
        NAND_128MiB_1_8V_8_bit_2(0x39, 512, 128, 0x4000, false),
        NAND_128MiB_3_3V_8_bit_1(0x79, 512, 128, 0x4000, false),
        NAND_128MiB_1_8V_16_bit_1(0x72, 512, 128, 0x4000, true),
        NAND_128MiB_1_8V_16_bit_2(0x49, 512, 128, 0x4000, true),
        NAND_128MiB_3_3V_16_bit_1(0x74, 512, 128, 0x4000, true),
        NAND_128MiB_3_3V_16_bit_2(0x59, 512, 128, 0x4000, true),

        NAND_256MiB_3_3V_8_bit_1(0x71, 512, 256, 0x4000, false),

        /*
         * These are the new chips with large page size. The pagesize and the
         * erasesize is determined from the extended id bytes
         */

        /*512 Megabit */
        NAND_64MiB_1_8V_8_bit_set(0xA2, 0,  64, 0, false),
        NAND_64MiB_3_3V_8_bit_set(0xF2, 0,  64, 0, false),
        NAND_64MiB_1_8V_16_bit_set(0xB2, 0,  64, 0, true),
        NAND_64MiB_3_3V_16_bit_set(0xC2, 0,  64, 0, true),

        /* 1 Gigabit */
        NAND_128MiB_1_8V_8_bit_set(0xA1, 0, 128, 0, false),
        NAND_128MiB_3_3V_8_bit_set(0xF1, 0, 128, 0, false),
        NAND_128MiB_1_8V_16_bit_set(0xB1, 0, 128, 0, true),
        NAND_128MiB_3_3V_16_bit_set(0xC1, 0, 128, 0, true),

        /* 2 Gigabit */
        NAND_256MiB_1_8V_8_bit_set(0xAA, 0, 256, 0, false),
        NAND_256MiB_3_3V_8_bit_set(0xDA, 0, 256, 0, false),
        NAND_256MiB_1_8V_16_bit_set(0xBA, 0, 256, 0, true),
        NAND_256MiB_3_3V_16_bit_set(0xCA, 0, 256, 0, true),

        /* 4 Gigabit */
        NAND_512MiB_1_8V_8_bit_set(0xAC, 0, 512, 0, false),
        NAND_512MiB_3_3V_8_bit_set(0xDC, 0, 512, 0, false),
        NAND_512MiB_1_8V_16_bit_set(0xBC, 0, 512, 0, true),
        NAND_512MiB_3_3V_16_bit_set(0xCC, 0, 512, 0, true),

        /* 8 Gigabit */
        NAND_1GiB_1_8V_8_bit_set(0xA3, 0, 1024, 0, false),
        NAND_1GiB_3_3V_8_bit_set(0xD3, 0, 1024, 0, false),
        NAND_1GiB_1_8V_16_bit_set(0xB3, 0, 1024, 0, true),
        NAND_1GiB_3_3V_16_bit_set(0xC3, 0, 1024, 0, true),

        /* 16 Gigabit */
        NAND_2GiB_1_8V_8_bit_set(0xA5, 0, 2048, 0, false),
        NAND_2GiB_3_3V_8_bit_set(0xD5, 0, 2048, 0, false),
        NAND_2GiB_1_8V_16_bit_set(0xB5, 0, 2048, 0, true),
        NAND_2GiB_3_3V_16_bit_set(0xC5, 0, 2048, 0, true),
    }

    fun generateIDString(mid: Long, nid: Long, blockDim: Long, pageSize: Long, spareWide: Boolean, wide: Boolean): String {
        // Page size byte
        val psb = when (pageSize) {
            1024L -> 0b00L
            2048L -> 0b01L
            4096L -> 0b10L
            8192L -> 0b11L
            else -> throw IllegalArgumentException("Can't encode page size: $pageSize (1024..8192)")
        }

        // Block size byte
        // Blocksize is multiples of 64KiB
        val bsb = when(blockDim * pageSize) {
            0x1_0000L -> 0b00L
            0x2_0000L -> 0b01L
            0x4_0000L -> 0b10L
            0x8_0000L -> 0b11L
            else -> throw IllegalArgumentException("Can't encode block size: ${(blockDim*pageSize).hex} (0x1_0000..0x8_0000)")
        }

        // Fourth byte
        val fb = psb or (spareWide.toLong() shl 2) or (bsb shl 4) or (wide.toLong() shl 6)

        return "${mid.hex2}${nid.hex2}00${fb.hex2}00"
    }

    fun generate(
            parent: Module,
            name: String,
            manufacturer: Manufacturer,
            nandId: NANDID,
            wantedBlockDim: Long = 0,
            wantedPageSize: Long = 0,
            wantedWideSpare: Boolean = false): NAND {

        val blockSize: Long
        val blockDim: Long
        val pageSize: Long
        val columns: Int
        val rows: Int
        val spareSize: Long
        if (nandId.pageSize != 0L) {
            if (wantedBlockDim != 0L || wantedPageSize != 0L)
                throw IllegalArgumentException("You can't set page size and block dim manually for this NAND ID")
            blockSize = nandId.eraseSize
            blockDim = blockSize / nandId.pageSize
            pageSize = nandId.pageSize
            spareSize = nandId.pageSize / 32
            columns = 1
            rows = if (nandId.chipSize > 32L) 3 else 2 // for devices > 32MiB
        }
        else {
            if (wantedBlockDim == 0L || wantedPageSize == 0L)
                throw IllegalArgumentException("You have to set page size and block dim manually for this NAND ID")
            blockSize = wantedBlockDim * wantedPageSize
            blockDim = wantedBlockDim
            pageSize = wantedPageSize
            spareSize = (if (wantedWideSpare) 16 else 8) * (pageSize shr 9)
            columns = 2
            rows = if (nandId.chipSize > 128L) 3 else 2 // for devices > 128MiB
        }
        val blockCount = (nandId.chipSize shl 20) / blockSize
        if (pageSize <= 512) log.severe { "Please, do not use page size 512 and less with Linux. " +
                "NAND_CMD_READOOB isn't implemented!!!" }



        return NAND(
                parent,
                name,
                generateIDString(manufacturer.id, nandId.id, blockDim, pageSize, wantedWideSpare, false),
                blockCount.toInt(),
                columns,
                rows,
                pageSize.toInt(),
                spareSize.toInt(),
                blockDim.toInt())
    }

}