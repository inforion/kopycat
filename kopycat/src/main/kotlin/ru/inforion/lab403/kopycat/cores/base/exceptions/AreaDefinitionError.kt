package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.kopycat.cores.base.Area

class AreaDefinitionError(val area: Area, message: String) : Exception("$area: $message")