package ru.inforion.lab403.kopycat.cores.v850es.exceptions

import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.v850es.enums.ExcCode

/**
 * Created by user on 29.05.17.
 */

abstract class v850ESHardwareException(excCode: Enum<*>, where: Long, message: String? = null):
        HardwareException(excCode, where, message) {
    // TODO: Change pc value
    object IncorrectSegment: v850ESHardwareException(ExcCode.IncorrectSegment, -1)
    object DivisionByZero: v850ESHardwareException(ExcCode.DivisionByZero, -1)
    object Overflow: v850ESHardwareException(ExcCode.Overflow, -1)
}