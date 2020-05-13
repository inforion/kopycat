package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.kopycat.cores.base.Register

class RegisterDefinitionError(val register: Register, message: String) : Exception("$register: $message")