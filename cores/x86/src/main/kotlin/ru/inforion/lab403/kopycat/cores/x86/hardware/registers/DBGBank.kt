package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.DBGR
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 08.09.16.
 */

class DBGBank(core: x86Core) : ARegistersBank<x86Core, DBGR>(core, DBGR.values(), bits = 16) {
    override val name: String = "Debug Registers"
}