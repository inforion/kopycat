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
package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Status


interface ITracer<R: AGenericCore>: ICoreUnit {
    /**
     * {EN}This method is called before each instruction executed{EN}
     *
     * {RU}Метод вызывается каждый раз перед выполнением инструкции{RU}
     */
    fun preExecute(core: R): Boolean

    /**
     * {EN}This method is called after each instruction executed{EN}
     *
     * {RU}Метод вызывается каждый раз после выполнения инструкции{RU}
     */
    fun postExecute(core: R, status: Status): Boolean

    /**
     * {EN}This method is called before device is running{EN}
     *
     * {RU}Метод вызывается перед тем, как устройство переведено в состояние run{RU}
     */
    fun onStart() = Unit

    /**
     * {EN}This method is called before device is stopped{EN}
     *
     * {RU}Метод вызывается перед тем, как устройство переведено в состояние stop{RU}
     */
    fun onStop() = Unit

    /**
     * {EN}This method is called to run simulation{EN}
     *
     * {RU}Метод вызывается для начала симуляции устройства{RU}
     */
    fun run(block: () -> Unit) {
        onStart()
        block()
        onStop()
    }
}