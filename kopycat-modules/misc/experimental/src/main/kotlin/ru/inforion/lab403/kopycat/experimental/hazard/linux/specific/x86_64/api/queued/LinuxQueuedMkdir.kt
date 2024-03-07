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
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.auxiliary.capturable.Capturable
import ru.inforion.lab403.kopycat.runtime.funcall.FunQueuedUtils
import ru.inforion.lab403.kopycat.runtime.funcall.FunQueuedUtilsData
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data.interfaces.LinuxThreadInfo
import ru.inforion.lab403.kopycat.experimental.linux.api.interfaces.LinuxSysMkdirCapturableApi
import ru.inforion.lab403.kopycat.experimental.linux.common.LinuxReturn
import ru.inforion.lab403.kopycat.experimental.linux.common.toLinuxReturn
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class LinuxQueuedMkdir<T>(
    val raw: T,
    val queued: FunQueuedUtils,
    val path: String,
    val threadInfoBlock: () -> LinuxThreadInfo?
) where T : LinuxSysMkdirCapturableApi {
    companion object {
        @Transient
        val log = logger(INFO)
    }

    val availablePc = setOf(raw.PTR_SYS_MKDIR)

    fun start() {
        mkdir()
    }

    private fun mkdir() {
        queued.functionsQueue.push(
            FunQueuedUtilsData(
                isReadyToCall = {
                    // TODO: crutch
                    (core as x86Core).isRing0 && !(core as x86Core).cpu.flags.ifq && threadInfoBlock() != null
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
                            abi.call(raw.PTR_SYS_MKDIR)
                        }

                        override fun destroy() {
                            val filePointer = abi.getResult().toLinuxReturn()
                            sysMkdir.destroy()
                            threadInfo.restoreAddrLimit()

                            when (filePointer) {
                                is LinuxReturn.Success -> {}

                                else -> {
                                    log.severe { "mkdir result = 0x${filePointer.rawValue.hex}" }
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
