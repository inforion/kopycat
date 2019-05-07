package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.enums.eGPR
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.mips.hardware.systemdc.MipsSystemDecoder
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class MipsCPU(val mips: MipsCore, name: String) : ACPU<MipsCPU, MipsCore, AMipsInstruction, eGPR>(mips, name) {

    inner class BranchController: ICoreUnit {
        override val name: String = "Branch Controller"

        private var delayedJumpAddress: Long = WRONGL
        private var delayedJumpInsnRemain = 0
        private var hasDelayedJump = false

        val isDelaySlot: Boolean get() = hasDelayedJump && delayedJumpInsnRemain == 0

        override fun reset() {
            super.reset()
            delayedJumpInsnRemain = 0
            delayedJumpAddress = WRONGL
        }

        fun validate() {
            if (hasDelayedJump) throw GeneralException("Branch found in the delay slot: ${pc.hex8}")
        }

        fun setIp(ea: Long) {
            pc = ea
            delayedJumpInsnRemain = 0
            delayedJumpAddress = WRONGL
            hasDelayedJump = false
        }

        fun schedule(ea: Long, delay: Int = 1) {
            delayedJumpAddress = ea
            delayedJumpInsnRemain = delay
            hasDelayedJump = true
        }

        fun nop(delay: Int = 1) = schedule(-1, delay)
        fun jump(ea: Long) = schedule(ea, delay = 0)

        fun processIp(size: Int): Long {
            if (hasDelayedJump) {
                if (delayedJumpInsnRemain == 0) {
                    if (delayedJumpAddress == WRONGL) {
                        hasDelayedJump = false
                        pc += size
                    } else {
                        setIp(delayedJumpAddress)
                    }
                    return pc
                }
                delayedJumpInsnRemain -= 1
            }
            pc += size
            return pc
        }

        override fun stringify(): String {
            return ("\tController {\n" +
                    "\t\tIs waiting for jump? = %s\n" +
                    "\t\tInstructions remain  = %d\n" +
                    "\t\tDelayed jump address = %08X\n" +
                    "\t}").format(hasDelayedJump, delayedJumpInsnRemain, delayedJumpAddress)
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return mapOf(
                    "delayedJumpAddress" to delayedJumpAddress.hex8,
                    "delayedJumpInsnRemain" to delayedJumpInsnRemain.hex8,
                    "hasDelayedJump" to hasDelayedJump.toString()
            )
        }

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            delayedJumpAddress = (snapshot["delayedJumpAddress"] as String).hexAsULong
            delayedJumpInsnRemain = (snapshot["delayedJumpInsnRemain"] as String).hexAsUInt
            hasDelayedJump = (snapshot["hasDelayedJump"] as String).toBoolean()
        }
    }

    override fun reg(index: Int): Long = regs[index].value(mips)
    override fun reg(index: Int, value: Long) = regs[index].value(mips, value)
    override fun count() = regs.count()

    val bigEndianCPU = 0

    val decoder = MipsSystemDecoder(mips)
    val branchCntrl = BranchController()

    val regs = GPRBank(mips)

    val sgprs = Array(mips.countOfShadowGPR) { GPRBank(mips) }

    var hi: Long = 0
        get() = field and 0xFFFFFFFF
        set(value) {
            field = value and 0xFFFFFFFF
        }
    var lo: Long = 0
        get() = field and 0xFFFFFFFF
        set(value) {
            field = value and 0xFFFFFFFF
        }

    var status: Long = 0
        private set
    override var pc: Long = 0
    var llbit: Int = 0

    override fun reset() {
        branchCntrl.reset()
        regs.reset()
        hi = 0
        lo = 0
        status = 0
        pc = 0xBFC00000
    }

    private fun fetch(pc: Long): Long = core.inl(pc)

    override fun execute(): Int {
        val data = fetch(pc)
        insn = decoder.decode(data, pc)
        insn.ea = pc
        insn.execute()
        branchCntrl.processIp(insn.size)
        return 1  // TODO: get from insn.execute()
    }

    override fun stringify(): String {
        return arrayOf(
                "CentralProcessingUnit {\n",
                "%s\n".format(branchCntrl.stringify()),
                "\tpc        = %08X\n".format(pc),
                "\tstatus    = %08X\n".format(status),
                "\thi:lo     = %08X:%08X\n".format(hi, lo),
                "%s\n".format(regs.stringify()),
                "}"
        ).joinToString("")
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "hi" to hi.hex8,
                "lo" to lo.hex8,
                "status" to status.hex8,
                "pc" to pc.hex8,
                "llbit" to llbit.toString(),
                "regs" to regs.serialize(ctxt),
                "branchCntrl" to branchCntrl.serialize(ctxt),
                "pc" to pc.hex8)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        hi = (snapshot["hi"] as String).hexAsULong
        lo = (snapshot["lo"] as String).hexAsULong
        status = (snapshot["status"] as String).hexAsULong
        pc = (snapshot["pc"] as String).hexAsULong
        llbit = (snapshot["llbit"] as String).toInt()
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, String>)
        branchCntrl.deserialize(ctxt, snapshot["branchCntrl"] as Map<String, String>)
    }
}