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
package ru.inforion.lab403.kopycat.experimental.linux.biosless

import InterruptHookData
import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.common.extensions.hex4
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/** Handles interrupts used by kernel "setup code" */
class SetupCodeInterrupts(core: x86Core, private val e820map: Array<E820>) {
    companion object {
        @Transient val log = logger(FINE)
    }

    init {
        core.intHooks.put(InterruptHookData(0x10uL, skipInstrExecution = true, ::int10))
        core.intHooks.put(InterruptHookData(0x13uL, skipInstrExecution = true, ::int13))
        core.intHooks.put(InterruptHookData(0x15uL, skipInstrExecution = true, ::int15))
        core.intHooks.put(InterruptHookData(0x16uL, skipInstrExecution = true, ::int16))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun int10(core: x86Core, irq: ULong, data: InterruptHookData) =
        when (val ah = core.cpu.regs.ah.value) {
            0x00uL -> Unit // Set video mode; Ignore
            0x02uL -> Unit // Set cursor position; Ignore
            // Read cursor position; Just return zeroes
            0x03uL -> core.cpu.regs.run {
                cx.value = 0uL
                dx.value = 0uL
            }
            // Teletype output; same as `out 0x3f8, al`
            0x0euL -> core.ports.io.write(0x3f8uL, 0, 1, core.cpu.regs.al.value)
            // Get video state
            0x0fuL -> core.cpu.regs.run {
                // ah - number of screen columns
                // al - mode
                ax.value = 0x5003uL
                bx.value = 0uL
            }

            0x12uL -> core.cpu.regs.al.value = 0x12uL // Video subsystem configuration; Just return expected value
            0x4fuL -> Unit // VESA-related; Ignore
            else -> TODO("Unexpected AH in int 0x10: ${ah.hex2}")
        }

    @Suppress("UNUSED_PARAMETER")
    private fun int15(core: x86Core, irq: ULong, data: InterruptHookData): Unit =
        when (val ax = core.cpu.regs.ax.value) {
            0xec00uL -> Unit // Tells BIOS we run in long mode; Ignore
            0xe980uL -> Unit // Get Intel SpeedStep (IST) information; Ignore
            // Get memory map entry
            0xe820uL -> core.cpu.run {
                val entry = e820map.getOrNull(regs.ebx.value.int)
                regs.ebx.value += 1uL

                if (regs.ebx.value >= e820map.size.ulong_z) {
                    regs.ebx.value = 0uL
                    regs.cl.value = 0uL
                }

                if (entry != null) {
                    entry.write(core)
                    regs.eax.value = regs.edx.value
                    regs.cl.value = 20uL
                    flags.cf = false
                } else {
                    flags.cf = true
                }
            }
            // Taken from SeaBIOS
            0xe801uL -> core.cpu.regs.run {
                this.ax.value = 0x3c00uL
                cx.value = 0x3c00uL
                bx.value = 0x7efduL
                dx.value = 0x7efduL
            }
            // Get the amount of extended memory (above 1M); taken from SeaBIOS
            0x8800uL -> core.cpu.regs.ax.value = 0xfc00uL
            // Return System Configuration Parameters (PS/2 only); Return error (CF = 1)
            0xC000uL -> core.cpu.flags.cf = true
            else -> TODO("Unexpected AX in int 0x15: ${ax.hex4}")
        }

    @Suppress("UNUSED_PARAMETER")
    private fun int16(core: x86Core, irq: ULong, data: InterruptHookData): Unit =
        when (val ah = core.cpu.regs.ah.value) {
            0x02uL -> core.cpu.regs.al.value = 0uL // Read Keyboard Flags
            0x03uL -> Unit // Keyboard repeat rate; Ignore
            else -> TODO("Unexpected AH in int 0x16: ${ah.hex2}")
        }

    @Suppress("UNUSED_PARAMETER")
    private fun int13(core: x86Core, irq: ULong, data: InterruptHookData): Unit =
        when (val ah = core.cpu.regs.ah.value) {
            0x41uL -> core.cpu.flags.cf = true // Test Whether Extensions Are Available
            else -> {
                log.severe { "Unexpected AH in int 0x13: ${ah.hex2}, skipping" }
            }
        }
}
