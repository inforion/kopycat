package ru.inforion.lab403.kopycat.cores.ppc.hardware.peripheral

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.abstracts.APIC
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eIrq
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_e500v2
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


 
class PIC(parent: Module, name: String) : APIC(parent, name) {


    inner class Ports : ModulePorts(this) {
        //val mem = Slave("mem", 0x400)
        val irq = Slave("irq", eIrq.count)
    }

    override val ports = Ports()


    inner class Interrupt(irq: Int) : AInterrupt(irq, irq.toString()) {
        override val cop
            get() = core.cop

        override val vector: Int = irq

        val address: Long = PPCRegister_Embedded.OEAext.IVPR.value(core as PPCCore)[31..16] or when (irq) {

            eIrq.CriticalInput.irq -> PPCRegister_e500v2.OEAext.IVOR0           // Ignored: no information
            eIrq.MachineCheck.irq -> PPCRegister_e500v2.OEAext.IVOR1            // Ignored: no need
            eIrq.DataStorage.irq -> PPCRegister_e500v2.OEAext.IVOR2             //
            eIrq.InstStorage.irq -> PPCRegister_e500v2.OEAext.IVOR3
            eIrq.ExternalInput.irq -> PPCRegister_e500v2.OEAext.IVOR4
            eIrq.Alignment.irq -> PPCRegister_e500v2.OEAext.IVOR5
            eIrq.Program.irq -> PPCRegister_e500v2.OEAext.IVOR6
            eIrq.FPUnavailable.irq -> PPCRegister_e500v2.OEAext.IVOR7
            eIrq.SystemCall.irq -> PPCRegister_e500v2.OEAext.IVOR8
            eIrq.APUnavailable.irq -> PPCRegister_e500v2.OEAext.IVOR9
            eIrq.Decrementer.irq -> PPCRegister_e500v2.OEAext.IVOR10
            eIrq.FIT.irq -> PPCRegister_e500v2.OEAext.IVOR11
            eIrq.Watchdog.irq -> PPCRegister_e500v2.OEAext.IVOR12
            eIrq.DataTLBError.irq -> PPCRegister_e500v2.OEAext.IVOR13
            eIrq.InstTLBError.irq -> PPCRegister_e500v2.OEAext.IVOR14
            eIrq.Debug.irq -> PPCRegister_e500v2.OEAext.IVOR15
            eIrq.SPEEmbedded.irq -> PPCRegister_e500v2.OEAext.IVOR32
            eIrq.EmbeddedFPData.irq -> PPCRegister_e500v2.OEAext.IVOR33
            eIrq.EmbeddedFPRound.irq -> PPCRegister_e500v2.OEAext.IVOR34
            eIrq.EmbeddedPerfMonitor.irq -> PPCRegister_e500v2.OEAext.IVOR35
            //eIrq.ProcessorDoorbell.irq -> PPCRegister_e500v2.OEAext.IVOR36
            //eIrq.ProcessorCritDoorbell.irq -> PPCRegister_e500v2.OEAext.IVOR37
            else -> throw GeneralException("Wrong interrupt irq $irq")
        }.value(core as PPCCore) and 0xFFFF_FFF0 // Clear lower 4 bit

        override val priority: Int
            get() = when(irq) {

                // 1. Sync (non debug):
                eIrq.DataStorage.irq,       // Data storage
                eIrq.InstStorage.irq,       // Instruction Storage
                eIrq.Alignment.irq,         // Alignment
                eIrq.Program.irq,           // Program
                eIrq.FPUnavailable.irq,     // Floating-Point Unit Unavailable
                eIrq.APUnavailable.irq,     // Auxiliary Processor Unavailable
                                            // Embedded Floating-Point Unavailable [SP.Category: SP.Embedded Float_*]
                eIrq.SPEEmbedded.irq,       // SPE/Embedded Floating-Point/Vector Unavailable
                eIrq.EmbeddedFPData.irq,    // Embedded Floating-Point Data [Category: SP.Embedded Float_*]
                eIrq.EmbeddedFPRound.irq,   // Embedded Floating-Point Round [Category: SP.Embedded Float_*]
                eIrq.SystemCall.irq,        // System Call
                eIrq.DataTLBError.irq,      // Data TLB Error
                eIrq.InstTLBError.irq       // Instruction TLB Error
                    -> 0

                // 2. Machine Check
                eIrq.MachineCheck.irq -> 1

                // 3. Debug
                eIrq.Debug.irq -> 2

                // 4. Critical Input
                eIrq.CriticalInput.irq -> 3

                // 5. Watchdog Timer
                eIrq.Watchdog.irq -> 4

                // 6. Processor Doorbell Critical
                eIrq.ProcessorCritDoorbell.irq -> 5

                // 7. External Input
                eIrq.ExternalInput.irq -> 6

                // 8. Fixed-Interval Timer
                eIrq.FIT.irq -> 7

                // 9. Decrementer
                eIrq.Decrementer.irq -> 8

                // 10. Processor Doorbell
                eIrq.ProcessorDoorbell.irq -> 9

                // 11. Embedded Performance Monitor
                eIrq.EmbeddedPerfMonitor.irq -> 10

                else -> throw GeneralException("Wrong interrupt irq $irq")
            }

    }

    private val interrupts = Interrupts(ports.irq, "IRQ", *Array(eIrq.count) { Interrupt(it) })

    fun emitInterrupt(irq: Int) {
        interrupts[eIrq.toIndex(irq)].enabled = true
    }

}