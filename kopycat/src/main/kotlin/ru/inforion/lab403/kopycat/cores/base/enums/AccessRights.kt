package ru.inforion.lab403.kopycat.cores.base.enums


enum class AccessRights(val mask: Int) {
    READ(0b01),
    WRITE(0b10),
    READ_WRITE(0b11)
}