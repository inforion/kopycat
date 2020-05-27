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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.gradle.common

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task

inline val Task.extraProperties: MutableMap<String, Any> get() = extensions.extraProperties.properties

@Suppress("UNCHECKED_CAST")
inline fun <T>Task.forEachExtension(name: String, block: (T) -> Unit) =
        (extensions.getByName(name) as NamedDomainObjectContainer<T>).forEach { block(it) }

@Suppress("UNCHECKED_CAST")
inline fun <T, R>Task.mapExtensions(name: String, transform: (T) -> R) =
        (extensions.getByName(name) as NamedDomainObjectContainer<T>).map { transform(it) }

@Suppress("UNCHECKED_CAST")
inline fun <T>Task.getExtensionsAsList(name: String) = mapExtensions<T, T>(name) { it }

inline fun Task.rebuildRequired() = outputs.upToDateWhen { false }

inline fun <reified T: Task>T.doFirstTyped(crossinline block: (T) -> Unit): Task = doFirst { block(it as T) }
inline fun <reified T: Task>T.doLastTyped(crossinline block: (T) -> Unit): Task = doLast { block(it as T) }