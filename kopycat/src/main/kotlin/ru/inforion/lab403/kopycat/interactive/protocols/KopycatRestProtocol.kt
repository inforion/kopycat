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
package ru.inforion.lab403.kopycat.interactive.protocols

import io.javalin.Javalin
import io.javalin.core.plugin.Plugin
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.div
import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.common.extensions.applyRoutes
import ru.inforion.lab403.common.extensions.getAny
import ru.inforion.lab403.common.extensions.postAny
import ru.inforion.lab403.common.extensions.postVoid
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.library.builders.text.BusConfig
import ru.inforion.lab403.kopycat.library.builders.text.ConnectionConfig
import ru.inforion.lab403.kopycat.library.builders.text.PortConfig
import ru.inforion.lab403.kopycat.serializer.Serializer

class KopycatRestProtocol(private val kopycat: Kopycat, val modules: MutableList<Module>): Plugin {
    companion object {
        val log = logger(FINE)
    }

    internal data class MemoryLoadInfo(val address: Long, val size: Int, val ss: Int)
    internal data class MemoryStoreInfo(val address: Long, val data: String, val ss: Int)
    internal data class RegisterReadInfo(val index: Int)
    internal data class RegisterWriteInfo(val index: Int, val value: Long)
    internal data class PcWriteInfo(val value: Long)
    internal data class OpenInfo(val top: String, val gdbPort: Int?, val gdbBinaryProto: Boolean, val traceable: Boolean)

    val name = "kopycat"

    override fun apply(app: Javalin) = app.applyRoutes {
        /**
         * {RU}
         * Пример тела и параметров запроса для bus
         *  {
         *      "name": "mem",
         *      "size": "BUS32"
         *  }
         *
         * Params:
         * {
         *      "designator": "device"
         * }
         *
         * @return имя шины
         * {RU}
         */
        postAny("$name/bus") {
            val designator = it.header("designator")!!
            val bus = it.bodyAsClass(BusConfig::class.java)
            log.finer { "bus(designator=$designator bus=$bus)" }
            val module = modules.first { it.name == designator }
            bus.create(module).name
        }

        postAny("$name/port") {
            val designator = it.header("designator")!!
            val port = it.bodyAsClass(PortConfig::class.java)
            log.finer { "port(designator=$designator port=$port)" }
            val module = modules.first { it.name == designator }
            port.create(module).name
        }

        /**
         * {RU}
         * Пример тела и параметров запроса для connect
         *
         * Body
         * [
         *      "test.ports.mem", // src
         *      "buses.mem",      // dst
         *      0                 // offset
         * ]
         *
         * Params:
         * {
         *      "designator": "device"
         * }
         * {RU}
         */
        postAny("$name/connect") {
            val designator = it.header("designator")!!
            val connection = it.bodyAsClass(ConnectionConfig::class.java)
            log.finer { "connect(designator=$designator connection=$connection)" }
            val module = modules.first { it.name == designator }
            connection.create(module)
        }

        /**
         * {RU}
         * @return успешность выполнения шага (true/false)
         * {RU}
         */
        postAny("$name/step") {
            log.finer { "step()" }
            kopycat.step()
        }

        postVoid("$name/start") {
            log.finer { "start()" }
            kopycat.start()
        }

        postVoid("$name/halt") {
            log.finer { "halt()" }
            kopycat.halt()
        }

        /**
         * {RU}
         * Пример тела запроса для memLoad
         * {
         *     "address": 0x8000000,
         *     "size":    8,
         *     "ss":      0
         * }
         * @return массив байтов в виде строки
         * {RU}
         */
        postAny("$name/memLoad") {
            val params = it.bodyAsClass(MemoryLoadInfo::class.java)
            log.finer { "memLoad(params=$params)" }
            kopycat.memLoad(params.address, params.size, params.ss).hexlify()
        }

        /**
         * {RU}
         * Пример тела запроса для memStore
         * {
         *     "address": 0x8000000,
         *     "data":    "00000000bbaa053c0001a534ffff001000000000",
         *     "ss":      0
         * }
         * {RU}
         */
        postVoid("$name/memStore") {
            val params = it.bodyAsClass(MemoryStoreInfo::class.java)
            log.finer { "memStore(params=$params)" }
            kopycat.memStore(params.address, params.data.unhexlify(), params.ss)
        }

        postAny("$name/regRead") {
            val params = it.bodyAsClass(RegisterReadInfo::class.java)
            log.finer { "regRead(params=$params)" }
            kopycat.regRead(params.index)
        }

        postVoid("$name/regWrite") {
            val params = it.bodyAsClass(RegisterWriteInfo::class.java)
            log.finer { "regWrite(params=$params)" }
            kopycat.regWrite(params.index, params.value)
        }

        postAny("$name/pcRead") { kopycat.pcRead() }

        postVoid("$name/pcWrite") {
            val params = it.bodyAsClass(PcWriteInfo::class.java)
            log.finer { "pcWrite(params=$params)" }
            kopycat.pcWrite(params.value)
        }

        /**
         * {RU}
         * @return успешно или нет был сделан snapshot
         * {RU}
         */
        postAny("$name/save") {
            val name = it.header("name")
            val comment = it.header("comment")
            log.finer { "save(name=$name comment=$comment)" }
            kopycat.save(name, comment)
        }

        /**
         * {RU}
         * @return успешно или нет был загружен snapshot
         * {RU}
         */
        postAny("$name/load") {
            val name = it.header("name")
            log.finer { "load(name=$name)" }
            kopycat.load(name)
        }

        postVoid("$name/reset") {
            log.finer { "reset()" }
            kopycat.reset()
        }

        postVoid("$name/close") {
            log.finer { "close()" }
            kopycat.close()
        }

        postVoid("$name/exit") {
            log.finer { "exit()" }
            kopycat.exit()
        }

        /**
         * {RU}
         * Пример тела запроса для open
         * {
         *     "top":             "top",
         *     "gdbPort":          5555,
         *     "gdbBinaryProto":   false,
         *     "traceable":        false
         * }
         * {RU}
         */
        postVoid("$name/open") {
            val params = it.bodyAsClass(OpenInfo::class.java)
            log.finer { "open(params=$params)" }
            val gdb = if (params.gdbPort != null) GDBServer(params.gdbPort, true, params.gdbBinaryProto) else null
            val top = modules.first { module -> module.name == params.top }
            kopycat.open(top, params.traceable, gdb)
            kopycat.isTopModulePresented
        }

        getAny("$name/isRunning") {
            log.finer { "isRunning()" }
            kopycat.isRunning
        }

        getAny("$name/isTopModulePresented") {
            log.finer { "isTopModulePresented()" }
            kopycat.isTopModulePresented
        }

        getAny("$name/isGdbServerPresented") {
            log.finer { "isGdbServerPresented()" }
            kopycat.isGdbServerPresented
        }

        getAny("$name/gdbClientProcessing") {
            log.finer { "gdbClientProcessing()" }
            kopycat.gdbClientProcessing
        }

        getAny("$name/getSnapshotMetaInfo") {
            val path = it.header("path")
            log.finer { "getSnapshotMetaInfo(path=$path)" }
            require(path != null) { "Snapshot path must be specified!" }
            Serializer.getMetaInfo(kopycat.snapshotsDir / path) ?: error("Snapshot has no meta info!")
        }
    }
}
