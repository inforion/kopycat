package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.enums.Mode
import ru.inforion.lab403.kopycat.cores.arm.enums.ProcessorMode
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.VMSABank
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ARMDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.Thumb16Decoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.Thumb32Decoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core
import java.util.logging.Level



class ARMv6CPU(
        core: AARMv6Core,
        name: String,
        haveSecurityExt: Boolean = false,
        haveVirtExt: Boolean = false
) : AARMCPU(core, name, haveSecurityExt, haveVirtExt) {

    companion object {
        val log = logger(Level.FINER)
    }

    // TODO: get rid of spProcess
    override fun CurrentMode(): Mode = Mode.Thread

    override fun CurrentModeIsPrivileged(): Boolean = false

    override fun StackPointerSelect(): Int = regs.spMain.reg

    override fun ALUWritePC(address: Long) {
        if (ArchVersion() >= 7 && CurrentInstrSet() == InstructionSet.ARM)
            BXWritePC(address)
        else
            BranchWritePC(address)
    }

    override fun LoadWritePC(address: Long) {
        if (ArchVersion() >= 5) {
            BXWritePC(address)
        } else {
            BranchWritePC(address)
        }
    }

    override fun BXWritePC(address: Long) {
        if (CurrentInstrSet() == InstructionSet.THUMB_EE) {
            TODO("Not implemented")
        } else {
            when {
                address[0] == 1L -> {
                    SelectInstrSet(InstructionSet.THUMB)
                    BranchTo(address clr 0)
                }
                address[1] == 0L -> {
                    SelectInstrSet(InstructionSet.ARM)
                    BranchTo(address)
                }
                address[1..0] == 0b10L -> throw ARMHardwareException.Unpredictable
            }
        }
    }

    override fun BranchWritePC(address: Long) {
        when (CurrentInstrSet()) {
            InstructionSet.ARM -> {
                if (ArchVersion() < 6 && address[1..0] != 0L)
                    throw ARMHardwareException.Unpredictable
                BranchTo(address bzero 1..0)
            }
            InstructionSet.JAZELLE -> TODO("WILL NEVER BE IMPLEMENTED!")
            else -> BranchTo(address clr 0)
        }
    }

    override fun CurrentInstrSet(): InstructionSet = InstructionSet.from(status.ISETSTATE.asInt)

    override fun SelectInstrSet(target: InstructionSet) {
        if (target != InstructionSet.CURRENT)
            status.ISETSTATE = target.code.asLong
    }

    override fun InITBlock(): Boolean = status.ITSTATE[3..0] != 0b0000L

    override fun LastInITBlock(): Boolean = status.ITSTATE[3..0] == 0b1000L

    private val thumb16 = Thumb16Decoder(core)
    private val thumb32 = Thumb32Decoder(core)
    private val armDc = ARMDecoder(core)

    override fun reset() {
        super.reset()

        // ID for ARMv6
        // According to proc-v6.S from linux kernel sources:
        // 	.long	0x0007b000
        //	.long	0x0007f000
        // The first one is the masked value
        // The second one is the mask
        vmsa.midr.implementer = VMSABank.Implementer.ArmLimited.data
        vmsa.midr.architecture = VMSABank.Architecture.ARMv6.data
        vmsa.midr.primaryPartNumber = 0xB00

        // Cache type, see B4.1.42
        vmsa.ctr.format = VMSABank.CacheTypeFormat.ARMv6.data
        vmsa.ctr.cwg = 0b0000 // Not provide Cache Write-back Granule information
        vmsa.ctr.erg = 0b0000 // Not provide Cache Exclusives Reservation Granule information
        vmsa.ctr.dminLine = 0b0000 // Log2 of the number of words in the smallest cache line of all the data caches...
        vmsa.ctr.l1lp = VMSABank.L1IP.AIVIVT.data
        vmsa.ctr.iminLine = 0b0000 // Log2 of the number of words in the smallest cache line of all the instruction caches...

        val sp = 0x0000_0000L
        val pc = 0x0000_0000L

        log.fine { "pc=0x${pc.hex8} sp=0x${sp.hex8}" }

        BXWritePC(pc)
        regs.spMain.value = sp
        regs.lr.value = 0xFFFF_FFFF

        pipelineRefillRequired = false
    }

    private fun fetch(where: Long): Long = core.fetch(where, 0 ,4)

    private fun swapByte(data: Long): Long {
        val high = data and 0xFFFF_0000
        val low = data and 0xFFFF
        return (high shr 16) or (low shl 16)
    }

    private var offset: Int = 0

    override fun decode() {
        var data: Long
        val decoder: ADecoder<AARMInstruction>

        when (CurrentInstrSet()) {
            InstructionSet.ARM -> {
                data = fetch(pc)
                decoder = armDc
                offset = 4
            }
            InstructionSet.THUMB -> {
                data = fetch(pc clr 0)
                val type = data[15..11]
                // 16 bits thumb instruction
                if (type != 0b11101L && type != 0b11110L && type != 0b11111L){
                    data = data[15..0]
                    decoder = thumb16
                    offset = 2
                } else { // 32 bits thumb instruction
                    data = swapByte(data)
                    decoder = thumb32
                    offset = 0
                    TODO("CHECK IT (OFFSET)")
                }
            }
            else -> throw ARMHardwareException.Undefined
        }

        insn = decoder.decode(data)
        insn.ea = pc

//        println("[${pc.hex8}] ${insn.opcode.hex8} $insn")
    }



    override fun execute(): Int {
        pc += insn.size + offset
        val lrBefore = regs.lr.value
        try {
            insn.execute()

            // check A7.3.3 about ITSTATE in ARMv7-M ref. manual
            if (InITBlock())
                ITAdvance()
        } catch (error: Throwable) {
            pc = insn.ea
            throw error
        }

        if (!pipelineRefillRequired) {
            // PC points at the address for fetched instruction when executing
            // restore normal code flow if no jump occurred
            pc -= offset
            callOccurred = false
        } else {
            // PC has been changed during instruction execution-> nothing to fix up
            pipelineRefillRequired = false
            callOccurred = regs.lr.value != lrBefore
        }

        return 1  // TODO: get from insn.execute()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "regs" to regs.serialize(ctxt),
                "sregs" to sregs.serialize(ctxt),
                "spr" to spr.serialize(ctxt),
                "vmsa" to vmsa.serialize(ctxt),
                "ver" to ver.serialize(ctxt),
                "ser" to ser.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, String>)
        sregs.deserialize(ctxt, snapshot["sregs"] as Map<String, String>)
        spr.deserialize(ctxt, snapshot["spr"] as Map<String, String>)
        vmsa.deserialize(ctxt, snapshot["vmsa"] as Map<String, String>)
        ver.deserialize(ctxt, snapshot["ver"] as Map<String, String>)
        ser.deserialize(ctxt, snapshot["ser"] as Map<String, String>)
    }
}