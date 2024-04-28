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
package ru.inforion.lab403.kopycat.modules.common.piix4


import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.SEVERE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.common.PIC8259
import ru.inforion.lab403.kopycat.modules.common.PIT8254
import ru.inforion.lab403.kopycat.modules.common.RTC
import ru.inforion.lab403.kopycat.modules.memory.RAM

class PIIX4_82371AB(parent: Module, name: String, val busSize: ULong, val picCause: Int? = null) : Module(parent, name) {

    companion object {
        @Transient
        private val log = logger(SEVERE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x3F7u)
        val mem_proxy = Proxy("mem_proxy",  busSize)
    }

    inner class Buses : ModuleBuses(this) {
        val sb_mem = Bus("sb_mem", busSize)
    }

    override val buses = Buses()

    override val ports = Ports()

    val pic = PIC8259(this, "pic", picCause)
    val rtc = RTC(this, "rtc")
    val pit = PIT8254(this, "pit")
    val vga_io = RAM(this, "vga_stub_ram", 0x2f)

    inner class WSMB_4_7_Register : Register(ports.mem, 0xD4u, Datatype.BYTE, "WSMB_4_7") {

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.info { "Attempt to read from WO register WSMB_4_7" }
            return super.read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            when (value[0..1]) {
                0uL -> log.info { "Enable DMA Channel 4. In Kopycat we do nothing with that" }
                1uL -> log.info { "Enable DMA Channel 5. In Kopycat we do nothing with that" }
                2uL -> log.info { "Enable DMA Channel 6. In Kopycat we do nothing with that" }
                3uL -> log.info { "Enable DMA Channel 7. In Kopycat we do nothing with that" }
            }
        }

    }

    private val WSMB_4_7 = WSMB_4_7_Register()

    // TODO
    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt)

    init {
        ports.mem_proxy.connect(buses.sb_mem)

        pic.ports.io.connect(buses.sb_mem, 0x1800_0000uL)
        rtc.ports.io.connect(buses.sb_mem, 0x1800_0000uL)
        pit.ports.io.connect(buses.sb_mem, 0x1800_0000uL)
        vga_io.ports.mem.connect(buses.sb_mem, 0x1800_03b0uL)
    }

}