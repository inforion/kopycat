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
@file:Suppress("PropertyName", "unused")

package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.BUS07
import ru.inforion.lab403.kopycat.modules.BUS32

/**
 * Created by shiftdj on 18.01.2021.
 */


class I2C(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
//        val inp = Slave("in", BUS32)
        val ctrl = Slave("ctrl", BUS32)
        val outp = Master("outp", BUS07)
    }

    override val ports = Ports()


    open inner class I2Cx_Register(offset: ULong, n: Int, name: String, default: ULong = 0uL) :
            Register(ports.ctrl, 0x3000uL + offset + (n - 1) * 0x100, Datatype.BYTE, "I2C${n}_$name", default)


    inner class I2Cx_I2CFDR(val n: Int) : I2Cx_Register(0x4u, n, "I2CFDR") {
        var FDR by field(5..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value and 0x3Fu)

            val fDiv = when (FDR.int) {
                0x00 -> 384
                0x01 -> 416
                0x02 -> 480
                0x03 -> 576
                0x04 -> 640
                0x05 -> 704
                0x06 -> 832
                0x07 -> 1024
                0x08 -> 1152
                0x09 -> 1280
                0x0A -> 1536
                0x0B -> 1920
                0x0C -> 2304
                0x0D -> 2560
                0x0E -> 3072
                0x0F -> 3840
                0x10 -> 4608
                0x11 -> 5120
                0x12 -> 6144
                0x13 -> 7680
                0x14 -> 9216
                0x15 -> 10240
                0x16 -> 12288
                0x17 -> 15360
                0x18 -> 18432
                0x19 -> 20480
                0x1A -> 24576
                0x1B -> 30720
                0x1C -> 36864
                0x1D -> 40960
                0x1E -> 49152
                0x1F -> 61440
                0x20 -> 256
                0x21 -> 288
                0x22 -> 320
                0x23 -> 352
                0x24 -> 384
                0x25 -> 448
                0x26 -> 512
                0x27 -> 576
                0x28 -> 640
                0x29 -> 768
                0x2A -> 896
                0x2B -> 1024
                0x2C -> 1280
                0x2D -> 1536
                0x2E -> 1792
                0x2F -> 2048
                0x30 -> 2560
                0x31 -> 3072
                0x32 -> 3584
                0x33 -> 4096
                0x34 -> 5120
                0x35 -> 6144
                0x36 -> 7168
                0x37 -> 8192
                0x38 -> 10240
                0x39 -> 12288
                0x3A -> 14336
                0x3B -> 16384
                0x3C -> 20480
                0x3D -> 24576
                0x3E -> 28672
                0x3F -> 32768
                else -> throw GeneralException("Unknown FDRDivider value: ${FDR.hex2}")
            }

            log.warning { "$name: I2C FDRDivider value: $fDiv" }
        }
    }

    enum class State {
        STOPPED,
        STOPPING,
        STARTED,
        WRITING,
        READING
    }

    private var currentState = State.STOPPED
    private var currentAddress = 0uL

    private fun i2cAddressAcknowledge() = ports.outp.write(Datatype.BYTE, currentAddress, 0uL, ss = 1)
    private fun i2cRead() = ports.outp.read(Datatype.BYTE, currentAddress, ss = 0)
    private fun i2cWrite(value: ULong) = ports.outp.write(Datatype.BYTE, currentAddress, value, ss = 0)

    private fun start() {
        currentState = State.STARTED
        log.warning { "[I2C] Sent START bit" }
    }

    private fun stop() {
        currentState = State.STOPPED
        log.warning { "[I2C] Sent STOP bit" }
    }

    private fun stopping() {
        currentState = State.STOPPING
        log.warning { "[I2C] Sent TX NACK bit" }
    }

    private fun slaveSelect(address: ULong) {
        val mode = address and 1u
        currentAddress = address ushr 1

        val nextState = if (mode.truth) State.READING else State.WRITING
        log.warning { "[I2C] Sent address: ${currentAddress.hex2} with mode ${nextState.name}" }
        i2cAddressAcknowledge()

        currentState = nextState
    }

    private fun slaveWrite(value: ULong) {
        i2cWrite(value)
        log.warning { "[I2C] Sent ${value.hex2}" }
    }

    private fun slaveRead(): ULong {
        val data = i2cRead()
        log.warning { "[I2C] Read ${data.hex2}" }
        return data
    }


    inner class I2Cx_I2CCR(val n: Int) : I2Cx_Register(0x8u, n, "I2CCR") {
        var MEN by bit(7)
        var MIEN by bit(6)
        var MSTA by bit(5)
        var MTX by bit(4)
        var TXAK by bit(3)
        var RSTA by bit(2)
//        var Reserved by bit(1)
        var BCST by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val oldMSTA = MSTA
            super.write(ea, ss, size, value and 0xFDu) // TODO: print
            log.warning { "$name: I2C I2CCR value: ${value.hex8}" }

            if (oldMSTA != MSTA) {
                I2C_Registers[n-1].I2C_I2CSR.MBB = MSTA
                if (MSTA == 1) start()
            } else if (RSTA == 1) {
                start()
//                RSTA = 0 // No need - read function has mask
            }
        }

        override fun read(ea: ULong, ss: Int, size: Int) = super.read(ea, ss, size) and 0xF9u

    }

    inner class I2Cx_I2CSR(val n: Int) : I2Cx_Register(0xCu, n, "I2CSR", 0x81u) {
        var MCF by bit(7)
        var MAAS by bit(6)
        var MBB by bit(5)
        var MAL by bit(4)
        var BCSTM by bit(3)
        var SRW by bit(2)
        var MIF by bit(1)
        var RXAK by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, (data and 0xEDu) or (value and 0x12u)) // TODO: print
            log.warning { "$name: I2C I2CSR value: ${value.hex8}" }
        }

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            return super.read(ea, ss, size)
        }
    }

    inner class I2Cx_I2CDR(val n: Int) : I2Cx_Register(0x10u, n, "I2CDR") {

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            I2C_Registers[n - 1].I2C_I2CSR.MIF = 1
            I2C_Registers[n - 1].I2C_I2CSR.RXAK = 0 // ACK
            try {
                when (currentState) {
                    State.STOPPED -> throw GeneralException("I2C bus stopped")
                    State.STARTED -> slaveSelect(value)
                    State.WRITING -> slaveWrite(value)
                    State.STOPPING -> throw GeneralException("I2C wrong mode")
                    State.READING -> throw GeneralException("I2C wrong mode")
                }
            }
            catch (ex: I2CNotAcknowledgeException) {
//                I2C_Registers[n - 1].I2C_I2CSR.MIF = 0
                I2C_Registers[n - 1].I2C_I2CSR.RXAK = 1 // NACK
            }
        }

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            I2C_Registers[n - 1].I2C_I2CSR.MIF = 1
            I2C_Registers[n - 1].I2C_I2CSR.RXAK = 0 // ACK
            return try {
                when (currentState) {
                    State.STOPPED,
                    State.STARTED -> 0xFFu
                    State.WRITING -> throw GeneralException("I2C wrong mode")
                    State.READING -> slaveRead().also {
                        if (I2C_Registers[n - 1].I2C_I2CCR.TXAK.truth) stopping()
                    }
                    State.STOPPING -> slaveRead().also { stop() }
                }
            } catch (ex: I2CNotAcknowledgeException) {
//                I2C_Registers[n - 1].I2C_I2CSR.MIF = 0
                I2C_Registers[n - 1].I2C_I2CSR.RXAK = 1 // NACK
                0xFFu
            }
        }

    }

    inner class I2Cx_I2CDFSRR(val n: Int) : I2Cx_Register(0x14u, n, "I2CDFSRR", 0x10u) {
        var DFSR by field(5..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value and 0x3Fu)

            log.warning { "$name: I2C digital filter sampling rate: $DFSR" }
        }
    }

    inner class I2Cx_RegisterSet(val n: Int) {
        val I2C_I2CFDR = I2Cx_I2CFDR(n)
        val I2C_I2CCR = I2Cx_I2CCR(n)
        val I2C_I2CSR = I2Cx_I2CSR(n)
        val I2C_I2CDR = I2Cx_I2CDR(n)
        val I2C_I2CDFSRR = I2Cx_I2CDFSRR(n)
    }

    val I2C_Registers = arrayOf(
            I2Cx_RegisterSet(1),
            I2Cx_RegisterSet(2)
    )


}