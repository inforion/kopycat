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