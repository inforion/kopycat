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
package ru.inforion.lab403.elfloader.processors.ppc

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.elfloader.ElfFile
import ru.inforion.lab403.elfloader.ElfRel
import ru.inforion.lab403.elfloader.processors.AElfDecoder


class ElfDecoderPPC(file: ElfFile) : AElfDecoder(file) {

    companion object {
        private val log = logger()
    }

    override val prefix = "PPC"
    override val pltHeadSize = 0
    override val pltEntrySize = 0

    var symbolTableSize : Long? = null

    override fun checkHeader() = Unit

    override fun checkFlags() = Unit

    override fun checkSectionType(type: Int) = Unit

    override fun checkSectionFlags(flags: UInt) = TODO("Not implemented")

    override fun checkSectionName(name: String, type: Int, flags: UInt) = TODO("Not implemented")

    override fun checkSymbolBinding(bind: Int) = TODO("Not implemented")

    override fun checkSymbolType(type: Int) = Unit

    override fun checkSegmentType(type: Int) = TODO("Not implemented")

    override fun checkSegmentFlags(flags: UInt): Unit = TODO("Not implemented")

    override fun parseDynamic(hm: MutableMap<Int, ULong>, tag: Int, ptr: ULong): Unit = TODO("Not implemented")

    override fun applyStaticRelocation(rel: ElfRel, vaddr: ULong, symbol: ULong, got: Optional<ULong>, data: ULong) =
        TODO("Not implemented")

    override fun getProgramHeaderTypeNameById(type: Int): String = TODO("NI")

    override fun getRelocationNameById(type: Int): String = TODO("NI")

}