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
package ru.inforion.lab403.kopycat.veos

import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable


/**
 *
 */
class Configuration : IAutoSerializable {
    /**
     * Bind will use dynamic ports if this is true
     */
    var dynamicPortMapping = false

    /**
     * Task switch timeout in ticks
     */
    var processSwitchPeriod: Long = 100

    /**
     * Allocated memory heap size (should be configured before [VEOS.load] and [VEOS.startMainTask])
     */
    var heapSize: Long = 0x1000_0000 // 256 Mib

    /**
     * Allocated memory stack size (should be configured before [VEOS.load] and [VEOS.startMainTask])
     */
    var stackSize: Long = 0x20_0000 // 2 Mib (Linux default for x86-32, see NOTES in "man pthread_create")

    var systemDataStart: Long = 4L    // Reserve address 0 for nullptr
    var systemDataEnd: Long = 0x1000L

    var rootDirectory: String = "./"

    // rootDirectory-relative path
    var tempDirectory: String = "/tmp"

    var useEntropy: Boolean = false
}