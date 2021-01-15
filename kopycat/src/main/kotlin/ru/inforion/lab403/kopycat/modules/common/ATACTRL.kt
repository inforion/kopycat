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
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.serializer.*
import java.nio.ByteBuffer
import java.util.logging.Level.FINER
import java.util.logging.Level.FINEST


@Suppress("PropertyName", "ClassName", "MemberVisibilityCanBePrivate", "unused")

class ATACTRL(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val ata = Master("ata", ATA_BUS_SIZE)

        val irq = Master("irq", PIN)
        val io = Slave("io", BUS16)
    }

    override val ports = Ports()

    enum class COMMAND(val id: Int) {
        ATA_CMD_IDLE(-1),
        ATA_CMD_RECALIB(0x10),
        ATA_CMD_READ(0x20),
        ATA_CMD_WRITE(0x30),
        ATA_CMD_SEEK(0x70),
        ATA_CMD_DIAGNOSE(0x90),
        ATA_CMD_INITP(0x91),
        ATA_CMD_SET_MULTI(0xC6),
        ATA_CMD_IDENT_DEV(0xEC),
        ATA_CMD_SET_FEATURE(0xEF)
    }

    enum class ERROR(val id: Int) {
        NO_ERROR_DETECTED(0x01),
        FORMATTER_DEVICE_ERROR(0x02),
        SECTOR_BUFFER_ERROR(0x03),
        ECC_CIRCUITRY_ERROR(0x04),
        CONTROLLING_MICROPROCESSOR_ERROR(0x05),
        COMMON_ERROR(0xFF)
    }

    private val buffer = ByteBuffer.allocate(ATA_SECTOR_SIZE)

    private var lba = 0
    private var sectorsCount = 0

    private var command = COMMAND.ATA_CMD_IDLE
    private var error = ERROR.NO_ERROR_DETECTED

    // TODO: Move to common
    private fun ByteBuffer.rewrite(data: ByteArray) {
        clear()
        put(data)
        rewind()
    }

    private fun readParameters(buf: ByteBuffer) = buf.rewrite(ports.ata.load(0, ATA_SECTOR_SIZE, ATA_PARAM_AREA))

    private fun readParameter(id: Int): Long = ports.ata.inw(2 * id.asULong, ATA_PARAM_AREA)
    private fun writeParameter(id: Int, value: Int) = ports.ata.outw(2 * id.asULong, value.asULong, ATA_PARAM_AREA)

    private fun readSector(buf: ByteBuffer, lba: Int, offset: Int = 0) =
            buf.rewrite(ports.ata.load(getAddress(lba, offset), ATA_SECTOR_SIZE, ATA_DATA_AREA))
    private fun writeSector(buf: ByteBuffer, lba: Int, offset: Int = 0) =
            ports.ata.store(getAddress(lba, offset), buf.array(), ATA_DATA_AREA)

    private fun getAddress(lba: Int, offset: Int) = (lba * ATA_SECTOR_SIZE + offset).asULong

    private fun getLogicalBlockAddress(): Int {
        return if (ATA_SDH.LBA == 1) {
            insert(ATA_SDH.HS.asULong, 27..24)
                    .insert(ATA_CYL_HI.data, 23..16)
                    .insert(ATA_CYL_LO.data, 15..8)
                    .insert(ATA_SECTOR.data, 7..0)
        } else {
            val heads = readParameter(ATA_CURRENT_HEADS_PRM_ID)
            val sectors = readParameter(ATA_CURRENT_SECTORS_PRM_ID)
            val head = ATA_SDH.HS
            val sector = ATA_SECTOR.data
            val cyl = insert(ATA_CYL_HI.data, 15..8)
                    .insert(ATA_CYL_LO.data, 7..0)
            (cyl * heads + head) * sectors + (sector - 1)
        }.asInt
    }

    private fun getSectorsCount(): Int = ATA_SECCNT.data.asInt

    val readyTimer = object : SystemClock.OneshotTimer("ATA Work Ready Timer") {

        override fun trigger() {
            super.trigger()

            log.finest { "%s triggered at %,d us".format(name, core.clock.time()) }

            if (ATA_DCONTROL.IEn == 0)
                ports.irq.request(0)

            ATA_ERROR.data = error.id.asULong

            if (error != ERROR.NO_ERROR_DETECTED) {
                error = ERROR.NO_ERROR_DETECTED
                ATA_ASTATUS.BUSY = 0
                ATA_ASTATUS.RDY = 1
                return
            }

            when (command) {
                COMMAND.ATA_CMD_DIAGNOSE -> {
                    log.info { "ATA diagnose command done!" }
                    command = COMMAND.ATA_CMD_IDLE
                }

                COMMAND.ATA_CMD_IDENT_DEV -> {
                    readParameters(buffer)
                    ATA_ASTATUS.DRQ = 1
                }

                COMMAND.ATA_CMD_INITP -> {
                    writeParameter(55, ATA_SDH.HS + 1)  // currentHeads
                    writeParameter(56, ATA_SECCNT.data.asInt)  // currentSectors
                    command = COMMAND.ATA_CMD_IDLE
                }

                COMMAND.ATA_CMD_READ -> {
                    readSector(buffer, lba++)
                    ATA_ASTATUS.DRQ = 1
                }

                COMMAND.ATA_CMD_WRITE -> {
                    buffer.clear()
                    ATA_ASTATUS.DRQ = 1
                }

                else -> log.warning { "ATA command int. processing not implemented: $command" }
            }

            ATA_ASTATUS.BUSY = 0
            ATA_ASTATUS.RDY = 1
        }
    }

    val ATA_DATA = object : Register(ports.io, 0x1F0, WORD, "ATA_DATA") {

        override fun read(ea: Long, ss: Int, size: Int): Long {
            when (command) {
                COMMAND.ATA_CMD_IDLE -> {
                    log.warning { "Reading when IDLE state..." }
                }

                COMMAND.ATA_CMD_IDENT_DEV -> {
                    val result = when (size) {
                        WORD.bytes -> buffer.short.asULong
                        BYTE.bytes -> buffer.byte.asULong
                        else -> throw IllegalArgumentException("Wrong datatype!")
                    }

                    if (buffer.remaining() == 0) {
                        command = COMMAND.ATA_CMD_IDLE
                        ATA_ASTATUS.DRQ = 0
                    }

                    return result
                }

                COMMAND.ATA_CMD_READ -> {
                    val result = when (size) {
                        WORD.bytes -> buffer.short.asULong.swap16()
                        BYTE.bytes -> buffer.byte.asULong
                        else -> throw IllegalArgumentException("Wrong datatype!")
                    }

                    if (buffer.remaining() == 0) {
                        sectorsCount -= 1
                        ATA_ASTATUS.DRQ = 0
                        if (sectorsCount == 0) {
                            command = COMMAND.ATA_CMD_IDLE
                        } else {
                            ATA_ASTATUS.RDY = 0
                            ATA_ASTATUS.BUSY = 1
                            readyTimer.enabled = true
                        }
                    }

                    return result
                }

                else -> log.warning { "ATA command data read processing not implemented: $command" }
            }

            return 0
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            when (command) {
                COMMAND.ATA_CMD_IDLE -> {
                    log.warning { "Writing when IDLE state..." }
                }

                COMMAND.ATA_CMD_WRITE -> {
                    when (size) {
                        WORD.bytes -> buffer.putShort(value.swap16().asShort)
                        BYTE.bytes -> buffer.put(value.asByte)
                        else -> throw IllegalArgumentException("Wrong datatype!")
                    }

                    if (buffer.remaining() == 0) {
                        writeSector(buffer, lba++)
                        sectorsCount -= 1
                        ATA_ASTATUS.DRQ = 0
                        if (sectorsCount == 0) {
                            command = COMMAND.ATA_CMD_IDLE
                        } else {
                            ATA_ASTATUS.RDY = 0
                            ATA_ASTATUS.BUSY = 1
                            readyTimer.enabled = true
                        }
                    }

                }

                else -> log.warning { "ATA command data write processing not implemented: $command" }
            }
        }
    }

    val ATA_FEATURE = Register(ports.io, 0x1F1, BYTE,"ATA_FEATURE", readable = false, level = FINER)
    val ATA_ERROR = Register(ports.io, 0x1F1, BYTE,"ATA_ERROR", writable = false)

    val ATA_SECCNT = object : Register(ports.io, 0x1F2, BYTE, "ATA_SECCNT", level = FINER) {
        override fun read(ea: Long, ss: Int, size: Int): Long = if (ATA_SDH.DRV == 0) 0x01 else 0 // ata signature
    }

    val ATA_SECTOR = object : Register(ports.io, 0x1F3, BYTE, "ATA_SECTOR", level = FINER) {
        override fun read(ea: Long, ss: Int, size: Int): Long = if (ATA_SDH.DRV == 0) 0x01 else 0 // ata signature
    }

    val ATA_CYL_LO = object : Register(ports.io,0x1F4, BYTE, "ATA_CYL_LO", level = FINER) {
        override fun read(ea: Long, ss: Int, size: Int): Long = 0x00 // ata signature
    }
    val ATA_CYL_HI = object : Register(ports.io, 0x1F5, BYTE, "ATA_CYL_HI", level = FINER) {
        override fun read(ea: Long, ss: Int, size: Int): Long = 0x00 // ata signature
    }

    inner class ATA_SDH_CLASS(address: Long) : Register(ports.io, address, BYTE, "ATA_SDH", level = FINER) {
        var LBA by bit(6)
        var DRV by bit(4)
        var HS by field(3..0)
    }

    val ATA_SDH = ATA_SDH_CLASS(0x1F6)

    val ATA_STATUS = Register(ports.io, 0x1F7, BYTE, "ATA_STATUS", writable = false, level = FINEST)

    val ATA_COMMAND = object : Register(ports.io, 0x1F7, BYTE, "ATA_COMMAND", readable = false, level = FINEST) {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            val cmd = find<COMMAND> { data.asInt == it.id }

            ATA_ASTATUS.RDY = 0
            ATA_ASTATUS.BUSY = 1
            ATA_ERROR.data = 0

            readyTimer.enabled = true

            if (cmd == null) {
                log.warning { "Unknown ATA command number: ${data.hex2} at ${core.cpu.pc.hex}" }
                if (isDebuggerPresent) debugger.isRunning = false
                error = ERROR.COMMON_ERROR
                return
            }

            if (ATA_SDH.DRV != 0) {
                log.warning { "Only one drive accessible!" }
                if (isDebuggerPresent) debugger.isRunning = false
                error = ERROR.COMMON_ERROR
                return
            }

            command = cmd

            when (cmd) {
                COMMAND.ATA_CMD_READ, COMMAND.ATA_CMD_WRITE -> {
                    lba = getLogicalBlockAddress()
                    sectorsCount = getSectorsCount()
                    log.info { "ATA read: lba=${lba.hex8} sec=$sectorsCount" }
                }

                else -> {

                }
            }
        }
    }

    inner class ATA_RO_ASTATUS_CLASS(address: Long) : Register(
            ports.io, address, BYTE, "ATA_ASTATUS", writable = false, level = FINEST) {

//        The busy bit is set when the CompactFlash Memory Card has access to the command buffer and registers and
//        the host is locked out from accessing the command register and buffer. No other bits in this register are valid
//        when this bit is set to a 1.
        var BUSY by bit(7)

//        RDY indicates whether the device is capable of performing CompactFlash Memory Card operations. This bit
//        is cleared at power up and remains cleared until the CompactFlash Card is ready to accept a command.
        var RDY by bit(6)

//        This bit, if set, indicates a write fault has occurred.
        var DWF by bit(5)

//        This bit is set when the CompactFlash Memory Card is ready.
        var DSC by bit(4)

//        The Data Request is set when the CompactFlash Memory Card requires that information be transferred either
//        to or from the host through the Data register.
        var DRQ by bit(3)

//        This bit is set when a Correctable data error has been encountered and the data has been corrected. This
//        condition does not terminate a multi-sector read operation.
        var CORR by bit(2)

//        This bit is always set to 0.
        val IDX by bit(1)

//        This bit is set when the previous command has ended in some type of error. The bits in the Error register
//        contain additional information describing the error.
        var ERR by bit(0)
    }

    inner class ATA_WO_DCONTROL_CLASS(address: Long) : Register(
            ports.io, address, BYTE, "ATA_DCONTROL", readable = false) {

//        This bit is ignored by the CompactFlash Memory Card.
        val D3 by bit(3)

//        This bit is set to 1 in order to force the CompactFlash Memory Card to perform an AT Disk controller Soft
//        Reset operation. This does not change the PCMCIA Card Configuration Registers (4.3.2 to 4.3.5) as a
//        hardware Reset does. The Card remains in Reset until this bit is reset to '0'.
        val SWRst by bit(2)

//        The Interrupt Enable bit enables interrupts when the bit is 0. When the bit is 1, interrupts from the
//        CompactFlash Memory Card are disabled. This bit also controls the Int bit in the Configuration and Status
//        Register. This bit is set to 0 at power on and Reset.
        val IEn by bit(1)

//        This bit is ignored by the CompactFlash Memory Card.
        val D0 by bit(0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            if (D3 != 1) log.warning { "D3 bit written by 0" }
            if (D0 != 0) log.warning { "D0 bit written by 1" }

            if (SWRst == 0) {
                ATA_ASTATUS.BUSY = 0
                ATA_ASTATUS.RDY = 1
                ATA_ASTATUS.DSC = 1
            } else {
                ATA_ASTATUS.BUSY = 1
            }
        }
    }

    val ATA_ASTATUS = ATA_RO_ASTATUS_CLASS(0x3F6)
    val ATA_DCONTROL = ATA_WO_DCONTROL_CLASS(0x3F6)

    val ATA_ADDRESS = Register(ports.io, 0x3F7, BYTE, "ATA_ADDRESS")

    override fun initialize(): Boolean {
        if (!super.initialize()) return false
        core.clock.connect(readyTimer, 0x500, false)
        return true
    }

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
            "lba" to lba,
            "sectorsCount" to sectorsCount,
            "command" to command.toString(),
            "error" to error.toString(),
            "buffer" to storeByteBuffer(buffer))

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        lba = loadValue(snapshot, "lba")
        sectorsCount = loadValue(snapshot, "sectorsCount")
        command = loadEnum(snapshot, "command")
        error = loadEnum(snapshot, "error")
        loadByteBuffer(snapshot, "buffer", buffer)
    }
}