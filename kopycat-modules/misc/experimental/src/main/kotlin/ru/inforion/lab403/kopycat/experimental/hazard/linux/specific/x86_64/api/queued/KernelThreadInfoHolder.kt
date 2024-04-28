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
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data.interfaces.LinuxThreadInfo

class KernelThreadInfoHolder(val threadInfoBlock: () -> LinuxThreadInfo?) {
    companion object {
        @Transient
        val log = logger(INFO)
    }

    var oldAddrLimit: ULong = 0x0uL

    fun saveAddrLimit() {
        val kernelThreadInfo = threadInfoBlock
        oldAddrLimit = kernelThreadInfo()?.let { info ->
            log.info { "Current addr limit = 0x${info.addrLimit.hex}" }
            val addrLimit = info.addrLimit
            info.addrLimit = 0xFFFFFFFF_FFFFFFFFuL

            addrLimit
        } ?: throw IllegalStateException("no kernel thread info")
    }

    fun restoreAddrLimit() {
        threadInfoBlock()?.addrLimit = oldAddrLimit
    }
}
