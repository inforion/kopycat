package ru.inforion.lab403.kopycat.cores.x86

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Far
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.esp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.sp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ss
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by batman on 12/10/16.
 */
object x86utils {
    fun isWithinCodeSegmentLimits(ea: Long): Boolean = true

    fun getSegmentSelector(dev: x86Core, operand: AOperand<x86Core>): Long = when (operand) {
        is x86Far -> operand.ss
        is x86Displacement -> operand.ssr.value(dev)
        else -> throw GeneralException("Bogus decoded jmp")
    }

    fun push(core: x86Core, value: Long, dtyp: Datatype, prefs: Prefixes, isSSR: Boolean = false) {
        val sp = if (prefs.is16BitAddressMode) sp else esp
        val discrete = if (prefs.is16BitOperandMode) 2 else 4

        if (dtyp.bytes == 1) {
            sp.minus(core, discrete)
            val addr = sp.value(core)
            val dtypOvw = if (prefs.is16BitOperandMode) Datatype.WORD else Datatype.DWORD
            core.write(addr, ss.reg, dtypOvw.bytes, value)
        } else {
            if (!prefs.is16BitOperandMode && (dtyp.bytes == 2))
                sp.minus(core, 2)
            sp.minus(core, dtyp.bytes)
            val addr = sp.value(core)
            core.write(addr, ss.reg, dtyp.bytes, value)
        }

//        if(dtyp.bytes == 1) {
//            sp.minus(dev, 2)
//            val addr = sp.value(dev)
//            dev.memory.storeData(Datatype.WORD, addr, value, ss = Register.ss.value(dev))
//        } else {
////            if(isSSR && !prefs.is16BitOperandMode){
//            if(!prefs.is16BitOperandMode && (dtyp.bytes == 2)){
//                sp.minus(dev, 2)
//                dev.memory.storeData(dtyp, sp.value(dev), value, ss = Register.ss.value(dev))
//            }
//            sp.minus(dev, dtyp.bytes)
//            val addr = sp.value(dev)
//            dev.memory.storeData(dtyp, addr, value, ss = Register.ss.value(dev))
//        }
    }

    fun pop(core: x86Core, dtyp: Datatype, prefs: Prefixes, offset: Long = 0, isSSR: Boolean = false): Long {
        val sp = if (prefs.is16BitAddressMode) sp else esp
        val discrete = if (prefs.is16BitOperandMode) 2 else 4
        val data = core.read(sp.value(core), ss.reg, dtyp.bytes)
        if (dtyp.bytes == 1)
            sp.plus(core, discrete + offset)
        else {
            if (!prefs.is16BitOperandMode && (dtyp.bytes == 2))
                sp.plus(core, 2)
            sp.plus(core, dtyp.bytes + offset)
        }

//        val sp = if (prefs.is16BitAddressMode) Register.sp else Register.esp
//        val data = dev.memory.loadData(dtyp, sp.value(dev), ss = Register.ss.value(dev))
//        if(isSSR && !prefs.is16BitOperandMode)
//            sp.plus(dev, 2)
//
//        if(dtyp.bytes == 1)
//            sp.plus(dev, 2 + offset)
//        else
//            sp.plus(dev, dtyp.bytes + offset)
        return data
    }
}