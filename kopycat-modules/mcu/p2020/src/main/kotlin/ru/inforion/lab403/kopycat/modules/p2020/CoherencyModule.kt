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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.modules.BUS32
import java.io.File
import java.util.logging.Level




/*
*       +----------------------+
*       |CoherencyModule       |
*       |      +---------+     |
*       |      |LLCA     +----[>]ctrl
*    in[>]-----+         |     |
*       |      |         +----[>]out
*       |      +---------+     |
*       +----------------------+
*/
class CoherencyModule(parent: Module, name: String, startAddress: Long = 0xFF700000L) : Module(parent, name) {

    companion object {
        @Transient val log = logger(Level.FINE)
    }

    val outdump = File("temp/outdump.txt")

    inner class Ports : ModulePorts(this) {
        val outp = Master("out", BUS32)
        val ctrl = Master("ctrl", BUS32)
        val inp = Slave("in", BUS32)
    }

    override val ports = Ports()

    enum class eLCCA(val ind: Long) {
        CCSRBAR(0),
        LAW_LAWBAR0(0xC08),
        LAW_LAWAR0(0xC10),
        LAW_LAWBAR11(0xD68),
        LAW_LAWAR11(0xD70);

        companion object {
            fun find(ind: Long) = values().find { it.ind == ind } ?: GeneralException("Wrong LCCA register: ${ind.hex8}")
        }
    }

    // Target interface encodings
    enum class TIE(val id: Long, val iname: String) {
        PCIE3(0b00000, "PCI Express 3"),
        PCIE2(0b00001, "PCI Express 2"),
        PCIE1(0b00010, "PCI Express 1"),
        ELB(0b00100, "Enhanced Local Bus"),
        SRIO1(0b01100, "SRIO 1"),
        SRIO2(0b01101, "SRIO 2"),
        DDRMC(0b01111, "DDRController memory controller");

        companion object {
            fun find(id: Long) = values().find { it.id == id } ?: GeneralException("Wrong interface: $id")
        }
    }

    // Local configuration control and access
    inner class LCCA(startAddress: Long) : ISerializable {
        var address: Long = startAddress
        val size = 0x10_0000

        val LLCA_size = 0x1000

        val range: LongRange
            get() = address until (address + size)

        //TODO: REPLACE BY REGISTERS
        val regs = Array(LLCA_size / 8) { 0L }

        fun toOffset(ea: Long) = ea - address

        //TODO: REPLACE BY REGISTERS
        fun readLCCA(off: Long): Long = regs[off.toInt() / 8]

        //TODO: REPLACE BY REGISTERS
        fun writeLCCA(off: Long, value: Long) {
            val reg = eLCCA.find(off)
            when (reg) {
                eLCCA.CCSRBAR -> { address = value shl 12 }
                eLCCA.LAW_LAWBAR0 -> { log.severe { "LAW_LAWBAR0: address = ${(value shl 8).hex8}"} } //TODO: implementation
                eLCCA.LAW_LAWAR0 -> {
                    log.severe { "LAW_LAWAR0: local access window is ${if (value[31].toBool()) "enabled" else "disabled"}"}
                    log.severe { "LAW_LAWAR0: interface: ${TIE.find(value[24..20])}"}
                } //TODO: implementation
                eLCCA.LAW_LAWBAR11 -> { log.severe { "LAW_LAWBAR11: address = ${(value shl 8).hex8}"} } //TODO: implementation
                eLCCA.LAW_LAWAR11 -> {
                    log.severe { "LAW_LAWAR11: local access window is ${if (value[31].toBool()) "enabled" else "disabled"}"}
                    log.severe { "LAW_LAWAR11: interface: ${TIE.find(value[24..20])}"}
                } //TODO: implementation
            }
            regs[off.toInt() / 8] = value
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
            address = (snapshot["address"] as String).hexAsULong
            (snapshot["regs"] as List<String>).forEachIndexed { i, it -> regs[i] = it.hexAsULong}
            filter.deserialize(ctxt, snapshot["filter"] as Map<String, Any>)
        }

        private val filter = object : Area(ports.inp, 0, 0xFFFF_FFFF, "COHERENCY_AREA") {
            override fun fetch(ea: Long, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")

            override fun read(ea: Long, ss: Int, size: Int): Long =
                    if (ea in range) {
                        //TODO: See below
                        //if (size != 4)
                        //    throw GeneralException("Wrong write size: $size")

                        val off = toOffset(ea)
                        val area = eCCSR.find(off)

                        log.severe { "Read from $area at offset ${off.hex8}" }

                        when (area) {
                            eCCSR.LCCA -> readLCCA(off)
                            eCCSR.ECM -> readECM(off)
                            else -> ports.ctrl.read(off, ss, size)
                        }
                    }
                    else {
                        val ret = ports.outp.read(ea, ss, size)
                        val p2020 = parent as P2020
                        if ((p2020.e500v2.cpu.pc) != ea) {
//                        if ((ea in 0x0..0xF00000) ||  (ea in 0xF000_0000..0xF200_0000)) {
                            outdump.appendText("${(p2020.e500v2.cpu.pc - 4L).hex8}:R:${ea.hex8}:${ret.hex8}\n")
                        }
                        ret
                    }

            override fun write(ea: Long, ss: Int, size: Int, value: Long) {
                if (ea in range) {
                    //TODO: as i see at 0xFFFE2E14, there is several exceptions. So screw it
                    //if (size != 4)
                    //    throw GeneralException("Wrong write size: $size")

                    val off = toOffset(ea)
                    val area = eCCSR.find(off)

                    log.severe { "Write to $area at offset ${off.hex8}" }

                    when (area) {
                        eCCSR.LCCA -> writeLCCA(off, value)
                        eCCSR.ECM -> writeECM(off, value)
                        else -> ports.ctrl.write(off, ss, size, value)
                    }
                } else {
                    val p2020 = parent as P2020
                    //if ((p2020.e500v2.cpu.pc - 4L) != ea) {
                    //if ((ea in 0x0..0xF00000) ||  (ea in 0xF000_0000..0xF200_0000)) {
                    outdump.appendText("${(p2020.e500v2.cpu.pc - 4L).hex8}:W:${ea.hex8}:${value.hex8}\n")
                    //}
                    ports.outp.write(ea, ss, size, value)
                }
            }
        }

        init {
            regs[0] = address shr 12
            outdump.writeText("")
        }

    }

    val lcca = LCCA(startAddress)


    fun readECM(off: Long): Long {
        TODO("Unimplemented")
    }

    fun writeECM(off: Long, value: Long) {
        TODO("Unimplemented")
    }



    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "lcca" to lcca.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        lcca.deserialize(ctxt, snapshot["lcca"] as Map<String, Any>)
    }
}