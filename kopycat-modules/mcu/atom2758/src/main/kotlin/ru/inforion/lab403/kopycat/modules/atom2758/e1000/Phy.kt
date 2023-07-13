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
package ru.inforion.lab403.kopycat.modules.atom2758.e1000

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.field

internal class Phy(private val e1000: E1000, name: String) : Module(e1000, name) {
    companion object {
        const val PHY_R = 1
        const val PHY_W = 2
        const val PHY_RW = PHY_R or PHY_W
        const val PHY_ANYPAGE = 4

        private const val PAGES = 7
        private const val PAGE_SIZE = 32
        private const val MEM_SIZE = PAGES * PAGE_SIZE * 2

        private fun addr(page: Int, addr: ULong) = (page.ulong_z * 32uL + addr) * 2uL
    }

    private val portsmod = object : Module(parent, "portsmod") {
        inner class Ports : ModulePorts(this) {
            val slave = Slave("slave", MEM_SIZE)
            val master = Master("master", MEM_SIZE)
        }

        @DontAutoSerialize
        override val ports = Ports()
    }

    inner class Buses : ModuleBuses(this) {
        val smb = Bus("smb", MEM_SIZE)
    }

    @DontAutoSerialize
    override val buses = Buses()

    init {
        portsmod.ports.master.connect(buses.smb)
        portsmod.ports.slave.connect(buses.smb)
    }

    private val phyRegisters = ArrayList<PhyRegister>()

    private open inner class PhyRegister(
        val page: Int,
        val access: Int,
        val addr: ULong,
        name: String,
        default: ULong = 0uL,
    ) : Register(portsmod.ports.slave, addr(page, addr), WORD, name, default) {
        init {
            @Suppress("LeakingThis")
            phyRegisters.add(this)
        }
    }

    private inner class PhyCtrlRegister(name: String) : PhyRegister(
        0,
        PHY_ANYPAGE or PHY_RW,
        0uL,
        name,
    ) {
        var SPEED by field(13..6)

        /** Full duplex */
        var FD by bit(8)

        /** Restart auto negotiation */
        var RESTART_AUTO_NEG by bit(9)

        /** Auto Neg Enable */
        var AUTO_NEG_EN by bit(12)

        // /** 0 = normal, 1 = PHY reset */
        // var RESET by bit(15)

        override fun reset() {
            super.reset()
            FD = 1
            AUTO_NEG_EN = 1
            SPEED = 1u
        }
    }

    private val PHY_CTRL = PhyCtrlRegister("PHY_CTRL")

    private inner class PhyStatusRegister(name: String) : PhyRegister(
        0,
        PHY_ANYPAGE or PHY_R,
        1uL,
        name,
    ) {
        var SR_EXT_CAPS by bit(0)
        var SR_LINK_STATUS by bit(2)
        var SR_AUTONEG_CAPS by bit(3)
        var SR_AUTONEG_COMPLETE by bit(5)
        var SR_PREAMBLE_SUPPRESS by bit(6)
        var SR_EXTENDED_STATUS by bit(8)
        var SR_10T_HD_CAPS by bit(11)
        var SR_10T_FD_CAPS by bit(12)
        var SR_100X_HD_CAPS by bit(13)
        var SR_100X_FD_CAPS by bit(14)

        override fun reset() {
            super.reset()
            SR_EXT_CAPS = 1
            SR_LINK_STATUS = 1
            SR_AUTONEG_CAPS = 1
            SR_PREAMBLE_SUPPRESS = 1
            SR_EXTENDED_STATUS = 1
            SR_10T_HD_CAPS = 1
            SR_10T_FD_CAPS = 1
            SR_100X_HD_CAPS = 1
            SR_100X_FD_CAPS = 1
        }
    }

    private val PHY_STATUS = PhyStatusRegister("PHY_STATUS")

    @Suppress("unused")
    private val PHY_ID1 = PhyRegister(0, PHY_ANYPAGE or PHY_R, 2uL, "PHY_ID1", 0x141uL)

    @Suppress("unused")
    private val PHY_ID2 = PhyRegister(0, PHY_ANYPAGE or PHY_R, 3uL, "PHY_ID2", 0xCB1uL)

    @Suppress("unused")
    private val PHY_AUTONEG_ADV = PhyRegister(
        0,
        PHY_ANYPAGE or PHY_RW,
        4uL,
        "PHY_AUTONEG_ADV",
        0xde1uL,
    )

    private inner class PhyLpAbility(name: String) : PhyRegister(
        0,
        PHY_ANYPAGE or PHY_R,
        5uL,
        name,
        0x7e0uL,
    ) {
        var LPAR_LPACK by bit(14)
    }

    private val PHY_LP_ABILITY = PhyLpAbility("PHY_LP_ABILITY")

    @Suppress("unused")
    private val PHY_AUTONEG_EXP = PhyRegister(
        0,
        PHY_ANYPAGE or PHY_R,
        6uL,
        "PHY_AUTONEG_EXP",
        4uL,
    )

    @Suppress("unused")
    private val PHY_NEXT_PAGE_TX = PhyRegister(
        0,
        PHY_ANYPAGE or PHY_RW,
        7uL,
        "PHY_NEXT_PAGE_TX",
        0x2001uL,
    )

    @Suppress("unused")
    private val PHY_LP_NEXT_PAGE = PhyRegister(0, PHY_ANYPAGE or PHY_R, 8uL, "PHY_LP_NEXT_PAGE")

    @Suppress("unused")
    private val PHY_1000T_CTRL = PhyRegister(
        0,
        PHY_ANYPAGE or PHY_RW,
        9uL,
        "PHY_1000T_CTRL",
        0xf00uL,
    )

    @Suppress("unused")
    private val PHY_1000T_STATUS = PhyRegister(
        0,
        PHY_ANYPAGE or PHY_R,
        10uL,
        "PHY_1000T_STATUS",
        0x3c00uL,
    )

    @Suppress("unused")
    private val PHY_EXT_STATUS = PhyRegister(
        0,
        PHY_ANYPAGE or PHY_R,
        15uL,
        "PHY_EXT_STATUS",
        0x3000uL,
    )

    private val PHY_PAGE = PhyRegister(0, PHY_ANYPAGE or PHY_RW, 22uL, "PHY_PAGE")

    @Suppress("unused")
    private val PHY_COPPER_CTRL1 = PhyRegister(0, PHY_RW, 16uL, "PHY_COPPER_CTRL1", 0x3360uL)

    @Suppress("unused")
    private val PHY_COPPER_STAT1 = PhyRegister(0, PHY_R, 17uL, "PHY_COPPER_STAT1", 0xac08uL)

    @Suppress("unused")
    private val PHY_COPPER_CTRL3 = PhyRegister(0, PHY_RW, 20uL, "PHY_COPPER_CTRL3")

    @Suppress("unused")
    private val PHY_RX_ERR_CNTR = PhyRegister(0, PHY_R, 21uL, "PHY_RX_ERR_CNTR")

    private val PHY_OEM_BITS = PhyRegister(0, PHY_RW, 25uL, "PHY_OEM_BITS")

    @Suppress("unused")
    private val PHY_BIAS_1 = PhyRegister(0, PHY_RW, 29uL, "PHY_BIAS_1")

    @Suppress("unused")
    private val PHY_BIAS_2 = PhyRegister(0, PHY_RW, 30uL, "PHY_BIAS_2")

    @Suppress("unused")
    private val PHY_COPPER_INT_ENABLE = PhyRegister(0, PHY_RW, 18uL, "PHY_COPPER_INT_ENABLE")

    @Suppress("unused")
    private val PHY_COPPER_STAT2 = PhyRegister(0, PHY_R, 19uL, "PHY_COPPER_STAT2")

    @Suppress("unused")
    private val PHY_COPPER_CTRL2 = PhyRegister(0, PHY_RW, 26uL, "PHY_COPPER_CTRL2")

    @Suppress("unused")
    private val PHY_MAC_CTRL1 = PhyRegister(2, PHY_RW, 16uL, "PHY_MAC_CTRL1", 0x88uL)

    @Suppress("unused")
    private val PHY_MAC_INT_ENABLE = PhyRegister(2, PHY_RW, 18uL, "PHY_MAC_INT_ENABLE")

    @Suppress("unused")
    private val PHY_MAC_STAT = PhyRegister(2, PHY_R, 19uL, "PHY_MAC_STAT")

    @Suppress("unused")
    private val PHY_MAC_CTRL2 = PhyRegister(2, PHY_RW, 21uL, "PHY_MAC_CTRL2", 0x1046uL)

    @Suppress("unused")
    private val PHY_LED_03_FUNC_CTRL1 = PhyRegister(3, PHY_RW, 16uL, "PHY_LED_03_FUNC_CTRL1")

    @Suppress("unused")
    private val PHY_LED_03_POL_CTRL = PhyRegister(3, PHY_RW, 17uL, "PHY_LED_03_POL_CTRL")

    @Suppress("unused")
    private val PHY_LED_TIMER_CTRL = PhyRegister(3, PHY_RW, 18uL, "PHY_LED_TIMER_CTRL", 0x4005uL)

    @Suppress("unused")
    private val PHY_LED_45_CTRL = PhyRegister(3, PHY_RW, 1uL, "PHY_LED_45_CTRL")

    @Suppress("unused")
    private val PHY_1000T_SKEW = PhyRegister(5, PHY_R, 20uL, "PHY_1000T_SKEW")

    @Suppress("unused")
    private val PHY_1000T_SWAP = PhyRegister(5, PHY_R, 21uL, "PHY_1000T_SWAP")

    @Suppress("unused")
    private val PHY_CRC_COUNTERS = PhyRegister(6, PHY_R, 17uL, "PHY_CRC_COUNTERS")

    private fun findRegister(page: Int, addr: ULong): PhyRegister? = phyRegisters
        .find { it.page == page && it.addr == addr }

    private fun autoNegotiation() {
        e1000.STATUS.LU = 1
        PHY_STATUS.SR_LINK_STATUS = 1
        PHY_STATUS.SR_AUTONEG_COMPLETE = 1
        PHY_LP_ABILITY.LPAR_LPACK = 1

        if (PHY_CTRL.AUTO_NEG_EN.truth) {
            e1000.CTRL.apply {
                TFCE = 1
                RFCE = 1
            }
        }

        e1000.setInterruptCause(E1000.E1000_ICR_LSC)
    }

    fun write(page: Int, addr: ULong, data: UShort) {
        when (page to addr) {
            0 to 0x00uL -> {
                PHY_CTRL.data = data.ulong_z and 0x823fuL.inv()
                if (PHY_CTRL.RESTART_AUTO_NEG.truth && PHY_CTRL.AUTO_NEG_EN.truth) {
                    autoNegotiation()
                }
            }
            0 to 0x16uL -> PHY_PAGE.data = data.ulong_z and 0x7FuL
            0 to 0x19uL -> {
                PHY_OEM_BITS.data = data.ulong_z clr 10
                if (data[10].truth) {
                    autoNegotiation()
                }
            }
            else -> portsmod.ports.master.write(addr(page, addr), 0, 2, data.ulong_z)
        }
    }

    fun read(page: Int, addr: ULong) = portsmod.ports.master.read(addr(page, addr), 0, 2).ushort

    fun checkCap(addr: ULong, cap: Int): Int? {
        val page = if (((findRegister(0, addr)?.access ?: 0) and PHY_ANYPAGE).truth) 0 else PHY_PAGE.data.int

        if (page >= 7) {
            return null
        }

        return if (((findRegister(page, addr)?.access ?: 0) and cap).truth) page else null
    }
}
