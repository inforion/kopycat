package ru.inforion.lab403.kopycat.cores.mips

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import kotlin.reflect.KProperty

/**
 * Created by ra on 11.04.2017.
 */
class LongOperandField(val index: Int) {
    operator fun getValue(thisRef: AMipsInstruction, property: KProperty<*>): Long = thisRef[index - 1].value(thisRef.core)
    operator fun setValue(thisRef: AMipsInstruction, property: KProperty<*>, value: Long) {
        thisRef[index - 1].value(thisRef.core, value)
    }
}

class IntOperandField(val index: Int) {
    operator fun getValue(thisRef: AMipsInstruction, property: KProperty<*>): Int = thisRef[index - 1].value(thisRef.core).toInt()
    operator fun setValue(thisRef: AMipsInstruction, property: KProperty<*>, value: Int) {
        thisRef[index - 1].value(thisRef.core, value.toULong())
    }
}

class DoubleRegister(val index: Int) {
    operator fun getValue(thisRef: AMipsInstruction, property: KProperty<*>): Long {
        val op1 = thisRef[index - 1] as FPR
        val op2 = FPR(op1.reg + 1)
//        TODO("CHECK IT")
        return op1.value(thisRef.core).insert(op2.value(thisRef.core), 63..32)
    }
    operator fun setValue(thisRef: AMipsInstruction, property: KProperty<*>, value: Long) {
        val op1 = thisRef[index - 1] as FPR
        val op2 = FPR(op1.reg + 1)
        op1.value(thisRef.core, value[31..0])
        op2.value(thisRef.core, value[63..32])
//        TODO("CHECK IT")
    }
}