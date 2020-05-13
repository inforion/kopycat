package ru.inforion.lab403.kopycat.cores.base

import ru.inforion.lab403.kopycat.cores.base.abstracts.*
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.serializer.Serializer
import java.util.*


typealias AGenericCore = ACore<*, *, *>
typealias AGenericCOP = ACOP<*, *>
typealias AGenericCPU = ACPU<*, *, *, *>
typealias AGenericTracer = ATracer<AGenericCore>
typealias AGenericDebugger = Debugger

typealias GenericSerializer = Serializer<*>

typealias Register = Module.Register
typealias Area = Module.Area

typealias Bus = ModuleBuses.Bus

typealias APort = ModulePorts.APort
typealias MasterPort = ModulePorts.Master
typealias SlavePort = ModulePorts.Slave
typealias TranslatorPort = ModulePorts.Translator
typealias ProxyPort = ModulePorts.Proxy

typealias HardwareErrorHandler = (HardwareException) -> Long

typealias StackOfStrings = LinkedList<String>