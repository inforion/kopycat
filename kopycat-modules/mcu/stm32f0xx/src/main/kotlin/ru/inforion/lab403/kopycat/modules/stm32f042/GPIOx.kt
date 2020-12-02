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
package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level


@Suppress("PrivatePropertyName", "PropertyName")
class GPIOx(parent: Module, name: String, val index: Int) : Module(parent, name) {
    companion object {
        @Transient private val log = logger(Level.INFO)
        private enum class LockState { INIT, FIRST_WR, SECOND_WR, THIRD_WR, LOCKED }
        const val PIN_COUNT = 16
    }

    enum class RegisterType(val offset: Long) {
        MODER   (0x00),
        OTYPER  (0x04),
        OSPEEDR (0x08),
        PUPDR   (0x0C),
        IDR     (0x10),
        ODR     (0x14),
        BSRR    (0x18),
        LCKR    (0x1C),
        AFRL    (0x20),
        AFRH    (0x24),
        BRR     (0x28)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x80)
        val irq = Master("irq", PIN)
        val pin_input = Slave("pin_input", PIN_COUNT)
        val pin_output = Master("pin_output", PIN_COUNT)
    }

    override val ports = Ports()

    private val pins = object : Area(ports.pin_input, 0, 0xF, "GPIO_INPUT", ACCESS.I_W) {
        override fun fetch(ea: Long, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")
        override fun read(ea: Long, ss: Int, size: Int) = 0L    // should not used
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            GPIOx_IDR.data = value
        }
    }

    private open inner class RegisterBase(
            register: RegisterType,
            default: Long = 0x0000_0000,
            writable: Boolean = true,
            readable: Boolean = true,
            level: Level = Level.FINER
    ) : Register(ports.mem, register.offset, DWORD, "GPIO${index}_${register.name}", default, writable, readable, level)

    private inline fun lowBitsEach(action: (Int) -> Unit) = (0..15).forEach(action)
    private inline fun highBitsEach(action: (Int) -> Unit) = (16..31).forEach(action)
    private fun Long.copyBitByIndex(copyTo: Long, index: Int): Long = if (this[index] == 1L) copyTo.set(index) else copyTo.clr(index)

    private fun checkLockedBits(data: Long, value: Long, isTwoBitsPerPin: Boolean, log: (Int) -> Unit): Long {
        var filteredValue: Long = value
        lowBitsEach { bitIndex ->
            if (GPIOx_LCKR.state == LockState.LOCKED && GPIOx_LCKR.lastValue[bitIndex] == 1L) {

                if (isTwoBitsPerPin) {
                    val dataBitsIndex = (bitIndex * 2 to bitIndex * 2 + 1)

                    if (value[dataBitsIndex.first] != data[dataBitsIndex.first] || value[dataBitsIndex.second] != data[dataBitsIndex.second]) {

                        filteredValue = data.copyBitByIndex(filteredValue, dataBitsIndex.first)
                        filteredValue = data.copyBitByIndex(filteredValue, dataBitsIndex.second)
                        log.invoke(bitIndex)
                    }
                } else {
                    if (value[bitIndex] != data[bitIndex]) {

                        filteredValue = data.copyBitByIndex(filteredValue, bitIndex)
                        log.invoke(bitIndex)
                    }
                }
            }
        }
        return filteredValue
    }

    private val GPIOx_MODER  =  object : RegisterBase(RegisterType.MODER,      if (index == 1) 0x2800_0000 else 0x0000_0000) {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val filteredValue = checkLockedBits(data, value, isTwoBitsPerPin = true) {
                log.warning { "[$name] Software try to change mode of locked gpio$index at pin $it" }
            }
            super.write(ea, ss, size, filteredValue)
        }

    } // A..F
    private val GPIOx_OTYPER =  object : RegisterBase(RegisterType.OTYPER) {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val filteredValue = checkLockedBits(data, value, isTwoBitsPerPin = false) {
                log.warning { "[$name] Software try to change output mode of locked gpio$index at pin $it" }
            }
            super.write(ea, ss, size, filteredValue)
        }

    } // A..F
    private val GPIOx_OSPEEDR=  object : RegisterBase(RegisterType.OSPEEDR,    if (index == 1) 0x0C00_0000 else 0x0000_0000) {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val filteredValue = checkLockedBits(data, value, isTwoBitsPerPin = true) {
                log.warning {"[$name] Software try to change speed mode of locked gpio$index at pin $it" }
            }
            super.write(ea, ss, size, filteredValue)
        }

    } // A..F
    private val GPIOx_PUPDR  =  object : RegisterBase(RegisterType.PUPDR,      if (index == 1) 0x2400_0000 else 0x0000_0000) {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val filteredValue = checkLockedBits(data, value, isTwoBitsPerPin = true) {
                log.warning { "[$name] Software try to change speed mode of locked gpio$index at pin $it" }
            }
            super.write(ea, ss, size, filteredValue)
        }

    } // A..F
    private val GPIOx_IDR    =  object : RegisterBase(RegisterType.IDR, writable = false) {} // A..F
    private val GPIOx_ODR    =  object : RegisterBase(RegisterType.ODR) {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            lowBitsEach { ports.pin_output.write(it.toLong(), 0, 0, data[it]) }
        }
    }   // A..F
    private val GPIOx_BSRR   =  object : RegisterBase(RegisterType.BSRR) {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            super.read(ea, ss, size)
            return 0x0000
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            var data = GPIOx_ODR.data
            lowBitsEach {
                when {
                    value[it] == 1L -> data = data.set(it)
                    value[it + 16] == 1L -> data = data.clr(it)
                }
            }
            GPIOx_ODR.write(0, 0, 0, data)
            log.write(level)
        }
    }   // A..F
    private val GPIOx_LCKR   =  object : RegisterBase(RegisterType.LCKR) {
        var state: LockState = LockState.INIT
        var lastValue = 0L

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val LCKK = value[16]
            when (state) {
                LockState.INIT, LockState.THIRD_WR -> {
                    if (LCKK == 1L) {
                        state = LockState.FIRST_WR
                        lastValue = value
                        return super.write(ea, ss, size, value)
                    }
                }
                LockState.FIRST_WR -> {
                    if (!isBitsChange(value) && LCKK == 0L) {
                        state = LockState.SECOND_WR
                        return super.write(ea, ss, size, value)
                    }
                }
                LockState.SECOND_WR -> {
                    if (!isBitsChange(value) && LCKK == 1L) {
                        state = LockState.THIRD_WR
                        return super.write(ea, ss, size, value)
                    }
                }
                LockState.LOCKED -> return
            }
            state = LockState.INIT
            log.write(level)
        }

        override fun read(ea: Long, ss: Int, size: Int): Long {
            state = when (state) {
                LockState.THIRD_WR -> {
                    LockState.LOCKED
                }
                LockState.LOCKED -> {
                    return super.read(ea, ss, size)
                }
                else -> LockState.INIT
            }

            return super.read(ea, ss, size)
        }

        fun isBitsChange(newValue: Long): Boolean = newValue[15..0] != lastValue[15..0]

        override fun reset() {
            state = LockState.INIT
            lastValue = 0L
            super.reset()
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return super.serialize(ctxt) + mapOf(
                    "state" to state.name,
                    "lastValue" to lastValue.hex8
            )
        }

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            state = LockState.valueOf(snapshot["state"] as String)
            lastValue = (snapshot["lastValue"] as String).hexAsULong
            super.deserialize(ctxt, snapshot)
        }
    }   // A..B
    private val GPIOx_AFRL   =  object : RegisterBase(RegisterType.AFRL) {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val filteredValue = checkLockedBits(data, value, isTwoBitsPerPin = true) {
                log.warning { "[$name] Software try to change speed mode of locked gpio$index at pin $it" }
            }
            super.write(ea, ss, size, filteredValue)
        }

    } // A..F
    private val GPIOx_AFRH   =  object : RegisterBase(RegisterType.AFRH) {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val filteredValue = checkLockedBits(data, value, isTwoBitsPerPin = true) {
                log.warning { "[$name] Software try to change speed mode of locked gpio$index at pin $it" }
            }
            super.write(ea, ss, size, filteredValue)
        }

    } // A..F
    private val GPIOx_BRR    =  object : RegisterBase(RegisterType.BRR) {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            lowBitsEach { if (value[it] == 1L) GPIOx_ODR.data = GPIOx_ODR.data.clr(it) }
            log.write(level)
        }

        override fun read(ea: Long, ss: Int, size: Int): Long {
            super.read(ea, ss, size)
            return 0x0000
        }
    }   // A..F
}