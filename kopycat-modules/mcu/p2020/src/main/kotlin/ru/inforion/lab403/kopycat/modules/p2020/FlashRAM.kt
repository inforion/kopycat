package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.modules.memory.AMemory




// This class implements memory module that takes content from file on reset, then works like RAM memory
class FlashRAM(parent: Module, name: String, size: Int, vararg items: Pair<Any, Int>) :
        AMemory(parent, name, size, ACCESS.R_W, *items) {

    //constructor(parent: Module, name: String, )

}