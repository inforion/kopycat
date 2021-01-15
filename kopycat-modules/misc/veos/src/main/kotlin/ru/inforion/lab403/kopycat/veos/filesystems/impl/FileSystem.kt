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
package ru.inforion.lab403.kopycat.veos.filesystems.impl

import ru.inforion.lab403.common.extensions.convertToBytes
import ru.inforion.lab403.common.extensions.div
import ru.inforion.lab403.common.extensions.isNotTraverseDirectory
import ru.inforion.lab403.common.extensions.toFile
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.attributes
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONoSuchFileOrDirectory
import ru.inforion.lab403.kopycat.veos.filesystems.*
import ru.inforion.lab403.kopycat.veos.filesystems.AccessFlags.Companion.toAccessFlags
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IBasicFile
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IRandomAccessFile
import ru.inforion.lab403.kopycat.veos.kernel.System
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.set


class FileSystem(val sys: System): IAutoSerializable {
    companion object {
        val log = logger(INFO)

        const val STDIN_INDEX = 0
        const val STDOUT_INDEX = 1
        const val STDERR_INDEX = 2
    }

    private var currentDirectory = "/"

    private class VirtualFile(val file: IBasicFile): IAutoSerializable, IConstructorSerializable {
        val descriptors: MutableList<Int> = mutableListOf()
    }

    private val virtualFiles = mutableMapOf<String, VirtualFile>()

    private val tempFiles = mutableMapOf<Int, String>()

    enum class Seek(val id: Int) { Begin(0), Current(1), End(2) }

    init {
        log.info { "Root and current directory is ${concatPath(currentDirectory).absolutePath}" }
    }

    // REVIEW: rename this
    //  it converts to absolute path
    private fun concatPath(path: String): File {
        val rootDirectory = sys.conf.rootDirectory
        return when {
            path[0] == '/' -> {
                require(path.isNotTraverseDirectory(rootDirectory)) { "Oops, try of escape: $path" }
                rootDirectory / path
            } // TODO: Windows/DOS separator
            path[0] == '~' -> TODO("Home directory")
            else -> {
                val currentDirectoryAbsolute = rootDirectory / currentDirectory
                require(path.isNotTraverseDirectory(currentDirectoryAbsolute)) { "Oops, try of escape: $path" }
                currentDirectoryAbsolute / path
            }
        }.toFile()
    }

    // unsafe regarding exceptions function

    fun virtualFile(file: IRandomAccessFile, filename: String) {
        check(filename !in virtualFiles) { "File with name '$filename' already register!" }
        virtualFiles[filename] = VirtualFile(file)
    }

    fun unregisterFile(filename: String): Boolean {
        val list = virtualFiles[filename]
        if (list == null) {
            log.warning { "File '$filename' busy with descriptors: $list" }
            return false
        }
        virtualFiles.remove(filename)
        return true
    }

    fun file(fd: Int): IRandomAccessFile {
        log.fine { "Requested fd = $fd as IRandomAccessFile" }
        return sys.ioSystem.descriptor(fd) as IRandomAccessFile
    }

    fun dir(fd: Int): CommonDirectory {
        log.fine { "Requested fd = $fd as CommonDirectory" }
        return sys.ioSystem.descriptor(fd) as CommonDirectory
    }

    // safe function for use in posix layer

    fun open(filename: String, flags: AccessFlags): Int {
        val namedFile = virtualFiles[filename]
        return if (namedFile != null) {
            log.finer { "Named file hook detected '$filename'" }
            sys.ioSystem.register(namedFile.file).also {
                namedFile.descriptors.add(it)
                namedFile.file.open(it)
            }
        } else {
            val file = CommonFile(concatPath(filename).absolutePath, flags)
            sys.ioSystem.register(file).also { file.open(it) }
        }
    }

    fun open(filename: String, mode: String) = open(filename, mode.toAccessFlags())

    fun read(fd: Int, len: Int) = sys.ioSystem.read(fd, len)

    fun write(fd: Int, data: ByteArray) = sys.ioSystem.write(fd, data)

    fun write(fd: Int, string: String) = write(fd, string.convertToBytes())

    fun close(fd: Int) = sys.ioSystem.close(fd)

    fun seek(fd: Int, offset: Long, origin: Seek) {
        val file = file(fd)
        val position = when (origin) {
            Seek.Begin -> offset
            Seek.Current -> file.tell() + offset
            Seek.End -> file.size() + offset
        }
        file.seek(position)
    }

    fun tell(fd: Int) = file(fd).tell()

    // TODO: behaviour on close?
    fun dup2(fdOld: Int, fdNew: Int): Int {
        log.finest { "dup2($fdOld, $fdNew)" }

        if (fdOld == fdNew)
            return fdNew

        val file = sys.ioSystem.descriptor(fdOld)

        if (sys.ioSystem.isOpen(fdNew)) {
            sys.ioSystem.close(fdNew)
        }

        sys.ioSystem.reserve(file, fdNew)
        file.open(fdNew)  // TODO: fdOld erased!!!

        return fdNew
    }

    val cwd get() = currentDirectory

    fun share(fd: Int) = file(fd).apply { share() }

    fun exists(path: String) = concatPath(path).exists() // TODO: virtual files

    fun absolutePath(path: String) = concatPath(path).absolutePath

    fun isDirectory(path: String) = concatPath(path).isDirectory // TODO: virtual files

    fun listDir(path: String) = concatPath(path).list() ?: throw IONoSuchFileOrDirectory(path)

    fun fileSize(path: String): Long {
        val file = concatPath(path)
        return if (!file.exists() || !file.isFile) -1L else file.length()
    }

    fun attributes(path: String): PosixFileAttributes {
        val file = concatPath(path)
        try {
            return file.attributes()
        } catch (error: IOException) {
            throw IONoSuchFileOrDirectory(path)
        }
    }

    fun reset() {
        currentDirectory = "/"
        virtualFiles.clear()

        sys.ioSystem.reserve(StandardStreamFile.stdin, STDIN_INDEX)
        sys.ioSystem.reserve(StandardStreamFile.stdout, STDOUT_INDEX)
        sys.ioSystem.reserve(StandardStreamFile.stderr, STDERR_INDEX)

        virtualFile(NullFile(), "/dev/null")

        val tmpdir = concatPath(sys.conf.tempDirectory)
        if (!tmpdir.exists())
            check(tmpdir.mkdir()) { "Can't create temp directory ${tmpdir.absolutePath}" }
    }

    val stdout get() = file(STDOUT_INDEX)
    val stderr get() = file(STDERR_INDEX)

    fun openDir(path: String): Int {
        val dir = CommonDirectory(concatPath(path).absolutePath)
        return sys.ioSystem.register(dir).also { dir.open(it) }
    }

    fun readDir(fd: Int) = dir(fd).next()

    fun closeDir(fd: Int) = sys.ioSystem.close(fd)

    fun tempFilename(): String {
        for (i in 0 until 1024) {
            val calendar = Calendar.getInstance()
            val filename = SimpleDateFormat("dd-HH-mm-ss").format(calendar.time)
            val fullPath = sys.conf.tempDirectory / "$filename-$i"
            val file = concatPath(fullPath)
            if (!file.exists())
                return fullPath
        }
        throw IllegalStateException("Increase limit")
    }

    fun tempFile(): Int {
        val fullPath = tempFilename()
        val file = concatPath(fullPath)
        check (!file.exists()) { "New temporary file already exists" }
        return open(fullPath, "wb+").also { file.deleteOnExit() }
    }
}