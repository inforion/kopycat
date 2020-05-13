package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.gzipInputStreamIfPossible
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.library.types.Resource
import java.io.File
import java.io.InputStream

class RAM(parent: Module, name: String, size: Int, vararg items: Pair<Any, Int>) :
        AMemory(parent, name, size, ACCESS.R_W, *items) {

    @Suppress("RemoveRedundantSpreadOperator")
    constructor(parent: Module, name: String, size: Int) :
            this(parent, name, size, *emptyArray())

    constructor(parent: Module, name: String, size: Int, data: ByteArray) :
            this(parent, name, size, data to 0)

    constructor(parent: Module, name: String, size: Int, data: InputStream) :
            this(parent, name, size, data.readBytes())

    constructor(parent: Module, name: String, size: Int, data: File) :
            this(parent, name, size, gzipInputStreamIfPossible(data.path))

    constructor(parent: Module, name: String, size: Int, data: Resource) :
            this(parent, name, size, data.inputStream())
}