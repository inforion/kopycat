package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.common.extensions.hex8


abstract class HardwareException(
        val excCode: Enum<*>,
        val where: Long,
        message: String? = null
) : GeneralException(message) {
    override fun toString(): String = "$prefix[${where.hex8}]: $excCode"
}