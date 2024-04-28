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
//package ru.inforion.lab403.elfloader
//
//import ru.inforion.lab403.common.extensions.hex8
//import ru.inforion.lab403.common.extensions.int
//import ru.inforion.lab403.common.logging.WARNING
//import ru.inforion.lab403.common.logging.logger
//import ru.inforion.lab403.common.optional.Optional
//import ru.inforion.lab403.common.optional.emptyOpt
//import ru.inforion.lab403.common.optional.opt
//import ru.inforion.lab403.elfloader.ElfRegion.Companion.toElfRegion
//import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType
//import ru.inforion.lab403.elfloader.exceptions.EBadIntersect
//
//class ElfRegionImage(regions: List<ElfRegion>, val inputIdaLayer: IDARegionLayer? = null): Iterable<ElfRegion> {
//
//    val items = regions.toMutableList()
//    val idaLayer = inputIdaLayer?.copy() ?: IDARegionLayer()
//
//    val baseAddress: ULong by lazy {
//        filterLoadable().minByOrNull { it.vaddr }!!.vaddr
//    }
//
//    override fun iterator() = items.iterator()
//    operator fun get(i: Int) = items[i]
//    operator fun set(i: Int, value: ElfRegion) { items[i] = value }
//
//    fun byAddress(address: ULong) = items.first { it.vaddr == address }
//
//    companion object {
//        private val log = logger(WARNING)
//
//        fun fromElfLoader(elfLoader: ElfLoader, generateVirtualRegions: Boolean): ElfRegionImage {
//            val elfFile = elfLoader.elfFile
//            val result = if (elfFile.hasAllocSections)
//                elfFile.sections.map { it.toElfRegion(elfFile) }
//            else
//                elfFile.segments.map { it.toElfRegion(elfFile) }
//
//            return ElfRegionImage(result).apply {
//                if (generateVirtualRegions)
//                    idaLayer.addVirtualRegions(elfLoader, this)
//            }
//        }
//
//        fun List<ElfRegion>.toElfRegionImage(idaLayer: IDARegionLayer) = ElfRegionImage(this, idaLayer)
//    }
//
//    fun copy() = ElfRegionImage(items, idaLayer)
//
//    fun add(region: ElfRegion) = items.add(region)
//    fun addEmptyVirtual(name: String, vaddr: ULong, size: ULong) = items.add(ElfRegion.virtual(name, vaddr, size))
//
//    fun filterLoadable() = filter { it.isAllocate }.toElfRegionImage(idaLayer)
//
//    fun fixIntersections(): ElfRegionImage {
//        val result = toMutableList()
//        result.forEach { area ->
//            val intersect = result.find {
//                if (area == it || area notIntersects it)
//                    return@find false
//
//                if (area notFits it)
//                    throw EBadIntersect("Area ${area.name} [${area.range.hex8}] has bad intersection with ${it.name} [${it.range.hex8}]")
//
//                area divides it
//            }
//
//            if (intersect != null) {
//                val (first, second) = intersect split area
//                log.warning { "Area ${area.name} [${area.range.hex8}] has been divided by ${intersect.name} [${intersect.range.hex8}]" }
//
//                result.remove(intersect)
//
//                if (first != null) {
//                    log.warning { "First is ${first.name} [${first.range.hex8}]" }
//                    result.add(first)
//                }
//
//                if (second != null) {
//                    log.warning { "Second is ${second.name} [${second.range.hex8}]" }
//                    result.add(second)
//                }
//            }
//        }
//        return result.apply { sortBy { it.vaddr } }.toElfRegionImage(idaLayer)
//    }
//
//    // In-place
//    fun doRebase(rebaseAddress: ULong, isRelocatableFile: Boolean) {
//        idaLayer.rebase(this, rebaseAddress, isRelocatableFile)
//    }
//
//    fun doRebaseOrCopy(condition: Boolean, rebaseAddress: ULong, isRelocatableFile: Boolean) =
//        copy().apply { if (condition) doRebase(rebaseAddress, isRelocatableFile) }
//
//    fun offsetToAddress(elfFile: ElfFile, offset: ULong) =
//        items.filter { it.isSection && it.sectionType != ElfSectionHeaderType.SHT_GROUP }
//            .first { offset in it.offsetRange }.toAddress(offset)
//}