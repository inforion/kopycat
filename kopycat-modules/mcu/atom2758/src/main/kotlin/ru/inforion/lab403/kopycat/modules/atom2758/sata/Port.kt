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
package ru.inforion.lab403.kopycat.modules.atom2758.sata

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.auxiliary.fields.common.AbsoluteField
import ru.inforion.lab403.kopycat.modules.BUS64
import ru.inforion.lab403.kopycat.modules.atom2758.sata.commands.*
import ru.inforion.lab403.kopycat.serializer.*

internal class Port(val hba: SATA, private val portIndex: Int, private val disk: DiskInfo) :
    Module(hba, "${hba.name}_port$portIndex") {
    companion object {
        /** SSTS: Device present and PHY est. */
        private const val SSTS_PORT_DET_PRESENT = 3uL

        /** SSTS: Generation 1 communication rate negotiated */
        private val SSTS_PORT_SPD_PRESENT = 1uL shl 4

        /** SSTS: Active state */
        private val SSTS_PORT_IPM_ACTIVE = 1uL shl 8

        /** CMD: readonly mask */
        private const val CMD_RO_MASK = 0x007dffe0uL

        /** CMD: ICC mask */
        private val CMD_ICC_MASK = 0xfuL shl 28

        /** SATA drive signature */
        private const val SIG_SATA = 0x00000101uL

        // IDE
        val ATA_DEV_ALWAYS_ON: Byte = 0xA0.byte
        private const val ATA_DEV_LBA = 0x40u
        private const val ATA_DEV_LBA_MSB = 0x0FuL
        private const val ATA_DEV_HS = 0x0FuL

        // Не менять тип
        /** Число логических голов */
        const val DISK_HEADS: UShort = 16u

        // Не менять тип
        /** Число секторов на трек */
        const val DISK_SECTORS: UShort = 63u

        // Внутреннее состояние
        enum class PortState {
            Run,
            Reset,
        }
    }

    inner class Ports : ModulePorts(this) {
        val datam = MasterPort("datam", BUS64)
    }

    @DontAutoSerialize
    override val ports = Ports()

    private fun address(offset: Int) = 256u + portIndex.ulong_z * 128u + offset.ulong_z

    open inner class PortBAR(offset: Int, name: String, datatype: Datatype = Datatype.DWORD) :
        ByteAccessRegister(hba.mem, address(offset), datatype, name)

    open inner class PortR(offset: Int, name: String, datatype: Datatype = Datatype.DWORD) :
        Register(hba.mem, address(offset), datatype, name)

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + mapOf(
        "initD2HSent" to initD2HSent,
        "doneFirstDrq" to doneFirstDrq,
        "state" to state,
        "status" to status.data, // используется в handleCmd
    ) + if (ideIdentify.accessed) mapOf("ideIdentify" to ideIdentify.identifyData.hexlify()) else mapOf()

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        initD2HSent = loadValue(snapshot, "initD2HSent")
        doneFirstDrq = loadValue(snapshot, "doneFirstDrq")
        state = loadEnum(snapshot, "state")
        status.data = loadValue(snapshot, "status")

        loadValue<ByteArray?>(snapshot, "ideIdentify") { null }?.let {
            ideIdentify.identifyData = it
        }
    }

    private var initD2HSent = false
    private var doneFirstDrq = false
    private var state = PortState.Run
    val ideIdentify = Identify(disk)

    var feature: Byte = 0
    var error = PortErrorClass()
    var sector: Byte = 1
    var nsector: ULong = 1uL
    var hcyl: Byte = 0
    var lcyl: Byte = 0

    var hob_sector: Byte = 0
    private var hob_nsector: Byte = 0 // по сути не используется
    var hob_lcyl: Byte = 1
    var hob_hcyl: Byte = 0

    var select: Byte = ATA_DEV_ALWAYS_ON
    var status = PortStatusClass()

    /**
     * See https://wiki.osdev.org/PCI_IDE_Controller#Commands
     *
     * Also, see https://github.com/tpn/winsdk-10/blob/master/Include/10.0.10240.0/km/ata.h
     * and
     * https://github.com/torvalds/linux/blob/e62252bc55b6d4eddc6c2bdbf95a448180d6a08d/include/linux/ata.h
     */
    private val IDE_COMMANDS = mapOf(
        0x25.byte to ReadDMAExt(),
        0x35.byte to WriteDMAExt(),
        0xC8.byte to ReadDMA(),
        0xCA.byte to WriteDMA(),
        0xEC.byte to ideIdentify,
        0xEF.byte to SetFeatures(),

        0x91.byte to Nop(),

        // Linux on `echo  1 > /sys/block/sda/device/delete` command
        0xE0.byte to NopNoDsc(),
    )

    fun ideSetIrq() {
        // TODO: legacy прерывания не реализованы, предполагается использовать MSI
        return
    }

    fun resetPort() {
        log.warning {
            "$name: reset"
        }

        feature = 0
        // error.data = 0uL
        // nsector = 0uL
        // sector = 0
        // lcyl = 0
        // hcyl = 0

        hob_sector = 0
        hob_nsector = 0
        hob_lcyl = 0
        hob_hcyl = 0

        select = ATA_DEV_ALWAYS_ON
        // status.apply {
        // data = 0uL
        // ready = 1
        // seekSrv = 1
        // }

        SSTS.data = 0u
        SERR.data = 0u
        SACT.data = 0u
        TFD.data = 0x7Fu
        SIG.data = 0xFFFFFFFFuL

        hcyl = SIG_SATA[31..24].byte
        lcyl = SIG_SATA[23..16].byte
        sector = SIG_SATA[15..8].byte
        nsector = SIG_SATA[7..0]

        initD2HSent = false
        state = PortState.Run
        status.apply {
            data = 0uL
            seekSrv = 1
            wrerr = 1
        }
        error.apply {
            data = 0uL
            mark = 1
        }

        initD2H()
    }

    private fun initD2H() {
        if (initD2HSent) {
            return
        }

        if (FIS(this).writeD2H()) {
            initD2HSent = true
            SIG.data = (hcyl.ulong_z shl 24) or
                    (lcyl.ulong_z shl 16) or
                    (sector.ulong_z shl 8) or
                    nsector[7..0]
        }
    }

    private fun checkCmd() {
        if (CMD.ST.truth && CI.data.truth) {
            for (slot in 0..31) {
                if (CI.data[slot].truth && handleCmd(slot)) {
                    CI.data = CI.data clr slot
                }
            }
        }
    }

    private fun handleCmd(slot: Int): Boolean {
        if (status.busy.truth || status.drq.truth || CLB.data.untruth) {
            return false
        }

        val header = CommandHeader(this, slot.ulong_z)
        val table = header.table()

        if (table.CFIS_TYPE != FIS.FIS_REGISTER_H2D) {
            log.severe {
                "$name: command FIS type != host to device"
            }
        }

        if (table.CFIS_PMP.pmport.truth) {
            return true
        }

        if (table.CFIS_PMP.c.untruth) {
            when (state) {
                PortState.Run -> if (table.CFIS_CONTROL[2].truth) {
                    state = PortState.Reset
                }

                PortState.Reset -> if (table.CFIS_CONTROL[2].untruth) {
                    resetPort()
                }
            }

            return true
        }

        val cmd = table.CFIS_CMD.byte

        if (cmd in 0x60..0x65 && cmd != 0x62.byte) {
            TODO("NCQ command processing not implemented")
            // return true
        }

        feature = table.CFIS_FEATL.byte
        sector = table.CFIS_LBA0.byte
        lcyl = table.CFIS_LBA1.byte
        hcyl = table.CFIS_LBA2.byte
        select = table.CFIS_DEV.byte
        hob_sector = table.CFIS_LBA3.byte
        hob_lcyl = table.CFIS_LBA4.byte
        hob_hcyl = table.CFIS_LBA5.byte
        nsector = (table.CFIS_COUNTH shl 8) or table.CFIS_COUNTL

        error.data = 0uL
        doneFirstDrq = false
        header.PRDBC = 0uL

        ideExecCmd(slot, cmd)
        return true
    }

    private fun ideExecCmd(slot: Int, cmd: Byte) {
        val handler = IDE_COMMANDS[cmd]

        if (handler == null) {
            ideAbortCmd(slot)
            ideSetIrq()
            TODO("IDE command ${cmd.hex2}")
            // return
        }

        status.apply {
            data = 0uL
            ready = 1
            busy = 1
        }
        error.data = 0uL

        log.warning {
            "$name: issued ${handler.name}"
        }

        if (handler.execute(this, slot)) {
            status.busy = 0
            if (handler.setDSC && error.data.untruth) {
                status.seekSrv = 1
            }
            cmdDone(slot)
            ideSetIrq()
        }
    }

    private fun cmdDone(slot: Int) {
        CI.data = CI.data clr slot
        FIS(this).writeD2H()

        // Эмулируется один командный слот, но если бы их было несколько, то
        // наверняка пришлось бы начинать обработку след. команды здесь
        // if (CI.data.truth) ...
    }

    private fun ideTransferStop(slot: Int) {
        status.drq = 0
        cmdDone(slot)
    }

    fun ideAbortCmd(slot: Int) {
        ideTransferStop(slot)
        status.apply {
            data = 0uL
            ready = 1
            err = 1
        }
        error.apply {
            data = 0uL
            abrt = 1
        }
    }

    fun ideTransfer(slot: Int, buf: ByteArray) {
        if (status.err.untruth) {
            status.drq = 1
        }

        pioTransfer(slot, buf)
        ideTransferStop(slot)
    }

    private fun pioTransfer(slot: Int, buf: ByteArray) {
        val header = CommandHeader(this, slot.ulong_z)

        val isAtapi = header.OPTS.atapi.truth
        val isWrite = header.OPTS.write.truth

        val pioFisI = doneFirstDrq || (!isAtapi && !isWrite)
        FIS(this).writePIO(buf.size.ushort, pioFisI)

        if (isAtapi && !doneFirstDrq) {
            TODO("Unreachable: device does not support ATAPI")
            // doneFirstDrq = true
            // if (pioFisI) {
            // IS.PSS = 1
            // checkIrq()
            // }
            // return
        }

        val prds = header.prds().iterator()
        var prd = if (prds.hasNext()) prds.next() else TODO("PRDT length == 0")

        var memIdx = 0uL

        // Цикл записи/чтения по одному байту за итерацию
        for (bufIdx in buf.indices) {
            if (memIdx == prd.entrySize) {
                memIdx = 0uL
                prd = if (prds.hasNext()) prds.next() else TODO("PRDT overflow")
            }

            if (!isWrite) {
                hba.dmam.write(prd.ADDR + memIdx, 0, 1, buf[bufIdx].ulong_z)
            } else {
                buf[bufIdx] = hba.dmam.read(prd.ADDR + memIdx, 0, 1).byte
            }

            memIdx++
        }

        header.PRDBC = buf.size.ulong_z

        doneFirstDrq = true
        if (pioFisI) {
            IS.PSS = 1
            hba.checkIrq()
        }
    }

    fun ideCmdLba48Transform(lba48: Boolean) {
        if (!lba48) {
            if (nsector.untruth) {
                nsector = 256uL
            }
        } else {
            nsector = if (nsector.untruth && hob_nsector.untruth) {
                65536uL
            } else {
                (hob_nsector.ulong_z shl 8) or nsector
            }
        }
    }

    private fun ideGetSector(lba48: Boolean): ULong {
        return if ((select.uint_z and ATA_DEV_LBA).truth) {
            if (lba48) {
                (hob_hcyl.ulong_z shl 40) or
                        (hob_lcyl.ulong_z shl 32) or
                        (hob_sector.ulong_z shl 24) or
                        (hcyl.ulong_z shl 16) or
                        (lcyl.ulong_z shl 8) or
                        sector.ulong_z
            } else {
                // LBA28
                ((select.ulong_z and ATA_DEV_LBA_MSB) shl 24) or
                        (hcyl.ulong_z shl 16) or
                        (lcyl.ulong_z shl 8) or
                        sector.ulong_z
            }
        } else {
            // CHS
            ((hcyl.ulong_z shl 8) or lcyl.ulong_z) * DISK_HEADS * DISK_SECTORS +
                    (select.ulong_z and ATA_DEV_HS) * DISK_SECTORS + (sector.ulong_z - 1uL)
        }
    }

    private fun ideSetSector(lba48: Boolean, number: ULong) {
        if ((select.uint_z and ATA_DEV_LBA).truth) {
            if (lba48) {
                sector = number[7..0].byte
                lcyl = number[15..8].byte
                hcyl = number[23..16].byte
                hob_sector = number[31..24].byte
                hob_lcyl = number[39..32].byte
                hob_hcyl = number[48..40].byte
            } else {
                // LBA28
                select = ((select.ulong_z and ATA_DEV_LBA_MSB.inv()) or
                        (number[31..24] and ATA_DEV_LBA_MSB)).byte
                hcyl = number[23..16].byte
                lcyl = number[15..8].byte
                sector = number[7..0].byte
            }
        } else {
            // CHS
            val cyl = number / (DISK_HEADS * DISK_SECTORS)
            val r = number % (DISK_HEADS * DISK_SECTORS)
            hcyl = cyl[15..8].byte
            lcyl = cyl[7..0].byte
            select = ((select.ulong_z and ATA_DEV_HS.inv()) or
                    ((r / DISK_SECTORS) and ATA_DEV_HS)).byte
            sector = ((r % DISK_SECTORS) + 1uL).byte
        }
    }

    fun ideSectorDMARW(lba48: Boolean, slot: Int, isWrite: Boolean) {
        status.apply {
            data = 0u
            ready = 1
            seekSrv = 1
            drq = 1
        }

        val sectorNumber = ideGetSector(lba48)
        val diskSize = disk.size
        var diskOOB = false

        var diskOffset = sectorNumber shl 9
        var written = 0uL

        outer@ for (prd in CommandHeader(this, slot.ulong_z).prds()) {
            for (i in 0 until prd.entrySize.int) {
                if (!diskOOB && diskOffset >= diskSize) {
                    if (isWrite) {
                        break@outer
                    }
                    diskOOB = true
                }

                val disk = AbsoluteField(ports.datam, "disk", diskOffset, 1)
                val dma = AbsoluteField(hba.dmam, "dma", prd.ADDR + i, 1)

                if (isWrite) {
                    disk.data = dma.data
                } else {
                    dma.data = if (diskOOB) 0uL else disk.data
                }

                diskOffset++
                written++
            }
        }

        // Not sure
        CommandHeader(this, slot.ulong_z).PRDBC = written
        ideSetSector(lba48, sectorNumber + nsector)
        nsector = 0u

        log.info {
            "$name: DMA offset: $diskOffset, size: $written bytes, write: $isWrite"
        }

        status.apply {
            data = 0uL
            ready = 1
            seekSrv = 1
        }

        ideSetIrq()
        cmdDone(slot)
    }

    /** Command List Base Address */
    val CLB = PortBAR(0, "CLB", Datatype.QWORD)

    /** FIS Base Address */
    val FB = PortBAR(8, "FB", Datatype.QWORD)

    /** Interrupt Status */
    inner class ISClass(name: String) : PortR(16, name) {
        var TFES by bit(30)
        var PSS by bit(1)
        var DHRS by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data

            data = 0u
            super.write(ea, ss, size, value)

            data = old and data.inv()
            hba.checkIrq()
        }
    }

    val IS = ISClass("IS")

    /** Interrupt Enable */
    val IE = object : PortR(20, "IE") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            data = data and 0xfdc000ffuL
            hba.checkIrq()
        }
    }

    /** Command and Status */
    inner class CMDClass(name: String) : PortR(24, name) {
        /** Command List Running (DMA active) */
        private var CR by bit(15)

        /** FIS Receive Running */
        private var FR by bit(14)

        /** FIS Receive Enable */
        var FRE by bit(4)

        /** Power On Device */
        var POD by bit(2)

        /** Spin-up Device */
        var SUD by bit(1)

        /**
         * Start DMA
         *
         * Whenever this bit is changed from a 0 to a 1,
         * the HBA starts processing the command list at entry 0
         */
        var ST by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data

            data = 0u
            super.write(ea, ss, size, value)

            data = (old and CMD_RO_MASK) or (data and (CMD_RO_MASK or CMD_ICC_MASK).inv())

            CMD.CR = CMD.ST
            CMD.FR = CMD.FRE

            if (FR.truth && !initD2HSent) {
                initD2H()
            }

            checkCmd()
        }
    }

    val CMD = CMDClass("CMD")

    /** Reserved */
    @Suppress("unused")
    private val RES1 = PortR(28, "RES1")

    /** Task File Data */
    val TFD = object : PortR(32, "TFD") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit
    }

    /** Signature */
    private val SIG = object : PortR(36, "SIG") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit
    }

    /** Serial ATA Status */
    private val SSTS = object : PortR(40, "SSTS") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = SSTS_PORT_IPM_ACTIVE or SSTS_PORT_SPD_PRESENT or SSTS_PORT_DET_PRESENT
            return super.read(ea, ss, size)
        }
    }

    /** Serial ATA Control */
    val SCTL = object : PortR(44, "SCTL") {
        var DET by field(3..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val oldDET = DET
            super.write(ea, ss, size, value)

            if (oldDET == 1uL && DET.untruth) {
                resetPort()
            }
        }
    }

    /** Serial ATA Error */
    private val SERR = object : PortR(48, "SERR") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data

            data = 0u
            super.write(ea, ss, size, value)

            data = old and data.inv()
        }
    }

    /** Serial ATA Active */
    private val SACT = object : PortR(52, "SACT") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data

            data = 0u
            super.write(ea, ss, size, value)

            data = old or data
        }
    }

    /** Command Issue */
    private val CI = object : PortR(56, "CI") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data

            data = 0u
            super.write(ea, ss, size, value)

            data = old or data
            checkCmd()
        }
    }

    /** SNotification. Не в AHCI 1.0. */
    @Suppress("unused")
    private val SNTF = PortR(60, "SNTF")

    /** FIS-based Switching Control. Не в AHCI 1.0. */
    @Suppress("unused")
    private val FBS = PortR(64, "FBS")

    /** Device Sleep. Не в AHCI 1.0. */
    @Suppress("unused")
    private val DEVSLP = PortR(68, "DEVSLP")

    /** Reserved. Не в AHCI 1.0. */
    @Suppress("unused")
    private val RES2 = Array(10) { PortR(72 + it * 4, "RES2_${it + 1}") }

    /** Vendor Specific. Не в AHCI 1.0. */
    @Suppress("unused")
    private val VENDOR = Array(4) { PortR(112 + it * 4, "VENDOR$it") }
}
