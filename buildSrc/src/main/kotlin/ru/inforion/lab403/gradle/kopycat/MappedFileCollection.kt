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
package ru.inforion.lab403.gradle.kopycat

import org.gradle.api.internal.file.AbstractFileCollection
import org.gradle.api.internal.file.FileCollectionInternal
import org.gradle.api.internal.tasks.TaskDependencyResolveContext
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.Factory
import java.io.File
import java.util.function.Supplier

internal class MappedFileCollection(
    private val collection: AbstractFileCollection,
    private val f: (File) -> File,
) : AbstractFileCollection(collection.patternSetFactoryReflection()) {
    companion object {
        @Suppress("unchecked_cast")
        private fun AbstractFileCollection.patternSetFactoryReflection(): Factory<PatternSet> {
            val field = AbstractFileCollection::class.java.getDeclaredField("patternSetFactory")
            field.isAccessible = true
            return field.get(this) as Factory<PatternSet>
        }
    }

    override fun getDisplayName() = "file collection"

    override fun replace(original: FileCollectionInternal, supplier: Supplier<FileCollectionInternal?>): FileCollectionInternal {
        val newCollection = collection.replace(original, supplier) as AbstractFileCollection
        if (newCollection === collection) {
            return this
        }

        return MappedFileCollection(newCollection, f)
    }

    override fun visitDependencies(context: TaskDependencyResolveContext) = collection.visitDependencies(context)

    private val mapped by lazy { collection.map(f).toMutableSet() }
    override fun getFiles() = mapped
    override fun contains(file: File) = mapped.contains(file)
    override fun iterator() = mapped.iterator()
}

internal fun AbstractFileCollection.lazyMap(f: (File) -> File) = MappedFileCollection(this, f)
