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
package ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.queued

import org.jetbrains.kotlin.backend.common.push
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.readAvailableBytes
import ru.inforion.lab403.kopycat.experimental.common.capturable.Capturable
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.interfaces.LinuxVfsRWCapturableApi
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.interfaces.LinuxFilpCapturableApi
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.interfaces.LinuxSysFilesystem2CapturableApi
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data.interfaces.LinuxThreadInfo
import ru.inforion.lab403.kopycat.experimental.linux.common.LinuxReturn
import ru.inforion.lab403.kopycat.experimental.linux.common.buildLinuxFileControl
import ru.inforion.lab403.kopycat.experimental.linux.common.toLinuxReturn
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.queued.x86funQueuedUtils
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.queued.x86funQueuedUtilsData
import ru.inforion.lab403.kopycat.interfaces.inq
import java.io.File

class LinuxQueuedMkdir<T>(
    val raw: T,
    val queued: x86funQueuedUtils,
    val path: String,
    val threadInfoBlock: () -> LinuxThreadInfo?
) where T : LinuxSysFilesystem2CapturableApi {
    val availablePc = setOf(raw.PTR_SYS_MKDIR)

    fun start() {
        mkdir()
    }

    private fun mkdir() {
        queued.functionsQueue.push(
            x86funQueuedUtilsData(
                isReadyToCall = {
                    x86.isRing0 && !x86.cpu.flags.ifq && threadInfoBlock() != null
                },
                capturable = {
                    object : Capturable<Unit> {
                        val sysMkdir = raw.sysMkdirCapturable(
                            path,
                            0b111_111_111,
                        )
                        val threadInfo = KernelThreadInfoHolder(threadInfoBlock)

                        override fun initialize() {
                            threadInfo.saveAddrLimit()
                            sysMkdir.initialize()
                        }

                        override fun body() {
                            x86.pc = raw.PTR_SYS_MKDIR
                        }

                        override fun destroy() {
                            val filePointer = abi.getResult().toLinuxReturn()
                            sysMkdir.destroy()
                            threadInfo.restoreAddrLimit()

                            when (filePointer) {
                                is LinuxReturn.Success -> {}

                                else -> {
                                    println("ERROR: mkdir result = 0x${filePointer.rawValue.hex}")
                                }
                            }

                        }
                    }
                },
                functionName = "sys_mkdir for <${path}>"
            )
        )
    }
}
