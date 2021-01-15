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
@file:Suppress("FunctionName")

package ru.inforion.lab403.kopycat.veos.api.impl

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIVariable
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.misc.toStdCErrno
import ru.inforion.lab403.kopycat.veos.ports.posix.*
import ru.inforion.lab403.kopycat.veos.ports.signal.sigaction
import ru.inforion.lab403.kopycat.veos.ports.signal.sigset_t
import ru.inforion.lab403.kopycat.veos.ports.sysdep.ASystemDep.Companion.deps


/**
 * Implementation of grp.h of C standard library
 */
class SignalAPI constructor(os: VEOS<*>) : API(os) {
    companion object {
        @Transient val log = logger(FINE)
    }

    init {
        type(ArgType.Pointer) { _, it -> sigset_t(os.sys, it) }
        type(ArgType.Pointer) { _, it -> sys.deps.toSigaction(os.sys, it) }
    }

    val errno = APIVariable.int(os, "errno")

    override fun setErrno(error: Exception?) {
        errno.allocated.value = error?.toStdCErrno(ra)?.id ?: PosixError.ESUCCESS.id
    }

    // REVIEW: #include <signal.h>
    // POSIX.1-2001
    // https://linux.die.net/man/3/sigemptyset
    val sigemptyset = nullsub("sigemptyset") // REVIEW: should not be nullsub [Signal subsystem]
    // POSIX.1-2001
    // https://linux.die.net/man/3/sigemptyset
    val sigaddset = nullsub("sigaddset")  // REVIEW: should not be nullsub [Signal subsystem]

    // POSIX.1-2001
    // https://linux.die.net/man/3/sigfillset
    @APIFunc
    fun sigfillset(set: sigset_t): Int {
        set.fill()
        return 0
    }
    // POSIX.1-2001, SVr4
    // https://linux.die.net/man/2/sigaction
    @APIFunc
    fun sigaction(signum: Int, act: sigaction, oldact: sigaction): Int {
//        log.severe { "High probability of fail: sigset_t offsets" }
        log.severe { "[0x${ra.hex8}] sigaction(signum=${signum} act=$act oldact=$oldact) - not implemented" }
        if (oldact.isNotNull) {
            oldact.handler = 0L
            oldact.mask = 0L
        }
        return 0
    }
}