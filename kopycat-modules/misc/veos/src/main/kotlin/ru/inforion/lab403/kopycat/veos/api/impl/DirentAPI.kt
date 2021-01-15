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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIVariable
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.misc.toStdCErrno
import ru.inforion.lab403.kopycat.veos.api.pointers.CharPointer
import ru.inforion.lab403.kopycat.veos.ports.dirent.*
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixError
import java.nio.file.attribute.BasicFileAttributes

class DirentAPI constructor(os: VEOS<*>) : API(os) {
    companion object {
        @Transient val log = logger(WARNING)
    }

    init {
        type(ArgType.Pointer) { _, it -> DIR(os.sys, it) }

        ret<DIR> { APIResult.Value(it.address) }

        ret<dirent> { APIResult.Value(it.address) }
        ret<dirent64> { APIResult.Value(it.address) }
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/3/opendir
    @APIFunc
    fun opendir(name: CharPointer): DIR {
        log.fine { "[0x${ra.hex8}] opendir(name='${name.string}')" }

        return nothrow(DIR.nullPtr(os.sys)) {
            val fileDescriptor = os.filesystem.openDir(name.string)
            DIR.allocate(os.sys).apply {
                fd = fileDescriptor
            }
        }
    }

    private val dirents = mutableMapOf<Int, dirent>()

    private val dirents64 = mutableMapOf<Int, dirent64>()

    private val BasicFileAttributes.inode get() = fileKey().toString().substringBetween("ino=", ")").toULong()

    private val BasicFileAttributes.type get() = when {
        isDirectory -> DT_DIR
        isRegularFile -> DT_REG
        isSymbolicLink -> DT_LNK
        else -> DT_UNKNOWN
    }

    // SVr4, POSIX.1-2001, 4.3BSD
    // https://linux.die.net/man/3/readdir
    @APIFunc
    fun readdir(dirp: DIR): dirent {
        return nothrow(dirent.nullPtr(sys)) {
            val record = os.filesystem.readDir(dirp.fd) ?: return@nothrow dirent.nullPtr(sys)
            val dent = dirents.getOrPut(dirp.fd) { dirent.allocate(os.sys) }

            log.fine { "[0x${ra.hex8}] readdir(dirp=0x${dirp}) -> ${record.name} in ${os.currentProcess}" }

            dent.apply {
                d_name = record.name.convertToBytes() + 0x00
                // FIXME: Provide other cases for d_type
                d_type = record.attributes.type.asByte
                d_reclen = dirent.sizeOf.asShort
                d_ino = record.attributes.inode.asInt
            }
        }
    }

    @APIFunc
    fun readdir64(dirp: DIR): dirent64 {
        return nothrow(dirent64.nullPtr(sys)) {
            val record = os.filesystem.readDir(dirp.fd) ?: return@nothrow dirent64.nullPtr(sys)
            val dent64 = dirents64.getOrPut(dirp.fd) { dirent64.allocate(os.sys) }

            log.fine { "[0x${ra.hex8}] readdir64(dirp=0x${dirp}) -> ${record.name} in ${os.currentProcess}" }

            dent64.apply {
                d_name = record.name.convertToBytes() + 0x00
                // FIXME: Provide other cases for d_type
                d_type = record.attributes.type.asByte
                d_reclen = dirent64.sizeOf.asShort
                d_ino = record.attributes.inode
            }
        }
    }

    @APIFunc
    fun closedir(dirp: DIR): Int {
        log.fine { "[0x${ra.hex8}] closedir(dirp=0x${dirp}) in ${os.currentProcess}" }

        return nothrow(-1) {
            os.filesystem.closeDir(dirp.fd)
            dirents.remove(dirp.fd)?.free()
            dirp.free()
            0
        }
    }

    val errno = APIVariable.int(os, "errno")

    override fun setErrno(error: Exception?) {
        errno.allocated.value = error?.toStdCErrno(ra)?.id ?: PosixError.ESUCCESS.id
    }
}