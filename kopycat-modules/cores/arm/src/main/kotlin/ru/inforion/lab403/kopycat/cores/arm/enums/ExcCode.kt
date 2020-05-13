package ru.inforion.lab403.kopycat.cores.arm.enums



enum class ExcCode {
    Overflow,
    Unpredictable,
    Undefined,
    Unknown,

    CVC,
    DataAbort,
    PrefetchAbort,
    AligmentFault
}