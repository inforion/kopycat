package ru.inforion.lab403.kopycat.cores.x86.enums


enum class ExcCode(val code: Int, val hasError: Boolean = false) {
    DivisionByZero(0x00),
    Debug(0x01),
    NMI(0x02),
    Breakpoint(0x03),
    Overflow(0x04),
    BoundRangeExceeded(0x05),
    InvalidOpcode(0x06),
    DeviceNotAvailable(0x07),
    DoubleFault(0x08, true),
    CoprocessorSegmentOverrun(0x09),
    InvalidTSS(0x0A, true),
    SegmentNotPresent(0x0B, true),
    StackSegmentFault(0x0C, true),
    GeneralProtectionFault(0x0D, true),
    PageFault(0x0E, true),
    Reserved(0x0F),
    AlignmentCheck(0x11),
    MachineCheck(0x12),
    SIMDFloatingPointException(0x13),
    VirtualizationException(0x14),
    SecurityException(0x1E),

    FpuException(0xFF)
}

