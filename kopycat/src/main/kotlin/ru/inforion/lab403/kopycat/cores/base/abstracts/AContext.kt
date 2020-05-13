package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.kopycat.cores.base.AGenericCPU
import ru.inforion.lab403.kopycat.interfaces.ISerializable



abstract class AContext<out T: AGenericCPU>(protected val cpu: T): ISerializable {
    /**
     * Program counter value abstraction
     */
    abstract var vpc: Long

    /**
     * Stack pointer value abstraction
     */
    abstract var vsp: Long

    /**
     * Return address value abstraction
     */
    abstract var vra: Long

    /**
     * Return value abstraction
     */
    abstract var vRetValue: Long

    /**
     * Load data from context to cpu
     */
    abstract fun load()

    /**
     * Save data from cpu to context
     */
    abstract fun save()

    /**
     * Set registers
     */
    abstract fun setRegisters(map: Map<String, Long>)
}