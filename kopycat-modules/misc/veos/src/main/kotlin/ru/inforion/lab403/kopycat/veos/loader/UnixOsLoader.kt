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
package ru.inforion.lab403.kopycat.veos.loader

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.common.optional.opt
import ru.inforion.lab403.elfloader.ElfAccess
import ru.inforion.lab403.elfloader.ElfLoader
import ru.inforion.lab403.elfloader.ElfSymbol
import ru.inforion.lab403.elfloader.enums.ElfDynamicSectionTag
import ru.inforion.lab403.elfloader.enums.ElfSymbolTableType
import ru.inforion.lab403.elfloader.enums.ElfType
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmRelocationType
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsDynamicSectionTag
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsRelocationType
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.kernel.Symbol
import java.io.File


class UnixOsLoader(val os: VEOS<*>) : ALoader(os) {

    companion object {
        @Transient
        val log = logger(FINER)
    }

    override fun reset() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO: Override?
    // TODO: REMOVE!!!!
    // Do not forget that args[0] = executable
    override fun loadArguments(args: Array<String>) {
        check(os.currentProcess.contextInitialized) { "Context wasn't initialized" }

        val argc = args.size.ulong_z
        val envs = os.sys.allocateEnvironmentArray()
        val _args = (args.asList()).map { os.sys.allocateAsciiString(it) } + 0uL

        val envi = _args.size

        val arglist = _args + envs

        arglist.reversed().forEach { os.abi.push(it) }
        os.abi.push(argc)

        val argv = os.sys.allocateArray(os.abi.types.pointer, arglist)
        val envp = argv + (os.abi.types.pointer.bytes * envi)

        os.initApi(argc, argv, envp) // TODO: may cause bugs
    }
    private val ElfAccess.asAccess get() = when {
        isWrite && isRead -> ACCESS.R_W
        isWrite -> ACCESS.E_W
        isRead -> ACCESS.R_E
        else -> ACCESS.E_E
    }

    class ElfData constructor(
            val memoryName: String,
            val name: String,
            val baseAddress: ULong,
            val symbols: List<Symbol>,
            val dynamicSymbols: List<Symbol>,
            val undefinedSymbols: List<Symbol>?,
            val dynamicSegment: Map<Int, ULong>?,
            val jumpSlots: List<Symbol>,
            val globDat: List<Symbol>,
            val got: Optional<ULong>
    ) : IAutoSerializable, IConstructorSerializable

    private val elfData = mutableMapOf<String, ElfData>()

    override fun findSymbol(module: String, symbol: String) = elfData[module]?.symbols?.find { it.name == symbol }

    override fun moduleAddress(module: String) = elfData[module]!!.baseAddress

    override fun moduleName(address: ULong) = elfData.values.first { it.baseAddress == address }.name

    private fun linkReferences() = elfData.values.forEach { elf ->
        if (os.currentMemory.name != elf.memoryName)
            return@forEach

        if (elf.got.isPresent) {

//                // TODO: NOW NOT WORKS!!!!
//                val resolverName = "__resolve_undefined_reference__" // TODO: use internal handler
//                if (os.sys.addressOfSymbol(resolverName) == null) {
//                    val ea = os.systemData.word()
//                    os.abi.writeWord(ld.got, ea) // Self-resolving
//                    os.sys.registerInternalSymbol(resolverName, ea)
//                }


            if (os.abi.core is MipsCore) {

                // TODO: FIXME!!!!
                val gotsym = elf.dynamicSegment!![MipsDynamicSectionTag.DT_MIPS_GOTSYM.id]!!
                val local_gotno = elf.dynamicSegment[MipsDynamicSectionTag.DT_MIPS_LOCAL_GOTNO.id]!!

                elf.undefinedSymbols!!.filter { it.ind.ulong_z >= gotsym }.forEach {
                    val symName = elf.dynamicSymbols[it.ind].name
                    val new_addr = os.sys.addressOfSymbol(symName).get
                    val got_addr = elf.got.get + (local_gotno + it.ind - gotsym) * Datatype.DWORD.bytes
                    os.abi.writeInt(got_addr, new_addr)
                }
            }
        }

        elf.jumpSlots.forEach {
            val symName = elf.dynamicSymbols[it.ind].name
            val new_addr = os.sys.addressOfSymbol(symName).get
            os.abi.writeInt(it.address, new_addr)
        }
        elf.globDat.forEach {
            val symName = elf.dynamicSymbols[it.ind].name
            val new_addr = os.sys.addressOfSymbol(symName).get
            os.abi.writeInt(it.address, new_addr)
        }
    }

    fun ElfSymbol.toSymbol(base: ULong = 0uL): Symbol {
        val entity = when (infoType and 0xf) {
            ElfSymbolTableType.STT_FUNC.id -> Symbol.Entity.Function
            else -> Symbol.Entity.Other
        }
        return Symbol(name, value + base, ind, entity = entity)
    }

    // TODO: refactor this
    fun referencesProcessing(elfName: String, elf: ElfLoader, base: ULong = 0uL) {
        val regions = elf.relocatedRegions
        val symbols = elf.symbols
        val dynamicSymbols = elf.dynamicSymbols
        val undefinedSymbols = elf.undefinedSymbols

        val mapSymbols = ArrayList<Symbol>()
        if (symbols != null) {
            mapSymbols += symbols.map { it.toSymbol(base) }
//            os.sys.registerSymbols(mapSymbols)
        }

        val mapDynamicSymbols = ArrayList<Symbol>()
        // For resolving undefined references
        if (undefinedSymbols != null) {
            val extern = regions.first { it.name == "extern" }
            mapDynamicSymbols += dynamicSymbols!!.map { it.toSymbol(base) }

            undefinedSymbols.forEachIndexed { i, it ->
                mapDynamicSymbols[it.ind].address = extern.vaddr + i * 4 + base
                mapDynamicSymbols[it.ind].type = Symbol.Type.External
            }

        }
        val symbolsToRegister = mapSymbols.associateBy { it.name } + mapDynamicSymbols.associateBy { it.name }
        os.sys.registerSymbols(symbolsToRegister)

        val jumpSlots = elf.jumpSlots.map { Symbol("", it.vaddr + base, it.sym) } // TODO: Relocation class?
        val globDat = elf.globalData.map { Symbol("", it.vaddr + base, it.sym) } // TODO: Relocation class?
        val undSyms = elf.undefinedSymbols?.map { it.toSymbol(base) }
        val dynSeg = elf.dynamicSegment
        var got = dynSeg?.get(ElfDynamicSectionTag.DT_PLTGOT.id) /*symbols?.find { it.name == "_GLOBAL_OFFSET_TABLE_" }?.value*/

        if (dynSeg != null && got != null && base != 0uL) {
            got += base

            if (os.abi.core is MipsCore) {

                val gotsym = dynSeg[MipsDynamicSectionTag.DT_MIPS_GOTSYM.id]!!
                val local_gotno = dynSeg[MipsDynamicSectionTag.DT_MIPS_LOCAL_GOTNO.id]!!

                val lastSym = mapDynamicSymbols.last().ind
                val lastGotInd = local_gotno + lastSym - gotsym

//                val maxGotSym = undSyms!!.filter { it.ind >= gotsym }.maxOf { it.ind }

                for (i in 0uL..lastGotInd) {
                    val got_addr = got + i * Datatype.DWORD.bytes
                    val data = os.abi.readInt(got_addr)
                    os.abi.writeInt(got_addr, data + base)
                }
            } else if (os.abi.core !is AARMCore && base != 0uL)
                TODO("CHECK THIS **** OUT")

        }

        when (os.abi.core) {
            is MipsCore -> elf.staticRelocations.forEach {
                if (it.type == MipsRelocationType.R_MIPS_REL32.id) {
                    os.abi.writeInt(it.vaddr + base, it.value + base)
                }
            }
            is AARMCore -> elf.staticRelocations.forEach {
                if (it.type == ArmRelocationType.R_ARM_RELATIVE.id) {
                    os.abi.writeInt(it.vaddr + base, it.value + base)
                }
                if (it.type == ArmRelocationType.R_ARM_ABS32.id) {
                    os.abi.writeInt(it.vaddr + base, mapDynamicSymbols[it.sym].address)
                }
            }
            is PPCCore -> {
                require (elf.staticRelocations.isEmpty()) { "Not implemented" }
            }
            else -> TODO("CHECK THIS **** OUT")
        }

        val memoryName = os.currentMemory.name
        val elfBase = if (base == 0uL) regions.first().vaddr else base // TODO: not fully correct...
        elfData[elfName] = ElfData(memoryName, elfName, elfBase, mapSymbols, mapDynamicSymbols, undSyms, dynSeg, jumpSlots, globDat, got.opt)

        linkReferences()
    }


    override fun load(filename: String) {
        val executableName = File(filename).name

        val elf = ElfLoader.fromPath(filename, true)
        val base = if (elf.isSharedObject)
            loadLibrary(elf, executableName)
        else {
            val regions = elf.relocatedRegions

            regions.forEach {
                os.currentMemory.allocate(it.name, it.vaddr, it.size, it.access.asAccess, it.data)
            }

            0uL
        }

        os.currentProcess.initProcess(elf.entryPoint + base)

        referencesProcessing(executableName, elf, base)
    }

    fun loadLibrary(elf: ElfLoader, libName: String): ULong {
        val regions = elf.relocatedRegions
        val size = regions.maxByOrNull { it.vaddr }!!.end

        val freeRange = os.currentMemory.freeRangeBySize(size)
        check(freeRange != null) { "Can't allocate memory for loading $libName" }

        log.config { "'$libName' will be rebased to ${freeRange.first.hex8}..${(freeRange.first + size).hex8}" }
        val base = freeRange.first

        regions.forEach {
            os.currentMemory.allocate("$libName:${it.name}", it.vaddr + base, it.size, it.access.asAccess, it.data)
        }

        return base
    }

    override fun loadLibrary(filename: String) {
        val libName = File(filename).name

        // Yes, we don't use force specific base address
        val elf = ElfLoader.fromPath(filename, true)
        check (elf.elfFile.type == ElfType.ET_DYN.id) { "ELF file isn't a shared object" }

        val base = loadLibrary(elf, libName)

        referencesProcessing(libName, elf, base)
    }
}