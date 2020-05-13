package ru.inforion.lab403.kopycat.auxiliary

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import java.io.DataInputStream
import java.util.*
import java.util.logging.Level


class IntelHexTranslator(val data: String, val offset: Int = 0x0) {
    companion object {
        private val log = logger(Level.CONFIG)
    }

    enum class RECORD_TYPE(val id: Int) {
        DAT(0x00), // Data
        EOF(0x01),  // End Of File
        ESA(0x02),  // Extended Segment Address
        SSA(0x03),  // Start Segment Address
        ELA(0x04),  // Extended Linear Address
        SLA(0x05),  // Start Linear Address
    }

    class ParsingException(msg: String) : Throwable(msg)

    class Record(
            val size: Int,
            val address: Int,
            val type: RECORD_TYPE,
            val data: ByteArray,
            val checksum: Int) {

        val end = address + size

        companion object {
            fun create(info: String, verifyChecksum: Boolean = true): Record {
                if (info[0] != ':')
                    throw ParsingException("First symbol must be :")
                val raw = info.substring(1).unhexlify()
                val stream = DataInputStream(raw.inputStream())
                val size = stream.readUnsignedByte()
                val address = stream.readUnsignedShort()
                val id = stream.readByte().toUInt()
                val type = find<RECORD_TYPE> { it.id == id }!!
                val data = ByteArray(size)
                stream.read(data)
                val checksum = stream.readUnsignedByte()
                if (verifyChecksum) {
                    var caclCksum = 0
                    (0 until raw.size - 1).forEach { caclCksum -= raw[it] }
                    if (caclCksum.toByte() != checksum.toByte())
                        throw ParsingException("Checksum incorrect for $info (${caclCksum.toByte()} != ${checksum.toByte()})")
                }
                return Record(size, address, type, data, checksum)
            }
        }

        override fun toString(): String = "%s[%04X]: %s".format(type, address, data.hexlify())
    }

    private val records = data
            .lineSequence()
            .map { Record.create(it) }
            .toMutableList()
            .also { normalize(it) }

    private fun normalize(records: MutableCollection<Record>) {
        if (records.last().type != RECORD_TYPE.EOF)
            throw ParsingException("Last record incorrect (must be EOF)!")

        var addressOffset = 0L
        val normalized = LinkedList<Record>()
        records.forEach {
            when (it.type) {
                RECORD_TYPE.DAT -> {
                    val pAddr = it.address + addressOffset
                    val record = Record(it.size, pAddr.toInt(), RECORD_TYPE.DAT, it.data, -1)
                    normalized.add(record)
                }
                RECORD_TYPE.ELA -> {
                    val dis = DataInputStream(it.data.inputStream())
                    addressOffset = dis.readUnsignedShort().toULong() shl 16
                }
                RECORD_TYPE.EOF -> return@forEach
                else -> log.warning { "Can't interpret record $it" }
            }
        }
        var k = 0
        while (k < normalized.size) {
            val record = normalized[k]
            val tail = normalized.find { it.address == record.address + record.size }
            if (tail != null) {
                val merged = Record(
                        size = record.size + tail.size,
                        address = record.address,
                        type = RECORD_TYPE.DAT,
                        data = record.data + tail.data,
                        checksum = -1)
                normalized.remove(tail)
                normalized.remove(record)
                normalized.add(k, merged)
            } else {
                k++
            }
        }
        records.clear()
        records.addAll(normalized)
    }

    fun translate(): Array<Pair<ByteArray, Int>> {
        var addressOffset = 0
        val result = mutableListOf<Pair<ByteArray, Int>>()
        records.forEach {
            when (it.type) {
                RECORD_TYPE.DAT -> {
                    val pAddr = it.address + addressOffset - offset
                    log.config { "Adding 0x%04X bytes at 0x%08X".format(it.size, pAddr) }
                    result.add(it.data to pAddr)
                }
                RECORD_TYPE.ELA -> {
                    val dis = DataInputStream(it.data.inputStream())
                    addressOffset = dis.readUnsignedShort() shl 16
                    log.fine { "Adding Change base address to 0x%08X".format(addressOffset) }
                }
                RECORD_TYPE.EOF -> return@forEach
                else -> log.warning { "FLASHING: Can't interpret record $it" }
            }
        }
        return result.toTypedArray()
    }
}