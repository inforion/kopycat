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
package ru.inforion.lab403.kopycat.interactive.wsrpc

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.wsrpc.annotations.WebSocketRpcMethod
import ru.inforion.lab403.common.wsrpc.endpoints.SequenceEndpoint.Companion.toSequenceEndpoint
import ru.inforion.lab403.common.wsrpc.interfaces.WebSocketRpcEndpoint
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.library.builders.text.*
import ru.inforion.lab403.kopycat.library.types.LibraryInfo
import ru.inforion.lab403.kopycat.serializer.Serializer
import java.math.BigInteger
import kotlin.reflect.full.functions

class KopycatEndpoint constructor(
    private val kopycat: Kopycat,
    override val name: String = "Kopycat"
) : WebSocketRpcEndpoint {
    companion object {
        @Transient val log = logger()
    }

    private val modules = mutableListOf<Module>()

    private val String.moduleOrThrow get() = modules.first { it.name == this }

    private val String.moduleOrNull get() = modules.find { it.name == this }

    private inline val registryOrThrow get() = kopycat.registry.sure { "registry wasn't initialized" }

    private fun List<LibraryInfo>.getContentInfo() = associate { library ->
        library.name to library.modules.associate { module ->
            module.name to module.factories
        }
    }

    // Registry functions

    @WebSocketRpcMethod
    fun modules() = modules.toSequenceEndpoint()

    @WebSocketRpcMethod
    fun getAvailableTopModules() = registryOrThrow.getAvailableTopModules().getContentInfo()

    @WebSocketRpcMethod
    fun getAvailableAllModules() = registryOrThrow.getAvailableAllModules().getContentInfo()

    @WebSocketRpcMethod
    fun module(parent: String?, name: String, config: PluginConfig?) = if (config != null) {
        registryOrThrow.json(parent?.moduleOrNull, name, config)
    } else {
        Module(parent?.moduleOrNull, name)
    }.also { modules.add(it) }.name

    @WebSocketRpcMethod
    fun delete(name: String, hierarchy: Boolean) {
        val top = name.moduleOrThrow.also { it.terminate() }
        modules.removeIf { it.name == top.name }
        if (hierarchy) {
            val children = top.getAllComponents().map { it.name }.toMutableSet()
            // remove method returns true if value was removed from the Set and true result in removing it from modules
            modules.removeAll { children.remove(it.name) }
            if (children.isNotEmpty()) {
                log.warning { "Not all children of '${top.name}' was in REST modules: $children" }
            }
        }
    }

    @WebSocketRpcMethod
    fun deleteAll() {
        while (modules.isNotEmpty()) {
            delete(modules.first().name, true)
        }
    }

    @WebSocketRpcMethod
    fun instantiate(parent: String?, module: ModuleConfig): String {
        val parentModule = parent ifNotNull { moduleOrNull }
        return module.create(registryOrThrow, parentModule).also { modules.add(it) }.name
    }

    // Kopycat functions

    @WebSocketRpcMethod
    fun bus(designator: String, bus: BusConfig) = bus.create(designator.moduleOrThrow).name

    @WebSocketRpcMethod
    fun port(designator: String, port: PortConfig) = port.create(designator.moduleOrThrow).name

    @WebSocketRpcMethod
    fun connect(designator: String, connection: ConnectionConfig) =
            connection.create(designator.moduleOrThrow)

    @WebSocketRpcMethod
    fun step() = kopycat.step()

    @WebSocketRpcMethod
    fun start() = kopycat.start()

    @WebSocketRpcMethod
    fun halt() = kopycat.halt()

    @WebSocketRpcMethod
    fun memLoad(address: ULong, size: Int, ss: Int = 0, compress: Boolean = false): ByteArray {
        val data = kopycat.memLoad(address, size, ss)
        return if (compress) data.gzip() else data
    }

    @WebSocketRpcMethod
    fun memStore(address: ULong, data: ByteArray, ss: Int = 0, compress: Boolean = false) {
        val array = if (compress) data.ungzip() else data
        kopycat.memStore(address, array, ss)
    }

    @WebSocketRpcMethod
    fun regRead(index: Int) = kopycat.regRead(index)

    @WebSocketRpcMethod
    fun regWrite(index: Int, value: BigInteger) = kopycat.regWrite(index, value)

    @WebSocketRpcMethod
    fun pcRead() = kopycat.pcRead()

    @WebSocketRpcMethod
    fun pcWrite(value: ULong) = kopycat.pcWrite(value)

    @WebSocketRpcMethod
    fun save(name: String, comment: String) = kopycat.save(name, comment)

    @WebSocketRpcMethod
    fun load(name: String) = kopycat.load(name)

    @WebSocketRpcMethod
    fun reset() = kopycat.reset()

    @WebSocketRpcMethod
    fun close() = kopycat.close()

    @WebSocketRpcMethod
    fun exit() = kopycat.exit()

    @WebSocketRpcMethod
    fun execute(module: String, name: String, args: Array<Any?>): Boolean {
        val actual = module.moduleOrThrow
        val function = actual::class.functions.first { it.name == name }
        function.call(actual, *args)
        return true
    }

    @WebSocketRpcMethod
    fun open(top: String, gdbPort: Int?, gdbBinaryProto: Boolean, traceable: Boolean): Boolean {
        val gdb = gdbPort ifNotNull { GDBServer(this, binaryProtoEnabled = gdbBinaryProto) }
        kopycat.open(top.moduleOrThrow, gdb, traceable)
        return kopycat.isTopModulePresented
    }

    @WebSocketRpcMethod
    fun isRunning() = kopycat.isRunning

    @WebSocketRpcMethod
    fun isTopModulePresented() = kopycat.isTopModulePresented

    @WebSocketRpcMethod
    fun isGdbServerPresented() = kopycat.isGdbServerPresented

    @WebSocketRpcMethod
    fun gdbClientProcessing() = kopycat.gdbClientProcessing

    @WebSocketRpcMethod
    fun getSnapshotMetaInfo(path: String) = Serializer.getMetaInfo(kopycat.snapshotsDir / path)
            ?: error("Snapshot has no meta info!")
}