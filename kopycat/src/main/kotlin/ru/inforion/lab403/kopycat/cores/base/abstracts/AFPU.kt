package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.kopycat.cores.base.common.Component


/**
 * {RU}
 * Абстрактный класс Floating Point Unit (FPU)
 *
 *
 * @param name произвольное имя объекта FPU
 * @property core ядро, в которое встраивается модуль FPU
 * {RU}
 */
abstract class AFPU<R: ACore<R, *, *>>(val core: R, name: String): Component(core, name)