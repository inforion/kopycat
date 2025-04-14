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
@file:Suppress("PropertyName", "unused", "MemberVisibilityCanBePrivate")

package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINEST
import ru.inforion.lab403.common.logging.SEVERE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.modules.*


class DUART(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient
        val log = logger(FINEST)
    }

    inner class Ports : ModulePorts(this) {
        val ctrl = Port("ctrl")
        val tx1 = Port("tx1")
        val rx1 = Port("rx1")
        val tx2 = Port("tx2")
        val rx2 = Port("rx2")
    }

    override val ports = Ports()

    fun ULCR(n: Int) = when (n) {
        1 -> DUART_ULCR1
        2 -> DUART_ULCR2
        else -> throw GeneralException("Wrong n: $n")
    }

    private fun base(base: ULong, size: Int, n: Int): ULong = base + size * (n - 1)

    inner class DUART_BUFREG(val n: Int) :
        Register(ports.ctrl, base(0x4500u, 256, n), BYTE, "DUART_BUFREG$n") {

        val bytesIn = mutableListOf<Char>()
        val bytesOut = mutableListOf<Char>()//(' ', ' ', ' ', ' ', ' ', 'h', 'a', 'l', 'l', 'o', '\n')


        fun writeInBuffer(str: String) {
            duartBufreg(n).bytesOut.addAll(str.toList())
            duartBufreg(n).bytesOut.add('\n')
        }

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            return if (bytesOut.isEmpty()) 0uL else bytesOut.removeAt(0).ulong_z8
            //super.read(ea, ss, size)
            //throw GeneralException("Try of read by UART")
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val ulcr = ULCR(n)

            if (ulcr.DLAB.truth) {
                //TODO("Not implemented yet")
                super.write(ea, ss, size, value)
            } else {
                //TODO: UART FIFO processing
                //log.finest { "$name: write byte: ${value.hex8} : ${value.hex2} (${value.char})" }

                if (value.char == '\n') {
                    log.finest { "$name:\n${bytesIn.joinToString("")}" }
                    bytesIn.clear()
                } else bytesIn.add(value.char)

                if (value.char == '\n')
                    writeUART(n, '\r'.int_z8)

                writeUART(n, value.int)
            }
        }

    }

    inner class DUART_MSBINT(val n: Int) :
        Register(ports.ctrl, base(0x4501u, 256, n), BYTE, "DUART_MSBINT$n") {

        inner class DUART_UIER : IValuable {
            override var data = 0uL

            val EMSI by bit(3)
            val ERLSI by bit(2)
            val ETHREI by bit(1)
            val ERDAI by bit(0)
        }

        var UDMB = 0uL
        val UIER = DUART_UIER()

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val ulcr = ULCR(n)
            if (ulcr.DLAB.truth) {
                UDMB = value
                log.severe { "$name: UDMB=${UDMB.hex2}" }
            } else {
                UIER.data = value
                log.severe { "$name: new values is: EMSI=${UIER.EMSI}, ERLSI=${UIER.ERLSI}, ETHREI=${UIER.ETHREI}, ERDAI=${UIER.ERDAI}" }
            }
        }
    }

    inner class DUART_CTRLREG(val n: Int) :
        Register(ports.ctrl, base(0x4502u, 256, n), BYTE, "DUART_CTRLREG$n") {
        inner class DUART_UIIR : IValuable {
            override var data = 1uL

            fun reset() {
                data = 1uL
            }

            val FE by field(7..6)
            val IID3 by bit(3)
            val IID2 by bit(2)
            val IID1 by bit(1)
            val IID0 by bit(0)
        }

        inner class DUART_UFCR : IValuable {
            override var data = 0uL

            val RTL by field(7..6)
            val DMS by bit(3)
            val TFR by bit(2)
            val RFR by bit(1)
            val FEN by bit(0)
        }


        val UIIR = DUART_UIIR()

        override fun reset() {
            super.reset()
            UIIR.reset()
        }

        //TODO: NOT FULLY IMPLEMENTEED
    }


    inner class DUART_ULCR(n: Int) :
        Register(ports.ctrl, base(0x4503u, 256, n), BYTE, "DUART_USCR$n") {
        val DLAB by bit(7)
        val SB by bit(6)
        val SP by bit(5)
        val EPS by bit(4)
        val PEN by bit(3)
        val NSTB by bit(2)
        val WLS by field(1..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: new values is: DLAB=$DLAB, SB=$SB, SP=$SP, EPS=$EPS, PEN=$PEN, NSTB=$NSTB, WLS=$WLS" }
        }
    }


    inner class DUART_UMCR(val n: Int) :
        Register(ports.ctrl, base(0x4504u, 256, n), BYTE, "DUART_UMCR$n") {
        //TODO: NOT FULLY IMPLEMENTEED
    }

    inner class DUART_ULSR(val n: Int) : Register(
        ports.ctrl,
        base(0x4505u, 256, n),
        BYTE,
        "DUART_UMCR$n",
        0b01100000u,
        writable = false
    ) {
        //TODO: NOT FULLY IMPLEMENTEED

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            return data or DUART_BUFREG1.bytesOut.isEmpty().not().ulong
            /*
            if (!DUART_BUFREG1.bytesOut.isEmpty())
                return data or 1
            return data

            log.finest { "DUART checks for input data. Do you want to send anything? [y/N]" }
            val cmd = readLine().sure { "Wrong input" }
            while (true) {
                when (cmd) {
                    "", "n", "no" -> return data
                    "y", "yes" -> {
                        log.finest { "Enter DUART string" }
                        val str = readLine().sure { "Wrong input" }
                        DUART_BUFREG1.bytesOut.addAll(str.toList())
                        DUART_BUFREG1.bytesOut.add('\n')
                    }
                    else -> log.finest { "Wrong input" }
                }
            }*/
        }

    }

    inner class DUART_UMSR(n: Int) :
        Register(ports.ctrl, base(0x4506u, 256, n), BYTE, "DUART_UMSR$n", 0x10u, false) {
    }

    inner class DUART_USCR(n: Int) :
        Register(ports.ctrl, base(0x4507u, 256, n), BYTE, "DUART_USCR$n") {

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: write byte: ${value.hex2}, \"${value.char}\"" }
        }

    }

    val DUART_BUFREG1 = DUART_BUFREG(1)
    val DUART_MSBINT1 = DUART_MSBINT(1)
    val DUART_CTRLREG1 = DUART_CTRLREG(1)
    val DUART_ULCR1 = DUART_ULCR(1)
    val DUART_UMCR1 = DUART_UMCR(1)
    val DUART_ULSR1 = DUART_ULSR(1)
    val DUART_UMSR1 = DUART_UMSR(1)
    val DUART_USCR1 = DUART_USCR(1)


    val DUART_BUFREG2 = DUART_BUFREG(2)
    val DUART_MSBINT2 = DUART_MSBINT(2)
    val DUART_CTRLREG2 = DUART_CTRLREG(2)
    val DUART_ULCR2 = DUART_ULCR(2)
    val DUART_UMCR2 = DUART_UMCR(2)
    val DUART_ULSR2 = DUART_ULSR(2)
    val DUART_UMSR2 = DUART_UMSR(2)
    val DUART_USCR2 = DUART_USCR(2)

    private fun duartBufreg(ind: Int) = if (ind == 1) DUART_BUFREG1 else DUART_BUFREG2


    private fun writeUART1(value: Int) = ports.tx1.write(UART_MASTER_BUS_DATA, 0, 1, value.ulong_z)
    private fun readUART1() = ports.tx1.read(UART_MASTER_BUS_DATA, 0, 1).int
    private fun writeUART2(value: Int) = ports.tx2.write(UART_MASTER_BUS_DATA, 0, 1, value.ulong_z)
    private fun readUART2() = ports.tx2.read(UART_MASTER_BUS_DATA, 0, 1).int

    private fun writeUART(i: Int, value: Int) = if (i == 1) writeUART1(value) else writeUART2(value)
    private fun readUART(i: Int) = if (i == 1) readUART1() else readUART2()


    inner class TERMINAL_REQUEST_REG(val index: Int, port: Port) : Register(
        port,
        UART_SLAVE_BUS_REQUEST,
        DWORD,
        "TERMINAL_REQUEST_REG$index",
        readable = false,
        level = SEVERE
    ) {
        val buffer = mutableListOf<Char>()

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            when (ss) {
                UART_SLAVE_DATA_RECEIVED -> {
                    val x = readUART(index).char

                    // loopback
                    writeUART(index, x.int_z8)

                    when (x) {
                        '\r' -> {
                            writeUART(index, '\n'.int_z8)
                            buffer.add('\n')
                            duartBufreg(index).bytesOut.addAll(buffer)
                            buffer.clear()
                        }
                        0xFF.char -> buffer.removeAt(buffer.size - 1)
                        else -> buffer.add(x)
                    }
                }

                UART_SLAVE_DATA_TRANSMITTED -> {
                }
            }
        }
    }

    //    val TERMINAL_REQUEST_REG1 = TERMINAL_REQUEST_REG(1, ports.rx1)
    val TERMINAL_REQUEST_REG2 = TERMINAL_REQUEST_REG(2, ports.rx2)  // TODO: solve this

}