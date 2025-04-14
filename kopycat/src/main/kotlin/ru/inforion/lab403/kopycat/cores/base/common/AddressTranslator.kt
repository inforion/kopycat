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
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction

/**
 * {EN}
 * Base class of the module for address translation. Contains two ports - input and output.
 * When an attempt is made to call the [ModulePorts.Translator.find] method of the input port, the [translate]
 * function is called, which takes the address and converts it, then the [find] method of the output port is called.
 * ```
 *                              _____________
 *  address1, ea, size, LorS   |             | translate(address1), ea, size, LorS
 * ---------------------------|| translate() ||------------------------------------
 *                            |_____________|
 * ```
 * From this class it is advisable to inherit all MMU and other modules that do any address translation.
 *
 *
 * @param parent parent module that contains the current module (translator)
 * @param name module name
 * {EN}
 *
 * {RU}
 * Базовый класс модуля для трансляции адреса. Содержит два порта - входной и выходной.
 * При попытке вызова метода [ModulePorts.Translator.find] входного порта вызывается функция [translate],
 * которая принимает на вход адрес и преобразует его, далее происходит вызов метода [find] выходного порта.
 * От этого класса целессобразно наследовать все MMU и другие модули эмулятора, которые занимаются каким-либо
 * преобразованием адресов.
 *
 * @param parent Родительский модуль, который содержит текущий модуль
 * @param name Имя модуля
 * {RU}
 */
open class AddressTranslator(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val outp = Port("out")
        val inp = Translator("in", outp, this@AddressTranslator)
    }

    @DontAutoSerialize
    final override val ports = Ports()
    /**
     * {RU}
     * Функция для преобразования адреса
     *
     * @param ea Значение входного (непреобразованного адреса)
     * @param ss Значение дополнительного адреса (используется в некоторых процессорных архитектурах, например, x86), в противном случае игнорируется
     * @param size Количество байт, которые должны быть записаны или прочитаны
     * @param LorS Преобразование адреса осуществляться перед чтением ([AccessAction.LOAD]) или записью ([AccessAction.STORE])
     *
     * @return Преобразованное значение адреса
     * {RU}
     *
     * {EN}
     * Function to convert address
     *
     * @param ea input value (non-translatable address)
     * @param ss the value of the additional address (used in some processor architectures, for example, x86), otherwise it is ignored
     * @param size bytes count to be written or read
     * @param LorS Address translation is performed before reading ([AccessAction.LOAD]) or writing ([AccessAction.STORE])
     *
     * @return Translated address
     * {EN}
     */
    open fun translate(ea: ULong, ss: Int, size: Int, LorS: AccessAction): ULong = ea
}
