package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.kopycat.cores.base.Bus

class BusDefinitionError(val bus: Bus, message: String) : Exception("$bus: $message")