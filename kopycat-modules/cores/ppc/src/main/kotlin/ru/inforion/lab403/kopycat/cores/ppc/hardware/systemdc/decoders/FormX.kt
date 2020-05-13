package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


class FormX(core: PPCCore,
            val construct:  (PPCCore, Int, Int, Int, Boolean) -> APPCInstruction
) : APPCDecoder(core) {

    //Disclamer:
    //X-Form, по сути, представляет собой группировку из около 70 других форматов декода.
    //Общее у них то, что все аргументы выравнены одинакого и имеют схему [OPCD|OP0|OP1|OP2|EXTOPCD|FLAG].
    //Например, первое же поле из битов с 25 по 21 может быть как регистром общего назначения, так регистром
    //вещественных чисел, так и делиться на еще меньшие подгруппы и подполя. Таким образом было ршено, что декод
    //здесь производится только для разделения базовых групп полей как аргументов типа Long.
    //Преобразования к конкретным типам операндов берет на себя инструкция.
    override fun decode(s: Long): APPCInstruction {

        //Bits 25..21 (6..10 in PPC notation)
        val fieldA = s[25..21].toInt()

        //Bits 20..16 (11..15 in PPC notation)
        val fieldB = s[20..16].toInt()

        //Bits 15..11 (16..20 in PPC notation)
        val fieldC = s[15..11].toInt()

        val flag = s[0].toBool()
        return construct(core,
                fieldA,
                fieldB,
                fieldC,
                flag
        )
    }
}