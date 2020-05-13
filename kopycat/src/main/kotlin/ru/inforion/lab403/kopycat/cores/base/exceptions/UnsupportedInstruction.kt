package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction


class UnsupportedInstruction(insn: AInstruction<*>): GeneralException("$insn wasn't implemented")