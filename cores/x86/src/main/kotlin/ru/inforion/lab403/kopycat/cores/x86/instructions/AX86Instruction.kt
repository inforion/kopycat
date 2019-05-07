package ru.inforion.lab403.kopycat.cores.x86.instructions

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 07.09.16.
 */

abstract class AX86Instruction(
        core: x86Core,
        type: Type,
        val opcode: ByteArray,
        val prefs: Prefixes,
        vararg operands: AOperand<x86Core>) : AInstruction<x86Core>(core, type, *operands) {
    final override val size: Int = opcode.size
    final override fun toString(): String {
        val spref = if (prefs.string != StringPrefix.NO) "${prefs.string.toString().toLowerCase()} " else ""
        val lpref = if (prefs.lock) "lock " else ""
        val address = if (ea != WRONGL) "[%08X]".format(ea.toInt()) else "[ UNDEF  ]"
        return "$address $lpref$spref$mnem ${joinToString()}"
    }

    open val cfChg = false
    open val pfChg = false
    open val afChg = false
    open val zfChg = false
    open val sfChg = false
    open val tfChg = false
    open val ifqChg = false
    open val dfChg = false
    open val ofChg = false
}