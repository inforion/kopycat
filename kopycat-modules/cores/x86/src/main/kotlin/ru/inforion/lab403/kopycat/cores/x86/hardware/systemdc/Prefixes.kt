package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Prefixes(
        val core: x86Core,
        var lock: Boolean = false,
        var string: StringPrefix = StringPrefix.NO,
        var segmentOverride: x86Register = ds,
        var operandOverride: Boolean = false,
        var addressOverride: Boolean = false) {

    val opsize: Datatype get() = if ((core.cpu.mode == x86CPU.Mode.R16) xor operandOverride) WORD else DWORD
    val addrsize: Datatype get() = if ((core.cpu.mode == x86CPU.Mode.R16) xor addressOverride) WORD else DWORD
    val is16BitAddressMode: Boolean get() = (core.cpu.mode == x86CPU.Mode.R16) xor addressOverride
    val is16BitOperandMode: Boolean get() = opsize == WORD
    val ssr: x86Register get() = segmentOverride

//    val eax get() = x86Register.gpr(opsize, x86GPR.EAX.id)
//    val ecx get() = x86Register.gpr(opsize, x86GPR.ECX.id)
//    val ebx get() = x86Register.gpr(opsize, x86GPR.EBX.id)
//    val edx get() = x86Register.gpr(opsize, x86GPR.EDX.id)
//    val esp get() = x86Register.gpr(opsize, x86GPR.ESP.id)
//    val ebp get() = x86Register.gpr(opsize, x86GPR.EBP.id)
//    val esi get() = x86Register.gpr(opsize, x86GPR.ESI.id)
//    val edi get() = x86Register.gpr(opsize, x86GPR.EDI.id)
}