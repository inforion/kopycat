package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.common.Module

abstract class AARMv7Core(parent: Module, name: String, frequency: Long, ipc: Double) :
        AARMCore(parent, name, frequency, 7, ipc)