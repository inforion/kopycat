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
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.BUS64
import ru.inforion.lab403.kopycat.modules.PIN
import ru.inforion.lab403.kopycat.modules.atom2758.BRIDGE
import ru.inforion.lab403.kopycat.modules.common.pci.PciMSICapability
import java.lang.IllegalArgumentException
import java.util.logging.Level
import java.util.logging.Level.CONFIG

class SATA(parent: Module, name: String, num: Int) : PciDevice(
    parent,
    name,
    0x8086,
    0x1F20 or num,
    classCode = 0x010601
) {
    companion object {
        const val BUS_SIZE = 0x1100
        const val BUS_MEM_INDEX_2 = 15
        const val BUS_MEM_INDEX_3 = 16

        /** PCI capabilities offset */
        private const val CAP_OFFSET = 0x40uL

        /** Supports AHCI mode only */
        private val CAP_SAM = 1uL shl 18

        /**
         * Interface Speed Support
         *
         * 0000 Reserved
         *
         * 0001 Gen 1 (1.5 Gbps)
         *
         * 0010 Gen 2 (3 Gbps)
         *
         * 0011 Gen 3 (6 Gbps)
         *
         * 0100 - 1111 Reserved
         */
        private val CAP_ISS_GEN1 = 0b0001uL shl 20

        /**
         * Supports Native Command Queuing
         *
         * If set to ‘1’, an HBA shall handle DMA Setup
         * FISes natively, and shall handle the auto-activate optimization through that FIS.
         */
        private val CAP_NCQ = 1uL shl 30

        /** Indicates whether the HBA can access 64-bit data structures */
        private val CAP_S64A = 1uL shl 31
    }

    val mem = ports.Slave("mem", BUS_SIZE)
    val dmam = ports.Master("dmam", BUS32)
    val irq = ports.Master("irq", PIN)

    private val busIndex = when (num) {
        2 -> BUS_MEM_INDEX_2
        3 -> BUS_MEM_INDEX_3
        else -> error("Unknown SATA device = $num")
    }

    // IDP is not implemented

    @Suppress("unused")
    private val ABAR = PCI_BAR(0x24, DWORD, "ABAR", BUS_SIZE, BRIDGE.MEMORY_AREA, busIndex, CONFIG)

    /** PCI MSI capability */
    private val CAP_MSI = PciMSICapability(this, "${name}.CAP_MSI", CAP_OFFSET, 0u)

    // Public

    /**
     * Подключает диск к HBA
     *
     * @param disk информация о диске
     * @param mem порт с данными диска
     */
    fun connect(disk: DiskInfo, mem: SlavePort) {
        if (PORTS.size == 32) {
            throw IllegalArgumentException("AHCI controller supports a maximum of 32 ports")
        }

        val disk2port = buses.Bus("${name}_port${PORTS.size}_disk", BUS64)
        mem.connect(disk2port)

        Port(this, PORTS.size, disk).run {
            ports.datam.connect(disk2port)
            PORTS.add(this)
        }
    }

    // HBA

    private fun hbaReset() {
        log.warning {
            "$name: HBA reset"
        }

        IS.data = 0u

        PORTS.forEach {
            it.IS.data = 0u
            it.IE.data = 0u
            it.SCTL.data = 0u
            with(it.CMD) {
                data = 0u
                SUD = 1
                POD = 1
            }
            it.resetPort()
        }
    }

    internal fun checkIrq() {
        IS.data = 0u

        PORTS.withIndex().forEach { (idx, it) ->
            if (it.IS.data and it.IE.data != 0uL) {
                IS.data = IS.data set idx
            }
        }

        if (IS.data.truth && GHC.IE.truth) {
            if (CAP_MSI.CAP_MC.MSIE.truth) {
                val addr = CAP_MSI.CAP_MA.data
                val data = CAP_MSI.CAP_MD.data

                dmam.write(addr, 0, 2, data)
                irq.request(CAP_MSI.CAP_MD.vector.int)
            } else {
                TODO("Legacy interrupts are not implemented")
            }
        } else {
            if (CAP_MSI.CAP_MC.MSIE.untruth) {
                // TODO("Legacy interrupts are not implemented")
                log.severe { "[0x${core.pc.hex}] Legacy interrupts are not implemented, breaking" }
                irq.request(1)
            }
        }
    }

    private open inner class RO_BAR(address: ULong, datatype: Datatype, name: String, default: ULong = 0u) :
        ByteAccessRegister(mem, address, datatype, name, default) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.warning {
                "[0x${core.pc.hex}] Can't access on write to SATA (${this.name}) register (0x${this.address.hex}) " +
                        "addr=0x${ea.hex} value=0x${value.hex} size=0x${size.hex}"
            }
        }
    }

    /** Host Capabilities */
    @Suppress("unused")
    private val CAP = object : RO_BAR(0u, DWORD, "CAP") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = CAP_SAM or CAP_ISS_GEN1 or CAP_NCQ or CAP_S64A or
                    (PORTS.size - 1).coerceAtLeast(0).ulong_z // 0 = one port
            return super.read(ea, ss, size)
        }
    }

    /** Global Host Control */
    private inner class GHCClass(name: String) : ByteAccessRegister(mem, 4u, DWORD, name) {
        /**
         * HBA Reset, rw
         *
         * When the HBA has performed the reset action, it shall reset this bit to 0
         */
        private var HR by bit(0)

        /**
         * Interrupt Enable, rw
         *
         * When cleared (reset default), all interrupt sources from all ports are disabled.
         * When set, interrupts are enabled.
         */
        var IE by bit(1)

        /**
         * AHCI Enable, rw
         *
         * If CAP.SAM is 1, then AE shall be read-only and shall have a reset value of 1.
         */
        private var AE by bit(31)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            if (HR.truth) {
                HR = 0
                hbaReset()
            } else {
                data = data and 0x3u
                checkIrq()
            }
        }

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            AE = 1
            return super.read(ea, ss, size)
        }
    }

    private val GHC = GHCClass("GHC")

    /** Interrupt Status */
    private val IS = object : ByteAccessRegister(mem, 8u, DWORD, "IS") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data

            data = 0u
            super.write(ea, ss, size, value)

            data = old and data.inv()
            checkIrq()
        }
    }

    /** Ports Implemented */
    @Suppress("unused")
    private val PI = object : RO_BAR(12u, DWORD, "PI") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = (0 until PORTS.size).fold(0uL) { acc, i -> acc set i }
            return super.read(ea, ss, size)
        }
    }

    /**
     * Version
     *
     * Indicates the major and minor version of the AHCI specification that the HBA implementation supports.
     * Example: Version 3.12 would be represented as 00030102h.
     */
    @Suppress("unused")
    private val VS = RO_BAR(16u, DWORD, "VS", default = 0x00_01_00_00uL)

    // Бесполезные регистры

    /** Command Completion Coalescing Control (не в AHCI 1.0). Нет необходимости реализовывать т.к. CAP.CCCS = 0 */
    @Suppress("unused")
    private val CCC_CTL = RO_BAR(20u, DWORD, "CCC_CTL", default = 0u)

    /** Command Completion Coalsecing Ports (не в AHCI 1.0). Нет необходимости реализовывать т.к. CAP.CCCS = 0 */
    @Suppress("unused")
    private val CCC_PORTS = RO_BAR(24u, DWORD, "CCC_PORTS", default = 0u)

    /** Enclosure Management Location (не в AHCI 1.0). Нет необходимости реализовывать т.к. CAP.EMS = 0 */
    @Suppress("unused")
    private val EM_LOC = RO_BAR(28u, DWORD, "EM_LOC", default = 0u)

    /** Enclosure Management Control (не в AHCI 1.0). Нет необходимости реализовывать т.к. CAP.EMS = 0 */
    @Suppress("unused")
    private val EM_CTL = RO_BAR(32u, DWORD, "EM_CTL", default = 0u)

    /** Host Capabilities Extended (не в AHCI 1.0) */
    @Suppress("unused")
    private val CAP2 = RO_BAR(36u, DWORD, "CAP2", default = 0u)

    /** BIOS/OS Handoff Control and Status (не в AHCI 1.0). Нет необходимости реализовывать т.к. CAP2.BOH = 0 */
    @Suppress("unused")
    private val BOHC = RO_BAR(40u, DWORD, "BOHC", default = 0u)

    /** Reserved */
    @Suppress("unused")
    private val RSV = RO_BAR(44u, DWORD, "RSV", default = 0u)

    /** Reserved, used by linux */
    @Suppress("unused")
    private val UNK_30 = RO_BAR(48u, DWORD, "RSV_30", default = 0u)

    /** Reserved */
    @Suppress("unused")
    val UNK_80 = PCI_CONF_FUNC_WR(0x80, WORD, "UNK_80")

    /**
     * Intel Atom Processor C2000 Product Family
     * for Microserver Datasheet, Vol. 2 page 260
     * */
    @Suppress("unused")
    private val SFM = RO_BAR(0xC8u, WORD, "SFM", default = 0u)

    /** Порты */
    private val PORTS = mutableListOf<Port>()

    /**
     * PCI SATA Configuration Registers
     *
     * Intel Atom™ Processor C2000 Product Family for Microserver
     * PCI Configuration Registers, page 257
     */

    val PCMDBA = PCI_CONF_FUNC_WR(0x10, DWORD, "PCMDBA")
    val PCTLBA = PCI_CONF_FUNC_WR(0x14, DWORD, "PCTLBA")
    val SCMDBA = PCI_CONF_FUNC_WR(0x18, DWORD, "SCMDBA")
    val SCTLBA = PCI_CONF_FUNC_WR(0x1C, DWORD, "SCTLBA")
    val LBAR = PCI_CONF_FUNC_WR(0x20, DWORD, "LBAR")

    val MAP = PCI_CONF_FUNC_WR(0x90, WORD, "MAP")
    val PCS = PCI_CONF_FUNC_WR(0x92, WORD, "PCS")

    val TM = PCI_CONF_FUNC_WR(0x94, DWORD, "TM2")
    val TM2 = PCI_CONF_FUNC_WR(0x98, DWORD, "TM2")

    val SATAGC = PCI_CONF_FUNC_WR(0x9C, DWORD, "SATAGC")
    val SIRI = PCI_CONF_FUNC_WR(0xA0, BYTE, "SIRI")
    val SIRD = PCI_CONF_FUNC_WR(0xA4, DWORD, "SIRD")

    val SATACR0 = PCI_CONF_FUNC_WR(0xA8, DWORD, "SATACR0")
    val SATACR1 = PCI_CONF_FUNC_WR(0xAC, DWORD, "SATACR1")

    val FLRCID = PCI_CONF_FUNC_WR(0xB0, WORD, "FLRCID")
    val FLRCAP = PCI_CONF_FUNC_WR(0xB2, WORD, "FLRCAP")
    val FLRCTL = PCI_CONF_FUNC_WR(0xB4, WORD, "FLRCTL")
}
