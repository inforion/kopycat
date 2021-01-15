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

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import java.io.Serializable

/**
 * {RU}
 * Любой объект, который может быть сохранён snapshot-е должет быть унаследован от этого интерфейса.
 * {RU}
 */
interface ISerializable: Serializable {
    /**
     * {RU}Сериализация объекта с использованием контекста <ctxt> в Map из строки.{RU}
     *
     * {EN}
     * Serialize object using specified context <ctxt> into Map from string to whatever you want.
     * Final Map should ended with Map<String, String>
     * {EN}
     */
    fun serialize(ctxt: GenericSerializer): Map<String, Any> = emptyMap()

    /**
     * {RU}Загрузка snapshot-а с использованием указанного контекста <ctxt> из любого типа.{RU}
     *
     * {EN}
     * Loading snapshot using specified deserialization context <ctxt> from any type
     * Peripheral legacy support
     * {EN}
     */
    @Deprecated("Old legacy support")
    fun deserialize(ctxt: GenericSerializer, snapshot: Any) { }

    /**
     * {RU}Загрузка snapshot-а с использованием указанного контекста <ctxt> из Map<String, Any>{RU}
     *
     * {EN}Loading snapshot using specified deserialization context <ctxt> from Map<String, Any>{EN}
     */
    fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) { }

    /**
     * {RU}
     * Восстановить последний загруженное или сериализованное состояние snapshot используя
     * указанный контекст десериализации <ctxt> из Map<String, Any>
     * {RU}
     *
     * {EN}
     * Restore to last loaded or serialized snapshot state using specified
     * deserialization context <ctxt> from Map<String, Any>
     * {EN}
     */
    fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) = deserialize(ctxt, snapshot)
}