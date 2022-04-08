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

import ru.inforion.lab403.common.wsrpc.WebSocketRpcServer
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry

object StartKopycatRemote {
    /**
     * Инициализация симулятора
     *
     * @param regCfgLine путь к реестру библиотек Kopycat
     * @param snapshotDir путь к директории для сохранения снапшотов
     */
    fun kopycat(regCfgLine: String?, snapshotDir: String): Kopycat {
        val registry = ModuleLibraryRegistry.create(regCfgLine, null)
        return Kopycat(registry).apply { setSnapshotsDirectory(snapshotDir) }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val kopycat = kopycat(null, "temp")

        val kopycatRPC = KopycatEndpoint(kopycat)

        val server = WebSocketRpcServer("localhost", 6969).apply { register(kopycatRPC) }

        server.start()
    }
}