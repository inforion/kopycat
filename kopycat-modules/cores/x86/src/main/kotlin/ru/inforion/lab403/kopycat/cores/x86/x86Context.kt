package ru.inforion.lab403.kopycat.cores.x86

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hexAsULong
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AContext
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU



class x86Context(cpu: x86CPU): AContext<x86CPU>(cpu) {
    var eax = 0L
    var ebx = 0L
    var ecx = 0L
    var edx = 0L
    var edi = 0L
    var esi = 0L
    var ebp = 0L
    var esp = 0L
    var eip = 0L
    var eflags = 0L

    override var vpc: Long
        get() = eip
        set(value) { eip = value }

    override var vsp: Long
        get() = esp
        set(value) { esp = value }

    override var vra = 0L

    override var vRetValue: Long
        get() = eax
        set(value) { eax = value }

    override fun setRegisters(map: Map<String, Long>) {
        eax = map.getOrDefault("eax", 0)
        ebx = map.getOrDefault("ebx", 0)
        ecx = map.getOrDefault("ecx", 0)
        edx = map.getOrDefault("edx", 0)
        esi = map.getOrDefault("esi", 0)
        edi = map.getOrDefault("edi", 0)
    }

    override fun load() {
        cpu.regs.eip = eip
        cpu.regs.eax = eax
        cpu.regs.ebx = ebx
        cpu.regs.ecx = ecx
        cpu.regs.edx = edx
        cpu.regs.edi = edi
        cpu.regs.esi = esi
        cpu.regs.esp = esp
        cpu.regs.ebp = ebp
        cpu.flags.eflags = eflags
    }

    override fun save() {
        eflags = cpu.flags.eflags
        eax = cpu.regs.eax
        ebx = cpu.regs.ebx
        ecx = cpu.regs.ecx
        edx = cpu.regs.edx
        esi = cpu.regs.esi
        edi = cpu.regs.edi
        esp = cpu.regs.esp
        ebp = cpu.regs.ebp
        eip = cpu.regs.eip
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "eip" to eip.hex,
                "esp" to esp.hex,
                "eax" to eax.hex,
                "ebx" to ebx.hex,
                "ecx" to ecx.hex,
                "edx" to edx.hex,
                "edi" to edi.hex,
                "esi" to esi.hex,
                "ebp" to ebp.hex,
                "eflags" to eflags.hex)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        eip = (snapshot["eip"] as String).hexAsULong
        esp = (snapshot["esp"] as String).hexAsULong
        eax = (snapshot["eax"] as String).hexAsULong
        ebx = (snapshot["ebx"] as String).hexAsULong
        ecx = (snapshot["ecx"] as String).hexAsULong
        edx = (snapshot["edx"] as String).hexAsULong
        esi = (snapshot["esi"] as String).hexAsULong
        edi = (snapshot["edi"] as String).hexAsULong
        ebp = (snapshot["ebp"] as String).hexAsULong
        eflags = (snapshot["eflags"] as String).hexAsULong
    }
}