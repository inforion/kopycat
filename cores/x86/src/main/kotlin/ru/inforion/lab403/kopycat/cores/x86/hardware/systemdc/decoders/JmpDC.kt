package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch.Jmp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Far
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 28.09.16.
 */
class JmpDC(core: x86Core) : ADecoder<AX86Instruction>(core) {

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        return when (opcode) {
            0xEB -> Jmp(core, s.near8(prefs), s.data, prefs, isRelative = true)
            0xE9 -> Jmp(core, s.near(prefs), s.data, prefs, isRelative = true)
            0xEA -> Jmp(core, s.far(prefs), s.data, prefs, isRelative = false, isFar = true)
            0xFF -> {
                val sopcode = s.peekByte().toInt()
                val row = sopcode[5..3]
                val rm = RMDC(s, prefs)
                when (row) {
                    0x04 -> Jmp(core, rm.mpref, s.data, prefs, isRelative = false, isFar = false)
                    0x05 -> {
                        val mpref = rm.mpref
                        val where = mpref.effectiveAddress(core)
                        val ssr = mpref.ssr
                        val address = core.read(prefs.opsize, where, ssr.reg)
                        val far_ss = core.inw(where + prefs.opsize.bytes, ssr.reg)
                        Jmp(core, x86Far(address, far_ss), s.data, prefs, isRelative = false, isFar = true)
                    }
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
    }
}