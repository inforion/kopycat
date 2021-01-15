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

    val UARTxTHR_RBR = object : Register(ports.io, 0x00F8, BYTE, "UART${id}THR_RBR") {
        override fun beforeRead(from: MasterPort, ea: Long): Boolean = !isBaudRateConnected
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean = !isBaudRateConnected

        var THR by wfield(7..0)
        val RBR by rfield(7..0)

        // FIXME : IMPLEMENT INTERACTION WITH USER

//        val buf = StringBuffer()

        override fun read(ea: Long, ss: Int, size: Int): Long = 0

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            print(value.toChar())
//            val ch = value.toChar()
//            buf.append(ch)
//            if (ch == '\n' || buf.length > 64) {
//                log.info { "$name -> $buf" }
//                buf.setLength(0)
//            }
//            log.info { "$name -> %c".format(value.toChar()) }
        }
    }

    val UARTxBCDL = object : Register(ports.io, 0x00F8, BYTE, "UART${id}BCDL") {
        override fun beforeRead(from: MasterPort, ea: Long): Boolean = isBaudRateConnected
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean = isBaudRateConnected

        override fun read(ea: Long, ss: Int, size: Int): Long = baudClockValue[7..0].asULong

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            baudClockValue = baudClockValue.insert(data.asInt, 7..0)
        }
    }

    val UARTxBCDH = object : Register(ports.io, 0x00F9, BYTE, "UART${id}BCDH") {
        override fun beforeRead(from: MasterPort, ea: Long): Boolean = isBaudRateConnected
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean = isBaudRateConnected

        override fun read(ea: Long, ss: Int, size: Int): Long = baudClockValue[15..8].asULong

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            baudClockValue = baudClockValue.insert(data.asInt, 15..8)
        }
    }

    val UARTxINTENB = object : Register(ports.io, 0x00F9, BYTE, "UART${id}INTENB", level = Level.FINEST) {
        override fun beforeRead(from: MasterPort, ea: Long): Boolean = !isBaudRateConnected
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean = !isBaudRateConnected

        val Reserved by reserved(7..4)
        val EMSI by rwbit(3)
        val ERLSI by rwbit(2)
        val ETHREI by rwbit(1)
        val ERDAI by rwbit(0)
    }

    val UARTxINTID_FCR = Register(ports.io, 0x00FA, BYTE, "UART${id}INTID_FCR", level = Level.FINEST)

    val UARTxLCR = object : Register(ports.io, 0x00FB, BYTE, "UART${id}LCR", level = Level.FINEST) {
        val DLAB by rwbit(7)
        val SB by rwbit(6)
        val SP by rwbit(5)
        val EPS by rwbit(4)
        val PENB by rwbit(3)
        val STP by rwbit(2)
        val WLS by rwfield(1..0)
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            isBaudRateConnected = DLAB.toBool()
        }
    }

    val UARTxMCR = Register(ports.io, 0x00FC, BYTE, "UART${id}MCR")

    val UARTxLSR = object : Register(ports.io, 0x00FD, BYTE, "UART${id}LSR", level = Level.FINEST) {
        val ERR_IN_FIFO by rbit(7)
        var TEMT by rbit(6, initial = 1)
        var THRE by rbit(5, initial = 1)
        val BI by rbit(4)
        val FE by rbit(3)
        val PE by rbit(2)
        val OE by rbit(1)
        val DR by rbit(0)

        override fun read(ea: Long, ss: Int, size: Int): Long {
            TEMT = 1
            THRE = 1
            return super.read(ea, ss, size)
        }
    }

    val UARTxMSR = Register(ports.io, 0x00FE, BYTE, "UART${id}MSR")
    val UARTxSCRATCH = Register(ports.io, 0x00FF, BYTE, "UART${id}SCRATCH")

    val UARTxCTL = Register(ports.mmcr, 0x0000, BYTE, "UART${id}CTL")
    val UARTxSTA = Register(ports.mmcr, 0x0001, BYTE, "UART${id}STA")
    val UARTxFCRSHAD = Register(ports.mmcr, 0x0002, BYTE, "UART${id}FCRSHAD")

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