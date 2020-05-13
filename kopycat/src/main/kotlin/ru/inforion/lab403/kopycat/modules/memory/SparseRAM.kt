package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import java.io.InputStream


class SparseRAM(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
    }

    override val ports = Ports()
    private val segments = ArrayList<Memory>()

    fun addSegment(start: Long,
                   size: Int,
                   stream: InputStream,
                   name: String? = null,
                   access: ACCESS = ACCESS.R_W,
                   verbose: Boolean = false) {
        val end = start + size - 1
        val actualName = name ?: "RAM_MEMORY_${start.hex8}_${end.hex8}"
        val segment = Memory(ports.mem,
                start,
                end,
                actualName,
                access,
                verbose).apply {
            write(start, stream)
        }
        log.info { "Added memory segment $actualName (${start.hex8}:${end.hex8}) to SparseRAM ${this.name}" }
        segments.add(segment)
    }

    fun addSegment(start: Long, size: Int, data: ByteArray, name: String? = null, access: ACCESS = ACCESS.R_W, verbose: Boolean = false) {
        addSegment(start, size, data.inputStream(), name, access, verbose)
    }

    fun addSegment(start: Long, size: Int, name: String? = null, access: ACCESS = ACCESS.R_W, verbose: Boolean = false) {
        addSegment(start, size, ByteArray(size).inputStream(), name, access, verbose)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) { TODO() }
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> { TODO() }
    override fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) { TODO() }
    override fun reset() { TODO() }

}