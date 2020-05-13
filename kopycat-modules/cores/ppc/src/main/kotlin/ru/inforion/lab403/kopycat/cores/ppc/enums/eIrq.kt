package ru.inforion.lab403.kopycat.cores.ppc.enums



enum class eIrq(val irq: Int) {
    CriticalInput(0),
    MachineCheck(1),
    DataStorage(2),
    InstStorage(3),
    ExternalInput(4),
    Alignment(5),
    Program(6),
    FPUnavailable(7),
    SystemCall(8),
    APUnavailable(9),
    Decrementer(10),
    FIT(11),
    Watchdog(12),
    DataTLBError(13),
    InstTLBError(14),
    Debug(15),
    SPEEmbedded(32),
    EmbeddedFPData(33),
    EmbeddedFPRound(34),
    EmbeddedPerfMonitor(35),
    ProcessorDoorbell(36),
    ProcessorCritDoorbell(37);

    companion object {
        fun toIndex(irq: Int) = if (irq < 16) irq else irq - 16
        val count = values().count()
    }
}