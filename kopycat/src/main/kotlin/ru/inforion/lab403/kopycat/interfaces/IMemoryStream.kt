package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype


interface IMemoryStream {
    /**
     * Starting position of the start of stream
     */
    var mark: Long

    /**
     * Current position of the of stream at the moment.
     * The value of position is incremented with each time you
     * reading stream by the corresponding number of bytes
     */
    var position: Long

    /**
     * Value of last reading result/
     */
    val last: Int

    /**
     * Mov position to next item and read it
     */
    fun read(datatype: Datatype): Long

    /**
     * Mov position to next item and write it
     */
    fun write(datatype: Datatype, data: Long)

    /**
     * Read data from the top of stream without moving position
     */
    fun peek(datatype: Datatype): Long

    fun peekOpcode(): Int = peekByte().toInt()
    fun peekByte(): Long = peek(Datatype.BYTE)
    fun peekWord(): Long = peek(Datatype.WORD)
    fun peekDword(): Long = peek(Datatype.DWORD)
    fun peekQword(): Long = peek(Datatype.QWORD)

    /**
     * Read data and move the stream position
     */
    fun readOpcode(): Int = readByte().toInt()

    fun readByte(): Long = read(Datatype.BYTE)
    fun readWord(): Long = read(Datatype.WORD)
    fun readDword(): Long = read(Datatype.DWORD)
    fun readQword(): Long = read(Datatype.QWORD)

    /**
     * Write data and move the stream position
     */
    fun writeByte(data: Long) = write(Datatype.BYTE, data)
    fun writeWord(data: Long) = write(Datatype.WORD, data)
    fun writeDword(data: Long) = write(Datatype.DWORD, data)
    fun writeQword(data: Long) = write(Datatype.QWORD, data)

    /**
     * Rewind the stream start position
     * I.e. position = mark.
     */
    fun rewind()

    /**
     * A value that is equal to: position - mark
     */
    val offset: Int

    /**
     * Return byte array with last read values from 0..offset
     */
    val data: ByteArray
}