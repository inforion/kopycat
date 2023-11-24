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
import ru.inforion.lab403.kopycat.experimental.common.capturable.Capturable
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.interfaces.LinuxVfsRWCapturableApi
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.interfaces.LinuxFilpCapturableApi
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data.interfaces.LinuxThreadInfo
import ru.inforion.lab403.kopycat.experimental.linux.common.LinuxReturn
import ru.inforion.lab403.kopycat.experimental.linux.common.buildLinuxFileControl
import ru.inforion.lab403.kopycat.experimental.linux.common.toLinuxReturn
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.queued.x86funQueuedUtils
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.queued.x86funQueuedUtilsData
import ru.inforion.lab403.kopycat.interfaces.inq
import java.io.File

class LinuxQueuedFilesystemRead<T>(
    val raw: T,
    val queued: x86funQueuedUtils,
    val sourcePath: String,
    val destinationPath: String,
    val bucketSize: ULong = 1024uL,
    val threadInfoBlock: () -> LinuxThreadInfo?
) where T : LinuxVfsRWCapturableApi, T : LinuxFilpCapturableApi {
    val stream = File(destinationPath).outputStream()
    val availablePc = setOf(raw.PTR_FILP_CLOSE, raw.PTR_FILP_OPEN, raw.PTR_VFS_WRITE, raw.PTR_VFS_READ)

    fun start() {
        fileOpen()
    }

    private fun fileOpen() {
        queued.functionsQueue.push(
            x86funQueuedUtilsData(
                isReadyToCall = {
                    x86.pc in availablePc && threadInfoBlock() != null
                },
                capturable = {
                    object : Capturable<Unit> {
                        val threadInfo = KernelThreadInfoHolder(threadInfoBlock)
                        val filpOpen = raw.filpOpenCapturable(
                            sourcePath,
                            buildLinuxFileControl { RDONLY },
                            0b110_110_110
                        )

                        override fun initialize() {
                            threadInfo.saveAddrLimit()
                            filpOpen.initialize()
                        }

                        override fun body() {
                            x86.pc = raw.PTR_FILP_OPEN
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

                                else -> println("ERROR: file pointer = 0x${filePointer.rawValue.hex}")
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
        queued.functionsQueue.push(
            x86funQueuedUtilsData(
                isReadyToCall = {
                    x86.pc in availablePc && threadInfoBlock() != null
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
                            x86.pc = raw.PTR_VFS_READ
                        }

                        override fun destroy() {
                            val readSize = abi.getResult()
                            val newIterator = x86.inq(vfsWrite.pointer.address)
                            val data = x86.load(vfsWrite.alloca.address, vfsWrite.alloca.size.int)

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
        queued.functionsQueue.push(
            x86funQueuedUtilsData(
                isReadyToCall = {
                    x86.pc in availablePc && threadInfoBlock() != null
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
                            x86.pc = raw.PTR_FILP_CLOSE
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
