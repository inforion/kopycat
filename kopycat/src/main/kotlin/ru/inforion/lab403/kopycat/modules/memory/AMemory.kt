package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.gzipInputStreamIfPossible
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.library.types.Resource
import java.io.File
import java.io.InputStream
import java.nio.ByteOrder

abstract class AMemory(
        parent: Module,
        name: String,
        val size: Int,
        access: ACCESS,
        vararg items: Pair<Any, Int>
): Module(parent, name), IFetchReadWrite {

    @Suppress("RemoveRedundantSpreadOperator")
    constructor(parent: Module, name: String, size: Int, access: ACCESS) :
            this(parent, name, size, access, *emptyArray())

    constructor(parent: Module, name: String, size: Int, access: ACCESS, data: ByteArray) :
            this(parent, name, size, access,data to 0)

    constructor(parent: Module, name: String, size: Int, access: ACCESS, data: InputStream) :
            this(parent, name, size, access, data.readBytes())

    constructor(parent: Module, name: String, size: Int, access: ACCESS, data: File) :
            this(parent, name, size, access, gzipInputStreamIfPossible(data.path))

    constructor(parent: Module, name: String, size: Int, access: ACCESS, data: Resource) :
            this(parent, name, size, access, data.inputStream())

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", this@AMemory.size)
    }

    final override val ports = Ports()

    private val prefix = javaClass.simpleName

    private val records = items.associate {
        val result = when (val item = it.first) {
            is ByteArray -> item
            is InputStream -> item.readBytes()
            is File -> gzipInputStreamIfPossible(item.path).readBytes()
            is Resource -> item.readBytes()
            else -> throw IllegalArgumentException("Can't read data from $item as ${item.javaClass}")
        }
        it.second to result
    }

    private val memory = Memory(ports.mem, 0, size.asULong - 1, "${prefix}_MEMORY", access)

    var endian: ByteOrder
        get() = memory.endian
        set(value) {
            memory.endian = value
        }

    override fun reset() {
        super.reset()
        records.forEach { (offset, data) -> store(offset.asULong, data) }
    }

    override fun read(ea: Long, ss: Int, size: Int): Long = memory.read(ea, ss, size)
    override fun write(ea: Long, ss: Int, size: Int, value: Long): Unit = memory.write(ea, ss, size, value)
    override fun fetch(ea: Long, ss: Int, size: Int): Long = memory.fetch(ea, ss, size)

    override fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        memory.restore(ctxt, snapshot)
    }
}