package ru.inforion.lab403.kopycat.modules.msp430x44x

//import ru.inforion.lab403.common.extensions.*
//import ru.inforion.lab403.kopycat.cores.msp430.ADebuggableMSP430
//import ru.inforion.lab403.kopycat.cores.base.common.PeripheralsBank
//import ru.inforion.lab403.kopycat.modules.memory.RAM
//import ru.inforion.lab403.kopycat.modules.msp430.*
//import ru.inforion.lab403.kopycat.serializer.Serializer
//
///**
// * Created by shiftdj on 16/02/18.
// */

//class MSP430x44x(override val name: String) : ADebuggableMSP430(freq = 16.MHz, debugger = MSP430Debugger())  {
//    override val mapping = null
//
////    override val memory = MSP430SystemMemory(this).apply {
////        PeripheralIO("sfr",     start = 0x0000_0000, end = 0x0000_000F, access = ACCESS.R_W, extbus = 0, io = false)
////        PeripheralIO("per8",    start = 0x0000_0010, end = 0x0000_00FF, access = ACCESS.R_W, extbus = 0, io = false)
////        PeripheralIO("per16",   start = 0x0000_0100, end = 0x0000_01FF, access = ACCESS.R_W, extbus = 0, io = false)
////        Segment("ram",          start = 0x0000_0200, end = 0x0000_FFDF, access = ACCESS.R_W)
////        Segment("ivt",          start = 0x0000_FFE0, end = 0x0000_FFFF, access = ACCESS.R_W)
////        Segment("rom",          start = 0x0001_0000, end = 0x0001_FFFF, access = ACCESS.R_W)
////    }
//
//    val ram = RAM(this,"RAM", 0x0000_0200, 0x0000_FFDF)
//    val ivt = RAM(this,"IVT", 0x0000_FFE0, 0x0000_FFFF)
//    val rom = RAM(this,"ROM", 0x0001_0000, 0x0001_FFFF)
//
//    val ic = InterruptController(this, this.cpu.memBus)
//    val watchdog = Watchdog(this, this.cpu.memBus)
//    val timerA = TimerA(this, this.cpu.memBus, ic.intTimerATACCR0, ic.intTimerARegular)
//    val usart1 = USART1(this, this.cpu.memBus, ic.intUSART1TX, ic.intUSART1RX)
//    val hardwareMultiplier = HardwareMultiplier(this, this.cpu.memBus)
//
//
//    override val periphs = PeripheralsBank("EXAMPLE_DEVICE", ic, watchdog, timerA, usart1, hardwareMultiplier)
//
//    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
//        return super.serialize(ctxt) + mapOf(
//                "name" to name
//        )
//    }
//}
