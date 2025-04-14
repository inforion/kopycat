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
package ru.inforion.lab403.kopycat.modules.common.sata.commands

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.modules.common.sata.DiskInfo
import ru.inforion.lab403.kopycat.modules.common.sata.IIDECommand
import ru.inforion.lab403.kopycat.modules.common.sata.Port
import ru.inforion.lab403.kopycat.modules.common.sata.Port.Companion.DISK_HEADS
import ru.inforion.lab403.kopycat.modules.common.sata.Port.Companion.DISK_SECTORS

internal class Identify(disk: DiskInfo) : IIDECommand {
    companion object Extensions {
        operator fun ByteArray.set(idx: Int, value: UShort) {
            this[idx * 2] = value[7..0].byte
            this[idx * 2 + 1] = value[15..8].byte
        }

        operator fun ByteArray.set(idx: Int, value: String) {
            value.chunked(2).withIndex().forEach { (idx2, twoChars) ->
                this[idx + idx2] = (twoChars[0].ushort shl 8).ushort or twoChars[1].ushort
            }
        }
    }

    override val setDSC: Boolean = false
    override val name: String = "IDENTIFY"

    private var _identifyData: ByteArray

    var accessed = false
        private set

    var identifyData: ByteArray
        get() {
            accessed = true
            return _identifyData
        }
        set(value) {
            accessed = true
            _identifyData = value
        }

    init {
        val nbSectors: ULong = disk.size ceil 512uL // число секторов
        val cylinders = (nbSectors / (DISK_HEADS.ulong_s * DISK_SECTORS.ulong_s)).let {
            if (it > 16383uL) {
                16383u
            } else if (it < 2uL) {
                2u
            } else {
                it.ushort
            }
        }

        _identifyData = ByteArray(512) { 0 }

        // hdparm/identify.c
        _identifyData[0] = 0x0040u
        _identifyData[1] = cylinders
        _identifyData[3] = DISK_HEADS
        _identifyData[4] = (512u * DISK_SECTORS).ushort
        _identifyData[5] = 512u
        _identifyData[6] = DISK_SECTORS
        _identifyData[10] = disk.serial.padEnd(20, ' ')

        _identifyData[20] = 3u
        _identifyData[21] = 512u
        _identifyData[22] = 4u
        _identifyData[23] = disk.firmwareRevision.padEnd(8, ' ')
        _identifyData[27] = disk.model.padEnd(40, ' ')

        _identifyData[48] = 1u
        _identifyData[49] = ((1u shl 11) or (1u shl 9) or (1u shl 8)).ushort
        _identifyData[51] = 0x200u
        _identifyData[52] = 0x200u
        _identifyData[53] = (1u or (1u shl 1) or (1u shl 2)).ushort
        _identifyData[54] = cylinders
        _identifyData[55] = DISK_HEADS
        _identifyData[56] = DISK_SECTORS

        _identifyData[57] = nbSectors[15..0].ushort
        _identifyData[58] = nbSectors[31..16].ushort

        var nbSectorsLba28 = nbSectors
        if (nbSectorsLba28 >= (1uL shl 28)) {
            nbSectorsLba28 = (1uL shl 28) - 1uL
        }

        _identifyData[60] = nbSectorsLba28[15..0].ushort
        _identifyData[61] = nbSectorsLba28[31..16].ushort

        _identifyData[62] = 0x07u
        _identifyData[63] = 0x07u
        _identifyData[64] = 0x03u
        _identifyData[65] = 120u
        _identifyData[66] = 120u
        _identifyData[67] = 120u
        _identifyData[68] = 120u

        _identifyData[75] = 0u
        // Диск не умеет NCQ
        _identifyData[76] = 0u

        _identifyData[80] = 0xF0u
        _identifyData[81] = 0x16u
        _identifyData[82] = ((1u shl 5) or (1u shl 14)).ushort
        _identifyData[83] = ((1u shl 10) or (1u shl 12) or (1u shl 13) or (1u shl 14)).ushort
        _identifyData[84] = (1u shl 14).ushort
        _identifyData[85] = (1u shl 14).ushort
        _identifyData[86] = ((1u shl 10) or (1u shl 12) or (1u shl 13)).ushort
        _identifyData[87] = (1u shl 14).ushort
        _identifyData[88] = (0x3Fu or (1u shl 13)).ushort

        // QEMU: (1u or (1u shl 14) or 0x2000u).ushort
        // Linux ожидает 0 для SATA: include/linux/ata.h -> ata_id_is_sata
        _identifyData[93] = 0

        // LBA48
        _identifyData[100] = nbSectors[15..0].ushort
        _identifyData[101] = nbSectors[31..16].ushort
        _identifyData[102] = nbSectors[47..32].ushort
        _identifyData[103] = nbSectors[64..48].ushort

        _identifyData[217] = 0x0401u
    }

    override fun execute(port: Port, slot: Int): Boolean {
        port.status.apply {
            data = 0uL
            ready = 1
            seekSrv = 1
        }

        port.ideTransfer(slot, identifyData)
        port.ideSetIrq()

        return false
    }
}
