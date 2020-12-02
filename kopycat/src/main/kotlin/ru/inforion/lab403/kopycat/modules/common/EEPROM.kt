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
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.PIN
import java.io.File
import java.io.InputStream
import java.nio.ByteOrder.BIG_ENDIAN
import java.util.logging.Level


class EEPROM(parent: Module, name: String, stream: InputStream? = null): Module(parent, name) {
    companion object {
        @Transient val log = logger(Level.FINE)
    }

    constructor(parent: Module, name: String, resource: Resource) :
            this(parent, name, resource.inputStream())

    constructor(parent: Module, name: String, file: File) :
            this(parent, name, gzipInputStreamIfPossible(file.path))

    constructor(parent: Module, name: String, data: String) :
            this(parent, name, data.unhexlify().inputStream())

    inner class Ports : ModulePorts(this) {
        // Bitwise SPI
        val spi = Slave("spi", PIN)
    }

    override val ports = Ports()

    enum class CMD(val OPCODE: Int) {
        IDLE(-1),
        READ(0b1000),
        WRITE(0b0100),
        ERASE(0b1100),
        EWEN(0b0011),
        EWDS(0b0000),
        ERAL(0b0010),
        WRAL(0b0001)
    }

    private val dtyp: Datatype = WORD
    private val eraseValue: Long = 0xFFFF
    private val sizeInWords = 64
    private val memory = ByteArray(dtyp.bytes * sizeInWords).apply { stream?.read(this) }
    private val order = BIG_ENDIAN

    // Current state of input bits stream
    private var cmdReg: Int = 0
    private var dataReg: Int = 0
    private var bitNo: Int = 0

    // Parsed state of EEPROM
    private var start: Int = 0
    private var opcode: Int = 0
    private var eaddr: Int = 0

    private var cmd: CMD = CMD.IDLE

    // Outer module should create registers where it want to work properly
    // NOTE: If there are more than one register on of it type then it should be STATELESS!
    val EEPROM_CONTROL_REG = object : Register(ports.spi, 0, DWORD, "EEPROM_CONTROL_REG") {
        // by datasheet indexes start from 16 but register has offset in 2 bytes (lower 16 bits are reserved)
        var EEDO by bit(3)
        val EEDI by bit(2)
        val EECS by bit(1)
        val EESK by bit(0)

        fun negEdge(prev: Int, current: Int): Boolean = prev == 1 && current == 0
        fun posEdge(prev: Int, current: Int): Boolean = prev == 0 && current == 1

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val eecs = EECS
            val eesk = EESK

            super.write(ea, ss, size, value)

            if (negEdge(eecs, EECS)) onChipDisable()
            else if (posEdge(eecs, EECS)) onChipEnable()

            // datasheet tells data latched on negEdge but software and timing-diagram works on posEdge
            if (posEdge(eesk, EESK)) onClock()
        }

        private fun onChipEnable() {
            cmd = CMD.IDLE
            EEDO = 1
            cmdReg = 0
            dataReg = 0
            bitNo = 0
            start = 0
            opcode = 0
            eaddr = 0
        }

        private fun onChipDisable() = when (cmd) {
            CMD.READ -> Unit
            CMD.WRITE -> memory.putInt(eaddr * dtyp.bytes, dataReg.asULong, dtyp.bytes, order)
            CMD.ERASE -> memory.putInt(eaddr * dtyp.bytes, eraseValue, dtyp.bytes, order)
            else -> throw IllegalStateException("Unsupported command: $cmd")
        }

        private fun onCommandLatch() {
            start = cmdReg[8]
            opcode = cmdReg[7..6] shl 2
            eaddr = cmdReg[5..0]
            if (opcode == 0) {
                opcode = cmdReg[7..4]
                eaddr = 0
            }
            cmd = find<CMD> { it.OPCODE == opcode } ?: // onChipEnable()
                    throw IllegalStateException("Unknown opcode latched for $name: $opcode")
            log.fine { "$name command = $cmd[$opcode] address = $address / ${address * dtyp.bytes} start = $start" }
            if (cmd == CMD.READ) {
                dataReg = memory.getInt(eaddr * dtyp.bytes, dtyp.bytes, order).asInt
                log.fine { "$name data loaded from memory in dataReg = ${dataReg.hex8}" }
                // Setup proper order for data output to software
                dataReg = dataReg.bitReverse() ushr 16
                EEDO = 0
            }
        }

        private fun onCommandClock() {
            cmdReg = (cmdReg shl 1) or EEDI
            log.finer { "$name cmd-clock[$bitNo] cmdReg = ${cmdReg.hex8}" }
        }

        private fun onDataClock() {
            log.finer { "$name data-clock[$bitNo] dataReg = ${dataReg.hex8}" }
            when (cmd) {
                CMD.IDLE -> throw IllegalStateException("Working with IDLE $name")
                CMD.READ -> {
                    EEDO = dataReg and 1
                    dataReg = dataReg ushr 1
                }
                CMD.WRITE -> {
                    dataReg = (dataReg shl 1) or EEDI
                }
                CMD.ERASE -> Unit
                else -> throw IllegalStateException("Unsupported command: $cmd")
            }
        }

        private fun onClock() {
            if (bitNo < 9) onCommandClock() else onDataClock()

            bitNo += 1

            if (bitNo == 9) onCommandLatch()
        }
    }
}