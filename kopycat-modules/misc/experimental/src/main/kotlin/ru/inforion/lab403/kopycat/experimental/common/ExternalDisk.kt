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
package ru.inforion.lab403.kopycat.experimental.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import java.io.RandomAccessFile

/**
 * {RU}
 * Модуль для выноса больших дисков из снапшота в файловую систему.
 * Для избежания проблем с синхронизацией записи, используйте "s" или "d" в аргументе
 * mode конструктора [RandomAccessFile]
 *
 * @param parent родительский компонент (необязательный параметр)
 * @param name произвольное имя объекта модуля
 * @param f файл с данными диска
 * @param regionSize если не 0, то сохраняет изменения диска в снапшот блоками с размером [regionSize],
 *   при этом не меняя сам диск в ФС. В таком случае можно открывать диск только на чтение.
 * {RU}
 */
class ExternalDisk(
    parent: Module?,
    name: String,
    private val f: RandomAccessFile,
    private val regionSize: ULong = 0uL,
) : Module(parent, name) {
    val size = f.length().ulong

    inner class Ports : ModulePorts(this) {
        val mem = Port("mem")
    }

    override val ports = Ports()

    private val sparseOverlay = if (regionSize != 0uL) {
        SparseRAM(this, "overlay", size, regionSize)
    } else {
        null
    }

    // SATA reads one byte at a time, no need to implement anything but single byte reads and writes
    init {
        object : Area(ports.mem, 0uL, size, "area") {
            private fun readByteFromDisk(ea: ULong): ULong {
                return synchronized(f) {
                    f.seek(ea.long)
                    f.readByte().ulong_z
                }
            }

            override fun read(ea: ULong, ss: Int, size: Int) = if (sparseOverlay?.hasMemoryForAddress(ea) == true) {
                sparseOverlay.area.read(ea, ss, size)
            } else {
                readByteFromDisk(ea)
            }

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                if (sparseOverlay != null) {
                    if (!sparseOverlay.hasMemoryForAddress(ea)) {
                        val start = (ea / regionSize) * regionSize
                        sparseOverlay.area.store(
                            start,
                            synchronized(f) {
                                f.seek(start.long)
                                val b = ByteArray(regionSize.int)
                                val count = f.read(b)
                                if (count == -1) {
                                    byteArrayOf()
                                } else {
                                    b.copyOfRange(0, count)
                                }
                            },
                        )
                    }

                    sparseOverlay.area.write(ea, ss, size, value)
                    return
                }

                f.seek(ea.long)
                when (size) {
                    // Datatype.QWORD.bytes -> f.writeLong(value.long)
                    // Datatype.DWORD.bytes -> f.writeInt(value.int)
                    // Datatype.WORD.bytes -> f.writeShort((value and 0xFFFFuL).int)
                    Datatype.BYTE.bytes -> f.writeByte((value and 0xFFuL).int)
                    else -> throw MemoryAccessError(core.pc, ea, AccessAction.LOAD, "Unsupported write size $size bytes")
                }
            }
        }
    }
}
