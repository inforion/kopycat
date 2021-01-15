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
package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.ATA_BUS_SIZE
import ru.inforion.lab403.kopycat.modules.ATA_DATA_AREA
import ru.inforion.lab403.kopycat.modules.ATA_PARAM_AREA
import ru.inforion.lab403.kopycat.modules.ATA_SECTOR_SIZE
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN

@Suppress("PropertyName", "ClassName", "MemberVisibilityCanBePrivate")

class CompactFlash(
        parent: Module,
        name: String,
        val file: File? = null,
        val config: Int = 0x848A,
        val cylinders: Int = 980,
        val heads: Int = 4,
        val sectors: Int = 16,
        val serial: String = "N9TT-9G0A-B7FQ-RANC",
        val revision: String = "1.5.34",
        val model: String = "CF ZVEZDOLET-BUX",
        val multiSecs: Int = 0,
        val capabilities: Int = 0x300,
        val pioMode: Int = 0x200,
        val dmaMode: Int = 0,
        val valid: Int = 3,
        var currentCylinders: Int = cylinders,
        var currentHeads: Int = heads,
        var currentSectors: Int = sectors,
        val capacity: Int = cylinders * heads * sectors,
        val multiSet: Int = 0,
        val lba_size: Int = capacity,
        val singleDma: Int = 0,
        val multiDma: Int = 0,
        val advancedPio: Int = 0,
        val cycleTimeDma: Int = 0,
        val cycleTimeMulti: Int = 0,
        val cycleTimePioNoIordy: Int = 0,
        val cycleTimePioIordy: Int = 0) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val ata = Slave("ata", ATA_BUS_SIZE)
    }

    override val ports = Ports()

    private fun pack(): ByteArray {
        val baos = ByteArrayOutputStream()
        with(DataOutputStream(baos)) {
            writeShort(config)                  //  0
            writeShort(cylinders)               //  1
            writeShort(0)                    //  2
            writeShort(heads)                   //  3
            writeShort(0)                    //  4
            writeShort(0)                    //  5
            writeShort(sectors)                 //  6
            writeShort(0)                    //  7
            writeShort(0)                    //  8
            writeShort(0)                    //  9
            writeBytes("%20s".format(serial))
            writeShort(1)                    // 20
            writeShort(1)                    // 21
            writeShort(4)                    // 22
            writeBytes("%8s".format(revision))
            writeBytes("%40s".format(model))
            writeShort(multiSecs)               // 47
            writeShort(0)                    // 48
            writeShort(capabilities)            // 49
            writeShort(0)                    // 50
            writeShort(pioMode)                 // 51
            writeShort(dmaMode)                 // 52
            writeShort(valid)                   // 53
            writeShort(currentCylinders)        // 54
            writeShort(currentHeads)            // 55
            writeShort(currentSectors)          // 56
            writeShort(capacity[15..0])         // 57
            writeShort(capacity[31..16])
            writeShort(multiSet)                // 59
            writeShort(lba_size[15..0])         // 60
            writeShort(lba_size[31..16])
            writeShort(singleDma)               // 62
            writeShort(multiDma)                // 63
            writeShort(advancedPio)             // 64
            writeShort(cycleTimeDma)            // 65
            writeShort(cycleTimeMulti)          // 66
            writeShort(cycleTimePioNoIordy)     // 67
            writeShort(cycleTimePioIordy)       // 68
            write(ByteArray(374))
        }
        return baos.toByteArray()
    }

    private var parameters = pack()

    private val content = ByteBuffer.allocate(capacity * ATA_SECTOR_SIZE).apply {
        if (file != null) gzipInputStreamIfPossible(file.path).read(this.array())
    }

    val terminal = object : Area(ports.ata,"DATA") {
        override fun fetch(ea: Long, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")

        override fun read(ea: Long, ss: Int, size: Int) = when (ss) {
            ATA_PARAM_AREA -> parameters.getInt(ea.asInt, size, LITTLE_ENDIAN)

            ATA_DATA_AREA -> {
                require(size == 1)
                content.position(ea.asInt)
                content.get().asULong
            }

            else -> throw GeneralException("Unknown type: $ss")
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) = when (ss) {
            ATA_PARAM_AREA -> parameters.putInt(ea.asInt, value, size, LITTLE_ENDIAN)

            ATA_DATA_AREA -> {
                require(size == 1)
                content.position(ea.asInt)
                content.put(value.asByte)
                Unit
            }

            else -> throw GeneralException("Unknown type: $ss")
        }

//        override fun load(ea: Long, size: Int, ss: Int, onError: HardwareErrorHandler?) = when (ss) {
//            ATA_PARAM_AREA -> parameters.getArray(ea.asInt * 2, parameters.size - ea.asInt)
//
//            ATA_DATA_AREA -> ByteArray(ATA_SECTOR_SIZE).apply {
//                content.position(ea.asInt * ATA_SECTOR_SIZE)
//                content.get(this)
//            }
//
//            else -> throw GeneralException("Unknown type: $ss")
//        }
//
//        override fun store(ea: Long, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) = when (ss) {
//            ATA_PARAM_AREA -> parameters.putArray(ea.asInt * 2, data)
//
//            ATA_DATA_AREA -> {
//                content.position(ea.asInt * ATA_SECTOR_SIZE)
//                content.put(data)
//                Unit
//            }
//
//            else -> throw GeneralException("Unknown type: $ss")
//        }
    }

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
            "parameters" to parameters,
            "cf" to ctxt.storeBinary("cf", content))

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        parameters = loadValue(snapshot, "parameters")
        ctxt.loadBinary(snapshot, "cf", content)
    }
}