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
package ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.queued

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.auxiliary.capturable.Capturable
import ru.inforion.lab403.kopycat.runtime.funcall.FunQueuedUtils
import ru.inforion.lab403.kopycat.runtime.funcall.FunQueuedUtilsData
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data.interfaces.LinuxThreadInfo
import ru.inforion.lab403.kopycat.experimental.linux.api.interfaces.LinuxFilpCapturableApi
import ru.inforion.lab403.kopycat.experimental.linux.api.interfaces.LinuxVfsRWCapturableApi
import ru.inforion.lab403.kopycat.experimental.linux.common.LinuxReturn
import ru.inforion.lab403.kopycat.experimental.linux.common.file.ILinuxFileControl
import ru.inforion.lab403.kopycat.experimental.linux.common.toLinuxReturn
import ru.inforion.lab403.kopycat.interfaces.inq
import java.io.File

class LinuxQueuedFilesystemRead<T>(
    val raw: T,
    val queued: FunQueuedUtils,
    val control: ILinuxFileControl,
    val sourcePath: String,
    val destinationPath: String,
    val bucketSize: ULong = 1024uL,
    val threadInfoBlock: () -> LinuxThreadInfo?
) where T : LinuxVfsRWCapturableApi, T : LinuxFilpCapturableApi {
    companion object {
        @Transient
        val log = logger(INFO)
    }

    val stream = File(destinationPath).outputStream()
    val availablePc = setOf(raw.PTR_FILP_CLOSE, raw.PTR_FILP_OPEN, raw.PTR_VFS_WRITE, raw.PTR_VFS_READ)

    fun start() {
        fileOpen()
    }

    private fun fileOpen() {
        queued.functionsQueue.add(
            FunQueuedUtilsData(
                isReadyToCall = {
                    core.pc in availablePc && threadInfoBlock() != null
                },
                capturable = {
                    object : Capturable<Unit> {
                        val threadInfo = KernelThreadInfoHolder(threadInfoBlock)
                        val filpOpen = raw.filpOpenCapturable(
                            sourcePath,
                            control.run { RDONLY },
                            0b110_110_110
                        )

                        override fun initialize() {
                            threadInfo.saveAddrLimit()
                            filpOpen.initialize()
                        }

                        override fun body() {
                            abi.call(raw.PTR_FILP_OPEN)
                        }

                        override fun destroy() {
                            val filePointer = abi.getResult().toLinuxReturn()
                            filpOpen.destroy()
                            threadInfo.restoreAddrLimit()

                            when (filePointer) {
                                is LinuxReturn.Success -> filePartRead(
                                    filePointer.rawValue,
                                    fileIterator = 0uL,
                                )

                                else -> log.severe { "File pointer = 0x${filePointer.rawValue.hex}" }
                            }

                        }
                    }
                },
                functionName = "filp_open for <${sourcePath}>"
            )
        )
    }

    private fun filePartRead(
        filePointer: ULong,
        fileIterator: ULong
    ) {
        queued.functionsQueue.add(
            FunQueuedUtilsData(
                isReadyToCall = {
                    core.pc in availablePc && threadInfoBlock() != null
                },
                capturable = {
                    object : Capturable<Unit> {
                        val threadInfo = KernelThreadInfoHolder(threadInfoBlock)
                        val vfsWrite = raw.vfsReadCapturable(
                            filePointer,
                            bucketSize,
                            fileIterator,
                        )

                        override fun initialize() {
                            threadInfo.saveAddrLimit()
                            vfsWrite.initialize()
                        }

                        override fun body() {
                            abi.call(raw.PTR_VFS_READ)
                        }

                        override fun destroy() {
                            val readSize = abi.getResult()
                            val newIterator = core.inq(vfsWrite.pointer.address)
                            val data = core.load(vfsWrite.alloca.address, vfsWrite.alloca.size.int)

                            stream.write(data.sliceArray(0 until readSize.int))

                            vfsWrite.destroy()
                            threadInfo.restoreAddrLimit()

                            if (readSize == bucketSize) {
                                filePartRead(
                                    filePointer,
                                    fileIterator = newIterator,
                                )
                            } else {
                                fileClose(filePointer)
                            }
                        }
                    }
                },
                functionName = "vfs_read for <fp=${sourcePath}>"
            )
        )
    }

    private fun fileClose(filePointer: ULong) {
        queued.functionsQueue.add(
            FunQueuedUtilsData(
                isReadyToCall = {
                    core.pc in availablePc && threadInfoBlock() != null
                },
                capturable = {
                    object : Capturable<Unit> {
                        val threadInfo = KernelThreadInfoHolder(threadInfoBlock)
                        val filpClose = raw.filpCloseCapturable(
                            filePointer,
                        )

                        override fun initialize() {
                            threadInfo.saveAddrLimit()
                            filpClose.initialize()
                        }

                        override fun body() {
                            abi.call(raw.PTR_FILP_CLOSE)
                        }

                        override fun destroy() {
                            filpClose.destroy()
                            threadInfo.saveAddrLimit()
                            stream.close()
                        }
                    }
                },
                functionName = "filp_close for <fp=${sourcePath}>"
            )
        )
    }
}
