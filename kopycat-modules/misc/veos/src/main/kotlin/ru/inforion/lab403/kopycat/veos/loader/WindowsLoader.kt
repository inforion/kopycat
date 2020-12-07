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
package ru.inforion.lab403.kopycat.veos.loader

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.kernel.Symbol
import ru.inforion.lab403.kopycat.veos.loader.peloader.PELoader
import ru.inforion.lab403.kopycat.veos.loader.peloader.headers.ImageThunkData
import ru.inforion.lab403.kopycat.veos.loader.peloader.structs.PECharacteristic
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WindowsLoader(val os: VEOS<*>) : ALoader(os) {
    override fun reset() {
        //TODO("Not yet implemented")
    }

    override fun loadArguments(args: Array<String>) {
        check(os.currentProcess.contextInitialized) { "Context wasn't initialized" }

        val argc = args.size.asLong
        val envs = os.sys.allocateEnvironmentArray()
        val _args = (args.asList()).map { os.sys.allocateAsciiString(it) } + 0L

        val envi = _args.size

        val arglist = _args + envs

//        argv.reversed().forEach { os.abi.push(it) }
//        os.abi.push(argc)

        val argv = os.sys.allocateArray(os.abi.types.pointer, arglist)
        val envp = argv + (os.abi.types.pointer.bytes * envi)

        os.initApi(argc, argv, envp) // TODO: may cause bugs
    }

    private val PECharacteristic.asAccess get() = when {
        memWrite && memRead -> ACCESS.R_W
        memWrite -> ACCESS.E_W
        memRead -> ACCESS.R_E
        else -> ACCESS.E_E
    }

    // TODO: entity of symbol
    private fun ImageThunkData.toSymbol(base: Long): Symbol = Symbol(toImageImportByName().name, base + importAddress)

    override fun load(filename: String) {
        val pe = PELoader.fromPath(filename)

        val sections = pe.relocatedSections.toMutableList()
        val baseAddress = pe.baseAddress

        val rData = sections
                .first { it.name == ".rdata" }.also {
                    sections.remove(it)
                }
        val rDataBuffer = ByteBuffer.wrap(rData.data).apply { order(ByteOrder.LITTLE_ENDIAN) }

        val mapDynamicSymbols = ArrayList<Symbol>()
        pe.imports.forEach {dll ->
            mapDynamicSymbols += dll.addressTable.map {
                it.toSymbol(baseAddress).apply {
                    rDataBuffer.putInt((it.importAddress - rData.header.virtualAddress).asInt, address.asInt)
                    type = Symbol.Type.External
                }
            }
        }
        val symbolsToRegister = mapDynamicSymbols.associateBy { it.name }
        os.sys.registerSymbols(symbolsToRegister)


        sections.forEach {
            os.currentMemory.allocate(it.name, it.start, it.defaultAlignedSize.asInt, it.header.characteristics.asAccess, it.data)
        }
        os.currentMemory.allocate(rData.name, rData.start, rData.defaultAlignedSize.asInt, rData.header.characteristics.asAccess, rDataBuffer.array())

        os.currentProcess.initProcess(pe.entryPoint)
    }

    override fun loadLibrary(filename: String) {
        TODO("Not yet implemented")
    }

    override fun findSymbol(module: String, symbol: String): Symbol? {
        TODO("Not yet implemented")
    }

    override fun moduleAddress(module: String): Long {
        TODO("Not yet implemented")
    }

    override fun moduleName(address: Long): String {
        TODO("Not yet implemented")
    }


}
