package ru.inforion.lab403.kopycat.cores.base.exceptions

import ru.inforion.lab403.kopycat.cores.base.common.Breakpoint


class BreakpointException(val breakpoint: Breakpoint) : GeneralException("$breakpoint")