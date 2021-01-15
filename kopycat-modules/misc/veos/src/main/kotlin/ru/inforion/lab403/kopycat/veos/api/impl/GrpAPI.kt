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
package ru.inforion.lab403.kopycat.veos.api.impl

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.ports.grp.group


/**
 * Implementation of grp.h of C standard library
 */
class GrpAPI constructor(os: VEOS<*>) : API(os) {
    companion object {
        @Transient val log = logger(FINE)
    }

    init {
        ret<group> { APIResult.Value(it.address) }
    }

    @DontAutoSerialize
    private val group_root by lazy {
        group.allocate(sys).apply {
            gr_name = sys.allocateAsciiString("root")
            gr_passwd = sys.allocateAsciiString("*")
            gr_gid = 0
            gr_mem = sys.allocatePointersArray(
                    sys.allocateAsciiString("root")
            )
        }
    }

    @DontAutoSerialize
    private val group_kc by lazy {
        group.allocate(sys).apply {
            gr_name = sys.allocateAsciiString("kc")
            gr_passwd = sys.allocateAsciiString("********")
            gr_gid = 20
            gr_mem = sys.allocatePointersArray(
                    sys.allocateAsciiString("kc")
            )
        }
    }

    // TODO: Make uid_t
    // TODO: Make it work right
    // https://linux.die.net/man/3/getpwuid
    @APIFunc
    fun getgrgid(gid: Int): group {
        log.warning { "[0x${ra.hex8}] getgrgid(gid=$gid) in ${os.currentProcess}" }
        return if (gid == 0) {
            group_root
        } else {
            group_kc.apply { gr_gid = gid }
        }
    }
}