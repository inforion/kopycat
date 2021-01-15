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
import ru.inforion.lab403.kopycat.veos.api.abstracts.*
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.datatypes.Sizet
import ru.inforion.lab403.kopycat.veos.api.datatypes.VaArgs
import ru.inforion.lab403.kopycat.veos.api.datatypes.VaList
import ru.inforion.lab403.kopycat.veos.api.datatypes.size_t
import ru.inforion.lab403.kopycat.veos.api.format.*
import ru.inforion.lab403.kopycat.veos.api.pointers.BytePointer
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.misc.*
import ru.inforion.lab403.kopycat.veos.api.pointers.CharPointer
import ru.inforion.lab403.kopycat.veos.api.pointers.IntPointer
import ru.inforion.lab403.kopycat.veos.exceptions.BadAddressException
import ru.inforion.lab403.kopycat.veos.exceptions.InvalidArgument
import ru.inforion.lab403.kopycat.veos.filesystems.AccessFlags.Companion.toAccessFlags
import ru.inforion.lab403.kopycat.veos.filesystems.impl.FileSystem
import ru.inforion.lab403.kopycat.veos.ports.cstdlib.*
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixError
import ru.inforion.lab403.kopycat.veos.ports.stdc.FILE
import java.io.File

/**
 *
 * Implementation of stdio.h of C standard library
 */
class StdioAPI(os: VEOS<*>) : API(os) {
    /**
        Operations on files:
        [remove]    - Remove file
        [rename]    - Rename file
        [tmpfile]   - Open a temporary file
        [tmpnam]    - Generate temporary filename

        File access:
        [fclose]    - Close file
        [fflush]    - Flush stream
        [fopen]     - Open file
        [fopen64]   - Open file (O_LARGEFILE)
        TODO: freopen - Reopen stream with different file or mode
        TODO: setbuf - Set stream buffer
        TODO: setvbuf - Change stream buffering

        Formatted input/output:
        [fprintf]   - Write formatted data to stream
        [fscanf]    - Read formatted data from stream
        [printf]    - Print formatted data to stdout
        [scanf]     - Read formatted data from stdin
        [snprintf]  - Write formatted output to sized buffer
        [sprintf]   - Write formatted data to string
        [sscanf]    - Read formatted data from string
        [vfprintf]  - Write formatted data from variable argument list to stream
        [vfscanf]   - Read formatted data from stream into variable argument list
        [vprintf]   - Print formatted data from variable argument list to stdout
        [vscanf]    - Read formatted data into variable argument list
        [vsnprintf] - Write formatted data from variable argument list to sized buffer
        [vsprintf]  - Write formatted data from variable argument list to string
        [vsscanf]   - Read formatted data from string into variable argument list

        Character input/output:
        [fgetc]     - Get character from stream
        [fgets]     - Get string from stream
        [fputc]     - Write character to stream
        [fputs]     - Write string to stream
        [getc]      - Get character from stream
        [getchar]   - Get character from stdin
        TODO: gets  - Get string from stdin
        [putc]      - Write character to stream
        [putchar]   - Write character to stdout
        [puts]      - Write string to stdout
        [ungetc]    - Unget character from stream

        Direct input/output:
        [fread]     - Read block of data from stream
        [fwrite]    - Write block of data to stream

        File positioning:
        [fgetpos]   - Get current position in stream
        [fseek]     - Reposition stream position indicator
        [fseeko64]  - REVIEW: description
        [fsetpos]   - Set position indicator of stream
        [ftell]     - Get current position in stream
        [ftello64]  - REVIEW: description
        [rewind]    - Set position of stream to the beginning

        Error-handling:
        [clearerr]  - Clear error indicators
        [feof]      - Check end-of-file indicator
        [ferror]    - Check error indicator
        [perror]    - Print error message

        Macros:
        [BUFSIZ]    - Buffer size
        [EOF]       - End-of-File
        TODO: FILENAME_MAX - Maximum length of file names
        TODO: FOPEN_MAX - Potential limit of simultaneous open streams
        [L_tmpnam]  - Minimum length for temporary file name
        [TMP_MAX]   - Number of temporary files

        Types:
        [FILE]      - Object containing information to control a stream
        TODO: fpos_t - Object containing information to specify a position within a file
        [size_t]    - Unsigned integral type

        Other internal functions:
        [_IO_putc]          - alias for putc
        [_IO_getc]          - alias for getc
        [__uflow]           - REVIEW: description
        [__fputc_unlocked]  - REVIEW: description

        REVIEW: --- POSIX extension of Stdio functions ---
        [fileno] - REVIEW: description
     **/
    companion object {
        @Transient val log = logger(WARNING)
    }

    init {
        type(ArgType.Pointer) { _, address -> FILE(sys, address) }

        ret<FILE> { APIResult.Value(it.address) }
    }

    override fun init(argc: Long, argv: Long, envp: Long) {
        stdin.allocated.value = FILE.new(sys, FileSystem.STDIN_INDEX).address
        stdout.allocated.value = FILE.new(sys, FileSystem.STDOUT_INDEX).address
        stderr.allocated.value = FILE.new(sys, FileSystem.STDERR_INDEX).address
    }

    override fun setErrno(error: Exception?) {
        errno.allocated.value = error?.toStdCErrno(ra)?.id ?: PosixError.ESUCCESS.id
    }

    fun vsprintf_internal(fmt: String?, args: Iterator<Long>): String {
        if (fmt == null) throw InvalidArgument()
        val buffer = CharArray(10000) // TODO: get rid of
        val count = vsprintfMain(os, buffer.charArrayPointer, fmt, args)
        return buffer.slice(0 until count).joinToString("")
    }

    fun vsscanf_internal(buf: ICharArrayConstPointer, fmt: String?, args: Iterator<Long>): Int {
        if (fmt == null) throw InvalidArgument()
        return vsscanfMain(os, buf, fmt, args)
    }

    fun checkNotOverflow(value: UInt) = check(value <= Int.MAX_VALUE.uint) { "Unsupported overflow" }
    fun checkNotOverflow(value: Long) = check( value in Int.MIN_VALUE..Int.MAX_VALUE) { "Unsupported overflow" }


    // TODO: move to Errno
    val errno = APIVariable.int(os, "errno")

    val stdin = APIVariable.pointer(os, "stdin")
    val stdout = APIVariable.pointer(os, "stdout")
    val stderr = APIVariable.pointer(os, "stderr")

    val tmpnamBuffer by lazy { sys.allocate(L_tmpnam, os.systemData) }

    val stdinFile get() = FILE(sys, stdin.value)
    val stdoutFile get() = FILE(sys, stdout.value)
    val stderrFile get() = FILE(sys, stderr.value)

    // --- Operations on files ---

    // REVIEW: fill all errno
    // Errno codes: ...
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/remove/
    @APIFunc
    fun remove(path: CharPointer): Int {
        val file = File(sys.filesystem.absolutePath(path.string))
        val result = file.exists() && file.delete() // REVIEW: security exception
        return (!result).toInt()
    }

    // REVIEW: fill all errno
    // Errno codes: ...
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/remove/
    @APIFunc
    fun rename(oldname: CharPointer, newname: CharPointer): Int {
        val oldFile = File(sys.filesystem.absolutePath(oldname.string))
        val newFile = File(sys.filesystem.absolutePath(newname.string))
        val result = oldFile.renameTo(newFile)
        return (!result).toInt()
    }

    // REVIEW: fill all errno
    // Errno codes: ...
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/tmpfile/
    @APIFunc
    fun tmpfile() = FILE.new(sys, sys.filesystem.tempFile())

    // REVIEW: fill all errno
    // Errno codes: ...
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/tmpnam/
    @APIFunc
    fun tmpnam(str: CharPointer): CharPointer {
        val buffer = if (str.isNotNull) str else CharPointer(sys, tmpnamBuffer)
        return buffer.apply { string = sys.filesystem.tempFilename() }
    }

    // --- File access ---

    // Errno codes: EBADF
    // http://www.cplusplus.com/reference/cstdio/fclose/
    @APIFunc
    fun fclose(stream: FILE) = nothrow(EOF) {
        segfault(stream) { "stream is null" }

        log.fine { "[0x${ra.hex8}] fclose(fd=${stream.fd})" }

        if (!stream.isOpened) // Double close
            return@nothrow 0

        stream.close()

        return@nothrow 0
    }

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/fflush/
    @APIFunc
    fun fflush(stream: FILE) = nothrow(EOF) {
        segfault(stream) { "stream is null" }
        log.fine { "[0x${ra.hex8}] fflush(fd=${stream.fd}) -> Not implemented" }
        stream.flush()
        return@nothrow 0
    }

    // REVIEW: fill all errno
    // Errno codes: EINVAL, ...
    // http://www.cplusplus.com/reference/cstdio/fopen/
    @APIFunc
    fun fopen(filename: CharPointer, type: CharPointer) = nothrow(FILE.nullPtr(sys)) {
        // Errno order: at first it checks the type, then the filename
        // also may throw InvalidArgument
        type.string.toAccessFlags()

        if (filename.isNull)
            throw BadAddressException()

        log.fine { "[0x${ra.hex8}] fopen(filename='${filename.string}' type='${type.string}')" }

        return@nothrow FILE.open(sys, filename.string, type.string)
    }

    // TODO: determine difference
    // REVIEW: not tested
    // REVIEW: check, which errno use
    @APIFunc
    fun fopen64(filename: CharPointer, type: CharPointer) = fopen(filename, type)

    // http://www.cplusplus.com/reference/cstdio/freopen/
    // TODO: freopen
    // http://www.cplusplus.com/reference/cstdio/setbuf/
    // TODO: setbuf
    // http://www.cplusplus.com/reference/cstdio/setvbuf/
    // TODO: setvbuf

    // --- Formatted input/output ---

    // Errno codes: EINVAL, EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/fprintf/
    @APIFunc
    fun fprintf(stream: FILE, format: CharPointer, vaArgs: VaArgs) = vfprintf(stream, format, vaArgs)

    // Errno codes: EINVAL, EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/fscanf/
    @APIFunc
    fun fscanf(stream: FILE, format: CharPointer, vaArgs: VaArgs) = vfscanf(stream, format, vaArgs)

    // Errno codes: EINVAL, EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/printf/
    @APIFunc
    fun printf(format: CharPointer, vaArgs: VaArgs) = vprintf(format, vaArgs)

    // Errno codes: EINVAL, EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/scanf/
    @APIFunc
    fun scanf(format: CharPointer, vaArgs: VaArgs) = vscanf(format, vaArgs)

    // Errno codes: EINVAL, ...
    // REVIEW: bufSize is size_t
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/snprintf/
    @APIFunc
    fun snprintf(buffer: CharPointer, bufSize: UInt, format: CharPointer, vaArgs: VaArgs) =
            vsnprintf(buffer, bufSize, format, vaArgs)

    // Errno codes: EINVAL, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/sprintf/
    @APIFunc
    fun sprintf(buffer: CharPointer, format: CharPointer, vaArgs: VaArgs) =
            vsprintf(buffer, format, vaArgs)

    // Errno codes: EINVAL, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/sscanf/
    @APIFunc
    fun sscanf(buffer: CharPointer, format: CharPointer, vaArgs: VaArgs) = vsscanf(buffer, format, vaArgs)

    // Errno codes: EINVAL, EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/vfprintf/
    @APIFunc
    fun vfprintf(stream: FILE, format: CharPointer, vaList: VaList) = nothrow(-1) {
        segfault(stream) { "stream is null" }
        segfault(format) { "format is null" }

        log.finest { "[0x${ra.hex8}] vfprintf(fd='${stream.fd}' format='${format.string.replace("\n", "\\n")}')" }

        val string = vsprintf_internal(format.string, vaList) // InvalidArgument -> EINVAL
        stream.write(string)

        return@nothrow string.length
    }

    // Errno codes: EINVAL, EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/vfscanf/
    @APIFunc
    fun vfscanf(stream: FILE, format: CharPointer, vaList: VaList) = nothrow(EOF) {
        segfault(stream) { "stream is null" }
        segfault(format) { "format is null" }

        log.finest { "[0x${ra.hex8}] vfscanf(fd=${stream.fd} format='${format.string.replace("\n", "\\n")}')" }
        // also work if isOpened = false
        val pointer = FileStreamPointer(os, stream.fd) // IONotFoundError -> EBADF
        // TODO: if va_arg is NULL, causes segmentation fault
        return@nothrow vsscanf_internal(pointer, format.string, vaList) // InvalidArgument -> EINVAL
    }

    // Errno codes: EINVAL, EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/vprintf/
    @APIFunc
    fun vprintf(format: CharPointer, vaList: VaList) = vfprintf(stdoutFile, format, vaList)

    // Errno codes: EINVAL, EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/vscanf/
    @APIFunc
    fun vscanf(format: CharPointer, vaList: VaList) = vfscanf(stdinFile, format, vaList)

    // Errno codes: EINVAL, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // REVIEW: bufSize is size_t
    // http://www.cplusplus.com/reference/cstdio/vsnprintf/
    @APIFunc
    fun vsnprintf(buffer: CharPointer, bufSize: UInt, format: CharPointer, vaList: VaList): Int {
        checkNotOverflow(bufSize)
        return nothrow(-1) {
            segfault(buffer) { "buffer is null" }
            segfault(format) { "format is null" }

            log.fine { "[0x${ra.hex8}] vsnprintf(buffer='${buffer.string}' format='${format.string.replace("\n", "\\n")}')" }

            val string = vsprintf_internal(format.string, vaList) // InvalidArgument -> EINVAL

            if (bufSize > 0U) // if 0, just return length
                buffer.string = string.substring(0 until minOf(bufSize.toInt() - 1, string.length))

            string.length
        }
    }

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/vsprintf/
    @APIFunc
    fun vsprintf(buffer: CharPointer, format: CharPointer, vaList: VaList)
            = vsnprintf(buffer, Int.MAX_VALUE.uint, format, vaList)

    // Errno codes: EINVAL, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/vsscanf/
    @APIFunc
    fun vsscanf(buffer: CharPointer, format: CharPointer, vaList: VaList): Int {
        segfault(buffer) { "buffer is null" }
        segfault(format) { "format is null" }

        val bufferString = buffer.string
        val pointer = bufferString.toCharArray().charArrayPointer

        log.finest { "[0x${ra.hex8}] vsscanf(buffer='${bufferString.replace("\n", "\\n")}' format='${format.string.replace("\n", "\\n")}')" }

        return nothrow(EOF) {
            // TODO: if va_arg is NULL, causes segmentation fault
            vsscanf_internal(pointer, format.string, vaList) // InvalidArgument -> EINVAL
        }
    }

    // --- Character input/output ---

    // REVIEW: fill all errno
    // Errno codes: EBADF, ...
    // http://www.cplusplus.com/reference/cstdio/fgetc/
    @APIFunc
    fun fgetc(stream: FILE) /* deferred Int */ {
        segfault(stream) { "stream is null" }

        log.finest { "[0x${ra.hex8}] fgetc(fd=${stream.fd})" }

        os.block<Int> {
            execute {
                stream.read()
            }

            success {
                it.asULong
            }

            failure {
                setErrno(it)
                EOF.asULong
            }
        }
    }

    // REVIEW: fill all errno
    // Errno codes: ...
    // TODO: can't handle num ~= 0x7FFF_FFFF (writing during execute may fix this)
    // http://www.cplusplus.com/reference/cstdio/fgets/
    @APIFunc
    fun fgets(str: CharPointer, num: Int, stream: FILE) /* deferred CharPointer */ {
        if (num <= 0) {
            sys.abi.setReturnValue(0)
            return
        }

        segfault(str) { "str is null" }
        segfault(stream) { "stream is null" }

        os.block<ByteArray> {
            execute {
                log.finest { "[0x${ra.hex8}] fgets(str=${str.address} num=$num fd=${stream.fd})" }

                // REVIEW: refactor this (another while-var-iterator-cycle)
                val result = mutableListOf<Byte>()
                var i = 0
                while (i < num - 1) {
                    val data = stream.read()
                    if (data == EOF)
                        break

                    result.add(data.asByte)

                    if (data == '\n'.asUInt)
                        break

                    i++
                }
                result.add(0)

                result.toByteArray()
            }

            success {
                str.store(it)
                str.address
            }

            failure {
                setErrno(it)
                0L
            }
        }
    }

    // REVIEW: fill all errno
    // Errno codes: EBADF, ...
    // http://www.cplusplus.com/reference/cstdio/fputc/
    @APIFunc
    fun fputc(ch: Int, stream: FILE) = nothrow(EOF) {
        segfault(stream) { "stream is null" }
        log.finest { "[0x${ra.hex8}] fputc(ch=${ch.asChar} fd=${stream.fd})" }
        stream.write(ch)
        ch.asByte.asUInt
    }

    // REVIEW: fill all errno
    // Errno codes: EBADF, ...
    // http://www.cplusplus.com/reference/cstdio/fputs/
    @APIFunc
    fun fputs(str: CharPointer, stream: FILE) = nothrow(EOF) {
        segfault(stream) { "stream is null" }
        log.finest { "[0x${ra.hex8}] fputs(str=${str.string} fd=${stream.fd})" }
        stream.write(str.string)
        // returns non-negative value on success
        0
    }

    // REVIEW: fill all errno
    // Errno codes: EBADF, ...
    // http://www.cplusplus.com/reference/cstdio/getc/
    @APIFunc
    fun getc(stream: FILE) = fgetc(stream)

    // REVIEW: fill all errno
    // Errno codes: EBADF, ...
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/getchar/A
    @APIFunc
    fun getchar() = getc(stdinFile)

    // http://www.cplusplus.com/reference/cstdio/gets/
    // TODO: gets

    // REVIEW: fill all errno
    // Errno codes: EBADF, ...
    // http://www.cplusplus.com/reference/cstdio/putc/
    @APIFunc
    fun putc(character: Int, stream: FILE) = fputc(character, stream)

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/putchar/
    @APIFunc
    fun putchar(character: Int) = putc(character, stdoutFile)

    // Errno codes: EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // Returns non-negative value on success
    // http://www.cplusplus.com/reference/cstdio/puts/
    @APIFunc
    fun puts(str: CharPointer) = nothrow(EOF) { stdoutFile.write(str.string + "\n"); 0 }

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/ungetc/
    @APIFunc
    fun ungetc(character: Int, stream: FILE) = nothrow(EOF) { stream.unget(character); character }

    // --- Direct input/output ---

    // Errno codes: EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/fread/
    @APIFunc
    fun fread(buffer: BytePointer, size: Sizet, count: Sizet, stream: FILE) /*: SizeT */ {
        segfault(buffer) { "buffer is null" }
        segfault(stream) { "stream is null" }

        log.fine { "[0x${ra.hex8}] fread(ptr=0x$buffer size=$size count=$count stream=${stream.fd})" }

        os.block<ByteArray> {
            execute {
                stream.read((size * count).toInt()) // IONotFoundError -> EBADF
            }

            success {
                buffer.store(it)
                (Sizet(it.size.asLong.ulong) / size).toLong()  // TODO: add asLong ext-property to KE
            }

            failure {
                setErrno(it); 0
            }
        }
    }

    // Errno codes: EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/fwrite/
    @APIFunc
    fun fwrite(buffer: BytePointer, size: Sizet, count: Sizet, stream: FILE) = nothrow(Sizet(0UL)) {
        segfault(buffer) { "buffer is null" }
        segfault(stream) { "stream is null" }
        stream.write(buffer.load((size * count).toInt()))
        count
    }

    // --- File positioning ---

    // REVIEW: not tested
    // REVIEW: pos is fpos_t*
    // http://www.cplusplus.com/reference/cstdio/fgetpos/
    @APIFunc
    fun fgetpos(stream: FILE, pos: IntPointer) = nothrow(-1) {
        segfault(stream) { "stream is null" }
        log.fine { "[0x${ra.hex8}] ftell(fd=${stream.fd})" }
        pos.set(stream.tell().asInt)
        0
    }

    // Errno codes: EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: offset is off_t
    // http://www.cplusplus.com/reference/cstdio/fseek/
    @APIFunc
    fun fseek(stream: FILE, offset: Int, whence: Int) = nothrow(-1) {
        segfault(stream) { "stream is null" }
        log.fine { "[0x${ra.hex8}] fseek(fd=${stream.fd} offset=0x${offset.hex8} whence=0x${whence.hex8})" }
        stream.seek(offset.asULong, whence)
        0
    }

    // Errno codes: EBADF, ...
    // REVIEW: check, which errno use
    // REVIEW: not tested
    // REVIEW: offset is off64_t
    @APIFunc
    fun fseeko64(stream: FILE, offset: Long, whence: Int) = nothrow(-1) {
        segfault(stream) { "stream is null" }
        log.fine { "[0x${ra.hex8}] fseeko64(fd=${stream.fd} offset=0x${offset.hex8} whence=0x${whence.hex8})" }
        stream.seek(offset, whence)
        0
    }

    // REVIEW: not tested
    // REVIEW: pos is fpos_t*
    // http://www.cplusplus.com/reference/cstdio/fsetpos/
    @APIFunc
    fun fsetpos(stream: FILE, pos: IntPointer) = nothrow(-1) {
        segfault(stream) { "stream is null" }
        log.fine { "[0x${ra.hex8}] ftell(fd=${stream.fd})" }
        stream.seek(pos.get.asULong, FileSystem.Seek.Begin.id)
        0
    }

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // REVIEW: return type is long
    // http://www.cplusplus.com/reference/cstdio/ftell/
    @APIFunc
    fun ftell(stream: FILE) = nothrow(-1) {
        segfault(stream) { "stream is null" }
        log.fine { "[0x${ra.hex8}] ftell(fd=${stream.fd})" }
        stream.tell().asInt
    }

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // REVIEW: return type is off64_t
    @APIFunc
    fun ftello64(stream: FILE): Long {
        segfault(stream) { "stream is null" }
        log.fine { "[0x${ra.hex8}] ftello64(fd=${stream.fd})" }
        return stream.tell()
    }

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/rewind/
    @APIFunc
    fun rewind(stream: FILE) {
        segfault(stream) { "stream is null" }
        log.fine { "[0x${ra.hex8}] rewind(fd=${stream.fd})" }
        nothrow(0) { stream.seek(0, FileSystem.Seek.Begin.id) }
    }

    // --- Error-handling ---

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/clearerr/
    @APIFunc
    fun clererr(stream: FILE) {
        segfault(stream) { "stream is null" }
        log.fine { "clererr(fd=${stream.fd})" }
        stream.clearError()
        stream.clearEOF()
    }

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/feof/
    @APIFunc
    fun feof(stream: FILE): Int {
        segfault(stream) { "stream is null" }
        log.fine { "feof(fd=${stream.fd})" }
        return stream.isEOF.asInt
    }

    // REVIEW: check, which errno use
    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/ferror/
    @APIFunc
    fun ferror(stream: FILE): Int {
        segfault(stream) { "stream is null" }
        log.fine { "ferror(fd=${stream.fd})" }
        return stream.isError.asInt
    }

    // REVIEW: not tested
    // http://www.cplusplus.com/reference/cstdio/perror/
    @APIFunc
    fun perror(str: CharPointer) {
        log.fine { "perror(str=${str.nullableString})" }

        val message = if (str.isNotNull) "${str.string}: " else ""
        val errString = errlistEnternal[errno.value.asInt] ?: "Unknown error ${errno.value}"
        stderrFile.write("$message$errString")
    }

    // --- Other internal functions ---

    // From Glibc
    // https://refspecs.linuxbase.org/LSB_3.0.0/LSB-PDA/LSB-PDA/baselib--io-putc-3.html
    @APIFunc
    fun _IO_putc(c: Int, fp: FILE) = putc(c, fp)

    // From Glibc
    // https://refspecs.linuxbase.org/LSB_5.0.0/LSB-Core-generic/LSB-Core-generic/baselib--io-getc-3.html
    @APIFunc
    fun _IO_getc(fp: FILE) = getc(fp)

    // From Glibc
    // Calls without checks
    @APIFunc
    fun __uflow(f: FILE) = f.read()

    @APIFunc
    fun __overflow(f: FILE, c: Int): Int {
        if (c != EOF) {
            f.write(c)
            return 1
        } else {
            return 0
        }
    }

    @APIFunc
    fun fwrite_unlocked(buffer: BytePointer, size: Sizet, count: Sizet, stream: FILE) = fwrite(buffer, size, count, stream)

    @APIFunc
    fun fputs_unlocked(str: CharPointer, stream: FILE) = fputs(str, stream)

    // From uClibc
    @APIFunc
    fun __fputc_unlocked(c: Int, stream: FILE) = putc(c, stream)

    @APIFunc
    fun __fgetc_unlocked(stream: FILE) = getc(stream)

    // FIXME: flags unused
    @APIFunc
    fun __snprintf_chk(buffer: CharPointer, maxlen: size_t, flags: Int, strlen: size_t, format: CharPointer, vaArgs: VaArgs): Int {
        log.fine { "[0x${ra.hex8}] __snprintf_chk(buffer=0x$buffer maxlen=$maxlen flags=$flags strlen=$strlen format=0x$format)" }
        segfault(maxlen > strlen) { "__snprintf_chk where maxlen > strlen" }
        return snprintf(buffer, maxlen.toUInt(), format, vaArgs)
    }

    @APIFunc
    fun __sprintf_chk(buffer: CharPointer, flags: Int, strlen: size_t, format: CharPointer, vaArgs: VaArgs): Int {
        log.fine { "[0x${ra.hex8}] __sprintf_chk(buffer=0x$buffer flags=$flags strlen=$strlen format=0x$format)" }
        return sprintf(buffer, format, vaArgs)
    }

    // REVIEW: --- POSIX extension of Stdio functions ---

    // Errno codes: EBADF
    // REVIEW: check, which errno use
    // REVIEW: not tested
    @APIFunc
    fun fileno(stream: FILE) = nothrow(-1) {
        stream.file // validate file exists
        stream.fd
    }
}