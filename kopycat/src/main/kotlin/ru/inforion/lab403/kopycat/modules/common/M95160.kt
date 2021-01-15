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
package ru.inforion.lab403.kopycat.modules.common

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.modules.PIN
import java.io.File
import java.util.logging.Level


class M95160(parent: Module, name: String, val eeprom: File): Module(parent, name) {
    companion object {
        @Transient val log = logger(Level.CONFIG)
    }

    enum class CMD(val OPCODE: Int) {
        IDLE(-1),
        WRSR(0b0000_0001), // 1
        WRITE(0b0000_0010), // 2
        READ(0b0000_0011), // 3
        WRDI(0b0000_0100), // 4
        RDSR(0b0000_0101), // 5
        WREN(0b0000_0110), // 6
        WRIDorLID(0b1000_0010), // 82
        RDIDorLS(0b1000_0011)  // 83
    }

    enum class SR(val bit: Int) {
        WIP(0),
        WEL(1),
        BP0(2),
        BP1(3),
        SRWD(7)
    }

    inner class Ports : ModulePorts(this) {
        val irq = Master("irq", PIN)
        val csi = Slave("csi", PIN)
        val cs = Slave("cs", PIN)
    }

    override val ports = Ports()

    private val totalFlashSize = 0x800
    private val content = ByteArray(totalFlashSize).apply {
        gzipInputStreamIfPossible(eeprom.path).read(this)
    }

    private var identificationPage = 0L
    private var addressIdentificationPage: Int = 0

    private var statusRegister = 0L
    private var addressRegister = 0
    private var stateRegister = 0

    var cmd = CMD.IDLE

    fun stateReset() {
        stateRegister = 0
        cmd = CMD.IDLE
        statusRegister = statusRegister.clr(SR.WIP.bit)
    }

    val CS_TOGGLE = object : Register(ports.cs, 0, BYTE, "CS_TOGGLE") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            if (value == 1L) stateReset()
        }
    }

    val SPI_DAT_REG = object : Register(ports.csi, 0, DWORD, "SPI_DAT_REG") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            when (cmd) {
                CMD.READ -> {
                    when (stateRegister) {
                        2 -> {
                            stateRegister += 1
                            return 0
                        }
                        3 -> {
                            stateRegister += 1
                        }
                        4 -> {
                            addressRegister += 1
                            if (addressRegister == totalFlashSize - 1)
                                stateRegister += 1
                        }
                        5 -> {
                            addressRegister = 0
                            stateRegister -= 1
                        }
                        else -> {
                            // log.warning { "$name [${dev.cpu.pc.hex8}] -> Wrong state noticed: $cmd, return 0" }
                            return 0
                        }
                    }
                    val result = content.getInt8(addressRegister).asULong
//                log.finest { "$name [${cpu.pc.hex8}] -> sending from ${addr.hex4} data ${result.hex2}" }
                    log.finest { "$name  -> sending from ${addressRegister.hex4} data ${result.hex2}" }
                    return result
                }
                CMD.RDSR -> {
                    stateRegister = 0
                    cmd = CMD.IDLE
                    val result = statusRegister
//                log.finest { "$name [${cpu.pc.hex8}] -> sending status = ${result.hex2}" }
                    log.finest { "$name -> sending status = ${result.hex2}" }
                    return result
                }
                CMD.RDIDorLS -> {
                    TODO()
                }
                CMD.WRDI -> {
                    stateReset()
                    return 0
                }
                CMD.WREN -> {
                    stateReset()
                    return 0
                }
                CMD.IDLE, CMD.WRITE -> {
                    return 0
                }
                else -> {
//                log.warning { "$name [${cpu.pc.hex8}] -> Unexpected state reading $cmd, return 0" }
                    log.warning { "$name -> Unexpected state reading $cmd, return 0" }
                    return 0
                }
            }
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (cmd) {
                CMD.IDLE -> {
                    if (value == 0L) {
                        ports.irq.request(0)
                        return
                    }
//                log.finest { "$name [${cpu.pc.hex8}] <- received command = ${value.hex2}" }
                    log.finest { "$name <- received command = ${value.hex2}" }
                    val newCmd = find<CMD> { value.asInt == it.OPCODE }
                    if (newCmd != null) {
                        if (newCmd == CMD.WRITE && statusRegister[SR.WIP.bit] == 1L) {
                            log.warning { "Write operation already in process" }
                        } else {
                            cmd = newCmd
                            stateRegister = 0
                            addressRegister = 0
//                        log.fine { "$name [${cpu.pc.hex8}] -> new cmd latched: $cmd" }
                            log.fine { "$name -> new cmd latched: $cmd" }
                        }
                    } else log.warning { "Unknown command received: ${value.hex2}" }
                    when (cmd) {
                        CMD.WRDI -> {
//                        log.finest { "$name [${cpu.pc.hex8}] <- write operation disabled" }
                            log.finest { "$name <- write operation disabled" }
                            statusRegister = statusRegister.clr(SR.WEL.bit)
                        }
                        CMD.WREN -> {
//                        log.finest { "$name [${cpu.pc.hex8}] <- write operation enabled" }
                            log.finest { "$name <- write operation enabled" }
                            statusRegister = statusRegister.set(SR.WEL.bit)
                        }
                        else -> {
                            // do nothing command with parameter will be processed on next cycles
                        }
                    }
                }
                CMD.READ -> {
                    when (stateRegister) {
                        0 -> {
//                        log.finest { "$name [${cpu.pc.hex8}] <- received value = ${value.hex2}" }
                            log.finest { "$name <- received value = ${value.hex2}" }
                            addressRegister = addressRegister.insert(value.toInt(), 15..8)
                            stateRegister += 1
                        }
                        1 -> {
//                        log.finest { "$name [${cpu.pc.hex8}] <- received value = ${value.hex2}" }
                            log.finest { "$name <- received value = ${value.hex2}" }
                            addressRegister = addressRegister.insert(value.toInt(), 7..0)
                            stateRegister += 1
//                        log.fine { "$name [${cpu.pc.hex8}] -> addr acquired: ${addr.hex4}" }
                            log.fine { "$name -> addr acquired: ${addressRegister.hex4}" }
                        }
                    }
                }
                CMD.WRITE -> {
//                log.finest { "$name [${cpu.pc.hex8}] <- received value = ${value.hex2}" }
                    log.finest { "$name <- received value = ${value.hex2}" }
                    when {
                        statusRegister[SR.WEL.bit] != 1L -> log.warning { "Write operation disabled" }
                        else -> {
                            statusRegister = statusRegister.set(SR.WIP.bit)
                            when (stateRegister) {
                                0 -> {
                                    addressRegister = addressRegister.insert(value.toInt(), 15..8)
                                    stateRegister += 1
                                }
                                1 -> {
                                    addressRegister = addressRegister.insert(value.toInt(), 7..0)
                                    stateRegister += 1
                                }
                                2 -> {
                                    if (checkMemoryBlock(statusRegister, addressRegister)) {
                                        content[addressRegister] = value.toByte()
                                        stateRegister += 1
                                    } else {
                                        stateRegister += 2
                                    }
                                }
                                3 -> {
                                    addressRegister += 1
                                    content[addressRegister] = value.toByte()
                                }
                                4 -> stateReset()
                            }
                        }
                    }
                }
                CMD.WRSR -> {
//                log.finest { "$name [${cpu.pc.hex8}] <- received value = ${value.hex2}" }
                    log.finest { "$name <- received value = ${value.hex2}" }
                    statusRegister = value
                    stateRegister = 0
                    cmd = CMD.IDLE
                }
                CMD.WRIDorLID -> {
//                log.finest { "$name [${cpu.pc.hex8}] <- received value = ${value.hex2}" }
                    log.finest { "$name <- received value = ${value.hex2}" }
                    when (stateRegister) {
                        0 -> stateRegister += if (value[3] == 0L) 2 else 1
                        1 -> TODO()
                        2 -> {
                            addressIdentificationPage = (value and 0b11111).toInt()
                            stateRegister = 1
                        }
                        3 -> {
                            identificationPage = identificationPage.insert(value, addressIdentificationPage + 7..addressIdentificationPage)
                        }
                    }
                }
                CMD.RDIDorLS -> {
//                log.finest { "$name [${cpu.pc.hex8}] <- received value = ${value.hex2}" }
                    log.finest { "$name <- received value = ${value.hex2}" }
                    TODO()
                }
                else -> {
//                log.warning { "$name [${cpu.pc.hex8}] Unexpected writing in state $cmd value = ${value.hex2}" }
                    log.warning { "$name Unexpected writing in state $cmd value = ${value.hex2}" }
                    cmd = CMD.IDLE
                }
            }
            ports.irq.request(0)
        }
    }

    fun checkMemoryBlock(statusRegister: Long, address: Int): Boolean {
        val memPart = statusRegister[SR.BP1.bit..SR.BP0.bit]
        return when (memPart) {
            0L -> true
            1L -> address < 0x600
            2L -> address < 0x400
            3L -> false
            else -> false
        }
    }

    override fun command(): String = "m95160"

    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent)?.apply {
                subparser("load").apply {
                    variable<String>("path", help = "Load M95160 memory from file with specified path")
                }
            }

    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        context.result = when (context.command()) {
            "load" -> {
                val path = context.getString("path")
                gzipInputStreamIfPossible(path).read(content)
                "File loaded successfully: ${content.getArray(0, 16).hexlify()}..."
            }

            else -> return false
        }

        return true
    }
}