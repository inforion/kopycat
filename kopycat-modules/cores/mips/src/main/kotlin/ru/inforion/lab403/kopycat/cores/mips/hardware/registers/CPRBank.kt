package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.mips.enums.eCPR
import ru.inforion.lab403.kopycat.cores.mips.operands.CPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class CPRBank(core: MipsCore) : ARegistersBank<MipsCore, eCPR>(core, eCPR.values(), bits = 32) {
    override val name: String = "Coprocessor General Purpose Registers"

    var Index by valueOf(CPR.Index)
    var Config0 by valueOf(CPR.Config0)
    var Config1 by valueOf(CPR.Config1)
    var Config2 by valueOf(CPR.Config2)
    var Config3 by valueOf(CPR.Config3)
    var Cause by valueOf(CPR.Cause)
    var Context by valueOf(CPR.Context)
    var UserLocal by valueOf(CPR.UserLocal)
    var Compare by valueOf(CPR.Compare)
    var PageMask by valueOf(CPR.PageMask)
    var EntryHi by valueOf(CPR.EntryHi)
    var EntryLo0 by valueOf(CPR.EntryLo0)
    var EntryLo1 by valueOf(CPR.EntryLo1)
    var Count by valueOf(CPR.Count)
    var EPC by valueOf(CPR.EPC)
    var ErrorEPC by valueOf(CPR.ErrorEPC)
    var PRId by valueOf(CPR.PRId)
    var Random by valueOf(CPR.Random)
    var Wired by valueOf(CPR.Wired)
    var Status by valueOf(CPR.Status)
    var ECC by valueOf(CPR.ErrCnt)
    var TagLo0 by valueOf(CPR.TagLo0)
    var TagLo2 by valueOf(CPR.TagLo2)
    var BadVAddr by valueOf(CPR.BadVAddr)
    var IntCtl by valueOf(CPR.IntCtl)
    var SRSCtl by valueOf(CPR.SRSCtl)
    var SRSMap by valueOf(CPR.SRSMap)
    var EBase by valueOf(CPR.EBase)
}
