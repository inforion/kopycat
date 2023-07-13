/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.veos.loader.peloader.headers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.veos.loader.peloader.PEFile
import ru.inforion.lab403.kopycat.veos.loader.peloader.string
import java.nio.ByteBuffer

class ImageImportDescriptor(private val peFile: PEFile, private val input: ByteBuffer) {

    val characteristics = input.int.ulong_z  /* 0 for terminating null import descriptor  */
    val originalFirstThunk = characteristics /* RVA to original unbound IAT */
    val timeDateStamp = input.int.ulong_z	/* 0 if not bound,
				 * -1 if bound, and real date\time stamp
				 *    in IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT
				 * (new BIND)
				 * otherwise date/time stamp of DLL bound to
				 * (Old BIND)
				 */
    val forwarderChain = input.int.ulong_z /* -1 if no forwarders */
    val name = input.int.ulong_z
    /* RVA to IAT (if bound this IAT has actual addresses) */
    val	firstThunk = input.int.ulong_z

    val nameString: String get() {
        input.position(peFile.rva2foa(name).int)
        return input.string
    }

    fun readThunkTable(rva: ULong): Array<ImageThunkData> {
        val thunksList = mutableListOf<ImageThunkData>()
        val origin = peFile.rva2foa(rva).int
        input.position(origin)
        while (true) {
            val thunk = ImageThunkData(peFile, input, rva + input.position() - origin)
            if (thunk.addressOfData == 0uL)
                break
            thunksList.add(thunk)
        }
        return thunksList.toTypedArray()
    }

    val lookupTable by lazy { readThunkTable(originalFirstThunk) }

    val addressTable by lazy { readThunkTable(firstThunk) }

    val importsByName by lazy {
        lookupTable.filter { !it.ordinal }.map { it.toImageImportByName() }.toTypedArray()
    }
}