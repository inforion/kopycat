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
package ru.inforion.lab403.kopycat.veos.kernel

import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.extensions.div
import ru.inforion.lab403.common.extensions.getResourceUrl
import ru.inforion.lab403.common.extensions.string
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.modules.memory.VirtualMemory
import ru.inforion.lab403.kopycat.modules.veos.ARMApplication
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.filesystems.StreamFile
import ru.inforion.lab403.kopycat.veos.filesystems.impl.FileSystem
import java.io.ByteArrayOutputStream
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class StdioTest {

    companion object {
        val log = logger()
    }

    fun runTest(executable: String) {
        val extension = executable.split('.').last()
        val pathToExecutable = "stdio" / extension / executable
        val root = getResourceUrl(pathToExecutable).toURI().resolve(".").path

        val top = ARMApplication(null, "top", root, executable)

        VirtualMemory.log.level = INFO
        val kopycat = Kopycat(null).also { it.open(top, null, false) }
        top.veos.ioSystem.close(FileSystem.STDOUT_INDEX)
        val stream = ByteArrayOutputStream(100 * 1024)
        top.veos.ioSystem.reserve(StreamFile(stream), FileSystem.STDOUT_INDEX)

        kopycat.use { _ -> kopycat.run { step, core -> true } }

        var failed = false

        stream.toByteArray()
            .string
            .lines().filter { it.isNotEmpty() }
            .forEach {
                if (it.startsWith("[")) {
                    log.fine { it }
                } else {
                    log.severe { it }
                    failed = true
                }
            }

        assertTrue { top.veos.state == VEOS.State.Exit }
        assertFalse(failed)
    }

    @Test fun fopenARMTests() = runTest("fopen.arm")
    @Test fun fcloseARMTests() = runTest("fclose.arm")
    @Test fun fgetcARMTests() = runTest("fgetc.arm")
    @Test fun fgetsARMTests() = runTest("fgets.arm")
    @Test fun fputcARMTests() = runTest("fputc.arm")
    @Test fun fputsARMTests() = runTest("fputs.arm")
    @Test fun getcARMTests() = runTest("getc.arm")
    @Test fun putcARMTests() = runTest("putc.arm")
}