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

import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.interfaces.IValuable

/** Статус IDE устройства */
internal class PortStatusClass : IValuable {
    // READY | SEEK по умолчанию
    override var data: ULong = (1uL shl 6) or (1uL shl 4)

    var err by bit(0)
    // var index by bit(1)
    // var ecc by bit(2)
    var drq by bit(3)
    var seekSrv by bit(4)
    var wrerr by bit(5)
    var ready by bit(6)
    var busy by bit(7)
}
