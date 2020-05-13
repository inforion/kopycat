package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.arm.enums.ProcessorMode



class RegisterBanking(val mode: ProcessorMode) : ARegisterBankNG(32) {
    override val name = "ARM Register bank for mode ${mode.name}"

    val r8 = Register()
    val r9 = Register()
    val r10 = Register()
    val r11 = Register()
    val r12 = Register()
    val sp = Register()
    val lr = Register()
    val spsr = Register()


    init {
        initialize()
    }
}