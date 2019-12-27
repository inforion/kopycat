package ru.inforion.lab403.kopycat.cores.arm.exceptions

import ru.inforion.lab403.kopycat.cores.arm.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException

/**
 * Created by a.gladkikh on 13.01.18.
 */

abstract class ARMHardwareException(excCode: Enum<*>, where: Long, message: String? = null):
        HardwareException(excCode, where, message) {
    // TODO: Change pc value
    object Overflow: ARMHardwareException(ExcCode.Overflow, -1)
    object Unpredictable: ARMHardwareException(ExcCode.Unpredictable, -1)
    object Undefined: ARMHardwareException(ExcCode.Undefined, -1)
    object Unknown: ARMHardwareException(ExcCode.Unknown, -1)
}