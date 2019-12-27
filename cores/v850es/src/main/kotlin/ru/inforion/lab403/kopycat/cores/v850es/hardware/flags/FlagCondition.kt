package ru.inforion.lab403.kopycat.cores.v850es.hardware.flags

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

@Suppress("NOTHING_TO_INLINE")
/**
 * Created by v.davydov on 03.06.17.
 */

object FlagCondition {
    inline fun CheckCondition(core: v850ESCore, cc: Long): Boolean = when (cc) {
        0x0L -> core.cpu.flags.ov
        0x1L -> core.cpu.flags.cy
        0x2L -> core.cpu.flags.z
        0x3L -> core.cpu.flags.cy or core.cpu.flags.z
        0x4L -> core.cpu.flags.s
        0x5L -> true
        0x6L -> core.cpu.flags.s xor core.cpu.flags.ov
        0x7L -> (core.cpu.flags.s xor core.cpu.flags.ov) or core.cpu.flags.z
        0x8L -> !core.cpu.flags.ov
        0x9L -> !core.cpu.flags.cy
        0xAL -> !core.cpu.flags.z
        0xBL -> !(core.cpu.flags.cy or core.cpu.flags.z)
        0xCL -> !core.cpu.flags.s
        0xDL -> core.cpu.flags.sat
        0xEL -> !(core.cpu.flags.s xor core.cpu.flags.ov)
        0xFL -> !((core.cpu.flags.s xor core.cpu.flags.ov) or core.cpu.flags.z)
        else -> throw GeneralException("Incorrect condition code in CheckCondition")
    }
}