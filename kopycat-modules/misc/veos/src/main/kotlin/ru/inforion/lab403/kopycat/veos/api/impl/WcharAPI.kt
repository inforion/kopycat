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
import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIVariable
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.misc.toStdCErrno
import ru.inforion.lab403.kopycat.veos.api.pointers.BytePointer
import ru.inforion.lab403.kopycat.veos.api.pointers.CharPointer
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixError

class WcharAPI(os: VEOS<*>) : API(os) {
    companion object {
        @Transient val log = logger(FINE)
    }

    @APIFunc
    fun mbstowcs(dest: BytePointer, src: CharPointer, n: Int): Int {
        log.fine { "[0x${ra.hex8}] mbstowcs(dest=0x$dest src=${src.string} n=$n) in ${os.currentProcess}" }

        var wchar = src.string.toByteArray(Charsets.UTF_8)
        if (n > 0) wchar = wchar.copyOfRange(0, n.coerceAtMost(wchar.size))
        if (dest.isNotNull) {
            os.abi.writeBytes(dest.address, wchar)
        }

        return wchar.size
    }

    @APIFunc
    fun iswprint(wc: Int): Int {
        log.fine { "[0x${ra.hex8}] iswprint(wc=$wc) in ${os.currentProcess}" }
        return (!wc.toChar().isISOControl()).toInt()
    }

    @APIFunc
    fun wcswidth(s: BytePointer, n: Int): Int {
        log.fine { "[0x${ra.hex8}] wcswidth(s=$s n=$n) in ${os.currentProcess}" }
        val data = s.load(n)
        val string = data.toString(Charsets.UTF_8)
        return string.length.coerceAtMost(n)
    }

    @APIFunc
    fun __ctype_get_mb_cur_max(): Int {
        log.finer { "[0x${ra.hex8}] __ctype_get_mb_cur_max() -> 1 in ${os.currentProcess}" }
        return 1
    }

    val errno = APIVariable.int(os, "errno")

    override fun setErrno(error: Exception?) {
        errno.allocated.value = error?.toStdCErrno(ra)?.id ?: PosixError.ESUCCESS.id
    }
}