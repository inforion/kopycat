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
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.modules.*
import java.io.File
import java.util.logging.Level
import java.util.logging.Level.FINEST



class DUART(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient val log = logger(FINEST)
    }

    inner class Ports : ModulePorts(this) {
        val inp = Slave("in", BUS32)
        val ctrl = Slave("ctrl", BUS32)
        val tx = Master("tx", BUS32)
        val rx = Slave("rx", BUS32)
    }


    val outdump = File("temp/DUART.txt")

    override val ports = Ports()



    fun ULCR(n: Int) = when (n) {
        1 -> DUART_ULCR1
        2 ->  DUART_ULCR2
        else -> throw GeneralException("Wrong n: $n")
    }

    inner class DUART_BUFREG(val n: Int) : Register(ports.ctrl, 0x4500 + 256 * (n.toLong() - 1), Datatype.BYTE, "DUART_BUFREG$n") {

        val bytesIn = mutableListOf<Char>()
        val bytesOut = mutableListOf<Char>(' ', ' ', ' ', ' ', ' ', 'h', 'a', 'l', 'l', 'o', '\n')


        fun writeInBuffer(str: String) {
            DUART_BUFREG1.bytesOut.addAll(str.toList())
            DUART_BUFREG1.bytesOut.add('\n')
        }

        override fun read(ea: Long, ss: Int, size: Int): Long {
            return if (bytesOut.isEmpty())
                0L
            else
                bytesOut.removeAt(0).toLong()
            //super.read(ea, ss, size)
            //throw GeneralException("Try of read by UART")
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {

            val ulcr = ULCR(n)
            if (ulcr.DLAB.toBool()) {
                //TODO("Not implemented yet")
                super.write(ea, ss, size, value)
            }
            else {
                //TODO: UART FIFO processing
                //log.finest { "$name: write byte: ${value.hex8} : ${value.hex2} (${value.toByte().toChar()})" }
                if (value == 0xAL) {
                    log.finest { "$name:\n${bytesIn.joinToString("")}" }
                    bytesIn.clear()
                }
                else
                    bytesIn.add(value.toByte().toChar())
                if (value.toChar() == '\n')
                    writeData('\r'.toInt())
                writeData(value.toByte().toInt())
                outdump.appendText("${value.toByte().toChar()}")
            }
        }

    }

    init {
        outdump.writeText("")
    }

    inner class DUART_MSBINT(val n: Int) : Register(ports.ctrl, 0x4501 + 256 * (n.toLong() - 1), Datatype.BYTE, "DUART_MSBINT$n") {

        inner class DUART_UIER : IValuable {
            override var data: Long = 0L

            val EMSI by bit(3)
            val ERLSI by bit(2)
            val ETHREI by bit(1)
            val ERDAI by bit(0)
        }


        var UDMB: Long = 0
        val UIER = DUART_UIER()

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val ulcr = ULCR(n)
            if (ulcr.DLAB.toBool()) {
                UDMB = value
                log.severe { "$name: UDMB=${UDMB.hex2}"}
            }
            else {
                UIER.data = value
                log.severe { "$name: new values is: EMSI=${UIER.EMSI}, ERLSI=${UIER.ERLSI}, ETHREI=${UIER.ETHREI}, ERDAI=${UIER.ERDAI}" }
            }
        }
    }

    inner class DUART_CTRLREG(val n: Int) : Register(ports.ctrl, 0x4502 + 256 * (n.toLong() - 1), Datatype.BYTE, "DUART_CTRLREG$n") {
        inner class DUART_UIIR : IValuable {
            override var data: Long = 1L

            fun reset() {
                data = 1L
            }

            val FE by field(7..6)
            val IID3 by bit(3)
            val IID2 by bit(2)
            val IID1 by bit(1)
            val IID0 by bit(0)
        }

        inner class DUART_UFCR : IValuable {
            override var data: Long = 0L

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


    inner class DUART_ULCR(n: Int) : Register(ports.ctrl, 0x4503 + 256 * (n.toLong() - 1), Datatype.BYTE, "DUART_USCR$n") {
        val DLAB by bit(7)
        val SB by bit(6)
        val SP by bit(5)
        val EPS by bit(4)
        val PEN by bit(3)
        val NSTB by bit(2)
        val WLS by field(1..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: new values is: DLAB=$DLAB, SB=$SB, SP=$SP, EPS=$EPS, PEN=$PEN, NSTB=$NSTB, WLS=$WLS"}
        }
    }


    inner class DUART_UMCR(val n: Int) : Register(ports.ctrl, 0x4504 + 256 * (n.toLong() - 1), Datatype.BYTE, "DUART_UMCR$n") {
        //TODO: NOT FULLY IMPLEMENTEED
    }

    inner class DUART_ULSR(val n: Int) : Register(ports.ctrl, 0x4505 + 256 * (n.toLong() - 1), Datatype.BYTE, "DUART_UMCR$n", 0b01100000, writable = false) {
        //TODO: NOT FULLY IMPLEMENTEED

        override fun read(ea: Long, ss: Int, size: Int): Long {
            return data or (DUART_BUFREG1.bytesOut.isEmpty().not().toLong())
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



    inner class DUART_USCR(n: Int) : Register(ports.ctrl, 0x4507 + 256 * (n.toLong() - 1), Datatype.BYTE, "DUART_USCR$n") {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: write byte: ${value.hex2}, \"${value.toByte().toChar()}\"" }
        }

    }

    val DUART_BUFREG1 = DUART_BUFREG(1)
    val DUART_MSBINT1 = DUART_MSBINT(1)
    val DUART_CTRLREG1 = DUART_CTRLREG(1)
    val DUART_ULCR1 = DUART_ULCR(1)
    val DUART_UMCR1 = DUART_UMCR(1)
    val DUART_ULSR1 = DUART_ULSR(1)
    val DUART_USCR1 = DUART_USCR(1)


    val DUART_BUFREG2 = DUART_BUFREG(2)
    val DUART_MSBINT2 = DUART_MSBINT(2)
    val DUART_CTRLREG2 = DUART_CTRLREG(2)
    val DUART_ULCR2 = DUART_ULCR(2)
    val DUART_UMCR2 = DUART_UMCR(2)
    val DUART_ULSR2 = DUART_ULSR(2)
    val DUART_USCR2 = DUART_USCR(2)


    private fun writeData(value: Int) = ports.tx.write(UART_MASTER_BUS_DATA, 0, 1, value.asULong)
    private fun readData() = ports.tx.read(UART_MASTER_BUS_DATA, 0, 1).asInt


    val TERMINAL_REQUEST_REG = object : Register(
            ports.rx,
            UART_SLAVE_BUS_REQUEST,
            Datatype.DWORD,
            "TERMINAL_REQUEST_REG",
            readable = false,
            level = Level.SEVERE
    ) {
        val buffer = mutableListOf<Char>()


        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (ss) {
                UART_SLAVE_DATA_RECEIVED -> {
                    val x = readData().toChar()

                    // loopback
                    writeData(x.toInt())

                    when (x) {
                        '\r' -> {
                            writeData('\n'.toInt())
                            buffer.add('\n')
                            DUART_BUFREG1.bytesOut.addAll(buffer)
                            buffer.clear()
                        }
                        0xFF.toChar() -> buffer.removeAt(buffer.size - 1)
                        else -> buffer.add(x)
                    }
                }

                UART_SLAVE_DATA_TRANSMITTED -> {
                }
            }
        }
    }


}