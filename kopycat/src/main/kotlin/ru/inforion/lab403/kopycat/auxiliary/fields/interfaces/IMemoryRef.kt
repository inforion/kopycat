/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.auxiliary.fields.interfaces

import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import ru.inforion.lab403.kopycat.interfaces.IReadable


/**
 * Class contains memory field is being used for reading data.
 * The memory is just a reference and not being owned by the device.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
interface IReadMemoryRef {
    @get:JvmName("getMemory")
    val memory: IReadable
}

/**
 * Class contains memory field is being used for reading / writing data.
 * The memory is just a reference and not being owned by the device.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
interface IMemoryRef: IReadMemoryRef {
    @get:JvmName("getMemory")
    override val memory: IReadWrite
}
