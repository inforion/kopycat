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
package ru.inforion.lab403.kopycat.modules.atom2758.sata

import java.lang.IllegalArgumentException

/**
 * Конфигурация диска
 *
 * @param serial серийный номер диска
 * @param firmwareRevision версия прошивки диска
 * @param model модель диска
 */
data class DiskInfo(
    val serial: String,
    val firmwareRevision: String,
    val model: String,
    val size: ULong,
) {
    init {
        if (serial.length > 20) {
            throw IllegalArgumentException("Disk serial is too long: must be 20 characters max")
        }

        if (firmwareRevision.length > 8) {
            throw IllegalArgumentException("Disk version is too long: must be 8 characters max")
        }

        if (model.length > 40) {
            throw IllegalArgumentException("Disk model is too long: must be 40 characters max")
        }
    }
}
