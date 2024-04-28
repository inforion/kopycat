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

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.interfaces.ISerializable

/**
 * Нужен для авто-сериализации не-компонентов
 * TODO: а он точно нужен?
 */
class ComponentWrapper(
    parent: Component?,
    name: String,
    val serializableClasses: Map<String, ISerializable>
) : Component(parent, name) {
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
            "serializableClasses" to serializableClasses
                .mapValues { (k, v) ->
                    runCatching { v.serialize(ctxt) }.getOrElse {
                        log.warning { "[$name] Unable to serialize $k (error below)" }
                        log.warning { it.stackTrace }
                        null
                    }
                }
                .filter { (_, v) -> v != null }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        (snapshot["serializableClasses"] as Map<String, Any>?)?.forEach { (k, v) ->
            if (k !in serializableClasses) {
                log.warning { "[$name] Unable to deserialize $k (not found)" }
                return@forEach
            }

            runCatching { serializableClasses[k]!!.deserialize(ctxt, v as Map<String, Any>) }.getOrElse {
                log.warning { "[$name] Unable to deserialize $k (error below)" }
                log.warning { it.stackTrace }
            }
        }
    }
}