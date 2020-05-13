package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction



class MemoryAccessError(where: Long, val address: Long, val LorS: AccessAction, message: String? = null) :
        HardwareException(if (LorS == AccessAction.LOAD) AccessAction.LOAD else AccessAction.STORE, where, message) {
    override fun toString(): String {
        val msg = if (message != null) " >> %s".format(message) else ""
        val pc = if (where != -1L) "[${where.hex8}]" else ""
        return "$prefix$pc: $LorS ${address.hex8}$msg"
    }
}