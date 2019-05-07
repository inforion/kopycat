package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support

import ru.inforion.lab403.kopycat.interfaces.ITableEntry

open class Stub(val name: String): ITableEntry {
    override fun toString(): String = name
}