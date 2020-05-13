package ru.inforion.lab403.kopycat.gdbstub

import ru.inforion.lab403.common.extensions.asUInt
import ru.inforion.lab403.common.extensions.convertToBytes
import ru.inforion.lab403.common.extensions.hex2


class GDBMessage private constructor(
        val packet: String,
        val checksum: Int = -1,
        val service: Boolean = false) {
    companion object {
        fun calcChecksum(data: String) = data.convertToBytes().sumBy { it.asUInt } and 0xFF

        fun message(data: String, checksum: Int = -1): GDBMessage {
            val value = if (checksum == -1) calcChecksum(data) else checksum
            return GDBMessage(data, value)
        }

        fun interrupt(interrupt: Int) = message( "T${interrupt.hex2}")
        fun error(error: Int) = message("E${error.hex2}")
        fun service(cmd: Char) = GDBMessage("$cmd", service = true)

        val ack = service('+')
        val rej = service('-')
        val ok = message("OK")
        val empty = message("")
    }

    val cmd: Char get() = packet[0]
    val data: String get() = packet.substring(1)

    fun build(noBinary: Boolean = false): String {
        val prefix = if (!service) "$" else ""
        val postfix = if (!service) "#${checksum.hex2}" else ""
        val payload = if (noBinary && packet.startsWith('X')) "${packet.split(':', limit = 2)[0]}..." else packet
        return "$prefix$payload$postfix"
    }

    override fun toString(): String = build(true)
}