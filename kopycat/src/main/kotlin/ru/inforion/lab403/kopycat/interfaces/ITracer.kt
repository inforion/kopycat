package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Status


interface ITracer<R: AGenericCore>: ICoreUnit {
    /**
     * {EN}This method is called before each instruction executed{EN}
     *
     * {RU}Метод вызывается каждый раз перед выполнением инструкции{RU}
     */
    fun preExecute(core: R): Boolean

    /**
     * {EN}This method is called after each instruction executed{EN}
     *
     * {RU}Метод вызывается каждый раз после выполнения инструкции{RU}
     */
    fun postExecute(core: R, status: Status): Boolean

    /**
     * {EN}This method is called before device is running{EN}
     *
     * {RU}Метод вызывается перед тем, как устройство переведено в состояние run{RU}
     */
    fun onStart() = Unit

    /**
     * {EN}This method is called before device is stopped{EN}
     *
     * {RU}Метод вызывается перед тем, как устройство переведено в состояние stop{RU}
     */
    fun onStop() = Unit

    /**
     * {EN}This method is called to run simulation{EN}
     *
     * {RU}Метод вызывается для начала симуляции устройства{RU}
     */
    fun run(block: () -> Unit) {
        onStart()
        block()
        onStop()
    }
}