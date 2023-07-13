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
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data.interfaces.LinuxThreadInfo
import ru.inforion.lab403.kopycat.experimental.linux.common.LinuxReturn
import ru.inforion.lab403.kopycat.experimental.linux.common.buildLinuxFileControl
import ru.inforion.lab403.kopycat.experimental.linux.common.toLinuxReturn
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.queued.x86funQueuedUtils
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.queued.x86funQueuedUtilsData
import ru.inforion.lab403.kopycat.interfaces.inq
import java.io.File

class LinuxQueuedFilesystemWrite<T>(
    val raw: T,
    val queued: x86funQueuedUtils,
    val sourcePath: String,
    val destinationPath: String,
    val bucketSize: Int = 1024,
    val threadInfoBlock: () -> LinuxThreadInfo?
) where T : LinuxVfsRWCapturableApi, T : LinuxFilpCapturableApi {
    val stream = File(sourcePath).inputStream()

    fun start() {
        fileOpen()
    }

    val availablePc = setOf(raw.PTR_FILP_CLOSE, raw.PTR_FILP_OPEN, raw.PTR_VFS_WRITE, raw.PTR_VFS_READ)

    val onOpen = mutableListOf<LinuxQueuedFilesystemWrite<T>.() -> Unit>()
    val onClose = mutableListOf<LinuxQueuedFilesystemWrite<T>.() -> Unit>()

    private fun fileOpen() {
        queued.functionsQueue.push(
            x86funQueuedUtilsData(
                isReadyToCall = {
                    threadInfoBlock() != null && x86.pc in availablePc
                },
                capturable = {
                    object : Capturable<Unit> {
                        val threadInfo = KernelThreadInfoHolder(threadInfoBlock)
                        val filpOpen = raw.filpOpenCapturable(
                            destinationPath,
                            buildLinuxFileControl { WRONLY; CREAT; TRUNC },
                            0b111_111_111
                        )

                        override fun initialize() {
                            onOpen.forEach { it(this@LinuxQueuedFilesystemWrite) }

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
                                is LinuxReturn.Success -> filePartWrite(
                                    filePointer.rawValue,
                                    stream.readAvailableBytes(bucketSize),
                                    fileIterator = 0uL,
                                )

                                else -> {
                                    onClose.forEach { it(this@LinuxQueuedFilesystemWrite) }
                                    println("ERROR: file pointer = 0x${filePointer.rawValue.hex}")
                                }
                            }

                        }
                    }
                },
                functionName = "filp_open for <${destinationPath}>"
            )
        )
    }

    private fun filePartWrite(
        filePointer: ULong,
        content: ByteArray,
        fileIterator: ULong
    ) {
        queued.functionsQueue.push(
            x86funQueuedUtilsData(
                isReadyToCall = {
                    threadInfoBlock() != null && x86.pc in availablePc
                },
                capturable = {
                    object : Capturable<Unit> {
                        val threadInfo = KernelThreadInfoHolder(threadInfoBlock)
                        val vfsWrite = raw.vfsWriteCapturable(
                            filePointer,
                            content,
                            fileIterator,
                        )

                        override fun initialize() {
                            threadInfo.saveAddrLimit()
                            vfsWrite.initialize()
                        }

                        override fun body() {
                            x86.pc = raw.PTR_VFS_WRITE
                        }

                        override fun destroy() {
                            val newIterator = x86.inq(vfsWrite.pointer.address)

                            vfsWrite.destroy()
                            threadInfo.restoreAddrLimit()

                            if (stream.available() != 0) {
                                filePartWrite(
                                    filePointer,
                                    stream.readAvailableBytes(bucketSize),
                                    fileIterator = newIterator,
                                )
                            } else {
                                fileClose(filePointer)
                            }
                        }
                    }
                },
                functionName = "vfs_write for <fp=${destinationPath}>"
            )
        )
    }

    private fun fileClose(filePointer: ULong) {
        queued.functionsQueue.push(
            x86funQueuedUtilsData(
                isReadyToCall = {
                    threadInfoBlock() != null && x86.pc in availablePc
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

                            onClose.forEach { it(this@LinuxQueuedFilesystemWrite) }
                        }
                    }
                },
                functionName = "filp_close for <fp=${destinationPath}>"
            )
        )
    }
}
