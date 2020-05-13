package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.modules.BUS12
import ru.inforion.lab403.kopycat.modules.BUS16
import java.util.logging.Level
import java.util.logging.Level.FINER


class SCP(parent: Module, name: String, RSTLD: Int) : Module(parent, name) {
    companion object {
        val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Slave("mmcr", BUS12)
        val io = Slave("io", BUS16)
    }

    override val ports = Ports()

    private val SYSINFO = Register(ports.mmcr, 0x0D70, Datatype.WORD, "SYSINFO", RSTLD.asULong, writable = false)

    private val RESCFG = object : Register(ports.mmcr, 0x0D72, Datatype.WORD, "RESCFG") {
        var ICE_ON_RST by bit(3)
        var PRG_RST_ENB by bit(2)
        var GP_RST by bit(1)
        var SYS_RST by bit(0)

        override fun stringify() = "${super.stringify()} " +
                "[SYS_RST=$SYS_RST " +
                "GP_RST=$GP_RST " +
                "PRG_RST_ENB=$PRG_RST_ENB " +
                "ICE_ON_RST=$ICE_ON_RST]"
    }

    private val RESSTA = object : Register(ports.mmcr, 0x0D74, Datatype.WORD, "RESSTA", 1) {
        var SCP_RST_DET by bit(6)
        var ICE_HRST_DET by bit(5)
        var ICE_SRST_DET by bit(4)
        var WDT_RST_DET by bit(3)
        var SD_RST_DET by bit(2)
        var PRGRST_DET by bit(1)
        var PWRGOOD_DET by bit(0)

        override fun stringify() = "${super.stringify()} " +
                "[PWRGOOD_DET=$PWRGOOD_DET " +
                "PRGRST_DET=$PRGRST_DET " +
                "SD_RST_DET=$SD_RST_DET " +
                "WDT_RST_DET=$WDT_RST_DET " +
                "ICE_SRST_DET=$ICE_SRST_DET " +
                "ICE_HRST_DET=$ICE_HRST_DET " +
                "SCP_RST_DET=$SCP_RST_DET]"
    }

    private val SCPDATA = object : Register(ports.io, 0x0060, BYTE, "SCPDATA") {
        val SCP_DATA by wfield(7..0)
        val A20G_CTL by wbit(1)
        val CPU_RST by wbit(0)

        override fun stringify() = "${super.stringify()} " +
                "[SCP_DATA=$SCP_DATA " +
                "A20G_CTL=$A20G_CTL " +
                "CPU_RST=$CPU_RST]"

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.warning { "data value=${value.hex} should go into GP port..." }
        }
    }

    private val SYSCTLB = object : Register(ports.io, 0x0061, BYTE, "SYSCTLB") {
        val PERR by rbit(7, initial = 0)
        val IOCHCK by rbit(6, initial = 0)
        val PITOUT2_STA by rbit(5, initial = 1)
        val PIT_OUT2_ENB by rwbit(1)
        val PIT_GATE2 by rwbit(0)

        override fun stringify() = "${super.stringify()} " +
                "[PERR=$PERR " +
                "IOCHCK=$IOCHCK " +
                "PITOUT2_STA=$PITOUT2_STA " +
                "PIT_OUT2_ENB=$PIT_OUT2_ENB " +
                "PIT_OUT2_ENB=$PIT_OUT2_ENB " +
                "PIT_GATE2=$PIT_GATE2"
    }

    private val SCPCMD = object : Register(ports.io, 0x0064, BYTE, "SCPCMD") {
        val SCP_CMD by wfield(7..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            if (SCP_CMD == 0xFE) {
                log.severe { "Device want reboot... something definitely goes wrong!" }
                if (core.isDebuggerPresent)
                    core.debugger.isRunning = false
            }
        }
    }

    private val SYSCTLA = object : Register(ports.io, 0x0092, BYTE, "SYSCTLA") {
        val Reserved by reserved(7..2)
        val A20G_CTL by rwbit(1)
        val CPU_RST by rwbit(0)

        override fun stringify() = "${super.stringify()} [A20G_CTL=$A20G_CTL CPU_RST=$CPU_RST]"

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            if (CPU_RST == 1) {
                log.severe { "SYSCTLA reset signal received" }
                core.reset()
            }
        }
    }
}