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

import org.jetbrains.kotlin.backend.common.push
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.readAvailableBytes
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

class LinuxQueuedFilesystemWrite<T>(
    val raw: T,
    val queued: FunQueuedUtils,
    val control: ILinuxFileControl,
    val sourcePath: String,
    val destinationPath: String,
    val bucketSize: Int = 1024,
    val threadInfoBlock: () -> LinuxThreadInfo?
) where T : LinuxVfsRWCapturableApi, T : LinuxFilpCapturableApi {
    companion object {
        @Transient
        val log = logger(INFO)
    }

    val stream = File(sourcePath).inputStream()

    fun start() {
        fileOpen()
    }

    val availablePc = setOf(raw.PTR_FILP_CLOSE, raw.PTR_FILP_OPEN, raw.PTR_VFS_WRITE, raw.PTR_VFS_READ)

    val onOpen = mutableListOf<LinuxQueuedFilesystemWrite<T>.() -> Unit>()
    val onClose = mutableListOf<LinuxQueuedFilesystemWrite<T>.() -> Unit>()

    private fun fileOpen() {
        queued.functionsQueue.push(
            FunQueuedUtilsData(
                isReadyToCall = {
                    core.pc in availablePc && threadInfoBlock() != null
                },
                capturable = {
                    object : Capturable<Unit> {
                        val threadInfo = KernelThreadInfoHolder(threadInfoBlock)
                        val filpOpen = raw.filpOpenCapturable(
                            destinationPath,
                            control.run { WRONLY or CREAT or TRUNC },
                            0b111_111_111
                        )

                        override fun initialize() {
                            onOpen.forEach { it(this@LinuxQueuedFilesystemWrite) }

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
                                is LinuxReturn.Success -> filePartWrite(
                                    filePointer.rawValue,
                                    stream.readAvailableBytes(bucketSize),
                                    fileIterator = 0uL,
                                )

                                else -> {
                                    onClose.forEach { it(this@LinuxQueuedFilesystemWrite) }
                                    log.severe { "File pointer = 0x${filePointer.rawValue.hex}" }
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
            FunQueuedUtilsData(
                isReadyToCall = {
                    core.pc in availablePc && threadInfoBlock() != null
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
                            abi.call(raw.PTR_VFS_WRITE)
                        }

                        override fun destroy() {
                            when (val result = abi.getResult().toLinuxReturn()) {
                                is LinuxReturn.Success -> {log.info {
                                    "SUCCESS: vfs_write <${destinationPath}> Result = 0x${result.rawValue.hex}"
                                }}
                                else -> log.severe {
                                    "ERROR: vfs_write <${destinationPath}> Error = 0x${result.rawValue.hex}"
                                }
                            }

                            val newIterator = core.inq(vfsWrite.pointer.address)

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

                            onClose.forEach { it(this@LinuxQueuedFilesystemWrite) }
                        }
                    }
                },
                functionName = "filp_close for <fp=${destinationPath}>"
            )
        )
    }
}
