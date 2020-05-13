package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype

/**
 * {EN}
 * Abstract class that describe basic functionality of phrase operand (specific for x86)
 * e.g. mov eax, [eax+4*edx+20h]
 * NOTE: Phrase is specific for x86 but may occurs for other arch.
 * {EN}
 */
abstract class APhrase<in T: AGenericCore>(
        dtyp: Datatype,
        val base: ARegister<T>,
        val index: ARegister<T>,
        val displ: Immediate<T>,
        access: Access,
        num: Int = WRONGI) :
        AOperand<T>(AOperand.Type.PHRASE, access, AOperand.Controls.VOID, num, dtyp) {

    override fun equals(other: Any?): Boolean {
        if (other is APhrase<*>) {
            return (other.type == AOperand.Type.PHRASE &&
                    other.dtyp == dtyp &&
                    other.base == base &&
                    other.index == index &&
                    other.specflags == specflags)
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + displ.hashCode()
        result += 31 * result + dtyp.ordinal
        result += 31 * result + base.hashCode()
        result += 31 * result + index.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }
}