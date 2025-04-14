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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.ISerializable




/*
*       +----------------------+
*       |CoherencyModule       |
*       |      +---------+     |
*       |      |LLCA     +----[>]ctrl
*    in[>]-----+         |     |
*       |      |         +----[>]out
*       |      |         |     |
*       |      |         +----[>]io
*       |      +---------+     |
*       +----------------------+
*/
// TODO: rename to bridge
class CoherencyModule(parent: Module, name: String, startAddress: ULong = 0xFF700000uL) : Module(parent, name) {

    companion object {
        @Transient val log = logger(FINE)
    }

    inner class Ports : ModulePorts(this) {
        val outp = Port("out")
        val ctrl = Port("ctrl")
//        val io = Master("io", BUS32)
        val inp = Port("in")
    }

    override val ports = Ports()

    enum class eLCCA(val ind: ULong) {
        CCSRBAR(0u),
        LAW_LAWBAR0(0xC08u),
        LAW_LAWAR0(0xC10u),
        LAW_LAWBAR1(0xC28u),
        LAW_LAWAR1(0xC30u),
        LAW_LAWBAR2(0xC48u),
        LAW_LAWAR2(0xC50u),
        LAW_LAWBAR3(0xC68u),
        LAW_LAWAR3(0xC70u),
        LAW_LAWBAR4(0xC88u),
        LAW_LAWAR4(0xC90u),
        LAW_LAWBAR5(0xCA8u),
        LAW_LAWAR5(0xCB0u),
        LAW_LAWBAR6(0xCC8u),
        LAW_LAWAR6(0xCD0u),
        LAW_LAWBAR7(0xCE8u),
        LAW_LAWAR7(0xCF0u),
        LAW_LAWBAR8(0xD08u),
        LAW_LAWAR8(0xD10u),
        LAW_LAWBAR9(0xD28u),
        LAW_LAWAR9(0xD30u),
        LAW_LAWBAR10(0xD48u),
        LAW_LAWAR10(0xD50u),
        LAW_LAWBAR11(0xD68u),
        LAW_LAWAR11(0xD70u);

        companion object {
            fun find(ind: ULong) = values().find { it.ind == ind } ?: GeneralException("Wrong LCCA register: ${ind.hex8}")
        }
    }

    // Target interface encodings
    enum class TIE(val id: ULong, val iname: String) {
        PCIE3(0b00000u, "PCI Express 3"),
        PCIE2(0b00001u, "PCI Express 2"),
        PCIE1(0b00010u, "PCI Express 1"),
        ELB(0b00100u, "Enhanced Local Bus"),
        SRIO1(0b01100u, "SRIO 1"),
        SRIO2(0b01101u, "SRIO 2"),
        DDRSDRAM1(0b01111u, "DDR SDRAM 1"),
        DDRSDRAM2(0b10110u, "DDR SDRAM 2");

        companion object {
            fun find(id: ULong) = values().find { it.id == id } ?: GeneralException("Wrong interface: $id")
        }
    }

    // Local configuration control and access
    inner class LCCA(startAddress: ULong) : ISerializable {
        var address = startAddress
        val size = 0x10_0000

        val LLCA_size = 0x1000

        val range get() = address until (address + size)

        //TODO: REPLACE BY REGISTERS
        val regs = Array(LLCA_size / 8) { 0uL }

        fun toOffset(ea: ULong) = ea - address

        //TODO: REPLACE BY REGISTERS
        fun readLCCA(off: ULong): ULong {
            if (off % 8u != 0uL)
                log.warning { "Read from not aligned address: ${off.hex8}" }
            return regs[off.int / 8]
        }

        private fun defaultLAWBAR(ind: Int, value: ULong) {
            log.severe { "LAW_LAWBAR$ind: address = ${(value shl 8).hex8}"}
        }

        private fun defaultLAWAR(ind: Int, value: ULong) {
            val sizeValue = value[5..0].int + 1
            val postfix = when (sizeValue / 10) {
                1 -> "KB"
                2 -> "MB"
                3 -> "GB"
                else -> throw GeneralException("Wrong size value: ${sizeValue - 1}")
            }

            val size = 1 shl (sizeValue % 10)
            log.severe { "LAW_LAWAR$ind: local access window is ${if (value[31].truth) "enabled" else "disabled"}"}
            log.severe { "LAW_LAWAR$ind: interface: ${TIE.find(value[24..20])}"}
            log.severe { "LAW_LAWAR$ind: size: $size $postfix"}
        }

        //TODO: REPLACE BY REGISTERS
        fun writeLCCA(off: ULong, value: ULong) {
            require (off % 8u == 0uL) { "Read from not aligned address: ${off.hex8}" }

            val reg = eLCCA.find(off)
            when (reg) {
                eLCCA.CCSRBAR -> {
                    address = value shl 12
                    log.warning { "LAW_CCSRBAR changed => Now remapped at ${address.hex8}" }
                }
                eLCCA.LAW_LAWBAR0 -> defaultLAWBAR(0, value)
                eLCCA.LAW_LAWBAR1 -> defaultLAWBAR(1, value)
                eLCCA.LAW_LAWBAR2 -> defaultLAWBAR(2, value)
                eLCCA.LAW_LAWBAR3 -> defaultLAWBAR(3, value)
                eLCCA.LAW_LAWBAR4 -> defaultLAWBAR(4, value)
                eLCCA.LAW_LAWBAR5 -> defaultLAWBAR(5, value)
                eLCCA.LAW_LAWBAR6 -> defaultLAWBAR(6, value)
                eLCCA.LAW_LAWBAR7 -> defaultLAWBAR(7, value)
                eLCCA.LAW_LAWBAR8 -> defaultLAWBAR(8, value)
                eLCCA.LAW_LAWBAR9 -> defaultLAWBAR(9, value)
                eLCCA.LAW_LAWBAR10 -> defaultLAWBAR(10, value)
                eLCCA.LAW_LAWBAR11 -> defaultLAWBAR(11, value)
                eLCCA.LAW_LAWAR0 -> defaultLAWAR(0, value)
                eLCCA.LAW_LAWAR1 -> defaultLAWAR(1, value)
                eLCCA.LAW_LAWAR2 -> defaultLAWAR(2, value)
                eLCCA.LAW_LAWAR3 -> defaultLAWAR(3, value)
                eLCCA.LAW_LAWAR4 -> defaultLAWAR(4, value)
                eLCCA.LAW_LAWAR5 -> defaultLAWAR(5, value)
                eLCCA.LAW_LAWAR6 -> defaultLAWAR(6, value)
                eLCCA.LAW_LAWAR7 -> defaultLAWAR(7, value)
                eLCCA.LAW_LAWAR8 -> defaultLAWAR(8, value)
                eLCCA.LAW_LAWAR9 -> defaultLAWAR(9, value)
                eLCCA.LAW_LAWAR10 -> defaultLAWAR(10, value)
                eLCCA.LAW_LAWAR11 -> defaultLAWAR(11, value)
            }
            regs[off.int / 8] = value
        }


        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return mapOf(
                    "address" to address.hex,
                    "regs" to regs.map { it.hex },
                    "filter" to filter.serialize(ctxt)
            )
        }

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            address = (snapshot["address"] as String).ulongByHex
            (snapshot["regs"] as List<String>).forEachIndexed { i, it -> regs[i] = it.ulongByHex}
            filter.deserialize(ctxt, snapshot["filter"] as Map<String, Any>)
        }

        private val filter = object : Area(ports.inp, 0u, 0xFFFF_FFFFu, "COHERENCY_AREA") {
            override fun fetch(ea: ULong, ss: Int, size: Int) = read(ea, ss, size)

            override fun read(ea: ULong, ss: Int, size: Int): ULong =
                    if (ea in range) {
                        //TODO: See below
                        //if (size != 4)
                        //    throw GeneralException("Wrong write size: $size")

                        val off = toOffset(ea)
                        val area = eCCSR.find(off)

//                        log.severe { "Read from $area at offset ${off.hex8}" }

                        when (area) {
                            eCCSR.LCCA -> readLCCA(off) // TODO: remove
                            else -> ports.ctrl.read(off, ss, size)
                        }
                    }
                    else {
                        val ret = ports.outp.read(ea, ss, size)
                        val p2020 = parent as P2020
                        if ((p2020.e500v2.cpu.pc) != ea) {
//                        if ((ea in 0x0..0xF00000) ||  (ea in 0xF000_0000..0xF200_0000)) {
//                            outdump.appendText("${(p2020.e500v2.cpu.pc - 4L).hex8}:R:${ea.hex8}:${ret.hex8}\n")
                        }
                        ret
                    }

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                if (ea in range) {
                    //TODO: as i see at 0xFFFE2E14, there is several exceptions. So screw it
                    //if (size != 4)
                    //    throw GeneralException("Wrong write size: $size")

                    val off = toOffset(ea)
                    val area = eCCSR.find(off)

//                    log.severe { "Write to $area at offset ${off.hex8}" }

                    when (area) {
                        eCCSR.LCCA -> writeLCCA(off, value) // TODO: remove
                        else -> ports.ctrl.write(off, ss, size, value)
                    }
                } else {
                    val p2020 = parent as P2020
                    //if ((p2020.e500v2.cpu.pc - 4L) != ea) {
                    //if ((ea in 0x0..0xF00000) ||  (ea in 0xF000_0000..0xF200_0000)) {
//                    outdump.appendText("${(p2020.e500v2.cpu.pc - 4L).hex8}:W:${ea.hex8}:${value.hex8}\n")
                    //}
                    ports.outp.write(ea, ss, size, value)
                }
            }
        }

        init {
            regs[0] = address ushr 12
        }

    }

    val lcca = LCCA(startAddress)


    override fun serialize(ctxt: GenericSerializer) = mapOf(
            "lcca" to lcca.serialize(ctxt)
    )

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        lcca.deserialize(ctxt, snapshot["lcca"] as Map<String, Any>)
    }
}