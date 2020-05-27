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

import ru.inforion.lab403.common.extensions.asULong
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder



class NANDPart(val pageSize: Int, val pagesInBlock: Int, val blockCount: Int, val spareSize: Int) {

    val fullPageSize = pageSize + spareSize
    val blockSize = fullPageSize * pagesInBlock
    val size = blockCount * blockSize

    val buffer: ByteBuffer = ByteBuffer.allocate(size).apply { order(ByteOrder.LITTLE_ENDIAN) }

//    init {
//        for (i in 0 until buffer.limit())
//            buffer.put(0xFC.toByte())
//    }

    fun load(stream: InputStream): NANDPart {
        var total = 0
        val buf = ByteArray(pageSize)
        buffer.position(0)
        var offset = 0
        do {
            val count = stream.read(buf, 0, pageSize - offset)
            if (count < 0)
                break

            offset += count

//            println("off: ${buffer.position()/1024/1024} $count")
            buffer.put(buf, 0, count)

//            if (count != pageSize)
//                println("${buffer.position().hex} : ${count.hex}")

            if (offset == pageSize) {
                offset = 0
                for (i in 0 until spareSize)
                    buffer.put(0x7C)
//                buffer.position(buffer.position() + spareSize)
//                total += count
            }
        } while (count > 0)

        return this
    }

    fun fillSpare(vararg data: Byte): NANDPart {
        for (i in 0 until pagesInBlock*blockCount) {
            buffer.position(i*fullPageSize + pageSize)
            buffer.put(data)
        }
        return this
    }


    val invparity = arrayOf(
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1
    )

    // bp - input buffer
    // wideEcc - if true, ECC size is 512, else 256
    // smc - CONFIG_MTD_NAND_ECC_SMC is y
    fun nandCalculateEcc(bp: ByteBuffer, wideEcc: Boolean, bigEndian: Boolean = false, smc: Boolean = false): ByteArray {

        /* rp0..rp15..rp17 are the various accumulated parities (per byte) */
        var rp0 = 0L
        var rp1 = 0L
        var rp2 = 0L
        var rp3 = 0L
        var rp4 = 0L
        var rp5 = 0L
        var rp6 = 0L
        var rp7 = 0L
        var rp8 = 0L
        var rp9 = 0L
        var rp10 = 0L
        var rp11 = 0L
        var rp12 = 0L
        var rp13 = 0L
        var rp14 = 0L
        var rp15 = 0L
        var rp16 = 0L
        var rp17 = 0L

        var cur = 0L /* current value in buffer */
        var par = 0L		/* the cumulative parity for all data */
        var tmppar = 0L /*  the cumulative parity for this iteration;
                            for rp12, rp14 and rp16 at the end of the loop */
        val eccsize_mult = if (wideEcc) 2 else 1 /* 256 or 512 bytes/ecc  */


        /*
         * The loop is unrolled a number of times;
         * This avoids if statements to decide on which rp value to update
         * Also we process the data by longwords.
         * Note: passing unaligned data might give a performance penalty.
         * It is assumed that the buffers are aligned.
         * tmppar is the cumulative sum of this iteration.
         * needed for calculating rp12, rp14, rp16 and par
         * also used as a performance improvement for rp6, rp8 and rp10
         */
        for (i in 0 until (eccsize_mult shl 2)) {
            cur = bp.int.asULong
            tmppar = cur
            rp4 = rp4 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp6 = rp6 xor tmppar
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp4 = rp4 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp8 = rp8 xor tmppar

            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp4 = rp4 xor cur
            rp6 = rp6 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp6 = rp6 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp4 = rp4 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp10 = rp10 xor tmppar

            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp4 = rp4 xor cur
            rp6 = rp6 xor cur
            rp8 = rp8 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp6 = rp6 xor cur
            rp8 = rp8 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp4 = rp4 xor cur
            rp8 = rp8 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp8 = rp8 xor cur

            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp4 = rp4 xor cur
            rp6 = rp6 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp6 = rp6 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur
            rp4 = rp4 xor cur
            cur = bp.int.asULong
            tmppar = tmppar xor cur

            par = par xor tmppar
            if ((i and 0x1) == 0)
                rp12 = rp12 xor tmppar
            if ((i and 0x2) == 0)
                rp14 = rp14 xor tmppar
            if (eccsize_mult == 2 && (i and 0x4) == 0)
                rp16 = rp16 xor tmppar
        }
        /*
         * handle the fact that we use longword operations
         * we'll bring rp4..rp14..rp16 back to single byte entities by
         * shifting and xoring first fold the upper and lower 16 bits,
         * then the upper and lower 8 bits.
         */
        rp4 = rp4 xor (rp4 shr 16)
        rp4 = rp4 xor (rp4 shr 8)
        rp4 = rp4 and 0xff
        rp6 = rp6 xor (rp6 shr 16)
        rp6 = rp6 xor (rp6 shr 8)
        rp6 = rp6 and 0xff
        rp8 = rp8 xor (rp8 shr 16)
        rp8 = rp8 xor (rp8 shr 8)
        rp8 = rp8 and 0xff
        rp10 = rp10 xor (rp10 shr 16)
        rp10 = rp10 xor (rp10 shr 8)
        rp10 = rp10 and 0xff
        rp12 = rp12 xor (rp12 shr 16)
        rp12 = rp12 xor (rp12 shr 8)
        rp12 = rp12 and 0xff
        rp14 = rp14 xor (rp14 shr 16)
        rp14 = rp14 xor (rp14 shr 8)
        rp14 = rp14 and 0xff
        if (eccsize_mult == 2) {
            rp16 = rp16 xor (rp16 shr 16)
            rp16 = rp16 xor (rp16 shr 8)
            rp16 = rp16 and 0xff
        }

        /*
         * we also need to calculate the row parity for rp0..rp3
         * This is present in par, because par is now
         * rp3 rp3 rp2 rp2 in little endian and
         * rp2 rp2 rp3 rp3 in big endian
         * as well as
         * rp1 rp0 rp1 rp0 in little endian and
         * rp0 rp1 rp0 rp1 in big endian
         * First calculate rp2 and rp3
         */
        if (bigEndian) {
            rp2 = (par shr 16)
            rp2 = rp2 xor (rp2 shr 8)
            rp2 = rp2 and 0xff
            rp3 = par and 0xffff
            rp3 = rp3 xor (rp3 shr 8)
            rp3 = rp3 and 0xff
        }
        else {
            rp3 = (par shr 16)
            rp3 = rp3 xor (rp3 shr 8)
            rp3 = rp3 and 0xff
            rp2 = par and 0xffff
            rp2 = rp2 xor (rp2 shr 8)
            rp2 = rp2 and 0xff
        }

        /* reduce par to 16 bits then calculate rp1 and rp0 */
        par = par xor (par shr 16)
        if (bigEndian) {
            rp0 = (par shr 8) and 0xff
            rp1 = (par and 0xff)
        }
        else {
            rp1 = (par shr 8) and 0xff
            rp0 = (par and 0xff)
        }

        /* finally reduce par to 8 bits */
        par = par xor (par shr 8)
        par = par and 0xff

        /*
         * and calculate rp5..rp15..rp17
         * note that par = rp4 xor rp5 and due to the commutative property
         * of the xor operator we can say:
         * rp5 = (par xor rp4);
         * The and 0xff seems superfluous, but benchmarking learned that
         * leaving it out gives slightly worse results. No idea why, probably
         * it has to do with the way the pipeline in pentium is organized.
         */
        rp5 = (par xor rp4) and 0xff
        rp7 = (par xor rp6) and 0xff
        rp9 = (par xor rp8) and 0xff
        rp11 = (par xor rp10) and 0xff
        rp13 = (par xor rp12) and 0xff
        rp15 = (par xor rp14) and 0xff
        rp17 = (par xor rp16) and 0xff

        /*
         * Finally calculate the ecc bits.
         * Again here it might seem that there are performance optimisations
         * possible, but benchmarks showed that on the system this is developed
         * the code below is the fastest
         */
        val code = byteArrayOf(0, 0, 0)
        if (smc) {
            code[0] = ((invparity[rp7.toInt()] shl 7) or
            (invparity[rp6.toInt()] shl 6) or
            (invparity[rp5.toInt()] shl 5) or
            (invparity[rp4.toInt()] shl 4) or
            (invparity[rp3.toInt()] shl 3) or
            (invparity[rp2.toInt()] shl 2) or
            (invparity[rp1.toInt()] shl 1) or
            (invparity[rp0.toInt()])).toByte()
            code[1] = ((invparity[rp15.toInt()] shl 7) or
            (invparity[rp14.toInt()] shl 6) or
            (invparity[rp13.toInt()] shl 5) or
            (invparity[rp12.toInt()] shl 4) or
            (invparity[rp11.toInt()] shl 3) or
            (invparity[rp10.toInt()] shl 2) or
            (invparity[rp9.toInt()] shl 1)  or
            (invparity[rp8.toInt()])).toByte()
        }
        else {
            code[1] = ((invparity[rp7.toInt()] shl 7) or
            (invparity[rp6.toInt()] shl 6) or
            (invparity[rp5.toInt()] shl 5) or
            (invparity[rp4.toInt()] shl 4) or
            (invparity[rp3.toInt()] shl 3) or
            (invparity[rp2.toInt()] shl 2) or
            (invparity[rp1.toInt()] shl 1) or
            (invparity[rp0.toInt()])).toByte()
            code[0] = ((invparity[rp15.toInt()] shl 7) or
            (invparity[rp14.toInt()] shl 6) or
            (invparity[rp13.toInt()] shl 5) or
            (invparity[rp12.toInt()] shl 4) or
            (invparity[rp11.toInt()] shl 3) or
            (invparity[rp10.toInt()] shl 2) or
            (invparity[rp9.toInt()] shl 1)  or
            (invparity[rp8.toInt()])).toByte()
        }
        if (eccsize_mult == 1) {
            code[2] = ((invparity[(par and 0xf0).toInt()] shl 7) or
            (invparity[(par and 0x0f).toInt()] shl 6) or
            (invparity[(par and 0xcc).toInt()] shl 5) or
            (invparity[(par and 0x33).toInt()] shl 4) or
            (invparity[(par and 0xaa).toInt()] shl 3) or
            (invparity[(par and 0x55).toInt()] shl 2) or
            3).toByte()
        }
        else {
            code[2] = ((invparity[(par and 0xf0).toInt()] shl 7) or
            (invparity[(par and 0x0f).toInt()] shl 6) or
            (invparity[(par and 0xcc).toInt()] shl 5) or
            (invparity[(par and 0x33).toInt()] shl 4) or
            (invparity[(par and 0xaa).toInt()] shl 3) or
            (invparity[(par and 0x55).toInt()] shl 2) or
            (invparity[rp17.toInt()] shl 1) or
            (invparity[rp16.toInt()] shl 0)).toByte()
        }
        return code
    }

    class NandECCLayout(val eccBytes: Int, val eccPos: IntArray)

    val nand_oob_8 = NandECCLayout(
            3,
            intArrayOf(0, 1, 2)
    )

    val nand_oob_16 = NandECCLayout(
            6,
            intArrayOf(0, 1, 2, 3, 6, 7)
    )

    val nand_oob_64 = NandECCLayout(
            24,
            intArrayOf(
                    40, 41, 42, 43, 44, 45, 46, 47,
                    48, 49, 50, 51, 52, 53, 54, 55,
                    56, 57, 58, 59, 60, 61, 62, 63
            )
    )

    val nand_oob_128 = NandECCLayout(
            48,
            intArrayOf(
                    80, 81, 82, 83, 84, 85, 86, 87,
                    88, 89, 90, 91, 92, 93, 94, 95,
                    96, 97, 98, 99, 100, 101, 102, 103,
                    104, 105, 106, 107, 108, 109, 110, 111,
                    112, 113, 114, 115, 116, 117, 118, 119,
                    120, 121, 122, 123, 124, 125, 126, 127
            )
    )
    val layout = when(spareSize) {
        8 -> nand_oob_8
        16 -> nand_oob_16
        64 -> nand_oob_64
        128 -> nand_oob_128
        else -> throw NotImplementedError("Not allowed")
    }


    fun fillECC(bigEndian: Boolean = false, smc: Boolean = false): NANDPart {
        val steps = pageSize / 256
        if (bigEndian)
            throw NotImplementedError("Not implemented")
//        val total = steps * 3

        for (i in 0 until pagesInBlock*blockCount) {

            for (j in 0 until steps) {
                buffer.position(i*fullPageSize + j*256)
                val ecc = nandCalculateEcc(buffer, false, bigEndian, smc)
             
                for (k in 0..2) {
                    buffer.position(i * fullPageSize + pageSize + layout.eccPos[j*3 + k])
                    buffer.put(ecc[k])
                }
            }
        }
        return this
    }

}