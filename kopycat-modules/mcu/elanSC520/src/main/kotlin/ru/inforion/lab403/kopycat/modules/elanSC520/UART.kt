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
@file:Suppress("unused")

package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.modules.BUS08
import java.util.logging.Level

@Suppress("PrivatePropertyName", "PropertyName")
/**
 * For info look at UART Registers—Direct-Mapped chapter in Élan™SC520 Microcontroller User’s Manual (page 352)
 */
class UART(parent: Module, name: String, val id: Int) : Module(parent, name) {
    companion object {
        @Transient val log = logger(Level.FINE)

        const val INTERRUPT_COUNT = 2
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
        val mmcr = Slave("mmcr", BUS08)
        val io = Slave("io", BUS08)
    }

    override val ports = Ports()

    private var baudClockValue = 0
    private var isBaudRateConnected = false

    val UARTxTHR_RBR = object : Register(ports.io, 0x00F8u, BYTE, "UART${id}THR_RBR") {
        override fun beforeRead(from: MasterPort, ea: ULong): Boolean = !isBaudRateConnected
        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong): Boolean = !isBaudRateConnected

        var THR by wfield(7..0)
        val RBR by rfield(7..0)

        // FIXME : IMPLEMENT INTERACTION WITH USER

//        val buf = StringBuffer()

        override fun read(ea: ULong, ss: Int, size: Int): ULong = 0u

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            print(value.char)
//            val ch = value.toChar()
//            buf.append(ch)
//            if (ch == '\n' || buf.length > 64) {
//                log.info { "$name -> $buf" }
//                buf.setLength(0)
//            }
//            log.info { "$name -> %c".format(value.toChar()) }
        }
    }

    val UARTxBCDL = object : Register(ports.io, 0x00F8u, BYTE, "UART${id}BCDL") {
        override fun beforeRead(from: MasterPort, ea: ULong): Boolean = isBaudRateConnected
        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong): Boolean = isBaudRateConnected

        override fun read(ea: ULong, ss: Int, size: Int): ULong = baudClockValue[7..0].ulong_z

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            baudClockValue = baudClockValue.insert(data.int, 7..0)
        }
    }

    val UARTxBCDH = object : Register(ports.io, 0x00F9u, BYTE, "UART${id}BCDH") {
        override fun beforeRead(from: MasterPort, ea: ULong): Boolean = isBaudRateConnected
        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong): Boolean = isBaudRateConnected

        override fun read(ea: ULong, ss: Int, size: Int): ULong = baudClockValue[15..8].ulong_z

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            baudClockValue = baudClockValue.insert(data.int, 15..8)
        }
    }

    val UARTxINTENB = object : Register(ports.io, 0x00F9u, BYTE, "UART${id}INTENB", level = Level.FINEST) {
        override fun beforeRead(from: MasterPort, ea: ULong): Boolean = !isBaudRateConnected
        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong): Boolean = !isBaudRateConnected

        val Reserved by reserved(7..4)
        val EMSI by rwbit(3)
        val ERLSI by rwbit(2)
        val ETHREI by rwbit(1)
        val ERDAI by rwbit(0)
    }

    val UARTxINTID_FCR = Register(ports.io, 0x00FAu, BYTE, "UART${id}INTID_FCR", level = Level.FINEST)

    val UARTxLCR = object : Register(ports.io, 0x00FBu, BYTE, "UART${id}LCR", level = Level.FINEST) {
        val DLAB by rwbit(7)
        val SB by rwbit(6)
        val SP by rwbit(5)
        val EPS by rwbit(4)
        val PENB by rwbit(3)
        val STP by rwbit(2)
        val WLS by rwfield(1..0)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            isBaudRateConnected = DLAB.truth
        }
    }

    val UARTxMCR = Register(ports.io, 0x00FCu, BYTE, "UART${id}MCR")

    val UARTxLSR = object : Register(ports.io, 0x00FDu, BYTE, "UART${id}LSR", level = Level.FINEST) {
        val ERR_IN_FIFO by rbit(7)
        var TEMT by rbit(6, initial = 1)
        var THRE by rbit(5, initial = 1)
        val BI by rbit(4)
        val FE by rbit(3)
        val PE by rbit(2)
        val OE by rbit(1)
        val DR by rbit(0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            TEMT = 1
            THRE = 1
            return super.read(ea, ss, size)
        }
    }

    val UARTxMSR = Register(ports.io, 0x00FEu, BYTE, "UART${id}MSR")
    val UARTxSCRATCH = Register(ports.io, 0x00FFu, BYTE, "UART${id}SCRATCH")

    val UARTxCTL = Register(ports.mmcr, 0x0000u, BYTE, "UART${id}CTL")
    val UARTxSTA = Register(ports.mmcr, 0x0001u, BYTE, "UART${id}STA")
    val UARTxFCRSHAD = Register(ports.mmcr, 0x0002u, BYTE, "UART${id}FCRSHAD")

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "baudClockValue" to baudClockValue,
                "isBaudRateConnected" to isBaudRateConnected
        )
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        baudClockValue = snapshot["baudClockValue"] as Int
        isBaudRateConnected = snapshot["isBaudRateConnected"] as Boolean
    }
}