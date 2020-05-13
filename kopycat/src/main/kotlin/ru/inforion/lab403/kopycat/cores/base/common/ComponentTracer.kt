package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.interfaces.ITracer

class ComponentTracer<R: AGenericCore>(
        parent: Module,
        name: String,
        vararg val args: ATracer<R>
): ATracer<R>(parent, name) {
    private val tracers = ArrayList<ITracer<R>>(args.toList())

    fun addTracer(vararg newTracers: ITracer<R>): Boolean = tracers.addAll(newTracers)
    fun removeTracer(tracer: ITracer<R>): Boolean = tracers.remove(tracer)

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> = HashMap()
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = Unit

    override fun preExecute(core: R): Boolean = tracers.all { it.preExecute(core) }
    override fun postExecute(core: R, status: Status): Boolean = tracers.all { it.postExecute(core, status) }
    override fun onStart() = tracers.forEach { it.onStart() }
    override fun onStop() = tracers.forEach { it.onStop() }
}
