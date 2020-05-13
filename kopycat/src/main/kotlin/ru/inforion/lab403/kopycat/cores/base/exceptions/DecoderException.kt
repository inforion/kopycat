package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hex8



class DecoderException(val data: Long, val where: Long, message: String? = null) : GeneralException(message) {
    override fun toString(): String {
        val msg = if (message != null) " [$message]" else ""
        return "$prefix -> data=0x${data.hex}$msg"
    }
}