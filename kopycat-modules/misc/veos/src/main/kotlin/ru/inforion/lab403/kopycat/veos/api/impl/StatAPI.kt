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

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIVariable
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.misc.toStdCErrno
import ru.inforion.lab403.kopycat.veos.api.pointers.CharPointer
import ru.inforion.lab403.kopycat.veos.api.pointers.StructPointer
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONoSuchFileOrDirectory
import ru.inforion.lab403.kopycat.veos.filesystems.CommonDirectory
import ru.inforion.lab403.kopycat.veos.filesystems.CommonFile
import ru.inforion.lab403.kopycat.veos.filesystems.NullFile
import ru.inforion.lab403.kopycat.veos.filesystems.StreamFile
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.ISocketFile
import ru.inforion.lab403.kopycat.veos.ports.posix.*
import ru.inforion.lab403.kopycat.veos.ports.stat.stat
import ru.inforion.lab403.kopycat.veos.ports.sysdep.ASystemDep.Companion.deps
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermission.*
import java.util.concurrent.TimeUnit


/**
 * Implementation of grp.h of C standard library
 */
class StatAPI constructor(os: VEOS<*>) : API(os) {
    companion object {
        @Transient val log = logger(FINE)
    }

    init {
        type(ArgType.Pointer) { _, it -> StructPointer(os.sys, it) }
    }

    val errno = APIVariable.int(os, "errno")

    override fun setErrno(error: Exception?) {
        errno.allocated.value = error?.toStdCErrno(ra)?.id ?: PosixError.ESUCCESS.id
    }

    private val Set<PosixFilePermission>.bits: Int get() {
        var result = 0

        if (OWNER_READ in this) result = result or S_IRUSR
        if (OWNER_WRITE in this) result = result or S_IWUSR
        if (OWNER_EXECUTE in this) result = result or S_IXUSR

        if (GROUP_READ in this) result = result or S_IRGRP
        if (GROUP_WRITE in this) result = result or S_IWGRP
        if (GROUP_EXECUTE in this) result = result or S_IXGRP

        if (OTHERS_READ in this) result = result or S_IROTH
        if (OTHERS_WRITE in this) result = result or S_IWOTH
        if (OTHERS_EXECUTE in this) result = result or S_IXOTH

        return result
    }

    private fun stat_intern(path: String): stat {
        val attrs = os.filesystem.attributes(path)

        val mode = when {
            attrs.isDirectory -> __S_IFDIR
            attrs.isSymbolicLink -> __S_IFLNK
            attrs.isRegularFile -> __S_IFREG
            else -> error("[0x${ra.hex8}] Unknown file attrs mode for '$path'")
        }

        val nlink = when {
            attrs.isDirectory -> os.filesystem.listDir(path).size
            else -> 1
        }

        return stat(
                st_mode = mode or attrs.permissions().bits.asULong,
                st_size = attrs.size(),
                // FIXME: idk where to get from java blksize and count currently let it be zero
//                st_blocks = 512,
//                st_blksize = 512,
                st_nlink = nlink.asULong,
                st_atime = attrs.lastAccessTime().to(TimeUnit.SECONDS),
                st_mtime = attrs.lastModifiedTime().to(TimeUnit.SECONDS),
                st_ctime = attrs.creationTime().to(TimeUnit.SECONDS)
        )
    }

    private fun fstat_intern(fd: Int) = when (val file = sys.ioSystem.descriptor(fd)) {
        is NullFile, is StreamFile -> stat(st_mode = __S_IFCHR)

        is CommonFile, is CommonDirectory -> {
            val mode = if (file is CommonDirectory) __S_IFDIR else __S_IFREG

            val nlink = if (file is CommonDirectory) file.count() else 1

            val attrs = file.attributes()

            stat(
                    st_mode = mode or attrs.permissions().bits.asULong,
                    st_size = attrs.size(),
                    // FIXME: idk where to get from java blksize and count currently let it be zero
//                    st_blocks = 512,
//                    st_blksize = 512,
                    st_nlink = nlink.asULong,
                    st_atime = attrs.lastAccessTime().to(TimeUnit.SECONDS),
                    st_mtime = attrs.lastModifiedTime().to(TimeUnit.SECONDS),
                    st_ctime = attrs.creationTime().to(TimeUnit.SECONDS)
            )
        }

        is ISocketFile -> stat(st_mode = __S_IFSOCK)

        else -> error("[0x${ra.hex8}] Can't process file with VEOS: ${file::class}")
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/stat
    @APIFunc
    fun stat(path: CharPointer, buf: StructPointer) = nothrow(-1) {
        segfault(buf) { "stat is null" }
        val stat = stat_intern(path.string)
        log.config { "[0x${ra.hex8}] stat(path='${path.string}' buf=0x$buf) -> $stat in ${os.currentProcess}" }
        sys.deps.toStat(sys, buf.address, stat)
        return@nothrow 0
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/stat64
    @APIFunc
    fun stat64(path: CharPointer, buf: StructPointer) = nothrow(-1) {
        segfault(buf) { "stat is null" }
        val stat = stat_intern(path.string)
        log.config { "[0x${ra.hex8}] stat64(path='${path.string}' buf=0x$buf) -> $stat in ${os.currentProcess}" }
        sys.deps.toStat64(sys, buf.address, stat)
        return@nothrow 0
    }

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/lstat
    @APIFunc
    fun lstat(path: CharPointer, buf: StructPointer) = stat(path, buf)

    // SVr4, 4.3BSD, POSIX.1-2001
    // https://linux.die.net/man/2/lstat64
    @APIFunc
    fun lstat64(path: CharPointer, buf: StructPointer) = stat64(path, buf)

    @APIFunc
    fun fstat(fd: Int, buf: StructPointer) = nothrow(-1) {
        segfault(buf) { "stat is null" }
        val stat = fstat_intern(fd)
        log.warning { "[0x${ra.hex8}] fstat(fd=$fd buf=$buf) -> $stat in ${os.currentProcess}" }
        sys.deps.toStat(sys, buf.address, stat)
        return@nothrow 0
    }

    // https://linux.die.net/man/2/fstat64
    @APIFunc
    fun fstat64(fd: Int, buf: StructPointer) = nothrow(-1) {
        segfault(buf) { "stat is null" }
        val stat = fstat_intern(fd)
        log.warning { "[0x${ra.hex8}] fstat(fd=$fd buf=$buf) -> $stat in ${os.currentProcess}" }
        sys.deps.toStat64(sys, buf.address, stat)
        return@nothrow 0
    }

    // https://refspecs.linuxbase.org/LSB_3.0.0/LSB-PDA/LSB-PDA/baselib-xstat64-1.html
    @APIFunc
    fun __xstat64(ver: Int, path: CharPointer, buf: StructPointer): Int {
        require(ver == 3) { "Unknown version of __xstat64: $ver" }
        return stat64(path, buf)
    }

    // https://refspecs.linuxbase.org/LSB_3.0.0/LSB-PDA/LSB-PDA/baselib-xstat64-1.html
    @APIFunc
    fun __lxstat64(ver: Int, path: CharPointer, buf: StructPointer): Int {
        require(ver == 3) { "Unknown version of __lxstat64: $ver" }
        return lstat64(path, buf)
    }

    // https://refspecs.linuxbase.org/LSB_3.0.0/LSB-PDA/LSB-PDA/baselib-xstat64-1.html
    @APIFunc
    fun __fxstat64(ver: Int, fd: Int, buf: StructPointer): Int {
        require(ver == 3) { "Unknown version of __fxstat64: $ver" }
        return fstat64(fd, buf)
    }
}