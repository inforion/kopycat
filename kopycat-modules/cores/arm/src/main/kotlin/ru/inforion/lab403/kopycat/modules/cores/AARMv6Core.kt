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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6COP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6CPU
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6MMU
import ru.inforion.lab403.kopycat.cores.base.common.Module

abstract class AARMv6Core(parent: Module, name: String, frequency: Long, ipc: Double) :
        AARMCore(parent, name, frequency, 6, ipc) {

    override val cpu = ARMv6CPU(this, "cpu", haveVirtExt = true)
    override val cop = ARMv6COP(this, "cop")

    override val mmu = ARMv6MMU(this, "mmu")
}