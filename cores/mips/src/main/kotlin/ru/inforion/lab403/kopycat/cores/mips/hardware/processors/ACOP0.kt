package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.mips.enums.Cause
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.mips.enums.IntCtl
import ru.inforion.lab403.kopycat.cores.mips.enums.Status
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException.*
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.CPRBank
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.RSVDBank
import ru.inforion.lab403.kopycat.cores.mips.operands.CPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.util.logging.Level

/**
 * Created by batman on 17/06/17.
 */
abstract class ACOP0(core: MipsCore, name: String) : ACOP<ACOP0, MipsCore>(core, name) {
    companion object { val log = logger(Level.INFO) }

    val cntrls = RSVDBank(core)
    val regs = CPRBank(core)

    override fun createException(name: String, where: Long, vAddr: Long, action: AccessAction): HardwareException {
        return when (name) {
            "TLBInvalid" -> TLBInvalid(action, where, vAddr)
            "TLBMiss" -> TLBMiss(action, where, vAddr)
            "TLBModified" -> TLBModified(where, vAddr)
            else -> throw IllegalArgumentException("Exception $name not implemented here!")
        }
    }

    fun setCountCompareTimerBits(oldCnt: Long, newCnt: Long) {
        if (regs.Compare in oldCnt until newCnt) {
            if (core.ArchitectureRevision > 1) {
                val IPTI = regs.IntCtl[IntCtl.IPTI.range].asInt
                if (IPTI >= 2) {
                    insertIPBits(1L shl IPTI)
                    regs.Cause = regs.Cause set Cause.TI.pos
                }
            } else {
                regs.Cause = regs.Cause set Cause.IP7.pos
            }
        }
    }

    fun clearCountCompareTimerBits() {
        if (core.ArchitectureRevision > 1) {
            core.cop.regs.Cause = core.cop.regs.Cause clr Cause.TI.pos
            val IPTI = core.cop.regs.IntCtl[IntCtl.IPTI.range].asInt
            if (IPTI >= 2)
                core.cop.regs.Cause = core.cop.regs.Cause clr (IPTI + Cause.IP0.pos)
        } else {
            core.cop.regs.Cause = core.cop.regs.Cause clr Cause.IP7.pos
        }
    }

    // Because instructionPerCycle is float and may be < 1.0 then Count register won't be grow
    private var countCompareCycles = 0.0 // TODO: Add serialization
    private val countCompareInc = core.countRateFactor * core.ipc

    override fun processInterrupts() {
        /*
        The Compare register acts in conjunction with the Count register to implement a timer and timer interrupt function.

        The Compare register maintains a stable value and does not change on its own.

        When the value of the Count register equals the value of the Compare register, an interrupt request is made. In
        Release 1 of the architecture, this request is combined in an implementation-dependent way with hardware interrupt 5
        to set interrupt bit IP(7) in the Cause register. In Release 2 (and subsequent releases) of the Architecture, the presence
        of the interrupt is visible to software via the CauseTI bit and is combined in an implementation-dependent way with a
        hardware or software interrupt. For Vectored Interrupt Mode, the interrupt is at the level specified by the IntCtlIPTI
        field.

        For diagnostic purposes, the Compare register is a read/write register. In normal use however, the Compare register is
        write-only. Writing a value to the Compare register, as a side effect, clears the timer interrupt. Figure 9.31 shows the
        format of the Compare register; Table 9.46 describes the Compare register fields.
         */

        // i.e ipc = 0.4 and regs.Count = 8000
        // countCompareCycles += 0.4, = 0.4  // first interrupt proc.
        // countCompareCycles += 0.4, = 0.8  // second
        // countCompareCycles += 0.4, = 1.2  // third
        //   decimal = 1
        //   countCompareCycles = 0.2
        //   oldCnt = 8000
        //   newCnt = 8001
        //   regs.Count = 8001
        countCompareCycles += countCompareInc

        if (countCompareCycles >= 1) {
            val decimal = countCompareCycles.asInt
            countCompareCycles -= decimal

            val oldCnt = regs.Count
            val newCnt = oldCnt + decimal

            regs.Count = newCnt

            // Due to countCompareCycles >= 1 -> oldCnt != newCnt
            if (core.countCompareSupported)
                setCountCompareTimerBits(oldCnt, newCnt)
        }
    }

    protected fun setExcCode(code: ExcCode) {
        regs.Cause = insertField(regs.Cause, code.id, Cause.EXC_H.pos..Cause.EXC_L.pos)
    }

    protected fun insertIPBits(bits: Long) {
        regs.Cause = regs.Cause or regs.Cause.insert(bits, Cause.IP7.pos..Cause.IP0.pos)
    }

    protected fun setIPValue(value: Long) {
        regs.Cause = regs.Cause.insert(value, Cause.IP7.pos..Cause.IP2.pos)
    }

    /* =============================== Interrupt support mechanism =============================== */

    fun VIntPriorityEncoder(): Int {
        // See Table 6.3 Relative Interrupt Priority for Vectored Interrupt Mode
        if (regs.Cause[Cause.IP7.pos] and regs.Status[Status.IM7.pos] == 1L) {  // Hardware interrupt 5
            regs.Cause = clearBit(regs.Cause, Cause.IP7.pos)
            return 7 // 0x1000
        } else if (regs.Cause[Cause.IP6.pos] and regs.Status[Status.IM6.pos] == 1L) {  // Hardware interrupt 4
            regs.Cause = clearBit(regs.Cause, Cause.IP6.pos)
            return 6 // 0x0E00
        } else if (regs.Cause[Cause.IP5.pos] and regs.Status[Status.IM5.pos] == 1L) {  // Hardware interrupt 3
            regs.Cause = clearBit(regs.Cause, Cause.IP5.pos)
            return 5 // 0x0C00
        } else if (regs.Cause[Cause.IP4.pos] and regs.Status[Status.IM4.pos] == 1L) {  // Hardware interrupt 2
            regs.Cause = clearBit(regs.Cause, Cause.IP4.pos)
            return 4 // 0x0A00
        } else if (regs.Cause[Cause.IP3.pos] and regs.Status[Status.IM3.pos] == 1L) {  // Hardware interrupt 1
            regs.Cause = clearBit(regs.Cause, Cause.IP3.pos)
            return 3 // 0x0800
        } else if (regs.Cause[Cause.IP2.pos] and regs.Status[Status.IM2.pos] == 1L) {  // Hardware interrupt 0
            regs.Cause = clearBit(regs.Cause, Cause.IP2.pos)
            return 2 // 0x0600
        } else if (regs.Cause[Cause.IP1.pos] and regs.Status[Status.IM1.pos] == 1L) {  // Software interrupt 1
            regs.Cause = clearBit(regs.Cause, Cause.IP1.pos)
            return 1 // 0x0400
        } else if (regs.Cause[Cause.IP0.pos] and regs.Status[Status.IM0.pos] == 1L) {  // Software interrupt 0
            regs.Cause = clearBit(regs.Cause, Cause.IP0.pos)
            return 0 // 0x0200
        }
        throw GeneralException("Wrong IP7:IP0 value: %02X".format(regs.Cause[Cause.IP7.pos..Cause.IP0.pos]))
    }

    protected fun isTlbRefill(exception: HardwareException): Boolean = exception is MipsHardwareException.TLBMiss

    protected fun isInterrupt(exception: HardwareException): Boolean = exception.excCode == ExcCode.INT

    /* =============================== Overridden methods =============================== */

    override fun reset() {
        super.reset()

        val upperRandomBound = (core.mmu.tlbEntries - 1).toLong()

        regs.reset()
        regs.PRId = core.PRId

        // registers are read-only using internal methods!

        regs.writeIntern(CPR.Random.reg, upperRandomBound)

        regs.writeIntern(CPR.Config0.reg, core.Config0Preset)
        regs.writeIntern(CPR.Config1.reg, core.Config1Preset)
        regs.writeIntern(CPR.Config2.reg, core.Config2Preset)
        regs.writeIntern(CPR.Config3.reg, core.Config3Preset)

        regs.writeIntern(CPR.IntCtl.reg, core.IntCtlPreset)
    }

    override fun stringify(): String {
        return arrayOf(
                "%s {\n".format(name),
                "%s\n".format(regs.stringify()),
                "%s\n".format(cntrls.stringify()),
                "}"
        ).joinToString("")
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "name" to name,
                "regs" to regs.serialize(ctxt),
                "cntrls" to cntrls.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        val snapsnotName = snapshot["name"] as String
        if (name != snapsnotName) {
            throw IllegalStateException("Wrong module name %s != %s".format(name, snapsnotName))
        }
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, Any>)
        cntrls.deserialize(ctxt, snapshot["cntrls"] as Map<String, Any>)
    }
}