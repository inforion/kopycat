package ru.inforion.lab403.kopycat.cores.ppc.operands

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class SPR(val name: String, val id: Int, val dest: PPCRegister, val moveTo: Access, val moveFrom: Access) {
    enum class Access {
        no,     //Not defined
        yes,    //Defined
        hypv    //Usable only in hypervisor state
    }

    fun value(core: PPCCore) = dest.value(core)
    fun value(core: PPCCore, data: Long) = dest.value(core, data)

    val sprH: Int
        get() = getHigh(id)
    val sprL: Int
        get() = getLow(id)
    val isPriveleged = id[4].toBool()

    companion object {
        private fun getHigh(data: Int) = ((data shr 5) and 0b11111)
        private fun getLow(data: Int) = (data and 0b11111)

        fun swap(data: Int) = (getLow(data) shl 5) or getHigh(data)

    }
}