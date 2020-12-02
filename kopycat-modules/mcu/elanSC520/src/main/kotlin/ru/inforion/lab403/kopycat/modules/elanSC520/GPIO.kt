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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.modules.BUS12
import ru.inforion.lab403.kopycat.modules.BUS16
import java.util.logging.Level
import java.util.logging.Level.FINER

@Suppress("unused", "PrivatePropertyName")

class GPIO(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Slave("mmcr", BUS12)
        val io = Slave("io", BUS16)
    }

    override val ports = Ports()

    private val pioOnWire = object : ISerializable {
        private var pio = 0L

        operator fun get(index: Int): Long = pio[index]
        operator fun set(index: Int, value: Boolean) { if(value) pio.insert(1, index) else pio.insert(0, index) }
        operator fun set(index: Int, value: Long) { set(index, value == 0L) }
        operator fun set(range: IntRange, value: Long) { pio.insert(value, range) }

        override fun toString(): String = "PIO: %s".format(Integer.toBinaryString(pio.toInt()))

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf("pio" to pio.toHexString())

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            pio = (snapshot["pio"] as String).toULongFromHex()
        }
    }

    private val PIOPFS15_0 = object : Register(ports.mmcr, 0xC20, WORD, "PIOPFS15_0") {
        // PIO15–PIO0 Pin Function Select
        // This register contains the direction bits for pins PIO15–PIO0.
        val PIO15_FNC by bit(15)
        val PIO14_FNC by bit(14)
        val PIO13_FNC by bit(13)
        val PIO12_FNC by bit(12)
        val PIO11_FNC by bit(11)
        val PIO10_FNC by bit(10)
        val PIO9_FNC by bit(9)
        val PIO8_FNC by bit(8)
        val PIO7_FNC by bit(7)
        val PIO6_FNC by bit(6)
        val PIO5_FNC by bit(5)
        val PIO4_FNC by bit(4)
        val PIO3_FNC by bit(3)
        val PIO2_FNC by bit(2)
        val PIO1_FNC by bit(1)
        val PIO0_FNC by bit(0)

        fun isPio(index: Int): Boolean = data[index] == 0L
    }

    private val PIOPFS31_16 = object : Register(ports.mmcr, 0xC22, WORD, "PIOPFS31_16") {
        // PIO31–PI16 Pin Function Select
        // This register contains the direction bits for pins PIO31–PIO16.
        val PIO31_FNC by bit(15)
        val PIO30_FNC by bit(14)
        val PIO29_FNC by bit(13)
        val PIO28_FNC by bit(12)
        val PIO27_FNC by bit(11)
        val PIO26_FNC by bit(10)
        val PIO25_FNC by bit(9)
        val PIO24_FNC by bit(8)
        val PIO23_FNC by bit(7)
        val PIO22_FNC by bit(6)
        val PIO21_FNC by bit(5)
        val PIO20_FNC by bit(4)
        val PIO19_FNC by bit(3)
        val PIO18_FNC by bit(2)
        val PIO17_FNC by bit(1)
        val PIO16_FNC by bit(0)

        fun isPio(index: Int): Boolean = data[index] == 0L
    }

    private val CSPFS = object : Register(ports.mmcr, 0xC24, WORD, "CSPFS") {
        // Chip Select Pin Function Select // If 1 - GPCS7, 0-  TMROUT0
        // This register selects the pin functionality for pins that have general-purpose chip selects (GPCSx) as their alternate function.
        val GPCS7_SEL by bit(7)
        val GPCS6_SEL by bit(6)
        val GPCS5_SEL by bit(5)
        val GPCS4_SEL by bit(4)
        val GPCS3_SEL by bit(3)
        val GPCS2_SEL by bit(2)
        val GPCS1_SEL by bit(1)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value[7..1])
            log.warning { (0..7).joinToString(" ") { if (data[it] == 0L) "TMROUT$it" else "GPCS$it" } }
        }
    }

    private val CLKSEL = object : Register(ports.mmcr, 0xC26, WORD, "CLKSEL") {
        // Clock Select  // IT IS USELESS REGISER
        // This register is used to set up the CLKTIMER[CLKTEST] pin.
        val CLK_TST_SEL by field(3..2)
        val CLK_PIN_DIR by bit(1)
        val CLK_PIN_ENB by bit(0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value and 0b01110011L)
        }
    }

    private val DSCTL = object : Register(ports.mmcr, 0xC28, WORD, "DSCTL", 0x200) {
        // Drive Strength Control. It is useless.
        // This register is used to set up the CLKTIMER[CLKTEST] pin.
        val SCS_DRIVE by field(9..8)
        val SRCW_DRIVE by field(7..6)
        val SDQM_DRIVE by field(5..4)
        val MA_DRIVE by field(3..2)
        val DATA_DRIVE by field(1..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value and 0b0000001111111111L)
        }
    }

    private val PIODIR15_0 = object : Register(ports.mmcr, 0xC2A, WORD, "PIODIR15_0") {
        // PIO15–PIO0 Direction/ 0 - input, 1 - output
        // This register contains the direction bits for pins PIO15–PIO0.
        val PIO15_DIR by bit(15)
        val PIO14_DIR by bit(14)
        val PIO13_DIR by bit(13)
        val PIO12_DIR by bit(12)
        val PIO11_DIR by bit(11)
        val PIO10_DIR by bit(10)
        val PIO9_DIR by bit(9)
        val PIO8_DIR by bit(8)
        val PIO7_DIR by bit(7)
        val PIO6_DIR by bit(6)
        val PIO5_DIR by bit(5)
        val PIO4_DIR by bit(4)
        val PIO3_DIR by bit(3)
        val PIO2_DIR by bit(2)
        val PIO1_DIR by bit(1)
        val PIO0_DIR by bit(0)

        fun isInput(index: Int): Boolean = data[index] == 0L

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.warning { (0..15).joinToString(" ") { "PIO$it=${isInput(it).toInt()}" } }
        }
    }

    private val PIODIR31_16 = object : Register(ports.mmcr, 0xC2C, WORD, "PIODIR31_16") {
        // PIO31–PIO16 Direction/ 0 - input, 1 - output
        // This register contains the direction bits for pins PIO31–PIO16.
        val PIO31_DIR by bit(15)
        val PIO30_DIR by bit(14)
        val PIO29_DIR by bit(13)
        val PIO28_DIR by bit(12)
        val PIO27_DIR by bit(11)
        val PIO26_DIR by bit(10)
        val PIO25_DIR by bit(9)
        val PIO24_DIR by bit(8)
        val PIO23_DIR by bit(7)
        val PIO22_DIR by bit(6)
        val PIO21_DIR by bit(5)
        val PIO20_DIR by bit(4)
        val PIO19_DIR by bit(3)
        val PIO18_DIR by bit(2)
        val PIO17_DIR by bit(1)
        val PIO16_DIR by bit(0)

        fun isInput(index: Int): Boolean = data[index] == 0L

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.warning { (0..16).joinToString(" ") { "PIO${it + 16}=${isInput(it).toInt()}" } }
        }
    }

    private val PIODATA15_0 = object : Register(ports.mmcr, 0xC30, WORD, "PIODATA15_0") {
        // PIO15–PIO0 Data
        // This register is used to read or write the value for pins PIO15–PIO0.
        val PIO15_DATA by bit(15)
        val PIO14_DATA by bit(14)
        val PIO13_DATA by bit(13)
        val PIO12_DATA by bit(12)
        val PIO11_DATA by bit(11)
        val PIO10_DATA by bit(10)
        val PIO9_DATA by bit(9)
        val PIO8_DATA by bit(8)
        val PIO7_DATA by bit(7)
        val PIO6_DATA by bit(6)
        val PIO5_DATA by bit(5)
        val PIO4_DATA by bit(4)
        val PIO3_DATA by bit(3)
        val PIO2_DATA by bit(2)
        val PIO1_DATA by bit(1)
        val PIO0_DATA by bit(0)

        operator fun get(index: Int): Long = data[index]
        operator fun set(index: Int, value: Boolean) {
            if (value) data.insert(1, index) else data.insert(0, index)
            updateWire()
        }

        operator fun set(index: Int, value: Long) {
            set(index, value == 0L)
        }

        fun updateWire() {
            pioOnWire[15..0] = data
        }
    }

    private val PIODATA31_16 = object : Register(ports.mmcr, 0xC32, WORD, "PIODATA31_16") {
        // PIO31–PIO16 Data
        // This register is used to read or write the value for pins PIO31–PIO16.
        val PIO31_DATA by bit(15)
        val PIO30_DATA by bit(14)
        val PIO29_DATA by bit(13)
        val PIO28_DATA by bit(12)
        val PIO27_DATA by bit(11)
        val PIO26_DATA by bit(10)
        val PIO25_DATA by bit(9)
        val PIO24_DATA by bit(8)
        val PIO23_DATA by bit(7)
        val PIO22_DATA by bit(6)
        val PIO21_DATA by bit(5)
        val PIO20_DATA by bit(4)
        val PIO19_DATA by bit(3)
        val PIO18_DATA by bit(2)
        val PIO17_DATA by bit(1)
        val PIO16_DATA by bit(0)

        operator fun get(index: Int): Long = data[index]
        operator fun set(index: Int, value: Boolean) {
            if (value) data.insert(1, index) else data.insert(0, index)
            updateWire()
        }

        operator fun set(index: Int, value: Long) {
            set(index, value == 0L)
        }

        fun updateWire() {
            pioOnWire[31..16] = data
        }
    }

    private val PIOSET15_0 = object : Register(ports.mmcr, 0xC34, WORD, "PIOSET15_0") {
        // PIO15–PIO0 Set
        // This register is used to make the output level High selectively for pins PIO15–PIO0.
        val PIO15_SET by bit(15)
        val PIO14_SET by bit(14)
        val PIO13_SET by bit(13)
        val PIO12_SET by bit(12)
        val PIO11_SET by bit(11)
        val PIO10_SET by bit(10)
        val PIO9_SET by bit(9)
        val PIO8_SET by bit(8)
        val PIO7_SET by bit(7)
        val PIO6_SET by bit(6)
        val PIO5_SET by bit(5)
        val PIO4_SET by bit(4)
        val PIO3_SET by bit(3)
        val PIO2_SET by bit(2)
        val PIO1_SET by bit(1)
        val PIO0_SET by bit(0)

        override fun read(ea: Long, ss: Int, size: Int): Long {
            throw GeneralException("In is write-only register")
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            (0..16)
                    .filter { data[it] == 1L && PIOPFS15_0.isPio(it) && PIODIR15_0.isInput(it) }
                    .forEach { PIODATA15_0[it] = true }
        }
    }

    private val PIOSET31_16 = object : Register(ports.mmcr, 0xC36, WORD, "PIOSET31_16") {
        // PIO31–PIO16 Set
        // This register is used to make the output level High selectively for pins PIO31–PIO16.
        val PIO31_SET by bit(15)
        val PIO30_SET by bit(14)
        val PIO29_SET by bit(13)
        val PIO28_SET by bit(12)
        val PIO27_SET by bit(11)
        val PIO26_SET by bit(10)
        val PIO25_SET by bit(9)
        val PIO24_SET by bit(8)
        val PIO23_SET by bit(7)
        val PIO22_SET by bit(6)
        val PIO21_SET by bit(5)
        val PIO20_SET by bit(4)
        val PIO19_SET by bit(3)
        val PIO18_SET by bit(2)
        val PIO17_SET by bit(1)
        val PIO16_SET by bit(0)

        override fun read(ea: Long, ss: Int, size: Int): Long {
            throw GeneralException("In is write-only register")
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            (0..16)
                    .filter { data[it] == 1L && PIOPFS31_16.isPio(it) && PIODIR31_16.isInput(it) }
                    .forEach { PIODATA31_16[it] = true }
        }
    }

    private val PIOCLR15_0 = object : Register(ports.mmcr, 0xC38, WORD, "PIOCLR15_0") {
        // PIO15–PIO0 Set
        // This register is used to make the output level High selectively for pins PIO15–PIO0.
        val PIO15_CLR by bit(15)
        val PIO14_CLR by bit(14)
        val PIO13_CLR by bit(13)
        val PIO12_CLR by bit(12)
        val PIO11_CLR by bit(11)
        val PIO10_CLR by bit(10)
        val PIO9_CLR by bit(9)
        val PIO8_CLR by bit(8)
        val PIO7_CLR by bit(7)
        val PIO6_CLR by bit(6)
        val PIO5_CLR by bit(5)
        val PIO4_CLR by bit(4)
        val PIO3_CLR by bit(3)
        val PIO2_CLR by bit(2)
        val PIO1_CLR by bit(1)
        val PIO0_CLR by bit(0)

        override fun read(ea: Long, ss: Int, size: Int): Long {
            throw GeneralException("In is write-only register")
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            (0..16)
                    .filter { data[it] == 1L && PIOPFS15_0.isPio(it) && PIODIR15_0.isInput(it) }
                    .forEach { PIODATA15_0[it] = false }
        }
    }

    private val PIOCLR31_16 = object : Register(ports.mmcr, 0xC3A, WORD, "PIOCLR31_16") {
        // PIO15–PIO0 Set
        // This register is used to make the output level High selectively for pins PIO15–PIO0.
        val PIO31_CLR by bit(15)
        val PIO30_CLR by bit(14)
        val PIO29_CLR by bit(13)
        val PIO28_CLR by bit(12)
        val PIO27_CLR by bit(11)
        val PIO26_CLR by bit(10)
        val PIO25_CLR by bit(9)
        val PIO24_CLR by bit(8)
        val PIO23_CLR by bit(7)
        val PIO22_CLR by bit(6)
        val PIO21_CLR by bit(5)
        val PIO20_CLR by bit(4)
        val PIO19_CLR by bit(3)
        val PIO18_CLR by bit(2)
        val PIO17_CLR by bit(1)
        val PIO16_CLR by bit(0)

        override fun read(ea: Long, ss: Int, size: Int): Long {
            throw GeneralException("In is write-only register")
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            (0..16)
                    .filter { data[it] == 1L && PIOPFS31_16.isPio(it) && PIODIR31_16.isInput(it) }
                    .forEach { PIODATA31_16[it] = false }
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf("PIN_ON_WIRE" to pioOnWire.serialize(ctxt))
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        pioOnWire.deserialize(ctxt, snapshot["PIN_ON_WIRE"] as Map<String, Any>)
    }
}