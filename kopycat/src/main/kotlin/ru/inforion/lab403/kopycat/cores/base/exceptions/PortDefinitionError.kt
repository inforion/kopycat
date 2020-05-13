package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.kopycat.cores.base.APort

class PortDefinitionError(val port: APort, message: String) : Exception("$port: $message")