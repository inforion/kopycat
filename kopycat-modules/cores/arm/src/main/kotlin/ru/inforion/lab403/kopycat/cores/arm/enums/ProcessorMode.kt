package ru.inforion.lab403.kopycat.cores.arm.enums

enum class ProcessorMode(val id: Int) {
    usr(0b10000),
    fiq(0b10001),
    irq(0b10010),
    svc(0b10011),
    abt(0b10111),
    hyp(0b11010),
    und(0b11011),
    sys(0b11111);
}